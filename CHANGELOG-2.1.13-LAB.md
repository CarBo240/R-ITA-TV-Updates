# R. ITA TV 2.1.13 LAB — correzione catalogo e catena pubblica

Versione 2.1.13, versionCode 71. Patch rispetto alla 2.1.12. Questa revisione non certifica la riproduzione live di CB01 o VidXGo.

## AltaDefinizione

- Il vecchio parser catturava gli article del carosello iniziale: sei titoli con loghi. Il nuovo adattatore legge prima gli anchor delle schede con data-title/data-id e immagine, poi gli altri anchor con locandina e URL di dettaglio.
- Associa titolo, locandina e dettaglio della stessa scheda, deduplica gli URL e preferisce data-src/data-original alle immagini placeholder. Scarta loghi riconoscibili, immagini original del carosello e backdrop w780/w1280. Non costruisce URL TMDB o immagini non dichiarate dal sito.
- Legge fino a 100 titoli dal markup della pagina. Rimane disponibile il parser DLE precedente come fallback. Nessuna paginazione automatica aggiunta.
- Ricerca aggiornata al percorso pubblico osservato nel browser: /archivio?search=… . I titoli ottenuti vengono filtrati anche per il testo cercato per evitare che una risposta contenente la home mostri risultati estranei. Il catalogo DLE generico conserva la propria ricerca index.php.

## Intermediario → host → resolver → media

- Eliminato il rifiuto immediato INTERMEDIARY_UNSUPPORTED. Il LAB ora richiede la pagina dell'intermediario e segue i redirect HTTPS ordinari, i link/iframe dichiarati a host riconosciuti o media, e i meta refresh pubblici verso tali destinazioni.
- La destinazione passa nuovamente al registro dei resolver: il catalogo e l'etichetta della riga non determinano il resolver finale. Un redirect a MaxStream usa MaxStream anche quando l'etichetta iniziale dice MixDrop.
- In caso di più destinazioni, l'etichetta del server viene usata soltanto se identifica un unico host. In caso contrario viene restituito INTERMEDIARY_AMBIGUOUS: non viene scelto un video arbitrario.
- Aggiunti limite di cinque passaggi, rilevamento dei cicli, deduplicazione dei link pubblici e rifiuto delle destinazioni HTTP. Gli iframe di domini sconosciuti non sono seguiti attraverso questi intermediari.
- Diagnostica HOST_SEARCH INTERMEDIARY/PUBLIC_LINK, RESOLVER REDISPATCH, INTERMEDIARY_NO_PUBLIC_LINK, INTERMEDIARY_AMBIGUOUS, CHAIN_CYCLE e CHAIN_LIMIT.
- Restano obbligatorie estrazione di una sorgente e verifica della firma HLS/DASH/MP4 prima dell'Intent al player. Riconoscere il nome del dominio non equivale a risolverlo.

## Trasporto del player LAB

- Il client dedicato a Media3 segue i redirect che mantengono lo schema HTTPS anche sulle richieste di manifest/segmenti. I cambi di schema restano disabilitati. Il client di estrazione conserva il proprio controllo manuale dei redirect e il limite esistente.
- Nessuna modifica ai player Streaming Community, VixSrc, Live TV o VOD principale.

## Verifica e limiti

- 81 controlli JVM superati; compilazione Java 17 del nucleo e dei test superata. Nuove prove complete con intermediario → MixDrop/MaxStream → bytes MP4 verificati, refresh, redispatch, ambiguità, ciclo, limite, challenge e catalogo moderno.
- Sintassi Java verificata per i quattro sorgenti toccati. 446 file originali identici alla 2.1.10 allegata, inclusi i percorsi SC/VixSrc e il VOD/Live. Non sono stati forniti archivi storici separati 2.1.1/2.1.8: è conservata la versione ereditata dalla base allegata.
- Layout pubblico AltaDefinizione e percorso ricerca osservati nel browser. Il browser non è una prova della risposta ricevuta da OkHttp sul televisore. Nessuna prova FIRST_FRAME Android effettuata.
- La riproduzione dei link reali di Animals su CB01 resta non verificata: cb01uno.wiki non è accessibile da questo ambiente e gli screenshot non riportano gli URL completi uprot/stayonline. Servono i collegamenti esatti per studiare il comportamento specifico.
- L'APK Veezie mostra che StayOnline può richiedere una chiamata AJAX dedicata e che alcuni percorsi Uprot usano un servizio con chiave incorporata. Tali integrazioni non sono implementate: non viene riutilizzata la chiave di Veezie, né presentato il solo attraversamento HTML come equivalente a quei metodi. Se la pagina non espone link pubblici compatibili, il LAB si ferma indicando precisamente il passaggio.
- VidXGo rimane non integrato: il 403 segnalato precede il recupero della sorgente; la causa non è determinata. Nessun aggiramento di verifiche, login o controlli di accesso aggiunto.
- Build Android bloccata dalla risoluzione di AGP 8.9.1 nell'ambiente. Nessun APK 2.1.13 consegnato; compilazione completa e installazione restano da eseguire nel workflow GitHub/Android Studio.

I rapporti LAB211/LAB212 inclusi sono storici; questo changelog e docs/LAB213-VERIFICA.md descrivono lo stato corrente.
