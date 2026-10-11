# R. ITA TV 2.1.17 LAB

Versione 2.1.17, versionCode 75. Patch rispetto alla 2.1.16. MixDrop è stato confermato funzionante dall'utente dopo la 2.1.16; il suo resolver e il player LAB sono conservati.

## MaxStream: confronto con Veezie

Nell'APK fornito, maxstreamExtract risolve prima gli intermediari. convertId ha rami diversi: per un ID isolato o /msfi/ chiama un servizio Uprot con chiave incorporata; per un URL completo usa getFinalUrl, che esegue HEAD e legge responseURL. callMaxstream carica la pagina finale, segue Continue/C0NTINUE e, se trova una verifica interattiva, apre un browser. parseMaxstreamLink gestisce lo script cccc e prova anche il secondo script compattato prima del primo. La cache di Veezie conserva il risultato per circa un'ora; non rende un ID Uprot una sorgente Media3.

La 2.1.17 implementa autonomamente due passaggi pubblici che mancavano al LAB:

- HEAD sui collegamenti HTTPS Uprot /msf/ e /msfi/, con massimo quattro redirect HTTPS. Una destinazione finale a host supportato o media viene passata nuovamente al gestore universale, quindi GET del vero host, estrazione e verifica dei bytes.
- Il percorso reale della destinazione è conservato: non viene inventato un URL maxstream.video/castv e non viene ricostruito un identificativo tramite un servizio con chiave.
- Diagnostica UPROT_PUBLIC_HEAD e UPROT_HEAD_DESTINATION. Un 403 interrompe la catena senza riprovare GET come metodo alternativo. HEAD non supportato, indicato dal server con 405/501, consente il normale GET già esistente. Il limite di redirect, HTTPS e i controlli della catena restano attivi.
- Il parser MaxStream decodifica separatamente fino a 30 script compattati letterali: ora può trovare la sorgente nel secondo script anche se il primo è pubblicitario. Non esegue eval o script remoti.
- Per i campi file/src si accettano dichiarazioni MP4/HLS/DASH riconoscibili, con query conservata, evitando che src di uno script pubblicitario sia trattato come video. Restano HTML5 esplicito, cccc e Continue/C0NTINUE sullo stesso host.
- Ogni sorgente deve superare SOURCE VERIFIED prima del player. L'alias Maxsun già presente rimane, ma non certifica da solo il recupero di un video.

## MaxStream: cosa non è risolto

Il ramo API Uprot con chiave incorporata in Veezie non viene utilizzato. Se il collegamento non espone un redirect o una destinazione pubblica, questi nuovi passaggi non sostituiscono quella conversione. Servirebbe un'integrazione autorizzata del servizio con credenziali proprie, oppure un collegamento finale fornito dal sito attraverso la normale navigazione.

Le verifiche precedenti dei link Uprot/Maxsun forniti erano state rifiutate da questo browser con Cloudflare 1005/ASN. Non sono state provate rotte alternative per aggirare il blocco. La nuova catena HEAD è verificata su fixture, non sul servizio reale: MaxStream sul dispositivo non è ancora dichiarato risolto.

## AltaDefinizione: risultato dell'analisi reale

La scheda Animals osservata dal catalogo è /thriller/45038-animals-streaming.html. Contiene:

- Anteprima VidXGo /31049299?content=trailer&player=false.
- Player del film VidXGo /31049299.
- Trailer YouTube separato.
- Iframe del film con referrerpolicy origin.

Il nuovo adattatore Alta conserva il referer d'origine dichiarato dall'iframe, per esempio https://altadefinizionex.surf/, invece dell'URL completo della scheda. In assenza della policy mantiene il comportamento precedente. Il filtro film/trailer resta attivo.

La pagina principale VidXGo aperta direttamente in questo browser restituisce 403, Access Denied e il testo «Stiamo lavorando, torniamo presto». L'anteprima incorporata ha caricato un'interfaccia video; i suoi script usano JavaScript offuscato e HLS. Questo non prova che il film completo sia disponibile e non viene usato per sostituire il film con il trailer.

Nei JavaScript e nelle configurazioni locali dell'APK Veezie analizzato non sono stati trovati riferimenti Altadefinizione o VidXGo. channels.min.js contiene estrazione generica dei cataloghi/iframe e instradamento degli host; services.min.js contiene trasporto HTTP e percorsi browser; servers.min.js i resolver degli host. Non è stata trovata una funzione VidXGo da cui ricavare un'integrazione specifica. Eventuali configurazioni remote o aggiunte successive dell'app non sono state verificate.

Questa versione corregge la policy del referer, ma NON dichiara implementato un resolver VidXGo per il player dinamico, né dichiara risolto il 403. Il fallback HTML5 pubblico esistente rimane disponibile quando una pagina accettata dichiara davvero un media supportato.

## Verifiche e conservazione

- 149 controlli JVM PASS con Java 17. Catene complete simulate: HEAD Uprot → MaxStream → MP4 verificato; secondo script compattato → media; catalogo Alta → iframe con referer origin → HTML5 pubblico → media verificato. Queste fixture non provano la disponibilità live degli host.
- Verificati arresto sul 403 HEAD senza GET alternativo, fallback per trasporto senza HEAD, script pubblicitario escluso, separazione trailer e conservazione del referer completo quando origin non è dichiarato.
- Compilazione del nucleo e dei test superata; sintassi Java modificati verificata.
- Compilazione Android tentata: AGP 8.9.1 non scaricabile da Google Maven/Maven Central, Network is unreachable. Fallimento prima del compilatore Android. Nessun APK 2.1.17 o FIRST_FRAME MaxStream/Alta sul dispositivo prodotto.
- 446 file originali protetti identici alla base 2.1.10. VOD, Live, Streaming Community LAB e VixSrc invariati. LabMultiPlayerActivity identico alla 2.1.16 confermata per MixDrop.
- Nessun codice o APK Veezie, chiave incorporata, cookie Veezie o URL firmato incluso negli ZIP.

Consultare docs/LAB217-VERIFICA.md. Applicare lo ZIP modificati alla 2.1.16 oppure usare lo ZIP completo.
