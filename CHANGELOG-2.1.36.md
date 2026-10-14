# R. ITA TV 2.1.36 — collegamenti R. ITA / LAB

VersionName 2.1.36, versionCode 94. Pacchetto cumulativo dei soli file nuovi/modificati rispetto allo ZIP 2.1.32 fornito; comprende 2.1.33–2.1.35.

- Fonti di catalogo già salvate con tipo Streaming Community: riconoscimento dell'adattatore dal dominio e normalizzazione al tipo web. Gli URL personali vengono conservati.
- Registrazione dei nuovi cataloghi ripetuta una volta nella 2.1.36, rispettando rimozioni e limite di 40 fonti.
- Il titolo della pagina principale LAB mostra la versione reale della build; rimossi alcuni riferimenti fissi 2.1.23. Serve a distinguere un problema di elenco dal caricamento di una build precedente.
- MixDrop nella pipeline condivisa: /f/ convertito in /e/, parametri non necessari rimossi dalla pagina embed, mixdrop.club normalizzato al dominio ag del riferimento. Intestazioni pagina condivise tra resolver dedicato e pipeline. Il parser di fallback non aggiunge più Referer al MP4.
- Player R. ITA con MP4 già risolto: rimosso il Referer del provider che veniva aggiunto anche quando il resolver l'aveva intenzionalmente omesso.
- SC: conserva l'URL finale dopo redirect; distingue un wrapper iframe da una risposta che contiene già la configurazione masterPlaylist. Gestisce anche URL relativi e prefissi playlist concatenati all'ID pubblico del video.
- Errori HTTP nel player: mostra codice effettivo, playlist/MP4/segmento e dominio, senza parametri firmati. Non significa che un 403 sia stato risolto o aggirato.
- MaxStream escluso. Percorso storico LAB 2.1.8, RaiPlay, Live e UI principale non modificati in questa correzione.

Verifica: nucleo Java compilato, 281 controlli sintetici passati (210 pipeline, 18 resolver 234, 43 resolver 235, 10 resolver 236), 5 test JS passati, parsing di 120 sorgenti senza errori sintattici. Aggiunta regressione Android per migrazione fonti e visibilità cataloghi; test Robolectric e build APK NON eseguiti in questo ambiente, perché la distribuzione Gradle non può essere scaricata. Non certificata la riproduzione reale sul box. Le differenze corrette sono dimostrate dalle fixture, non da richieste live al provider.

Dopo compilazione/installazione, verificare 2.1.36 in Impostazioni e nel titolo LAB. Se un provider resta in errore, riportare testo completo e titolo provato: il codice HTTP/lo stadio permettono di isolare il punto che fallisce senza presumere la causa.
