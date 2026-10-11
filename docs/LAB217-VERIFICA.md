# Verifica 2.1.17 LAB

| Componente | Stato |
| --- | --- |
| MixDrop | Funzionamento confermato dall'utente sulla 2.1.16; parser e player conservati |
| Uprot HEAD → MaxStream → sorgente | Implementato; PASS su fixture; non certificato sul link reale |
| Secondo script compattato MaxStream | Implementato; PASS fino ai bytes media su fixture |
| Uprot conversione con chiave | Non integrata; nessuna chiave incorporata Veezie utilizzata |
| Alta referrerpolicy origin | Implementata sulla dichiarazione dell'iframe reale; PASS su fixture |
| VidXGo film completo | HTTP 403 nella pagina diretta osservata; nessun resolver dinamico certificato |
| Java 17 | Nucleo compilato, 149 controlli PASS |
| Android | Build tentata, bloccata dal download AGP 8.9.1 prima del compilatore |
| Componenti originali | 446 file protetti identici; VOD/Live/Streaming Community/VixSrc invariati |

## Verifica sul dispositivo

1. Applicare la patch alla 2.1.16 o usare i sorgenti completi, mantenendo la propria firma Android.
2. Verificare che MixDrop continui a partire.
3. Aprire MaxStream da CB01. Diagnostica UPROT_PUBLIC_HEAD: se il servizio offre un redirect supportato, segue UPROT_HEAD_DESTINATION, ADAPTER MaxStream e, solo dopo estrazione/verifica, SOURCE VERIFIED e PLAYBACK FIRST_FRAME. Se non c'è destinazione pubblica, il vecchio errore intermediario resta: non è un successo del resolver.
4. Per distinguere l'intermediario dal parser, usare Prova URL host con il collegamento finale MaxStream già fornito dall'utente. Annotare il codice della fase che fallisce; questo test non sostituisce la risoluzione automatica Uprot.
5. Su AltaDefinizione il referer della fonte segue la policy dell'iframe. Un 403 resta un errore della pagina anteriore al recupero del media. Il trailer non viene passato al player del film.

I test si fermano all'ingresso Media3 verificato, non a un FIRST_FRAME reale. Le chiavi Uprot e l'apertura browser di Veezie non sono un resolver universale applicabile automaticamente a VidXGo.
