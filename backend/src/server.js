import express from 'express';
import cors from 'cors';
import multer from 'multer';
import ffmpegPath from 'ffmpeg-static';
import { spawn } from 'node:child_process';
import { promises as fs } from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import crypto from 'node:crypto';
import admin from 'firebase-admin';
import { Storage } from '@google-cloud/storage';

if (!admin.apps.length) admin.initializeApp();
const db = admin.firestore();
const storage = new Storage();
const bucketName = process.env.FIREBASE_STORAGE_BUCKET || process.env.GCLOUD_STORAGE_BUCKET;
const bucket = bucketName ? storage.bucket(bucketName) : null;
const app = express();
app.use(cors({ origin: process.env.ALLOWED_ORIGINS?.split(',').map(s => s.trim()).filter(Boolean) || '*' }));
app.use(express.json({ limit: '8mb' }));
const upload = multer({ dest: path.join(os.tmpdir(), 'divstudio-upload'), limits: { fileSize: 250 * 1024 * 1024, files: 10 }, fileFilter: (_req, file, cb) => cb(null, /^(video|audio)\//.test(file.mimetype)) });

async function requireAuth(req, res, next) {
  try {
    const header = req.get('authorization') || '';
    if (!header.startsWith('Bearer ')) return res.status(401).json({ error: 'Missing Firebase ID token.' });
    req.user = await admin.auth().verifyIdToken(header.slice(7), true);
    next();
  } catch { res.status(401).json({ error: 'Invalid or expired Firebase ID token.' }); }
}
function runFfmpeg(args) { return new Promise((resolve, reject) => { const child = spawn(ffmpegPath, ['-hide_banner', '-loglevel', 'error', ...args]); let stderr = ''; child.stderr.on('data', d => { stderr += d.toString(); if (stderr.length > 8000) stderr = stderr.slice(-8000); }); child.on('error', reject); child.on('close', code => code === 0 ? resolve() : reject(new Error(stderr || `FFmpeg exited with ${code}`))); }); }
function atempoChain(speed) { const filters = []; let value = speed; while (value < 0.5) { filters.push('atempo=0.5'); value /= 0.5; } while (value > 2) { filters.push('atempo=2'); value /= 2; } if (Math.abs(value - 1) > 0.0001) filters.push(`atempo=${value}`); return filters; }
async function cleanup(files) { await Promise.all(files.map(f => fs.rm(f, { force: true }).catch(() => {}))); }
async function publish(filePath, userId, operation) { if (!bucket) throw new Error('Firebase Storage is not configured. Set FIREBASE_STORAGE_BUCKET on the backend.'); const id = crypto.randomUUID(); const objectName = `users/${userId}/edits/${operation}/${id}.mp4`; const target = bucket.file(objectName); await bucket.upload(filePath, { destination: objectName, metadata: { contentType: 'video/mp4', metadata: { ownerUid: userId, operation } } }); const [url] = await target.getSignedUrl({ version: 'v4', action: 'read', expires: Date.now() + 60 * 60 * 1000 }); return { objectName, url, id }; }

const AI_MODEL_BY_TASK = { chat: 'gpt-5.6-luna', script: 'gpt-5.6-luna', storyboard: 'gpt-5.6-luna', image_prompt: 'gpt-5.6-luna', video_prompt: 'gpt-5.6-luna', code_debug: 'gpt-5.6-sol' };
async function callOpenAI({ model, prompt }) { const key = process.env.OPENAI_API_KEY; if (!key) throw new Error('OPENAI_API_KEY is not configured on the backend.'); const response = await fetch('https://api.openai.com/v1/responses', { method: 'POST', headers: { Authorization: `Bearer ${key}`, 'Content-Type': 'application/json' }, body: JSON.stringify({ model, input: prompt, store: false }) }); const raw = await response.text(); let data; try { data = JSON.parse(raw); } catch { data = { raw }; } if (!response.ok) throw new Error(data?.error?.message || 'OpenAI request failed.'); return { text: data?.output_text || '', responseId: data?.id, model }; }

async function runwayRequest(pathname, method = 'GET', body) {
  const key = process.env.RUNWAYML_API_SECRET;
  if (!key) throw new Error('RUNWAYML_API_SECRET is not configured on the backend.');
  const response = await fetch(`https://api.dev.runwayml.com${pathname}`, { method, headers: { Authorization: `Bearer ${key}`, 'Content-Type': 'application/json', 'X-Runway-Version': '2024-11-06' }, ...(body ? { body: JSON.stringify(body) } : {}) });
  const raw = await response.text(); let data; try { data = JSON.parse(raw); } catch { data = { raw }; }
  if (!response.ok) throw new Error(data?.error?.message || data?.message || `Runway request failed (${response.status}).`);
  return data;
}

async function createRunwayVideo({ prompt, imageUrl, model, ratio, duration }) {
  const seconds = Math.min(30, Math.max(4, Number(duration || 5)));
  const aspectRatio = ratio === '9:16' ? '9:16' : '16:9';
  const routerConfig = process.env.RUNWAY_ROUTER_CONFIG_ID;
  if (routerConfig) {
    const input = { promptText: prompt, aspectRatio, duration: seconds };
    if (imageUrl) input.referenceImages = [{ uri: imageUrl, role: 'first' }];
    const data = await runwayRequest('/v1/generate/video', 'POST', { configId: routerConfig, input });
    return { provider: 'runway-router', model: data?.routing?.model || 'router-selected', taskId: data?.id, routing: data?.routing };
  }
  const selectedModel = model || process.env.RUNWAY_VIDEO_MODEL || 'gen4.5';
  const directRatio = aspectRatio === '9:16' ? '720:1280' : '1280:720';
  const payload = { model: selectedModel, promptText: prompt, ratio: directRatio, duration: seconds };
  if (imageUrl) payload.promptImage = imageUrl;
  const data = await runwayRequest('/v1/image_to_video', 'POST', payload);
  return { provider: 'runway', model: selectedModel, taskId: data?.id };
}

app.get('/health', (_req, res) => res.json({ ok: true, service: 'DIVSTUDIO AI editing + AI gateway', version: '2.3.0', video_router: Boolean(process.env.RUNWAY_ROUTER_CONFIG_ID) }));

app.post('/v1/ai/generate', requireAuth, async (req, res) => { try { const taskType = String(req.body.task_type || 'chat'); const prompt = String(req.body.prompt || '').trim(); if (!prompt) return res.status(400).json({ error: 'prompt is required' }); const model = (typeof req.body.model === 'string' && req.body.model) || process.env.OPENAI_TEXT_MODEL || AI_MODEL_BY_TASK[taskType] || 'gpt-5.6-luna'; const result = await callOpenAI({ model, prompt }); await db.collection('aiJobs').doc(result.responseId || crypto.randomUUID()).set({ uid: req.user.uid, taskType, provider: 'openai', model: result.model, status: 'completed', prompt, createdAt: admin.firestore.FieldValue.serverTimestamp() }); res.json({ ok: true, provider: 'openai', model: result.model, text: result.text, response_id: result.responseId }); } catch (e) { res.status(500).json({ error: e.message || 'AI generation failed.' }); } });

app.post('/v1/video/generate', requireAuth, async (req, res) => {
  try {
    const prompt = String(req.body.prompt || '').trim(); if (!prompt) return res.status(400).json({ error: 'prompt is required' });
    const provider = String(req.body.provider || 'runway').toLowerCase();
    if (!['runway', 'runway-router'].includes(provider)) return res.status(503).json({ error: `Provider '${provider}' is registered but its adapter is not enabled yet.` });
    const imageUrl = typeof req.body.image_url === 'string' && req.body.image_url ? req.body.image_url : null;
    const result = await createRunwayVideo({ prompt, imageUrl, model: req.body.model, ratio: req.body.aspect_ratio || req.body.ratio || '16:9', duration: req.body.duration || 5 });
    if (!result.taskId) throw new Error('Runway returned no task ID.');
    await db.collection('videoJobs').doc(result.taskId).set({ uid: req.user.uid, provider: result.provider, model: result.model, prompt, status: 'PENDING', routing: result.routing || null, createdAt: admin.firestore.FieldValue.serverTimestamp() });
    res.status(202).json({ ok: true, provider: result.provider, model: result.model, task_id: result.taskId, status: 'PENDING', routing: result.routing || null });
  } catch (e) { res.status(502).json({ error: e.message || 'Video generation submission failed.' }); }
});

app.get('/v1/video/status/:provider/:taskId', requireAuth, async (req, res) => {
  const provider = String(req.params.provider || '').toLowerCase(); const taskId = String(req.params.taskId || ''); if (!taskId) return res.status(400).json({ error: 'taskId is required' });
  try {
    const jobRef = db.collection('videoJobs').doc(taskId); const jobSnap = await jobRef.get(); if (!jobSnap.exists || jobSnap.data()?.uid !== req.user.uid) return res.status(404).json({ error: 'Video job not found.' }); const job = jobSnap.data();
    if (!['runway', 'runway-router'].includes(provider)) return res.status(503).json({ error: `Provider '${provider}' status adapter is not enabled yet.` });
    const task = await runwayRequest(`/v1/tasks/${encodeURIComponent(taskId)}`); const status = String(task.status || 'PENDING');
    if (status === 'SUCCEEDED' && Array.isArray(task.output) && task.output[0]) {
      if (job.outputUrl) return res.json({ ok: true, provider, model: job.model, status, video_url: job.outputUrl, completed: true });
      const temp = path.join(os.tmpdir(), `divstudio-${crypto.randomUUID()}.mp4`);
      try { const download = await fetch(task.output[0]); if (!download.ok) throw new Error(`Provider output download failed (${download.status}).`); await fs.writeFile(temp, Buffer.from(await download.arrayBuffer())); const published = await publish(temp, req.user.uid, 'ai-generation'); await jobRef.update({ status, outputUrl: published.url, outputPath: published.objectName, completedAt: admin.firestore.FieldValue.serverTimestamp() }); return res.json({ ok: true, provider, model: job.model, status, video_url: published.url, completed: true }); } finally { await cleanup([temp]); }
    }
    if (status === 'FAILED' || status === 'CANCELED') { await jobRef.update({ status, error: task.failure || task.error || 'Provider task failed.', completedAt: admin.firestore.FieldValue.serverTimestamp() }); return res.json({ ok: false, provider, model: job.model, status, error: task.failure || task.error || 'Provider task failed.', completed: true }); }
    return res.json({ ok: true, provider, model: job.model, status, completed: false });
  } catch (e) { res.status(502).json({ error: e.message || 'Video status lookup failed.' }); }
});

app.post('/v1/edit/transform', requireAuth, upload.single('video'), async (req, res) => { const input = req.file?.path; if (!input) return res.status(400).json({ error: 'video file is required' }); const output = `${input}-edited.mp4`; const files = [input, output]; try { const start = Math.max(0, Number(req.body.start ?? 0)); const end = Number(req.body.end ?? 0); const duration = end > start ? end - start : 0; const speed = Math.min(4, Math.max(0.25, Number(req.body.speed ?? 1))); const volume = Math.min(4, Math.max(0, Number(req.body.volume ?? 1))); const mute = String(req.body.mute ?? 'false') === 'true'; const vf = []; if (req.body.rotate === '90') vf.push('transpose=1'); if (req.body.rotate === '180') vf.push('hflip,vflip'); if (req.body.rotate === '270') vf.push('transpose=2'); if (req.body.flip === 'horizontal') vf.push('hflip'); if (req.body.flip === 'vertical') vf.push('vflip'); const af = [...atempoChain(speed), `volume=${mute ? 0 : volume}`]; const args = ['-y', '-i', input]; if (start > 0) args.push('-ss', String(start)); if (duration > 0) args.push('-t', String(duration)); if (vf.length) args.push('-vf', vf.join(',')); args.push('-af', af.join(','), '-map', '0:v:0', '-map', '0:a?', '-c:v', 'libx264', '-preset', 'veryfast', '-crf', '20', '-c:a', 'aac', '-movflags', '+faststart', output); await runFfmpeg(args); const published = await publish(output, req.user.uid, 'transform'); res.json({ ok: true, ...published }); } catch (e) { res.status(500).json({ error: e.message || 'Video transform failed.' }); } finally { await cleanup(files); } });

app.post('/v1/edit/merge', requireAuth, upload.array('videos', 10), async (req, res) => { const inputs = (req.files || []).map(f => f.path); if (inputs.length < 2) return res.status(400).json({ error: 'At least two video files are required.' }); const list = `${inputs[0]}-concat.txt`; const output = `${inputs[0]}-merged.mp4`; try { await fs.writeFile(list, inputs.map(p => `file '${p.replaceAll("'", "'\\''")}'`).join('\n')); await runFfmpeg(['-y', '-f', 'concat', '-safe', '0', '-i', list, '-c:v', 'libx264', '-preset', 'veryfast', '-crf', '20', '-c:a', 'aac', '-movflags', '+faststart', output]); const published = await publish(output, req.user.uid, 'merge'); res.json({ ok: true, ...published }); } catch (e) { res.status(500).json({ error: e.message || 'Video merge failed.' }); } finally { await cleanup([...inputs, list, output]); } });
app.use((err, _req, res, _next) => res.status(500).json({ error: err.message || 'Unexpected backend error.' }));
const port = Number(process.env.PORT || 8080); app.listen(port, () => console.log(`DIVSTUDIO AI backend listening on ${port}`));
