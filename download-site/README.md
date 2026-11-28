# BLauncher Beta download site

Static Vercel site for the Android beta download. No build step, dependencies, analytics, or secrets are required.

## Vercel settings

- Framework preset: **Other**
- Root directory: `download-site`
- Build command: none
- Output directory: `.`

The page looks up the newest successful `Build CI` run on the current Arena branch (then `main` as a fallback), finds its unexpired `debug apk (all)` artifact, and links to GitHub's public artifact-download endpoint. GitHub packages Actions artifacts as ZIP files; the downloaded archive contains `BLauncher_Beta1.apk`.

## Current deployment

- Live: <https://blauncher-beta-download.vercel.app>
- The Vercel GitHub login connection is not configured, so this initial deployment was uploaded as static files rather than linked to automatic Git deployments. The source remains in this folder; connect GitHub in Vercel to enable automatic redeploys after future site edits.
- No custom domain was configured.
