# BLauncher Beta download site

Static Vercel site for the Android beta download. No build step, dependencies, analytics, or secrets are required.

## Download and deployment

The page links directly to the public GitHub Release asset `BLauncher_Beta1.apk` (no Actions-artifact ZIP). A matching `.sha256` checksum is published alongside it. To publish a beta from this branch, manually dispatch `Build CI` with `publish_beta` enabled and the run ID of a successful `Build CI` run; it calls the reusable `Publish Beta APK` workflow, which validates the source run and artifact, downloads and re-verifies the APK, then creates the prerelease and checksum asset.

## Current deployment

- Live: <https://blauncher-beta-download.vercel.app>
- The Vercel GitHub login connection is not configured, so this initial deployment was uploaded as static files rather than linked to automatic Git deployments. The source remains in this folder; connect GitHub in Vercel to enable automatic redeploys after future site edits.
- No custom domain was configured.
