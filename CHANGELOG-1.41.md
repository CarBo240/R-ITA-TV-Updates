# R. ITA TV 1.41

Base: 1.40, versionCode 49. Conservate le modifiche della 1.39 e la
configurazione di firma GitHub della 1.40.

## Piattaforme e telecomando

- Bordo bianco di 4 dp sopra il logo, visibile solo sulla card a fuoco.
- Sfondo accentato durante il focus e margine interno per il logo.
- I figli della card non ricevono il focus: OK agisce sulla piattaforma.

## Fonti VOD

- Rimosso il pulsante Gecko e l'avvio di GeckoEventActivity dal browser VOD.
- Rimosse le indicazioni Gecko nelle note di disponibilità delle fonti VOD.
- La sezione Eventi conserva il proprio motore e la propria navigazione.
- Titoli con anno tra parentesi, qualità HD/ITA e suffisso CB01 sono
  riconosciuti; un anno esplicito diverso resta escluso.
- Riconoscimento dei titoli nei titoletti delle card anche quando il link
  contiene voto, descrizione e altri dati.
- Ricerca statica del nome italiano e originale, e riconoscimento dei titoli
  già presenti nella pagina principale.
- Per CB01 e Altadefinizione, quando la ricerca HTML non è sufficiente,
  la fonte resta selezionabile con «Verifica nel sito · ricerca dinamica».
  Questa voce NON significa che la disponibilità sia già confermata.
- Il WebView dell'app carica la pagina reale, compila il modulo di ricerca
  ed esamina il DOM aggiornato dal JavaScript. Nessun nuovo bridge Java
  viene esposto alle pagine per questa ricerca.
- La pagina del titolo può essere riconosciuta anche dopo selezione manuale.
- Quando la pagina del film è confermata, viene cercato il player senza
  trailer; se si rileva un HLS/MP4 compatibile, si passa al player nativo.
- Le ricerche non confermate non sono memorizzate come disponibilità.

## GitHub e verifiche

- Il controllo prima della build è inserito direttamente nel workflow:
  non dipende più dalla presenza di scripts/check-web-movies.sh.
- Un messaggio esplicito segnala le cartelle o i sorgenti nuovi mancanti.
- Firma, Secrets e workflow Release invariati.
- 14 controlli JVM sui link; controlli sul metodo reale di ricerca/risoluzione
  con dipendenze Android/catalogo simulate; script di ricerca compilato e
  provato con DOM simulato; sintassi Java e manifest verificati.
- Aggiunti test Robolectric sul riconoscimento di titoli, anni e pagine,
  e sulla distinzione tra ricerca da verificare e disponibilità confermata.

## Da verificare sul dispositivo

APK, focus grafico su TV, ricerca live e riproduzione non sono stati testati
in questo ambiente, privo di Gradle e SDK Android. La ricerca dinamica usa
il modulo pubblico del sito: 403, sito indisponibile, cambi di pagina o
player non compatibili possono ancora impedirne il funzionamento. In quel
caso servono nome del titolo, fonte e messaggio visualizzato.
La riproduzione nativa aggiunta riguarda i film; le serie delle fonti web
mantengono la navigazione nel browser dell'app.
