# BLauncher Mod — Launch Guide

Step-by-step to take the app from this device to a live URL. Everything is doable from this phone (termux) or any desktop browser. The site also works fully WITHOUT a backend — every catalogue page just talks to the official Modrinth API directly, and any feature that needs your own server shows an honest "requires configuration" notice instead of fake content.

---

## 0. What you need

| Item | Where to get it | Needed for |
|---|---|---|
| Cloudflare account | dash.cloudflare.com (free) | hosting on Pages |
| Google OAuth Client ID | console.cloud.google.com → Credentials | Sign in with Google |
| CurseForge API key | console.curseforge.com (partner access) | CurseForge catalogue + Java Maps page |
| R2 bucket (e.g. `blauncher-uploads`) | Cloudflare R2 (free tier) | real file uploads |

Without the last three items, sign-in/upload/CurseForge pages automatically go into their honest "not configured" states. Nothing breaks.

## 1. Get the code
- Unzip `BLauncher-Update.zip` (or `BLauncher-Final.zip`) — it contains the whole `web/` folder (SPA + `functions/` + `wrangler.toml` + `package.json`).

## 2. Deploy to Cloudflare Pages
- Cloudflare Dashboard → Workers & Pages → Create → Pages → **Upload assets** → drop the unzipped folder.
- OR CLI route (desktop/termux): `npm i -g wrangler@latest && npx wrangler pages deploy .` (run inside the unzipped folder).
- Build settings: **no build step**, output directory `/`. `wrangler.toml` already sets `pages_build_output_dir = "."`.
- The `functions/` folder deploys automatically as Pages Functions — that's the whole backend.

## 3. Environment variables (Pages → your project → Settings → Variables)
```
GOOGLE_CLIENT_ID = 1234567890-abcdef.apps.googleusercontent.com
SESSION_SECRET   = (generate: openssl rand -base64 32)
MAX_FILE_MB      = 100
CURSEFORGE_API_KEY = $2a$10$...   (optional)
CURSEFORGE_PUBLISH_AUTH = shared-contact-secret (optional)
```
Secrets live ONLY in the dashboard — never commit them. `.env.example` documents them without real values.

## 4. R2 bucket
- Create the bucket `blauncher-uploads` (default settings are fine).
- Pages project → Settings → **Functions → R2 bucket bindings** → add binding name `FILES` → point to the bucket.
- Uploads then land under key `mods/<sha256>/<filename>` — deduplicated by real SHA-256, executed never.

## 5. Google Sign-In unlock
- console.cloud.google.com → APIs & Services → Credentials → OAuth 2.0 Client ID (type **Web application**).
- Add `https://YOUR-DOMAIN.pages.dev` (and your custom domain) under **Authorized JavaScript origins**.
- Labels: Google Identity Services is already wired; the server verifies every credential via Google's tokeninfo against `GOOGLE_CLIENT_ID` (test suite covers fake credentials being rejected).

## 6. Smoke-check (5 minutes)
- `GET /api/config` → shows `googleLogin: true/false`, `fileStorage: true/false`, `curseforgeProxy: true/false`, max upload MB — the site surface always matches reality.
- Search something, open a mod page, open the 4 Java categories, 1 Bedrock category (should show the honest Bedrock panel), Settings page toggles, `/special`.
- `node tests/run-tests.mjs && node tests/update-tests.mjs` on any machine with node + network: all live suites, ~130 checks.

## 7. Custom domain (optional)
- Pages → Custom domains → add `blauncher.yt` etc., follow DNS instructions. HTTPS is automatic.

## 8. Launch-day checklist
- [ ] `/api/config` returns all caps as configured
- [ ] Upload a real mod zip → check R2 & the server-side SHA-256 displayed
- [ ] Publish a draft to Modrinth with your PAT in the Publish page
- [ ] CurseForge pages show real data (probe mode banner disappears once key is set)
- [ ] Intro plays once, SFX toggle works, performance mode on a low-end device
- [ ] Footer still says "Sponsored & made by Arif The Gamer"

## Known honest limitations
- CurseForge data requires their API key legally; no key → those pages show the disclosure banner.
- Bedrock catalogue pages are device-upload-only by design (no public API exists for Bedrock listings).
- The Modrinth publish flow uses the **user's own personal access token** by design (they own what they publish); the optional CurseForge publish endpoint requires the separately-set `CURSEFORGE_PUBLISH_AUTH` — without it, status is `authorized:false` with the honest reason.

Sponsored & made by Arif The Gamer.
