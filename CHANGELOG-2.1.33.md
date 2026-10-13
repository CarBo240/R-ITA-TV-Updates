# R. ITA TV 2.1.33 — consolidamento VOD e R. ITA Eventi

Base: R. ITA TV 2.1.32, versionCode 90. Nuova versione 2.1.33, versionCode 91.

## Streaming Community

- Verificata la presenza del percorso funzionante introdotto nella 2.1.24: risoluzione fresca tramite `LabScResolver`, proxy HLS locale `LabHlsProxy` e singolo rinnovo automatico in caso di errore iniziale.
- Il percorso Streaming Community storico del LAB resta separato e non è stato modificato.
- Il proxy HLS inoltra ora anche cookie WebView disponibili e intestazioni browser CORS coerenti a playlist, varianti, chiavi e segmenti.

## MixDrop

- Le fonti MixDrop selezionate dal LAB/R. ITA non ricadono più nel solo parser generico `HTML5 pubblico`.
- Il collegamento viene inviato al resolver MixDrop specifico già usato dal percorso CB01, inclusa la risoluzione dell'intermediario StayOnline.
- Il player riceve URL media, MIME e referer effettivamente estratti; il semplice riconoscimento del dominio non viene dichiarato come risoluzione riuscita.

## Live ed Eventi

- Nella schermata Live principale resta un solo pulsante **Eventi**; il pulsante Daddy Live separato è rimosso.
- Daddy Live rimane disponibile dentro R. ITA Eventi.
- Intestazione aggiornata a `R. ITA TV • EVENTI` e navigazione coerente con la palette scura/ambra dell'app.
- Giorni inglesi convertiti in italiano e orari SportOnline convertiti da `Europe/Lisbon` a `Europe/Rome`, includendo automaticamente ora legale e passaggio al giorno successivo.
- Ogni evento mostra un'icona sportiva coerente con il titolo.

## Controllo generale

- Corretto il nome obsoleto dell'Artifact GitHub (`v1.41`) in `R-ITA-TV-v2.1.33-APK`.
- Confermato che Gradle compila i sorgenti sotto `src/`; le copie Java storiche nella radice non appartengono al source set dell'app.
- Conservati VOD, RaiPlay, Firebase, VixSrc, player Live, preferiti, EPG e aggiornamento OTA non interessati dalla modifica.

## Verifica richiesta

L'ambiente locale dispone del runtime Java ma non di `javac`, Gradle o Android SDK completi. La compilazione e firma devono essere confermate dal workflow GitHub Actions; eseguire poi prove reali su TIM Box/tablet per SC, MixDrop, Eventi e Daddy Live interno.
