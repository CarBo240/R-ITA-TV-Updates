# Streamflix reference

Upstream: Streamflix Reborn2 / Streamflix, https://git.gay/streamflix-reborn2/streamflix.git
Reviewed revision: 6dca4ea, 2026-10-14.
License: Apache License 2.0 (included alongside this notice).

R. ITA 2.1.35 uses independently written Java adapters based on the publicly declared provider protocols and domain alias information reviewed in that repository. Its integrations retain R. ITA networking, UI, source validation and player architecture. No upstream API credentials, private signing keys or TLS-verification disabling logic were transferred.

Relevant upstream areas: Italian providers (CB01, StreamingCommunity, Altadefinizione01, GuardaFlix, GuardaSerie, StreamingIta, AnimeWorld, AnimeSaturn, AnimeUnity) and their associated host extractors. PlutoTV Italy is live and outside this VOD alternatives package; MaxStream intentionally excluded.

R. ITA 2.1.37 imports the Kotlin host extractors directly. See vendor/streamflix/NOTICE.md, LICENSE and IMPORT-MANIFEST.json for the exact imported source and modifications. Earlier 2.1.35 Java adapter notes describe the preceding implementation.
