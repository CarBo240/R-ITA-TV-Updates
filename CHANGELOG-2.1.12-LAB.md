# R. ITA TV 2.1.12 LAB

VersionCode 70. Correzione successiva alla 2.1.11, a seguito delle schermate CB01/Animals e AltaDefinizione/Neagley. Mantiene le cartelle `lab211` già create su GitHub.

## Correzioni

- YouTube/YouTube-nocookie e trailer esplicitamente marcati vengono separati dalle fonti del titolo. Non vengono più inviati al resolver Media3 del film/episodio.
- Se la scheda contiene soltanto YouTube o trailer, compare un messaggio preciso: nessuna fonte del titolo individuata. Il numero dei candidati principali esclude le anteprime.
- Le fonti sono distinte fra media diretti, host con adattatore, pagina esterna da verificare, intermediario e trailer/YouTube. La dicitura «HTML5 pubblico» non viene più usata nella lista per suggerire che un host sconosciuto sia supportato.
- Recuperati anche i link dichiarati tramite `data-url`, `data-link`, pulsanti, lazy iframe e link esplicitamente etichettati «Titolo - Player». Un `data-src` vuoto non nasconde più un `src` valido.
- I link pubblici a stayonline/staycheck, uprot, rapidcrypt, linkhub, protectlinker e safevideo non vengono scartati. Compaiono come intermediari NON supportati: nessun resolver di questi intermediari è dichiarato implementato, né vengono aggirati gate o verifiche.
- Il testo degli anchor diventa l'etichetta della fonte, quando non è disponibile un titolo.
- L'errore 403 specifica che il server ha rifiutato l'accesso prima del recupero della sorgente; la causa non viene arbitrariamente attribuita ad antibot, DNS o player.
- Il nome di un film come «Trailer Park Boys» non viene interpretato come un trailer: il filtro sulle etichette richiede un'indicazione esplicita.

## Cosa rimane irrisolto

- CB01: lo screenshot mostra solamente un iframe YouTube. La correzione evita il falso positivo e amplia la lettura dei collegamenti pubblici, ma non certifica il recupero delle fonti del film Animals. Non è stata resa disponibile la pagina CB01 tramite lo strumento di verifica; serve l'URL completo della scheda o il suo markup accessibile per adattare il layout reale.
- AltaDefinizione/Neagley: la pagina dettaglio pubblica distingue Trailer e Player. Il collegamento del player su `v.vidxgo.co` restituisce HTTP 403 anche nella verifica esterna. La correzione NON sblocca questo server e NON implementa un resolver VidXGo completo.
- La riproduzione dei due titoli degli screenshot resta non dimostrata. Nessuna promessa di funzionamento dei servizi esterni.

## Verifiche

- 65 controlli JVM superati (47 precedenti più 18 regressioni specifiche).
- Pipeline Java compilata con il compilatore JDK 17 del sistema; sintassi delle attività modificate verificata.
- 446 file originari della 2.1.10 ancora identici byte per byte; i percorsi Streaming Community, VixSrc, Live e VOD principale restano invariati.
- Compilazione Android offline tentata: bloccata dal plugin `com.android.tools.build:gradle:8.9.1` assente in cache. Nessun APK prodotto o prova sul dispositivo.

## File e caricamento da tablet

Lo ZIP «solo file modificati» è una patch rispetto alla 2.1.11 già caricata. Sostituire i file nelle cartelle esistenti rispettando i percorsi. Lo ZIP completo include tutte le modifiche dalla base 2.1.10.

File di codice da sostituire:

- `src/it/carmine/streamplayer/lab211/LabPipeline.java`
- `src/it/carmine/streamplayer/LabMultiCatalogActivity.java`
- `src/it/carmine/streamplayer/ExperimentalHubActivity.java`
- `tests/lab211/LabPipelineCheck.java`
- `build.gradle`
- `.github/workflows/build-apk.yml`

Caricare inoltre i nuovi documenti presenti nello ZIP. Non creare una cartella `lab212`: il modulo mantiene il percorso esistente `lab211`.
