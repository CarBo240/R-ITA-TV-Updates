# R. ITA TV 1.40 — prova film Cineblog / Altadefinizione

Prima integrazione sperimentale, a partire dai sorgenti 1.39, conservando le correzioni UI della 1.39. Non è una
certificazione di compatibilità dei siti: la riproduzione sul dispositivo
deve ancora essere verificata.

- Corretto il percorso della fonte web principale: un film con flusso
  riconosciuto può aprire il player interno, come già accade in Altre fonti.
- Per i film di CB01/Cineblog e Altadefinizione con pagina riconosciuta,
  se la lettura statica non trova il flusso, il browser WebView prepara il
  player e osserva le richieste video HTTPS generate dal JavaScript.
- Playlist HLS verificata prima del passaggio a Media3; per MP4 si verifica
  una risposta video mediante una richiesta parziale.
- Il passaggio conserva Referer, Origin, User-Agent e cookie della richiesta.
  I dati del flusso temporaneo passano mediante Intent e non sono aggiunti
  ai titoli salvati o alle informazioni sincronizzate.
- Esclusi iframe e link identificati come trailer; supportati iframe
  data-src e collegamenti HTML al player. Limiti di visita e verifica.
- Ogni iframe usa il proprio Referer; un host o una playlist non disponibile
  non interrompe il controllo degli altri candidati.
- Riprova nel player rinnova il riconoscimento del video dinamico.
- Pulsante Gecko nella preparazione del film, per il ripiego manuale.

## Limiti della prima prova

- Solo film; le serie web mantengono il percorso browser precedente.
- Non è stato implementato un nuovo catalogo Cineblog/Altadefinizione:
  la ricerca del titolo usa ancora le pagine/form HTML già supportati.
  La ricerca client-side senza risultati HTML può non trovare il titolo.
- Il player dinamico deve effettuare una richiesta .m3u8 o .mp4 riconoscibile.
  Video blob senza tali richieste, manifest senza estensione e DRM non sono
  risolti da questa integrazione.
- Se il sito espone più player, la preparazione dinamica prova il primo
  collegamento riconosciuto che non sia un trailer. Il click iniziale sul
  video può essere necessario.
- Una risposta 403 o Site Unavailable nella ricerca non è superata:
  l'app non può affermare la disponibilità del titolo.

## Verifiche eseguite

- 14 asserzioni JVM sulle regole di riconoscimento, link, trailer, iframe
  differiti, esclusione pubblicità e URL video.
- Compilato ed eseguito il metodo reale VodWebSource.resolve con dipendenze
  Android/catalogo simulate: parent Referer, host indisponibile, playlist
  indisponibile, trailer escluso e limite di 5 pagine.
- Parsing della sintassi Java dei sorgenti e dei test.
- Letta la pagina pubblica Altadefinizione di Timecop: due iframe distinti,
  trailer e player film. Il player ha restituito 403 in questo ambiente.
- Cineblog/CB01 ha restituito Site Unavailable; nessuna riproduzione reale
  certificata per questa fonte.

Build APK, test Robolectric e test su Android non eseguiti localmente:
SDK Android e Gradle non disponibili. Il workflow GitHub conserva la
compilazione con la chiave esistente e include la verifica JVM dei link.

VersionCode 48. Configurazione di firma e workflow Release identici alla 1.39.
