# R. ITA TV — avanzamento e idee conservate

Aggiornato il 7 ottobre 2026. Il TIM Box ha superato la prova della versione 1.21 e l’utente ha autorizzato l’implementazione delle idee concordate nella versione 1.22.

## Implementato nella 1.22

- Rimosso il pulsante TMDB che consentiva di cambiare la chiave API; mantenuti catalogo e crediti TMDB.
- Continua a guardare con locandina, avanzamento, durata, scelta Riprendi/Inizia dall’inizio e rimozione manuale con pressione lunga sul titolo.
- Catalogo salvato visualizzato subito, con aggiornamento in sottofondo e conservazione dei titoli se la rete fallisce.
- Preferenza della fonte ricordata; ricerca simultanea delle sole Streaming Community e StreamingUnity, con risultati riutilizzati durante la sessione e scelta Altra fonte nel player.
- Ritorno al catalogo con titolo, posizione e focus conservati.
- Episodio successivo, anche nella stagione seguente quando disponibile; preferenza audio e sottotitoli ricordata.
- Ricerca TV nella home: digitare e confermare Cerca per vedere canali della fonte selezionata ed eventi distinti.
- Backup/importazione di URL, preferiti, Da vedere, cronologia e preferenze player; esclusi password, sessioni, configurazione cloud, identità dispositivo e segreti.
- Lista Da vedere separata dai preferiti.
- Integrazione Firebase facoltativa con email/password, recupero password, uscita locale, nome ed elenco dispositivi.
- Profilo cloud separato dalla libreria locale; unione facoltativa al primo accesso. Nessuna password incorporata nell’APK.
- Progressi salvati localmente ogni 10 secondi; coda cloud circa ogni 60 secondi e su pausa/uscita. Revisioni causali e marche di cancellazione proteggono da salvataggi offline vecchi. Non scegliere il minuto più alto.
- Identità TMDB con distinzione film/serie, stagione ed episodio per associare le fonti. La data server è usata sui salvataggi ricevuti per limitare l’effetto degli orologi diversi.
- APK universale, ARM32 e ARM64 con stesse funzioni e firma. La variante va scelta in base alle architetture supportate dal firmware, visibili nella schermata Backup.

## Configurazione ancora necessaria

L’utente ha creato il progetto Firebase r-ita-757c6 e riferito di aver configurato Auth, Firestore e regole. Il file pubblico di configurazione è incluso nella versione 1.25. Non sono state eseguite modifiche remote al progetto. Sono inclusi guida e regole Firestore; dopo la configurazione vanno verificati accesso e ripresa tra i dispositivi reali. I test delle regole usano solo un emulatore locale, senza collegarsi a un progetto reale.

La distribuzione dell’APK non concede accesso al profilo personale. Le regole richiedono autenticazione e documento owners/UID con enabled=true creato dal proprietario nella console.

Sono disponibili elenco dispositivi e uscita sul dispositivo in uso. Una revoca selettiva sicura di un dispositivo, mantenendo un solo account condiviso, richiede una successiva componente server: cancellare una voce dall’elenco non revocherebbe i token. Impostare enabled=false blocca l’intero profilo per gli accessi successivi.

## Idee future, non scelte per questa versione

- Abbinamento mediante QR o codice temporaneo.
- Account distinti per altre persone e profili familiari.
- Revoca selettiva dei dispositivi con autorizzazione server specifica.
- Ottimizzazione R8/release, da valutare senza compromettere Gecko o i player.

## Vincoli da conservare

Streaming Community predefinito e catalogo esclusivamente della fonte; dominio principale scelto dall’utente https://streamingcommunityz.promo. StreamingUnity e gli altri siti non torrent della lista sono riserve su richiesta. Gli URL manuali e le fonti rimosse non vengono ripristinati dalla lista. Nessun torrent.

Conservare tema grafite/ambrato, font Inter, menu a sottolineatura, controlli del telecomando, cursore Gecko e player attuali. Le diverse edizioni delle fonti possono avere durate e punti di ripresa non perfettamente equivalenti.

## Versione 1.26

Rinnovo Vavoo ogni 8 minuti in background; DNS Cloudflare opzionale anche Live con ripiego sistema; ritardo audio ±50 ms; login DPAD e BACK VOD corretti; pagine da 20 locandine con Altri 20 in fondo, filtri genere/anno estesi alle pagine successive e ordine recenti/A–Z; trailer ufficiali preferibilmente italiani dentro app; badge conservativo per nuovi episodi confermati dalla fonte; un solo spinner VOD; avvio diretto SC, alternative su richiesta e gestione manuale fonti non torrent. Browser Android visibile per login e pagine web, senza Gecko nella sezione VOD. Scelta locale e persistente tra avvio su Canali TV o VOD.

Google Sign-In: il file fornito non contiene client OAuth. Occorre abilitare Google in Firebase Auth, registrare SHA-1/SHA-256 della firma APK e fornire la configurazione aggiornata con il client web OAuth prima di completare questo metodo. Email/password è già implementato. Non indicare come verificata la sincronizzazione su due dispositivi finché non testata sul progetto reale.

## Versione 1.27

Corretta l’intera catena DPAD della schermata sincronizzazione: dopo Accedi sono raggiungibili Unisci libreria, recupero password, sincronizzazione, dispositivi, uscita e configurazione cloud. Aggiunta VixSrc come prima alternativa fissa tramite gli URL embed ufficiali TMDB; film e episodi usano il player web interno e i suoi eventi aggiornano il progresso locale/cloud. La fonte principale di riproduzione è ora selezionabile da Fonti oppure dalle impostazioni VOD, senza cambiare quando si sceglie una riserva per un singolo titolo. Streaming Community resta sempre il catalogo e la fonte principale predefinita.

## Versione 1.28

Separata la scelta del catalogo dalla fonte di riproduzione. Cataloghi disponibili: Streaming Community predefinito, TMDB generale, lista ufficiale VixSrc arricchita con metadati TMDB e altre fonti soltanto quando compatibili con l’adattatore di catalogo. Il player VixSrc resta WebView perché l’API ufficiale documenta l’embed ma non un flusso Media3 diretto; aggiunti blocco domini/script pubblicitari noti, popup, nuove finestre, navigazioni esterne ed elementi sovrapposti comuni. Distribuzione richiesta: solo APK universale e sorgenti.
