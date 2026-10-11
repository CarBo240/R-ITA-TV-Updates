# Verifica 2.1.16 LAB

| Prova | Esito |
| --- | --- |
| StayOnline reale XNnym, navigazione normale | Pagina titolo e destinazione mxdrop.top con embed raggiunte |
| Riproduzione nel browser | Unable to play media; nessuna currentSrc attiva |
| Screenshot dispositivo | AJAX supera ID e restituisce m1xdrop.net; registro 2.1.15 lo rifiutava |
| Catena StayOnline → m1xdrop → redirect → embed → MP4 | PASS su fixture |
| m1xdrop senza media / HTTP 403 | Rifiutati, non passati al player |
| Nucleo Java 17 | Compilato; 137 controlli PASS |
| Originali preservati | 446 file identici alla base 2.1.10 |
| Media3 FIRST_FRAME sul dispositivo | Non verificato |
| Uprot / VidXGo 403 | Ancora irrisolti |

## Prova della build sul dispositivo

Applicare la patch alla 2.1.15 oppure usare i sorgenti completi. Conservare firma e configurazione GitHub originali. Aprire la fonte MixDrop della scheda CB01: la catena deve passare STAYONLINE_ID_SOURCE, STAYONLINE_PUBLIC_POST, PUBLIC_API_LINK e ADAPTER MixDrop. Può seguire HOST_EMBED prima di SOURCE VERIFIED e PLAYBACK FIRST_FRAME.

Se la fonte si ferma dopo il passaggio MixDrop, la diagnostica distingue errore della pagina, estrazione della sorgente, verifica dei bytes e riproduzione. SOURCE VERIFIED non equivale a video visibile. Il nuovo alias rimuove il rifiuto della destinazione mostrato, ma non garantisce che il media remoto sia riproducibile.

Uprot è una fonte separata: il suo errore non certifica l'esito della fonte MixDrop. Streaming Community e VixSrc continuano nei percorsi preservati.

## Build Android

Gradle 8.11.1/JDK 17 compileDebugJavaWithJavac tentato: fallimento prima del compilatore Android perché AGP 8.9.1 non è recuperabile. Google Maven e Maven Central restituiscono Network is unreachable. Nessun APK 2.1.16 prodotto. Il workflow GitHub conserva firma e varianti originali ed esegue i 137 controlli LAB prima della build.
