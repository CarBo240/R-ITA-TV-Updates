# R. ITA TV 2.1.11 LAB

Data: 11 ottobre 2026. VersionName: 2.1.11. VersionCode: 69.
Base: archivio completo 2.1.10 LAB fornito dall'utente.

## Nuovo percorso multi-provider

- Pipeline dedicata: adattatore catalogo → locandine → scheda → elenco server → resolver scelto per URL → verifica della risposta → nuovo player Media3.
- Cataloghi CB01 (articoli/card), AltaDefinizione (schede DLE), WordPress (articoli), DLE (shortstory), JSON LAB con schema esplicito. Ricerca remota WordPress/DLE; filtro locale dei titoli per JSON.
- Registro universale di resolver indipendente dall'adattatore catalogo. Selezione per etichette del dominio, non per semplici sottostringhe; nuovo dispatch dopo redirect.
- Classi distinte MixDrop, MaxStream, VOE, StreamTape, DoodStream, SuperVideo, Vidoza e fallback HTML5. Sono estrattori di formati pubblici specifici, NON integrazioni complete garantite di tutti gli host. Stato dettagliato nella tabella sotto.
- MixDrop/SuperVideo/MaxStream: decodifica limitata di payload packer letterali pubblici (radix 2–62), senza eval o esecuzione JavaScript. VOE: anche dichiarazioni HLS/MP4 Base64 e atob letterali contenenti URL HTTPS.
- Verifica sorgente mediante richiesta Range limitata: firma MP4, intestazione HLS o elemento MPD DASH. Una risposta HTML 200 non viene dichiarata come video disponibile.
- Conservazione delle query HTTPS già dichiarate dal sito, incluse firme esistenti; nessuna generazione/rinnovo di token.
- Client HTTP dedicato al LAB, senza credenziali/cookie/interceptor del VOD; quattro redirect HTTPS al massimo, timeout e limite dimensioni.
- Diagnostica CATALOG, HOST_SEARCH, RESOLVER, SOURCE; nuovo player con BUFFERING, READY, FIRST_FRAME, ENDED, errore Media3 e codice HTTP. Non espone URL con token nella diagnostica.
- Nuovo player HLS/DASH/MP4 con scelta delle tracce audio/sottotitoli realmente dichiarate, preferenza audio italiana e sottotitoli HTML5 VTT/SRT/TTML. Ripristino posizione dopo stop/start; BACK chiude prima i controlli.
- Configurazione dei provider nel solo spazio di preferenze `lab211`; pressione lunga elimina una configurazione LAB. Per i provider web già presenti si sceglie l'adattatore nel laboratorio.
- Focus delle locandine visibile tramite overlay ambra; BACK dalla scheda torna al catalogo; risposte obsolete non sostituiscono una pagina più recente.

## Stato effettivo degli host

| Host | Recupero implementato | Limiti precisi |
| --- | --- | --- |
| MixDrop | HTML5; MDCore.wurl/vurl; packer letterale pubblico | No mirror automatici, debrid, gate o JS arbitrario; nessuna prova live |
| MaxStream | HTML5; file/src JWPlayer pubblici; packer letterale | No conversione ID uprot/stayonline, castv, API riservate o gate; nessuna prova live |
| VOE | hls/mp4 pubblici, URL o Base64/atob letterali; HTML5 | No varianti cifrate, challenge, JavaScript dinamico; nessuna prova live |
| StreamTape | robotlink/videolink statico o assegnazione letterale; HTML5 | No concatenazioni JS, substring dinamiche o ricostruzione expires/token; nessuna prova live |
| DoodStream | SOLO HTML5 pubblico | Estrattore pass_md5/token/expiry NON implementato; non dichiarato supporto Dood completo |
| SuperVideo | HTML5; file/src; packer letterale pubblico | No eval, form/gate o varianti dinamiche; nessuna prova live |
| Vidoza | HTML5; src pubblico | No API private o varianti dinamiche; nessuna prova live |
| Altri | HTML5 con source/video e URL HTTPS espliciti | Nessun resolver specializzato per FileMoon, StreamWish, StreamSB ecc. |

I layout reali dei cataloghi e i formati degli host cambiano: i parser hanno formati circoscritti. La presenza della classe non certifica che il servizio attuale funzioni. Nessun host esterno è stato verificato dal vivo in questa sessione.

## Conservazione tassativa

- `ExperimentalCatalogActivity`, `ExperimentalPlayerActivity`, `ExperimentalHostInspector`, `VodSource`, `VixSrc`, `VodNetwork` identici byte per byte alla base allegata.
- Streaming Community continua ad aprire ESATTAMENTE l'attività e il player sperimentali presenti nella 2.1.10, che l'utente identifica come percorso LAB 2.1.8. Non è stato allineato al VOD principale.
- VixSrc continua nel percorso precedente con `VixSrc.resolve`; nessun nuovo metodo di estrazione.
- Nessun file VOD principale, Live TV, EPG, sincronizzazione o risorsa grafica preesistente modificato.
- La base funzionale 2.1.1 è preservata nella forma ereditata dai sorgenti allegati. Non è stato fornito un archivio 2.1.1 separato: non si certifica una comparazione storica con quell'archivio.
- Modifiche a file preesistenti: hub LAB, manifest per due nuove attività, build.gradle per la versione, local.properties per eliminare un percorso SDK non portabile e workflow build-apk per test LAB e nome artifact. Le precedenti utility di ispezione restano intatte per confronto.

## Verifiche e build

- 47 controlli JVM superati, inclusi catalogo → host → resolver → risposta media verificata, query firmate, sottotitoli, formati specifici, packer, Base64, 403, challenge e HTML falso media. Il rapporto di verifica distingue i test locali dalle prove Android non eseguite.
- `LabPipeline` e `JsonCatalog` compilati con JDK 17 e Android SDK 36 (per org.json).
- Collegamento sorgente verificata → Intent → nuova attività player controllato sul codice. La riproduzione Media3, i decoder, le tracce reali e il DPAD su TV NON sono stati eseguiti.
- Tentati Gradle offline e online: entrambi bloccati durante la risoluzione di `com.android.tools.build:gradle:8.9.1`. Nessuna copia in cache; Google/Maven non raggiungibili nella rete del container.
- Nessun APK 2.1.11 prodotto; non si dichiara completata la compilazione Android, né eseguita la suite Android/Robolectric originale.
- Gli ZIP non includono l'APK Veezie, i suoi script, i file del SDK, cache build o chiavi di firma.

## Utilizzo e GitHub

1. Caricare lo ZIP dei file modificati nella root del repository mantenendo i percorsi; oppure usare lo ZIP completo come albero completo del progetto.
2. Aprire VOD → Impostazioni → Laboratorio TEST.
3. Streaming Community e VixSrc conservano le opzioni precedenti. Per un provider web: Catalogo con locandine → selezione adattatore.
4. Per aggiungere una fonte senza modificare il VOD: “Aggiungi / configura provider LAB 2.1.11”, nome, URL HTTPS e adattatore.
5. Usare “Diagnostica” per individuare la fase e il motivo esatto di un fallimento.

Vedere `docs/LAB211-ANALISI-VEEZIE.md`, `docs/LAB211-VERIFICA.md` e `docs/LAB211-GITHUB.md`.
