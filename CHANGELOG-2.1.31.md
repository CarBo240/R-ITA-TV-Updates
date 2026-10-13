# R. ITA TV 2.1.31 — modifiche dalla 2.1.30

- Versione Android aggiornata a 2.1.31 (`versionCode 89`).
- Dettaglio titolo: intestazione sovrapposta al banner della scheda, con leggero sfondo sfumato e testo bianco.
- Continua a guardare: mantenute le dimensioni delle schede; immagine adattata con `FIT_CENTER` anziché tagliata in `CENTER_CROP`, mantenendo sfondo scuro e barra di avanzamento.
- Pressione lunga sulle locandine: dizione coerente «Da vedere» al posto di «Da guardare»; dati e funzionalità invariati.
- Menu laterale VOD: si richiude automaticamente quando il focus esce dal menu; conservata l'apertura premendo SINISTRA dalla prima locandina.
- Fine episodio: eliminato il popup «Episodio successivo» e conservato l'avanzamento automatico usando la stessa sorgente del player corrente.
- Ricerca per piattaforma: aggiunti i controlli Anno e Generi già disponibili nella ricerca generale; resta attivo il vincolo provider TMDB Italia.
- RaiPlay: primi 20 elementi visualizzati, successivi 20 caricati durante lo scorrimento. Le pagine RaiPlay notificano progressivamente le locandine disponibili; la ricerca usa sempre la pagina RaiPlay di ricerca completa, non il sottoinsieme visualizzato.

## Verifiche da eseguire sui dispositivi

1. Compilare con il workflow GitHub Actions configurato nel repository (ambiente di lavoro attuale privo dell'SDK Android e di Gradle installato).
2. Verificare su TIM Box focus, menu, dettaglio e ricerca per piattaforma con generi/anni.
3. Verificare su RaiPlay ricerca di un titolo non presente nelle prime 20 locandine e scorrimento completo.
4. Verificare fine episodio e mantenimento della medesima fonte su contenuti riproducibili.

Il LAB e le altre funzioni non interessate sono mantenuti dai sorgenti originali 2.1.30.
