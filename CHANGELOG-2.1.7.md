# R. ITA TV 2.1.7 LAB — Catalogo visuale e prova fonti

- Il LAB ora propone, per ogni provider, la voce «Catalogo con locandine → Fonti → Player».
- Nuova Activity sperimentale isolata `ExperimentalCatalogActivity`, senza modifica del VOD principale.
- Catalogo per provider di tipo streamingcommunity con le locandine originali caricate dal resolver esistente; ricerca per titolo.
- Per provider web generici, tentativo conservativo di riconoscere locandine e collegamenti nell'HTML pubblico accessibile. I siti che generano il catalogo via JavaScript o API private potrebbero non offrire risultati.
- Selezione locandina: analisi dei collegamenti pubblicamente dichiarati nella pagina titolo e presentazione delle fonti/host riconosciuti.
- Le sorgenti HTTPS media dirette supportate (HLS/DASH/MP4) possono essere provate nel player Media3 del laboratorio. Per Streaming Community disponibile anche la verifica del resolver esistente: può rispondere HTTP 403.
- MixDrop, MaxStream e AltaDefinizione: riconoscimento delle pagine/host, NON ancora estrazione automatica del video da tutti gli host; nessun bypass DRM, restrizioni 403 o protezioni.
- Nome Artifact GitHub aggiornato. VersionCode 65 / versionName 2.1.7.
- Nessuna modifica ai componenti produttivi VOD, Live TV o ai resolver principali.
