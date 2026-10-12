# R. ITA TV 2.1.27 LAB

Versione Android 2.1.27, versionCode 85.

## Correzioni R. ITA

- Ripristinata la presenza di CB01 in `Altre fonti`: la ricerca CB01 usa sempre il relativo WebView dinamico e il risultato non viene più eliminato dal filtro riservato alle schede LAB.
- Streaming Community rinnova automaticamente una volta collegamento, token e proxy HLS se la risoluzione o Media3 falliscono prima dell'avvio.
- Dopo il secondo fallimento il player mostra il codice Media3 effettivo, invece della sola frase generica `video non disponibile`.

## Conservazione

- Il percorso sperimentale Streaming Community LAB 2.1.8 non è stato modificato.
- Nessuna modifica a Live TV, RaiPlay, VixSrc, cataloghi LAB o resolver MaxStream.

## Verifica richiesta

- Da un titolo TMDB aprire `Altre fonti` e verificare che CB01 compaia quando il titolo esatto viene trovato.
- Avviare un film Streaming Community: in caso di primo errore deve apparire `rinnovo collegamento/playlist Streaming Community`; se fallisce ancora, annotare il codice Media3 mostrato.
