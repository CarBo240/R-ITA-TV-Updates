# R. ITA TV 2.1.4 — diagnostica playlist HLS

- Versione 2.1.4, versionCode 62.
- Al rifiuto HTTP della playlist HLS, mostra se l'URL è identico o diverso dal precedente tentativo fallito nella stessa sessione del player.
- Controllo informativo dell'eventuale parametro `expires` nella playlist: segnala se la scadenza nominale è trascorsa, futura o non interpretabile. Non determina l'effettiva autorizzazione del server.
- Messaggi diagnostici contrassegnati `D214`.
- Nessun URL firmato, token o cookie viene mostrato o registrato dalla nuova diagnostica; il confronto usa soltanto un'impronta SHA-256 conservata in memoria nell'Activity.
- Il pulsante Riprova continua a recuperare il collegamento tramite il percorso originale; non sono stati inseriti tentativi automatici, cambi di User-Agent o aggiramenti dei controlli di accesso.
- Invariati gli altri provider, RaiPlay, Live TV e il comportamento del player.

La compilazione e il test Android TV sono da verificare su GitHub Actions e dispositivo.
