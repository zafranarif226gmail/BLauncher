# BLauncher Mod

A premium web experience for discovering Minecraft mods — Modrinth + CurseForge with real APIs, honest compatibility data, full version coverage, mod uploads, Google-gated publishing, and a performance-tuned mobile UI.

> Sponsored & made by **Arif The Gamer**. Not affiliated with Mojang, Microsoft, Modrinth, CurseForge or Overwolf.

## Features

| Area | What it does | Honesty guarantee |
|---|---|---|
| **Design** | Minecraft dark/green theme, light & dark mode, animated blockscape, responsive mobile UI, B pixel logo, no watermarks | Theme respects `prefers-reduced-motion` |
| **Modrinth** | Real v2 API: search, trending, popular, categories, sliders, full project details | Never fabricates data; errors surface as errors |
| **CurseForge** | Official API via the Cloudflare Functions proxy (key server-side), or user-supplied local key | Status banner tells you exactly which mode is active; browser-CORS blocks are disclosed |
| **Versions** | Every real Minecraft version — release, snapshot, beta, alpha + loader metas (Fabric/Quilt/Legacy Fabric official APIs; Forge/NeoForge from live Modrinth tags) | Exact deduped version counts; API pagination handled |
| **Upload** | `.jar` / `.mcpack` / `.mcaddon`; SHA-256 duplicate detection; real ZIP structural validation; configurable size cap; optional metadata; auto-detects loader + MC versions from genuine metadata with manual fallback | Files are never executed |
| **Platforms** | Linux · Windows · macOS · Android Phone · iPhone/iPad multi-select with dynamic "For …" heading | Universal Java/Bedrock facts only |
| **Google login** | Official Google Identity Services; backend verifies ID tokens (tokeninfo) and issues signed HttpOnly cookies | Browsing/downloading need **no** login; only upload/publish are gated |
| **Publishing** | Official Modrinth publishing API (`POST /v2/project`) with the user's own PAT, real XHR progress, drafts + preview + confirmation | Success shown only on Modrinth's real 2xx; CurseForge shown as authorization-gated |
| **Performance** | Performance Mode (auto on weak devices / reduced-motion), lazy images, cached API responses, debounced search, zero dependencies | Aimed at solid 60fps scrolling on Android 11 / 6 GB |
| **Hosting** | Cloudflare Pages + Functions + R2, custom domain ready (e.g. BLauncher.com), HTTPS enforced, no host watermarks | Domain connections are yours to verify in the Cloudflare dashboard |

## Run locally

```bash
# from this folder
python3 -m http.server 8080
# or
npx serve .
```

Open http://localhost:8080. Browsing, versions, Modrinth and platform tools work with **zero configuration**.

Backend-dependent features (CurseForge catalogue, upload to server, Google login, publishing) degrade gracefully and show honest state banners when no backend is configured.

## Deploy

See **DEPLOYMENT.md** for the full Cloudflare Pages + Functions + R2 walkthrough, secrets list and custom-domain (BLauncher.com) verification steps.

## Structure

```
web/
├── index.html            single-page app shell
├── css/                  theme + components + pages + new-features styles
├── js/
│   ├── app.js            bootstrap: theme, perf mode, nav, boot
│   ├── core.js           router, store, cached API client, UI kit
│   ├── data.js           Modrinth/CurseForge/Mojang/loader data layer
│   ├── cards.js          shared mod cards, search box, favorites
│   ├── views-*.js        home, search, versions, modrinth, cf, misc, upload, publish
│   ├── platforms.js      platform selector + genuine compatibility text
│   ├── platforms & auth  auth.js — Google Identity Services client
│   └── upload-lib.js     SHA-256, ZIP validation, metadata detection (testable)
├── functions/api/        Cloudflare Pages Functions
│   ├── config.js         capability discovery (no secrets)
│   ├── cf/[[path]].js    real CurseForge proxy (key stays server-side)
│   ├── upload.js         R2-backed uploads + SHA-256 dedupe
│   ├── publish/          CurseForge authorization status
│   └── auth/             session.js + me.js (Google tokeninfo → signed cookie)
├── tests/                Node test-suite (upload validation, version counts, live APIs)
├── wrangler.toml         Pages + R2 config (no secrets in the file)
├── _routes.json          /api/* runs Functions; everything else is static
├── _headers              security + caching headers
└── .env.example          all environment variable names documented
```

## Data sources (official)

- Modrinth API — https://docs.modrinth.com/api/
- CurseForge API — https://docs.curseforge.com/
- Mojang version manifest — https://piston-meta.mojang.com/mc/game/version_manifest_v2.json
- Fabric / Quilt / Legacy Fabric official meta APIs

## License

Project code © Arif The Gamer. Mod data, icon URLs and artwork belong to their respective owners and are displayed through the official public APIs.

## MAJOR FEATURE UPDATE — what's new

### 8 dedicated category pages (`/c/<id>`)
| Page | Edition | Source (real API only) |
|---|---|---|
| Shaders Java | Java | Modrinth `project_type:shader` facets |
| Resource Packs Java | Java | Modrinth `project_type:resourcepack` |
| Data Packs Java | Java | Modrinth `project_type:datapack` |
| Maps Java | Java | CurseForge **Worlds** class discovered at runtime via `/api/cf` proxy |
| Shaders Bedrock | Bedrock | device's real `.mcpack/.mcaddon` upload history |
| Resource Packs Bedrock | Bedrock | device uploads |
| Behavior Packs Bedrock | Bedrock | device uploads |
| Maps Bedrock | Bedrock | device uploads |

There is **no public community catalogue API for Minecraft Bedrock add-ons** (verified against Modrinth & CurseForge live). BLauncher never invents Bedrock listings — the four Bedrock pages state this honestly and surface only the device's own uploaded packs. Java categories carry modrinth-sourced search (query, version facet, author facet, sorts relevance/downloads/follows/newest/updated) with Featured · Trending · Latest · All tabs and Load-more pagination.

### Special page (`/special`)
Nine sections built only from live Modrinth responses: Project of the Day (real deterministic rotation over the real top-100 downloads), Featured with stats, Trending This Week (follows), Most Downloaded, New Releases, Recently Updated, Editor's Picks (relevance), Recommended for You (real favorites → real author facets), Special Collections (real facet combinations) and the dedicated-category tile grid.

### Loading screen + SFX + Settings
- Animated intro (pixel B, progress bar, real tips, ≤1.5 s, once per session, honors `prefers-reduced-motion`, removable in Settings).
- Sound effects are **100% WebAudio-synthesized** — no audio files exist, so there is nothing to license; toggle + volume in Settings.
- New Settings page: Performance mode, intro, SFX + volume, device upload-limit, live status of the CurseForge proxy — all device-local except server config.

### Verified this round (tests/)
- `tests/run-tests.mjs` — **39/39** (live: Modrinth, Mojang manifest, loader/quotas)
- `tests/security-tests.mjs` — **33/33** (session crypto, Google token rejection via real tokeninfo, upload guards incl. server-side SHA-256 + 413/415, proxy guard rails, config caps)
- `tests/update-tests.mjs` — **58/58** (8 categories static + live facet/author probes; honest NOT TESTED notes for CurseForge/R2 without deployment)
- Real bugs found & fixed while testing: upload.js bad relative import, Pages-context `url` misuse, test-harness context adapters.

Deliverables: `BLauncher-Final.zip` and `BLauncher-Update.zip` (web root only; builds/, node_modules/ and 4.6 GB of Android build caches excluded by design).
