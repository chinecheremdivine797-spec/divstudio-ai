# DIVSTUDIO AI v2

Version 2 upgrades DIVSTUDIO AI from a single-provider animation app into a multi-model AI creative studio.

## Core direction

`DIVSTUDIO AI -> AI Model Hub -> specialist AI providers -> creation pipeline -> editor -> MP4`

## Visual creation modes

- Cartoon: 2D, 3D, anime, comic, storybook/fantasy, African cartoon and other stylized looks.
- Realistic: photorealistic, cinematic, documentary and realistic environments/characters.

## Model hub

Provider adapters allow Gemini, Grok, Veo and future authorized providers to be connected without coupling the app to one vendor. A provider is only enabled when an official API/SDK and credentials are available; DIVSTUDIO AI does not bypass provider access controls.

## v2 workflow

1. Idea or script
2. Story and prompt planning
3. Character creation/reuse
4. Scene creation/reuse
5. Storyboard
6. Model selection by capability
7. Text-to-video or image-to-video generation
8. Generation state tracking
9. Real MP4 preview
10. Editing, voice and subtitles
11. Export and project persistence

## Backend

Firebase remains the backend direction for authentication, Firestore project data, Storage and security/App Check. No Supabase is part of v2.

## Payments

Payments are intentionally disabled for v2 foundation work. No Paystack, Flutterwave, Stripe, subscriptions or billing are included.

## Release strategy

Do not build another test APK during feature development. Complete the v2 implementation first, then run the full test/build pass and prepare the final release artifact.
