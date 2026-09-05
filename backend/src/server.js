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
app.use(express.json({ limit: '2mb' }));

const upload = multer({
  dest: path.join(os.tmpdir(), 'divstudio-upload'),
  limits: { fileSize: 250 * 1024 * 1024, files: 10 },
  fileFilter: (_req, file, cb) => cb(null, /^(video|audio)\//.test(file.mimetype))
});

async function requireAuth(req, res, next) {
  try {
    const header = req.get('authorization') || '';
    if (!header.startsWith('Bearer ')) return res.status(401).json({ error: 'Missing Firebase ID token.' });
    req.user = await admin.auth().verifyIdToken(header.slice(7), true);
    next();
  } catch {
    res.status(401).json({ error: 'Invalid or expired Firebase ID token.' });
  }
}

function runFfmpeg(args) {
  return new Promise((resolve, reject) => {
    const child = spawn(ffmpegPath, ['-hide_banner', '-loglevel', 'error', ...args]);
    let stderr = '';
    child.stderr.on('data', d => { stderr += d.toString(); if (stderr.length > 8000) stderr = stderr.slice(-8000); });
    child.on('error', reject);
    child.on('close', code => code === 0 ? resolve() : reject(new Error(stderr || `FFmpeg exited with ${code}`)));
  });
}

async function cleanup(files) {
  await Promise.all(files.map(f => fs.rm(f, { force: true }).catch(() => {})));
}

async function publish(filePath, userId, operation) {
  if (!bucket) throw new Error('Firebase Storage is not configured. Set FIREBASE_STORAGE_BUCKET on the backend.');
  const id = crypto.randomUUID();
  const objectName = `users/${userId}/edits/${operation}/${id}.mp4`;
  const target = bucket.file(objectName);
  await bucket.upload(filePath, { destination: objectName, metadata: { contentType: 'video/mp4', metadata: { ownerUid: userId, operation } } });
  const [url] = await target.getSignedUrl({ version: 'v4', action: 'read', expires: Date.now() + 60 * 60 * 1000 });
  return { objectName, url, id };
}

app.get('/health', (_req, res) => res.json({ ok: true, service: 'DIVSTUDIO AI editing backend', version: '1.0.0' }));

app.post('/v1/edit/transform', requireAuth, upload.single('video'), async (req, res) => {
  const input = req.file?.path;
  if (!input) return res.status(400).json({ error: 'video file is required' });
  const output = `${input}-edited.mp4`;
  const files = [input, output];
  try {
    const start = Math.max(0, Number(req.body.start ?? 0));
    const end = Number(req.body.end ?? 0);
    const speed = Math.min(4, Math.max(0.25, Number(req.body.speed ?? 1)));
    const volume = Math.min(4, Math.max(0, Number(req.body.volume ?? 1)));
    const mute = String(req.body.mute ?? 'false') === 'true';
    const vf = [];
    if (req.body.rotate === '90') vf.push('transpose=1');
    if (req.body.rotate === '180') vf.push('hflip,vflip');
    if (req.body.rotate === '270') vf.push('transpose=2');
    if (req.body.flip === 'horizontal') vf.push('hflip');
    if (req.body.flip === 'vertical') vf.push('vflip');
    const af = [];
    if (speed !== 1) af.push(`atempo=${speed}`);
    af.push(`volume=${mute ? 0 : volume}`);
    const args = ['-y', '-i', input];
    if (start > 0) args.push('-ss', String(start));
    if (end > start) args.push('-to', String(end));
    if (vf.length) args.push('-vf', vf.join(','));
    args.push('-af', af.join(','), '-map', '0:v:0', '-map', '0:a?', '-c:v', 'libx264', '-preset', 'veryfast', '-crf', '20', '-c:a', 'aac', '-movflags', '+faststart', output];
    await runFfmpeg(args);
    const published = await publish(output, req.user.uid, 'transform');
    await db.collection('editJobs').doc(published.id).set({ uid: req.user.uid, operation: 'transform', status: 'completed', createdAt: admin.firestore.FieldValue.serverTimestamp(), outputPath: published.objectName });
    res.json({ ok: true, ...published });
  } catch (e) {
    res.status(500).json({ error: e.message || 'Video transform failed.' });
  } finally { await cleanup(files); }
});

app.post('/v1/edit/merge', requireAuth, upload.array('videos', 10), async (req, res) => {
  const inputs = (req.files || []).map(f => f.path);
  if (inputs.length < 2) return res.status(400).json({ error: 'At least two video files are required.' });
  const list = `${inputs[0]}-concat.txt`;
  const output = `${inputs[0]}-merged.mp4`;
  try {
    await fs.writeFile(list, inputs.map(p => `file '${p.replaceAll("'", "'\\''")}'`).join('\n'));
    await runFfmpeg(['-y', '-f', 'concat', '-safe', '0', '-i', list, '-c:v', 'libx264', '-preset', 'veryfast', '-crf', '20', '-c:a', 'aac', '-movflags', '+faststart', output]);
    const published = await publish(output, req.user.uid, 'merge');
    await db.collection('editJobs').doc(published.id).set({ uid: req.user.uid, operation: 'merge', status: 'completed', createdAt: admin.firestore.FieldValue.serverTimestamp(), outputPath: published.objectName });
    res.json({ ok: true, ...published });
  } catch (e) { res.status(500).json({ error: e.message || 'Video merge failed.' }); }
  finally { await cleanup([...inputs, list, output]); }
});

app.use((err, _req, res, _next) => res.status(500).json({ error: err.message || 'Unexpected backend error.' }));

const port = Number(process.env.PORT || 8080);
app.listen(port, () => console.log(`DIVSTUDIO AI backend listening on ${port}`));
