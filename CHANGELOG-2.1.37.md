# R. ITA TV 2.1.37 — importazione diretta resolver Streamflix

VersionName 2.1.37, versionCode 95. Pacchetto cumulativo dei soli file nuovi/modificati rispetto allo ZIP 2.1.32 fornito. Copiare TUTTE le cartelle nella radice GitHub, inclusa vendor/streamflix; non inserire una nuova cartella project.

## Cosa è stato importato

Importati i sorgenti Kotlin degli estrattori Streamflix MixDrop, VixCloud, Dropload, Supervideo, VOE, Streamtape, Vidoza, DoodStream e StreamWish. Importati anche il loro modello Video, JsUnpacker, DecryptHelper, StringConverter e il converter Retrofit/Jsoup. Non si tratta più di riscrivere in Java questi parser.

Provenienza, licenza Apache-2.0, revisioni e SHA-256 originali/importati sono in vendor/streamflix. La revisione esaminata è 6dca4ea. Le modifiche ai file importati sono documentate: rete/DNS/sessione R. ITA, divieto di downgrade HTTPS e una correzione del fallback b=1 in VixCloud quando l'URL è l'ultima proprietà senza virgola.

## Collegamento a R. ITA e LAB

- SC: al raggiungimento dell'embed viene chiamato VixcloudExtractor originale. Il proxy HLS usa il medesimo client di riproduzione e la sessione cookie compatibile con Streamflix.
- CB01/MixDrop: entrambi i percorsi R. ITA (chooser CB01 e fonti LAB integrate) chiamano MixDropExtractor originale.
- Pipeline LAB multi-provider: chiama gli estrattori importati per gli host registrati; verifica il contenuto media usando il loro contesto di rete. Gli adattatori di catalogo Java della versione precedente restano collegati.
- Intestazioni del resolver conservate senza aggiungere un Referer/Origin generico; cookie condivisi con il player. Sottotitoli e preferenza extraBuffering vengono trasferiti. Il buffering aggiuntivo segue il valore massimo di cinque minuti del player Streamflix.
- Le fonti LAB riconosciute mostrano “Apri catalogo · nome provider” e aprono direttamente il catalogo. La scelta manuale dell'adattatore resta per i provider non riconosciuti. I nuovi adattatori rimangono nell'elenco completo.
- Sottotitoli: la selezione resta gestita dai controlli R. ITA; non è importato il sistema di preferenze Streamflix.
- Verifica dell'integrità dell'importazione obbligatoria sia nel workflow sia in preBuild. Una sostituzione incompleta di file non può produrre silenziosamente una build 2.1.37 con vecchi collegamenti.

MaxStream escluso, nessuna API o credenziale privata di Streamflix/Veezie. Il VidXGo esistente con refresh circoscritto al player è mantenuto; non è importato il loop globale di rinnovo. Nessun client trustAll o disattivazione del controllo dei certificati. Il percorso storico LAB SC 2.1.8 è invariato, così come i dati di RaiPlay e i controlli Live/Eventi rispetto alla correzione precedente.

## Verifica eseguita

- assembleDebug riuscito: tre APK, universale, armeabi-v7a e arm64-v8a, versionCode 95/versionName 2.1.37. Per questa verifica locale è stata usata una chiave temporanea esterna al progetto; il pacchetto non la contiene e GitHub continua a usare i Secrets di firma dell'utente.
- Compilazione Android Kotlin e Java completa riuscita, controllo duplicati e packaging APK riusciti.
- Test Android mirati: StreamflixImportedTest (2 test) e AlternativeProviders236Test (1 test), zero errori/fallimenti. Provano i resolver effettivamente importati con risposte sintetiche e la visibilità dei nuovi cataloghi dopo la migrazione.
- 281 controlli puri Java e 16 verifiche JVM dei sorgenti Kotlin importati superati; 5 test JavaScript superati.
- Hash dei sorgenti importati e collegamenti R. ITA/LAB verificati.

Questi risultati certificano compilazione, collegamenti e fixture di regressione. Non certificano la riproduzione dei siti dal TIM Box: rete, sessione, verifiche del sito e cambiamenti degli endpoint possono ancora influire. I parser non vengono sostituiti silenziosamente da un fallback generico quando l'estrattore importato fallisce.

## Prova nell'app

Nel LAB premi “Apri catalogo · nome provider”, scegli una locandina e poi il server. Per SC, scegli “Catalogo SC → proxy HLS LAB”; il percorso 2.1.8 conservato resta separato. In R. ITA usa Impostazioni → Catalogo temporaneo per aprire un catalogo, oppure Altre fonti dalla scheda TMDB. Il titolo principale LAB mostra la versione reale 2.1.37.
