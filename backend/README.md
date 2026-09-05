# DIVSTUDIO AI Editing Backend

A separate server-side video-processing service for DIVSTUDIO AI. It is intentionally separate from the Android app and keeps provider secrets off the APK.

## Current capabilities
- Firebase ID-token authentication
- FFmpeg video transform pipeline
- Trim by start/end time
- Speed control (0.25x–4x)
- Volume/mute
- Rotate and flip
- MP4 H.264/AAC output with fast-start
- Merge up to 10 clips
- Firebase Storage output publishing
- Firestore `editJobs` audit records
- 250 MB upload limit
- Temporary-file cleanup
- Health endpoint

## Environment
Cloud Run/Application Default Credentials are used. No service-account JSON or API secrets belong in this repository.

Required for publishing output:
- `FIREBASE_STORAGE_BUCKET`

Optional:
- `ALLOWED_ORIGINS` comma-separated list of allowed app origins
- `PORT` (default 8080)

## Endpoints
- `GET /health`
- `POST /v1/edit/transform` multipart field `video`
- `POST /v1/edit/merge` multipart fields `videos[]`

Every editing endpoint requires `Authorization: Bearer <Firebase ID token>`.

## Deployment
Deploy this `backend/` directory as a Cloud Run service using a service account with only the required Firebase Storage and Firestore permissions. Then configure the Android app with the deployed HTTPS backend URL.

Do not put Gemini, Veo, Seedance, Firebase service-account private keys, or other server credentials in the Android APK.
