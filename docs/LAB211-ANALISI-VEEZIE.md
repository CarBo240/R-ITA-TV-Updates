# Analisi dell'APK Veezie fornito

L'analisi riguarda esclusivamente `app.apk` allegato: package `st.veezie`, versione 3.4.7, versionCode 30407002, minSdk 22, targetSdk 35, identificati con aapt2. Non identifica automaticamente la versione distribuita oggi. Non è stata effettuata esecuzione di Veezie o reverse engineering dei servizi remoti privati.

## Architettura osservata

L'APK contiene un frontend Cordova/Angular in `assets/www`, con registri JavaScript di parser catalogo, servizi comuni e funzioni specifiche di estrazione host. La parte JavaScript delega anche operazioni a plugin nativi. Riconoscere il nome di un host costituisce soltanto una fase intermedia.

| Componente analizzato | Responsabilità osservata | Cosa è stato adottato in R. ITA |
| --- | --- | --- |
| channels.min.js | Registro `channelsFunctions`; parser `parsePage`, `parseMoviePage`, `parseSearch`, `scrapeEpisodio`, `getEpisodiNum`; configurazioni `categories` e `videoType` | Interfaccia CatalogAdapter con parser e endpoint ricerca separati dal resolver |
| services.min.js: channelManager | Elenco canali, liste dinamiche e aggiornamento degli URL | Elenco provider LAB separato dalle preferenze principali |
| services.min.js: hostManager | Pattern di host per film e serie; `extractMovieLinks`, `extractSerieTvLinks`, associazione stagione/episodio e host | Il catalogo restituisce fonti; il registro host sceglie il resolver indipendentemente dal catalogo |
| services.min.js: httpRequestManager | GET/POST, richieste native/proxy e fallback browser in alcune piattaforme | Transport LAB HTTPS limitato; nessun proxy Veezie né riuso di sessioni |
| servers.min.js | Funzioni `mixdropExtract`, `maxstreamExtract`, `voeExtract`, `streamtapeExtract`, `doodstreamExtract`, `supervideoExtract` e numerosi altri estrattori | Classi host separate, scritte ex novo per dati pubblicamente dichiarati |
| Plugin com.stats | Bridge Cordova per cookies, proxy, flussi, token HLS e refresh nativo | Non importato; nuovo player Media3 autonomo |
| Servizi player/cast | Chromecast, avanzamento, possibili proxy/transcodifica e gestione host | Media3 interno nel nuovo percorso LAB; nessuna transcodifica Veezie |

## channels.min.js

La lettura non si limita agli URL dei canali. Gli oggetti del registro definiscono strutture e percorsi diversi: articoli, classi del layout, immagini lazy load, titolo, link dettaglio, pagine successive e query di ricerca. `getListSearchString` raccoglie più endpoint, con fallback WordPress e ricerche in stile DLE. `getListChannelFunctions` restituisce parser candidati; alcuni parser distinguono film/serie e gestiscono stagione/episodio.

Il percorso di dettaglio può delegare a hostManager, leggere iframe o passare per intermediari. Sono presenti anche funzioni che decodificano redirect e wrapper. R. ITA non riutilizza gli intermediari né le tabelle proprietarie: l'adattatore restituisce gli URL espliciti nella pagina. Le serie che espongono episodi solo tramite form/API/JavaScript richiedono un ulteriore adattatore specifico; questa versione non inventa associazioni stagione/episodio.

Sono state esaminate anche `prepareSCP`, `rewriteSC`, `filterSubtitles`, `getFileM3U` e `sortM3U8ByResolution`. Nell'APK manipolano manifest, selezionano sottotitoli e scrivono copie in cache. Non vengono trasferite in R. ITA: il percorso Streaming Community originale deve rimanere invariato e il nuovo player legge i manifest via Media3.

## services.min.js

`hostManager` mantiene più famiglie di pattern, incluso l'host contenuto dentro wrapper/intermediari. Il risultato comprende host, identificatore/link e metadati di risoluzione. Le funzioni per serie aggiungono raggruppamenti e contesto stagione/episodio. Questo conferma che catalogo e server video sono sistemi distinti.

`httpRequestManager` contiene più strategie di recupero, incluse richieste POST, bridge e browser. Sono stati osservati anche percorsi che aspettano la disponibilità della pagina nel browser. Quei meccanismi non vengono considerati un'autorizzazione a replicare accessi protetti: il nuovo client non usa i proxy, le sessioni o i cookie di Veezie.

La parte Chromecast può utilizzare `stats.proxy` e `stats.getStreamingUrl`, anche per conversioni o seek. Il solo URL restituito da un estrattore non descrive dunque sempre l'intera catena di riproduzione. Il plugin Stats espone anche `setHlsToken`, `startNativeHlsRefresh` e `stopNativeHlsRefresh`: non è stato importato o riimplementato un rinnovo automatico di token.

## servers.min.js: differenze rilevanti fra host

| Host | Meccanismo osservato nell'APK | Scelta per la 2.1.11 |
| --- | --- | --- |
| MixDrop | Recupero pagina embed, metadati MDCore, possibile packer; ulteriori percorsi redirect/debrid | Lettura MDCore e packer pubblico letterale; niente debrid, mirror o JS arbitrario |
| MaxStream | Conversione ID, intermediari, possibili percorsi differenti e parsing successivo | Soltanto pagina host diretta con media pubblico; ID conversion e gate esclusi |
| VOE | Recupero pagina, possibile URL diverso e ricerca metadato HLS | Lettura URL pubblico e dichiarazioni Base64 letterali; redirect HTTP ordinari gestiti dal transport |
| StreamTape | Elementi DOM, URL con parametri e fallback browser | Elemento/link statico o assegnazione letterale; niente ricostruzione dinamica dei parametri |
| DoodStream | pass_md5 e costruzione di URL con token, expiry e caratteri casuali | Estrattore speciale NON implementato; soltanto HTML5 pubblico |
| SuperVideo | Recupero embed e unpack del codice del player | Decodifica di soli dati packer letterali, senza eval |
| Vidoza | Lettura di sorgenti nel markup/player | src/HTML5 pubblico |
| Altri host | Numerosi formati specifici, API, form, browser, wrapper o servizi | Non dichiarati supportati; fallback HTML5 soltanto dove una sorgente è esplicita |

Questo confronto riguarda il comportamento osservato nell'APK, non garantisce che gli estrattori di Veezie siano ancora funzionanti sui servizi attuali. Nessun pezzo dei tre file JavaScript è incluso nei sorgenti o negli ZIP consegnati.

## Nuova catena verificabile

1. `LabMultiCatalogActivity` seleziona CatalogAdapter e mostra le locandine recuperate dal provider.
2. `LabPipeline.sources` legge la scheda ed elenca soltanto sorgenti, link host o iframe espliciti, evitando immagini e navigazione ordinaria.
3. `LabPipeline.resolver` sceglie la classe dal dominio del link, con fallback HTML5 e nuovo dispatch sul dominio finale dopo redirect HTTP.
4. Il resolver restituisce URL, referer, MIME candidato e sottotitoli HTML5; non restituisce un successo se trova soltanto il nome del servizio.
5. `LabPipeline.resolve` verifica i primi byte della sorgente, accetta solo HLS/DASH/MP4 riconoscibili e conserva gli eventuali parametri già dichiarati. L'accessibilità dei segmenti successivi resta da verificare durante la riproduzione.
6. L'attività passa risultato e sottotitoli a `LabMultiPlayerActivity` via Intent. Il player configura Media3, selezione tracce e diagnostica. Questo collegamento è presente nel codice; non è stato eseguito su Android in questa sessione.

## Integrità degli input analizzati

- channels.min.js: 86.242 byte; SHA-256 `d10af8ba45ce0c28ec37477e5b1e79b52726159420bfa9e2fedb2d00190f0543`.
- services.min.js: 114.341 byte; SHA-256 `267e533f45417704a550a6f023d7b6fd2c65d2556264469fa8fec882844d7e95`.
- servers.min.js: 35.248 byte; SHA-256 `4f2bd58d23bdc73c3449352f9d4015aac0b53787ccc9caecf97d79d064d4f0b2`.

## Riferimenti tecnici primari

Le firme di `DefaultMediaSourceFactory(DataSource.Factory)` e `TrackSelectionOverride.getType()` sono state verificate sulla documentazione Android Developers. La factory seleziona HLS/DASH tramite MIME e delega i file progressivi agli estrattori.

- https://developer.android.com/reference/androidx/media3/exoplayer/source/DefaultMediaSourceFactory
- https://developer.android.com/reference/androidx/media3/common/TrackSelectionOverride

Questa verifica delle API non sostituisce la compilazione con le dipendenze della versione esatta né una prova sul dispositivo.
