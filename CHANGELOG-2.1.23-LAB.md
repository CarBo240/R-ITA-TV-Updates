# R. ITA TV 2.1.23 LAB

Versione Android 2.1.23, versionCode 81. Intervento limitato al Laboratorio TEST.

## Streaming Community / VixCloud

- Analizzato il percorso effettivo dell'APK Veezie fornito: il suffisso `#vixcloud` è un marcatore locale che attiva nel player nativo un proxy HLS; non è una chiave e non viene inviato al server.
- Aggiunto un percorso separato **Catalogo SC → proxy HLS LAB**, senza modificare le attività Streaming Community ereditate dal LAB 2.1.8.
- Il nuovo resolver ottiene una playlist fresca dalla pagina embed e non la richiede in anticipo, evitando una richiesta aggiuntiva prima di Media3.
- Il nuovo relay è vincolato a `127.0.0.1`, accetta soltanto destinazioni HTTPS pubbliche e trasmette Referer, Origin, User-Agent, cookie e richieste Range.
- Le playlist HLS vengono riscritte ricorsivamente: master, varianti, audio, sottotitoli, chiavi e segmenti passano dallo stesso contesto HTTP.
- Aggiunti catalogo con locandine e player Media3 dedicati al test proxy. Il vecchio percorso compare ancora come **Percorso Streaming Community 2.1.8 conservato**.

## Limiti di verifica

- Il link copiato da Veezie restituisce HTTP 403 anche con intestazioni browser perché token e sessione possono essere legati al dispositivo o alla rete che li ha emessi. Il nuovo percorso risolve il token direttamente sul dispositivo e avvia il relay nella stessa sessione.
- La riproduzione effettiva deve essere confermata su Android TV: l'ambiente corrente non contiene JDK/Gradle wrapper, quindi non è stato possibile produrre un APK o eseguire la compilazione Android locale.
- Serie ed episodi restano sul percorso 2.1.8 conservato in questa prima prova del proxy.

## Componenti invariati

VOD principale, Live TV, RaiPlay, Mediaset Infinity, VixSrc, resolver multi-provider e player di produzione non sono stati modificati. `ExperimentalCatalogActivity`, `ExperimentalPlayerActivity` e `VodSource` conservano gli hash precedenti.
