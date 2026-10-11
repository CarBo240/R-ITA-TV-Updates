# R. ITA TV 2.1.19 LAB

Base: sorgenti completi 2.1.18 LAB; patch incrementale sulla 2.1.18.

## EuroStreaming.design: film
- Adattatore dedicato: catalogo `/archive?type=movie`, ricerca `/search?type=movie&q=`, locandine dai blocchi post-thumb, schede `/detail/film-<TMDB>-<slug>`.
- Controllo obbligatorio della concordanza fra ID della scheda e embed pubblico VixSrc; nessuna fonte generata se embed assente o discordante.
- Fonte Diretto collegata al bridge LAB con ID TMDB numerico. Nessuna chiave TMDB necessaria per questo percorso; non si esegue ricerca approssimativa.
- Resolver VixSrc e player sperimentale originali R. ITA riutilizzati senza modificarli.
- Configurazioni LAB esistenti con dominio esatto eurostreaming.design selezionano automaticamente l'adattatore; selezione manuale disponibile per altri domini con lo stesso layout.
- Solo film: navigazione stagioni/episodi EuroStreaming non implementata. Eurostreamings.yachts è un altro catalogo e non è dichiarato supportato da questo adattatore.

## MaxStream: estrazione pubblica, correzione parziale
- Dopo il blocco player id=m3u8, legge anche un unico URL HLS pubblico presente nella pagina, come il fallback osservato negli script forniti di Veezie.
- Conserva query completa, virgole e percorso .urlset del manifest. Più manifest senza associazione certa producono MAXSTREAM_HLS_AMBIGUOUS.
- Conservati HEAD pubblico Uprot, redirect, Continue, parser packed e verifica del manifest.
- NON dichiarato risolto: msdec e signParams di Veezie dipendono da backend e identità client firmata; non sono copiati. Il flusso HLS copiato dall'utente dimostra quale sorgente restituisce Veezie, non come ottenerla automaticamente.
- L'ambiente di verifica era stato rifiutato con Cloudflare 1005/ASN da Uprot e dal link finale Maxsun. Nessun tentativo di aggiramento. Serve la diagnostica LAB aggiornata sul dispositivo per identificare la fase ancora in errore.

## l5hs.k9vo.com / PPCine
- Analizzati catalogo API, identità client, vod/info_new, ppcine_stream e firma CDN negli script forniti.
- Il nome Diretto in Veezie è il risultato di risoluzione API e firma dei manifest e segmenti, non un collegamento pubblico pronto.
- Integrazione non disponibile: richiede autorizzazione e credenziali proprie del servizio, specifiche per API e CDN. Una chiave inventata localmente non è riconosciuta dal server. Nessun segreto di Veezie estratto o riutilizzato.
- CATALOG/K9VO_SIGNED_API_REQUIRED spiega il limite quando si tenta di caricare questo dominio; non compare un falso catalogo HTML o un resolver fittizio.

## Conservazione e verifica
- AltaDefinizione funzionante secondo l'utente: adattatore, VidXGo, rinnovo token e player conservati. MixDrop conservato.
- VOD principale, Live TV, percorso Streaming Community 2.1.8 e resolver/player originali non modificati.
- 190 controlli Java su fixture: catalogo, ricerca fonti, selezione host, resolver, validazione ingresso player, casi negativi Euro e MaxStream. Non equivalgono a playback live sul dispositivo.
- 446 file originali protetti verificati identici. Controllo sintattico Java dei file Android modificati.
- Build Android locale bloccata prima della compilazione: plugin AGP 8.9.1 non disponibile nella cache e repository Google/Maven non raggiungibili. Nessun APK consegnato come compilato. Workflow GitHub predisposto per build 2.1.19.

## Prova sul dispositivo
1. Aprire EuroStreaming.design nel LAB, selezionare un film e la fonte Diretto VixSrc. Verificare VIX_TMDB, VIX_EXISTING, VIX_VERIFIED e primo frame nel player originale.
2. Riprovare il link Uprot già configurato e copiare tutta la Diagnostica: serve distinguere redirect/conversione, HTML finale, manifest e riproduzione.
3. Non sostituire sorgenti dinamiche con il manifest HLS copiato da una sessione Veezie.
