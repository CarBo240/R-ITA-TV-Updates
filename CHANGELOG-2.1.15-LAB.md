# R. ITA TV 2.1.15 LAB

Versione 2.1.15, versionCode 73. Patch rispetto alla 2.1.14. Correzioni autonome dopo un nuovo confronto con l'APK Veezie fornito. Nessun codice Veezie eseguito o incluso, nessuna chiave incorporata riutilizzata.

## Problemi segnalati

Gli screenshot della 2.1.14 mostrano STAYONLINE_NO_ID per MixDrop, INTERMEDIARY_NO_PUBLIC_LINK per Uprot e HTTP_403 per VidXGo. Sono errori anteriori al recupero del media: non dimostrano un errore del decoder Media3.

## StayOnline: ID prima dei redirect

- Veezie estrae l'ID dal collegamento selezionato prima dei redirect. Il LAB 2.1.14 analizzava invece soltanto l'URL finale e il markup: un redirect verso la root poteva perdere l'ID. La 2.1.15 conserva il collegamento originale e usa anche quello per estrarre l'ID.
- Rimosso il minimo arbitrario di sei caratteri. Gli ID pubblici accettati sono stringhe alfanumeriche con trattino/underscore di 1–128 caratteri. Si accettano anche percorsi dopo il segmento ID, senza includerli nell'ID.
- Restano disponibili l'URL finale e la dichiarazione pubblica linkId. Identificativi discordanti producono STAYONLINE_ID_AMBIGUOUS e interrompono la catena prima del POST, invece di scegliere una fonte incerta.
- STAYONLINE_ID_SOURCE identifica da dove arriva l'ID, senza mostrare credenziali o sorgenti firmate. STAYONLINE_NO_ID ora specifica quali tre punti sono stati controllati e richiede il collegamento completo.
- Rimane il controllo GET della pagina: HTTP 403/challenge fermano il flusso prima del POST. La conservazione dell'ID non evita queste verifiche. Il POST invia soltanto id e ref vuoto; nessuna chiave o cookie di Veezie.

## Altri formati pubblici osservati in Veezie

- Il client LAB conserva l'header HTTP Refresh nella risposta. Gli intermediari possono ora seguire una destinazione HTTPS dichiarata in un Refresh su risposta accettata. La destinazione deve essere host/media/intermediario supportato; restano limite di catena, controlli di ambiguità e arresto sui rifiuti. Refresh su HTTP 403 non viene seguito.
- MaxStream legge anche una dichiarazione HTTPS testuale nello script con id cccc, oltre ai formati file/src/packer già presenti. Il LAB conserva l'URL esatto e non aggiunge arbitrariamente .mp4; i bytes devono superare il controllo MP4/HLS/DASH prima del player.
- Questi formati sono ricavati dalle funzioni resolveStayOnline/mixdropExtract, extractNuovo e parseMaxstreamLink dell'APK analizzato. Le implementazioni R. ITA sono autonome. Nessuna esecuzione di script remoti o conversione Uprot con chiave.

## Collegamento selezionato visibile

- Pressione prolungata sulla riga di una fonte: mostra e permette di copiare il suo URL originale, quello effettivamente inviato alla pipeline. Questo consente di recuperare il link StayOnline completo dai risultati CB01 senza confonderlo con il dominio visualizzato nel pulsante.
- Non vengono mostrati o copiati automaticamente gli URL firmati dei media risolti.

## Verifiche e limiti

- 125 controlli JVM superati con Java 17. Nuovi casi: ID originale conservato dopo redirect alla root, ID breve, percorso aggiuntivo, ID dichiarato breve, conflitto senza POST, Refresh fino al media verificato, Refresh HTTP rifiutato, script cccc e falsa sorgente MP4 respinta. Le catene complete usano risposte sintetiche, non provano il servizio live.
- Sintassi dei quattro sorgenti Java toccati verificata. Compilazione Android tentata: vedere docs/LAB215-VERIFICA.md per l'esito finale. Nessun APK o FIRST_FRAME sul dispositivo viene dichiarato.
- 446 file originali identici alla base allegata 2.1.10. VOD principale, Live TV, Streaming Community sperimentale e VixSrc restano nelle attività e nei percorsi originali.
- CB01 Animals non accessibile dal browser di questo ambiente. Il collegamento StayOnline completo dello screenshot non è disponibile: la correzione copre una differenza concreta rispetto a Veezie, ma la causa esatta del caso segnalato resta da verificare.
- Uprot e Maxsun: nelle verifiche precedenti questo browser è stato rifiutato con Cloudflare 1005/ASN; nessun tentativo di aggiramento o nuova rotta alternativa. I rami Uprot con chiave incorporata in Veezie restano non integrati, in assenza di autorizzazione/documentazione per una chiave propria.
- VidXGo: HTTP 403 segnalato dal dispositivo ancora irrisolto. Nessuna chiave universale Veezie o resolver VidXGo trovato nell'APK. La 2.1.15 non promette di risolvere quel 403.

Lo ZIP dei soli file modificati si applica alla 2.1.14; quello completo contiene l'intero progetto. I rapporti 2.1.11–2.1.14 rimangono come cronologia.
