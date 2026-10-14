# Streamflix imported source

Source: Streamflix Reborn2 / Streamflix contributors
Repository: https://git.gay/streamflix-reborn2/streamflix.git
Revision: 6dca4ea (2026-10-14)
License: Apache License 2.0, included in LICENSE.

This directory contains the Kotlin extraction bodies of MixDrop, VixCloud, Dropload, Supervideo, VOE, Streamtape, Vidoza, DoodStream and StreamWish, with their upstream Video model, unpack/decode helpers and Jsoup converter. JsUnpacker retains its upstream credit to https://github.com/cylonu87/JsUnpacker.

Modifications to imported extractor files: network builders use R. ITA's selected DNS and the upstream CookieManager session contract; HTTPS downgrade remains disabled. Clients without an explicit Retrofit client are connected to this shared client. VixCloud's b=1 detection now keeps a final URL property when it has no following comma. Exact per-file upstream and imported SHA-256 hashes are in IMPORT-MANIFEST.json.

Extractor.kt is the upstream abstract API with a reduced registry supplied by R. ITA. NetworkClient.kt, StreamFlixApp.kt and the R. ITA StreamflixBridge are compatibility adapters, not a second application. No trustAll client, credential/signature service, MaxStream API or global VidXGo background token loop is imported. Existing VidXGo session handling is retained.

The R. ITA bridge retains the resolver's source, request headers, subtitle links and extraBuffering flag. One upstream invalid Supervideo Referer literal ("mainUrl") is omitted by the bridge; no guessed replacement header is added. R. ITA's subtitle selection remains under its player controls. The Streamflix UI, account system, recommendations and catalogue data models have not been copied.
