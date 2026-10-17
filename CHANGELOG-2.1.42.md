# R. ITA TV 2.1.42

- Eventi: la WebView non viene più silenziata continuamente mentre cerca il flusso per il player interno; mute e pausa avvengono al passaggio effettivo al player.
- Eventi: riconoscimento delle richieste HLS/DASH provenienti anche dai frame video osservati nella sessione corrente; le richieste arrivate prima del riconoscimento del frame vengono rivalutate.
- WebView Eventi: comando fullscreen dell'app disponibile anche nel menu; rimozione dei margini dell'app e adattamento del video o iframe alla finestra. Uscita con Indietro.
- Conservata la logica di risoluzione e riproduzione Daddy Live.
- Versione 2.1.42, versionCode 2142.

Verifiche: confronto degli script con l'APK 2.1.41 fornito, test JavaScript di audio/handoff/fullscreen e compilazione Java del modulo Live. La riproduzione e l'audio sul dispositivo richiedono prova dopo l'installazione.
