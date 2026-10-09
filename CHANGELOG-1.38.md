# R. ITA TV 1.38 — modifiche VOD (sorgenti da verificare con GitHub Actions)

- Player VOD: seek avanti e indietro di 15 secondi sia nei controlli Media3 sia con DPAD.
- Home: hero più compatto, cerca più compatta, card orizzontali e barre di avanzamento per «Continua a guardare»; la visibilità effettiva dipende dalla risoluzione e dalla densità dello schermo.
- Piattaforme: card orizzontali con loghi TMDB, dove presenti, e nome leggibile come fallback.
- Menu laterale: rail a icone persistente e menu completo al focus su Home, catalogo/schede e impostazioni. Mantiene tutte le voci esistenti.
- Scheda dettagli: modalità a tutta larghezza per accesso diretto da Home, immagine di sfondo, azioni, trama e cast. Cast ricavato da TMDB, con consultazione della filmografia della persona.
- Serie: pulsante Episodi apre selezione visuale delle stagioni (card a due colonne con locandina, anno, numero episodi e sinossi).
- Episodi: card con fotogramma TMDB, trama, barra individuale di avanzamento; OK richiama il player già integrato, senza cambiare fonte video.

## Ambito e verifiche
Sorgenti conservati dal progetto 1.37; non sono stati modificati i componenti Live TV, Eventi, DaddyLive e GeckoView. VersionCode 46, versionName 1.38. Verifica statica di struttura senza SDK Android; nessuna compilazione completa né test su TIM Box in questo ambiente. Prima di una Release serve compilazione verde GitHub Actions e prova su dispositivo.

## Attenzione
La scheda stagioni/episodi è presentata tramite dialoghi Android ampi (non nuove Activity). Le dimensioni delle card e il posizionamento del menu potrebbero richiedere affinamenti dopo la prima prova con il telecomando. Il servizio TMDB può non fornire fotografie e cast per ogni titolo.
