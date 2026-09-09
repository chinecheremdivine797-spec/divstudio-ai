import { withSupabase } from 'npm:@supabase/server@0.9.0'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
  'Access-Control-Allow-Methods': 'POST, OPTIONS',
}

const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })

const taskDefaults: Record<string, string> = {
  chat: 'gpt-5',
  script: 'gpt-5',
  story: 'gpt-5',
  image_prompt: 'gpt-5',
  video_prompt: 'gpt-5',
}

Deno.serve(withSupabase({ auth: 'user' }, async (req, ctx) => {
  if (req.method === 'OPTIONS') return new Response('ok', { headers: corsHeaders })
  if (req.method !== 'POST') return json({ error: 'Method not allowed' }, 405)

  const apiKey = Deno.env.get('OPENAI_API_KEY')
  if (!apiKey) return json({ error: 'OPENAI_API_KEY is not configured on the server' }, 503)

  const userId = ctx.userClaims?.sub
  if (!userId) return json({ error: 'Authentication required' }, 401)

  const body = await req.json().catch(() => null)
  const prompt = typeof body?.prompt === 'string' ? body.prompt.trim() : ''
  const taskType = typeof body?.task_type === 'string' ? body.task_type : 'chat'
  const requestedModel = typeof body?.model === 'string' ? body.model : null
  const projectId = typeof body?.project_id === 'string' ? body.project_id : null

  if (!prompt) return json({ error: 'prompt is required' }, 400)
  if (prompt.length > 30000) return json({ error: 'prompt is too long' }, 413)

  const { data: job, error: jobError } = await ctx.supabase
    .from('divstudio_ai_jobs')
    .insert({ owner_id: userId, project_id: projectId, task_type: taskType, provider: 'openai', model: requestedModel ?? taskDefaults[taskType] ?? 'gpt-5', prompt, status: 'running', started_at: new Date().toISOString() })
    .select('id, model')
    .single()

  if (jobError) return json({ error: jobError.message }, 500)

  try {
    const model = requestedModel ?? taskDefaults[taskType] ?? 'gpt-5'
    const response = await fetch('https://api.openai.com/v1/responses', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${apiKey}` },
      body: JSON.stringify({ model, input: prompt }),
    })

    const payload = await response.json()
    if (!response.ok) {
      await ctx.supabase.from('divstudio_ai_jobs').update({ status: 'failed', error: payload?.error?.message ?? 'Provider request failed', completed_at: new Date().toISOString() }).eq('id', job.id)
      return json({ error: payload?.error?.message ?? 'Provider request failed', job_id: job.id }, response.status)
    }

    const text = payload?.output_text ?? ''
    await ctx.supabase.from('divstudio_ai_jobs').update({ status: 'completed', result: payload, completed_at: new Date().toISOString() }).eq('id', job.id)
    await ctx.supabase.from('divstudio_usage').insert({ owner_id: userId, task_type: taskType, provider: 'openai', model, units: 1 })

    return json({ text, provider: 'openai', model, response_id: payload?.id ?? '', job_id: job.id })
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Unexpected server error'
    await ctx.supabase.from('divstudio_ai_jobs').update({ status: 'failed', error: message, completed_at: new Date().toISOString() }).eq('id', job.id)
    return json({ error: message, job_id: job.id }, 500)
  }
}))
