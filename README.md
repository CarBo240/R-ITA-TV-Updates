> **Versione corrente: 1.41.** Per GitHub e la firma consultare
> `ISTRUZIONI-GITHUB-1.41.txt`; modifiche e limiti in `CHANGELOG-1.41.md`.

# R. ITA TV 1.31 — Home VOD OTT, TMDB e VixSrc

Versione 1.31, codice 39, Android 8+. APK universale ARM32/ARM64 con lo stesso package e la stessa firma delle versioni precedenti, installabile come aggiornamento senza perdere configurazione e libreria.

## Aggiornamenti da GitHub

La versione 1.31 controlla automaticamente GitHub Releases (al massimo una volta ogni 12 ore). In VOD → Impostazioni sono disponibili **Controlla aggiornamenti app** e **Repository GitHub**. Inserire `proprietario/repository`; la release deve avere un asset `.apk`, preferibilmente con `universal` nel nome. Il download usa Download Manager e, al termine, apre l'installazione Android con la normale conferma del TIM Box. Il pacchetto deve mantenere `applicationId` e la stessa firma digitale delle versioni installate, altrimenti Android non lo considera un aggiornamento e i dati non possono essere conservati.

Il workflow `.github/workflows/release-apk.yml` pubblica l'APK universale quando viene creato un tag `v*` (ad esempio `v1.32`). Prima di usare l'OTA va quindi impostato il repository nell'app e va pubblicata almeno una release con l'APK universale.

## Decisioni definitive del VOD

- **Catalogo predefinito: TMDB**, in italiano (`it-IT`), con locandine, backdrop, trame, generi, anno, trailer e paginazione progressiva.
- **Fonte video predefinita: VixSrc**, indipendente dal catalogo. Il flusso HLS viene risolto e riprodotto nel player Media3 interno; quando il manifest dichiara tracce audio alternative viene accettata solo una traccia italiana.
- **Piattaforme italiane:** Netflix, Prime Video, Disney+, Apple TV+, NOW, Paramount+, Max e Infinity+. Il filtro usa la disponibilità TMDB per l’Italia (`watch_region=IT`) e combina film e serie; non garantisce che la stessa piattaforma sia la fonte di riproduzione.
- Le preferenze già impostate dall’utente non vengono sovrascritte. Streaming Community, SC API, StreamingUnity, CineSearch e le fonti web non torrent restano disponibili come cataloghi o fonti alternative secondo le loro capacità.

## Nuova grafica VOD

La schermata iniziale VOD è stata ricostruita come una Home OTT pensata per telecomando, mantenendo palette grafite/ambra e font Inter. Comprende menu laterale, hero panoramica con **Guarda**, **Trailer** e **Preferiti**, e righe orizzontali per **Continua a guardare**, **Novità**, **Top 10 della settimana**, **Consigliati per te**, **Esplora per piattaforma**, film e serie popolari. La pressione lunga su una locandina aggiorna **Da vedere**.

Film, Serie TV, Nuove uscite, Preferiti, Cerca e Impostazioni aprono il catalogo completo già collaudato. Un titolo scelto dalla Home viene caricato direttamente tramite ID TMDB, senza dipendere dai risultati testuali della ricerca. I filtri includono titolo, genere, anno, ordine e piattaforma; **Altri 20** continua la paginazione.

## Funzioni preservate

Restano attive: player interno e scelta fonti, Riprendi/Inizia da capo, salvataggio avanzamento, episodio e stagione successivi, preferiti e Da vedere, cache con aggiornamento in sottofondo, ripristino focus, audio/sottotitoli, sincronizzazione cloud e dispositivi, backup, avvio su Canali o VOD, DNS Cloudflare, Vavoo con rinnovo token ogni 8 minuti, Daddy Live, Gomstream, Gecko, EPG e tutte le funzioni della Home TV. Il menu del player continua a scomparire automaticamente; BACK chiude prima il menu senza uscire involontariamente.

Validazione 1.30: **253 test Android/Robolectric superati**. Compilazione Java completata con Android SDK 36/JDK 17. Restano da verificare sul dispositivo fisico TIM Box la resa finale, l’accessibilità reale dei servizi esterni e la sincronizzazione con il progetto Firebase reale.

# Note storiche

## R. ITA TV 1.29 — VixSrc interno, SC API e CineSearch

Versione 1.29, codice 37, Android 8+; distribuita come APK universale con la stessa firma. Installare sopra la versione precedente per mantenere i dati. VixSrc viene riprodotto nel player Media3 dell’app; SC API · Blu-Tiger è disponibile come fonte alternativa; CineSearch è disponibile come catalogo configurabile tramite l’URL del proprio backend.

Streaming Community resta il catalogo predefinito. In VOD → ⚙ → **Catalogo visualizzato** si può scegliere indipendentemente Streaming Community, TMDB, VixSrc oppure un’altra fonte che espone un catalogo compatibile. TMDB mostra il proprio catalogo generale; VixSrc mostra soltanto gli ID dichiarati disponibili dalla sua API e usa TMDB per locandine e trame. Le fonti web generiche non vengono presentate come cataloghi se non espongono dati compatibili.

Il pulsante **Sito** apre la fonte corrente in Android System WebView, visibile dentro l’app, conservando la normale sessione e i cookie del browser. In caso di HTTP 403 l’app propone la stessa apertura. Il browser dispone di Indietro, Home, Ricarica e fullscreen video e non usa Gecko. Il vecchio recupero WebView invisibile è stato rimosso.

Il catalogo VOD viene letto esclusivamente da Streaming Community. TMDB arricchisce in secondo piano soltanto i titoli realmente presenti con locandina e trama, senza aggiungere film o serie propri. Le richieste al catalogo non aggiungono più parametri casuali che alcuni frontend rifiutano con HTTP 403; vengono mantenuti cookie, lingua italiana e intestazioni da browser TV. StreamingUnity resta una fonte alternativa e segnala chiaramente quando il sito richiede accesso.

Rimosso il pulsante di modifica API TMDB. I metadati continuano a usare la chiave inclusa. I crediti sono in VOD → ⚙.

Aggiunti Da vedere, Riprendi con avanzamento e scelta di ripartenza, rimozione titoli con pressione lunga, cache del catalogo e aggiornamento in sottofondo. Nel player Altra fonte torna ai risultati della ricerca conservati durante la sessione. Tornando dal player restano titolo, posizione e focus.

Le serie propongono l’episodio successivo e, quando presente nella fonte, la stagione seguente. Audio e sottotitoli ricordano la lingua preferita. Streaming Community rimane il catalogo esclusivo; la fonte principale di riproduzione si sceglie direttamente da **Fonti** o da VOD → ⚙ → **Fonte principale**.

**VixSrc** è la prima fonte alternativa, subito dopo Streaming Community. Usa gli ID TMDB e il player web ufficiale incorporato nell’app, con lingua italiana, colori coordinati e ripartenza dal punto salvato. Per le serie la stagione e l’episodio vengono scelti nell’interfaccia R. ITA TV; gli eventi pubblicati dal player aggiornano avanzamento, durata e sincronizzazione cloud. Il sito non documenta un flusso diretto stabile per Media3: il player resta web, ma l’app blocca popup, nuove finestre, navigazioni pubblicitarie esterne, domini pubblicitari noti e comuni elementi sovrapposti. Il filtro è prudente per non bloccare i CDN video e potrebbe richiedere aggiornamenti se il sito cambia pubblicità. StreamingUnity e le altre fonti non torrent restano riserve successive. Scegliere una fonte in “Altre fonti” vale solo per quel titolo e non cambia più silenziosamente la fonte principale.

Il VOD mostra 20 titoli alla volta. **Altri 20** si trova sotto l’ultima riga delle locandine ed è raggiungibile con GIÙ dal telecomando; carica la pagina successiva quando serve. I filtri genere e anno continuano a leggere le pagine di Streaming Community finché trovano almeno 20 risultati compatibili, poi consentono di caricarne altri 20. L’ordine può essere **Aggiunti di recente** (ordine del catalogo della fonte) oppure **Titolo A–Z**. TMDB completa soltanto i metadati dei titoli della fonte. **Trailer**, sotto Guarda, preferisce l’italiano e apre il player YouTube ufficiale dentro l’app (non ExoPlayer). **Nuova puntata** appare soltanto dopo aver verificato episodi effettivamente disponibili nella stagione della serie seguita, quando il precedente è completato.

**Guarda** apre la fonte principale senza ricerca simultanea automatica. **Altre fonti** cerca nelle fonti salvate, mostrando prima la principale, poi VixSrc e le riserve; i risultati falliti consentono di aprire il sito per un accesso manuale. VixSrc è una fonte fissa, mentre gli altri siti possono essere aggiunti, modificati e rimossi. La lista automatica aggiunge fonti non torrent e aggiorna soltanto URL non modificati manualmente; conserva sempre il dominio Streaming Community scelto dall’utente.

Il rinnovo della sessione Vavoo viene richiesto in background ogni **8 minuti**, mentre la home o il player Live sono attivi, senza riavviare il video. Un errore transitorio Live provoca un solo nuovo tentativo automatico; 401, 403 e 429 rispettano le limitazioni del servizio.

Audio Live e VOD: **Sincronizzazione audio**, con passi ±50 ms e limite ±2000 ms. Valori positivi ritardano il suono, negativi lo anticipano. La preferenza Live è locale al dispositivo; VOD conserva il valore per titolo. La modifica ricarica il player nello stesso punto, ove il flusso supporta la ricerca. I processori lavorano sull’audio PCM decodificato.

Premere BACK nel catalogo VOD porta il focus al menu; una seconda pressione esce. Il login cloud permette di raggiungere con le frecce email, password, Accedi, Unisci libreria e tutte le voci successive fino alla configurazione cloud; lo scorrimento segue il focus. La password non viene salvata. Firebase conserva la normale sessione autenticata. Nei siti la voce Ricorda accesso usa i cookie privati di Android System WebView, senza incorporare credenziali nell’APK. Il player VOD mostra un solo indicatore di caricamento.

In VOD → ⚙ → **Avvio app** si sceglie se aprire direttamente **Canali TV** oppure **VOD**. La scelta è locale e viene ricordata sul dispositivo; tornando indietro dal VOD si rientra normalmente nella home TV.

Il DNS Cloudflare si può attivare anche per Live tenendo premuto **Canali → DNS Live**. In caso di mancata risoluzione DoH usa il DNS del dispositivo. Non modifica i permessi di accesso o gli errori HTTP 403.

Nella home, digita e conferma Cerca per i risultati separati canali/eventi. Il backup è in VOD → ⚙ → Backup e ripristino oppure tenendo premuto Aggiorna nella home. Il file esporta URL, preferiti, Da vedere, cronologia e scelte player, escludendo credenziali, sessioni e identità cloud.

Account e sincronizzazione sono in VOD → ⚙. La configurazione del progetto Firebase r-ita-757c6 è inclusa. Restano da verificare sul servizio reale le regole e l’abilitazione del proprio UID: istruzioni e regole sono in cloud/README.md e cloud/firestore.rules. Senza configurazione funziona tutto localmente. Non ci sono password nel codice o nell’APK. Elenco e nome dispositivi, recupero password e uscita locale sono inclusi. La revoca selettiva di un singolo dispositivo richiede una futura componente server, come spiegato nella guida.

La nuova modalità WebView visibile deve essere verificata sul TIM Box; il comportamento del sito può variare in base alla rete usata.

Per ripetere i test regole: Java 21+, Node e npm; nella directory cloud eseguire npm install e npm test. Il progetto demo-rita-tv è locale, senza accesso a un servizio reale.

Validazione 1.29: 246 test Android/Robolectric e 3 test Node superati. Verificati player interno VixSrc, catalogo CineSearch, fonte SC API, separazione catalogo/riproduzione, menu video con chiusura automatica e BACK senza uscita involontaria. L’APK universale conserva pacchetto e firma precedenti. Non sono stati eseguiti test su TIM Box fisico, hotspot o autenticazione/sincronizzazione sul progetto Firebase reale.

# Note storiche

# R. ITA TV 1.17 — Navigazione EPG e nuovo logo

Il movimento continuo viene generato dall'app quando si tiene premuta una
freccia, anche se il telecomando invia pochi eventi di ripetizione. Brevi
pressioni spostano di 10 dp; tenendo premuto il cursore accelera. Arresto al
rilascio, al menu, alla pausa e alla perdita di focus della finestra.
La freccia occupa un piccolo riquadro e si sposta senza ridisegnare un overlay
trasparente a tutto schermo. Hover verso Gecko limitato a circa 20 eventi/s;
hover immediato prima del clic. Nascondimento automatico dopo 3,5 secondi.

Daddy Live usa per default un tocco reale, come su tablet, mantenendo l'hover
mouse per far comparire i comandi del player. OK invia un solo DOWN/UP nelle
stesse coordinate, senza passare attraverso una coda browser.post. Pressione
annullata alla pausa o al menu, per evitare un pulsante rimasto premuto.
Nel menu Gecko è disponibile Clic: tocco / Clic: mouse; su Daddy Live la scelta
è persistente. Non vengono inviati due clic diversi insieme. Il touch fisico
sul tablet resta gestito direttamente da Gecko.

Filtro pubblicità: una scansione iniziale, poi solo i sottorami aggiunti o le
modifiche agli indirizzi. Accorpamento delle modifiche, niente scansioni di
tutta la pagina ogni 50 ms e niente nuova pulizia causata dalle sole rimozioni.
Blocco richieste pubblicitarie e nuove finestre mantenuto.

Validazione: build e 104 test Android superati (SDK 31/35), test Node del blocco
richieste e del filtro DOM superati, firma APK verificata.

Versione 1.17, codice 25, Android 8+, ARM32/ARM64, stesso pacchetto e firma.
Installare R-ITA-TV-v1.17-Navigazione.apk sopra la precedente.
Le verifiche locali controllano input e filtro; velocità effettiva e pulsanti
Fullscreen/Unmute Daddy Live devono essere provati sul TIM Box.

# Note storiche delle versioni precedenti

# R. ITA TV 1.13 — Vavoo e Gomstream nella stessa schermata

Clic normale su Canali: scegli Vavoo o Gomstream. La scelta viene ricordata
tra gli avvii. Gomstream usa la stessa lista, ricerca, categorie, immagini e
guida TV della schermata principale; rimosso il pulsante Gomstream separato.
Pressione lunga su Canali: menu cambio URL Vavoo oppure Gomstream, con Salva,
Ripristina e Annulla. I due indirizzi sono indipendenti e persistenti.
Vavoo: modifica il dominio del catalogo e della risoluzione; la sessione resta
sul servizio Vavoo originale. Il nuovo dominio deve supportare il protocollo
esistente. La cache del catalogo viene associata al dominio e non riutilizzata
per un dominio differente. Il dominio predefinito conserva il fallback storico.
Gomstream: modifica il dominio video. Il catalogo continua a usare il dominio
Daddy Live modificabile dal suo pulsante; i video vengono risolti anche dal
nuovo dominio Gomstream quando le pagine Daddy Live puntano al precedente.

Player unico: stessa lista in sovrimpressione, categorie, preferiti, EPG,
zapping CH+/CH- e SU/GIU, pausa, audio, formato video e ritorno alla diretta.
Spostamenti di 10 secondi abilitati soltanto quando consentiti dal flusso.
Primo BACK apre la lista mantenendo il video; secondo BACK torna alla home.
Nomi Gomstream ripuliti dal suffisso del paese per gli abbinamenti EPG esatti.
Canali con nomi non presenti nella fonte XMLTV mostrano guida non disponibile.
I preferiti Vavoo esistenti rimangono disponibili; quelli Gomstream sono
separati e identificati dall'ID, anche se il sito cambia il nome del canale.

Catalogo Gomstream riletto all'apertura, al ritorno e ogni 10 minuti mentre
l'app e la schermata sono attive. Ultima copia mantenuta in memoria se la
rete fallisce. Risposte tardive della fonte precedente scartate dopo la scelta.
URL video riletto a ogni apertura/zapping e fino a due nuovi tentativi dopo
errori di riproduzione. Nessun URL video fisso incluso nell'APK. Redirect e
fonti aggiornate dal sito sono seguiti; nuovi formati possono richiedere APK.
Adattatore PNG/WebP/MPEG-TS della 1.12 usato dal player condiviso.

Validazione: assembleDebug e 100 test superati (Android 12/15), filtro pubblicità
Node superato, firma APK verificata e anteprime home/player TV controllate.

Versione 1.13, codice 21, Android 8+, ARM32/ARM64, stessa firma e pacchetto.
Installare R-ITA-TV-v1.13-Fonti.apk sopra la versione precedente.
La riproduzione su TIM Box va ancora verificata sul dispositivo. Le fonti
indisponibili sul sito rimangono indisponibili anche nel player interno.

# Note storiche delle versioni precedenti

# R. ITA TV 1.12 — Gomstream nel player interno

Nuovo pulsante Gomstream nella schermata principale, catalogo italiano con
ricerca e categorie e riproduzione HLS nel player nativo Media3 1.10.1.
Il catalogo viene letto dal dominio Daddy Live salvato all'apertura, al ritorno
alla schermata e ogni 10 minuti mentre la schermata rimane attiva.
Gli indirizzi video non sono inclusi nell'APK: vengono risolti dalle pagine
pubbliche a ogni apertura del canale e nuovamente dopo errori di riproduzione.
Redirect e cambi delle fonti collegate vengono seguiti automaticamente.
Cambio URL è disponibile nel catalogo e tenendo premuto Gomstream nella home.
Un dominio nuovo senza collegamenti dal sito precedente richiede cambio URL;
modifiche al formato delle pagine o del video possono richiedere una nuova APK.

Adattatore per i segmenti pubblici PNG/WebP con MPEG-TS/gzip; playlist e chiavi
usano HTTP normale. Nessuna pagina pubblicitaria viene caricata nel player
nativo. Audio attivo e video a schermo intero; OK mostra i controlli, primo BACK
mostra il menu, secondo BACK torna al catalogo. Apri con Gecko è un'alternativa.

Verifiche: build e 87 test superati, filtro pubblicità Node superato, firma APK
verificata e anteprime TV controllate. Campioni reali Rai 1 e Sky Sport Uno
estratti dall'adattatore Java contengono H.264 1080p e audio AAC.
Il catalogo del 05/10/2026 contiene 57 voci con etichetta italiana: 31 hanno
restituito una playlist valida, altre risultano indisponibili. Una playlist
valida non prova la riproduzione completa. Prova su TIM Box ancora necessaria.
Le richieste di controllo non hanno usato VPN; accessibilità dalla rete del
singolo dispositivo non garantita.

Versione 1.12, codice 20, Android 8+, ARM32/ARM64, stessa firma e pacchetto.
Installare R-ITA-TV-v1.12-Gomstream.apk sopra la versione precedente.

# Note storiche delle versioni precedenti

# R. ITA TV 1.11 — Daddy Live da telecomando

Movimento e clic mouse reali nel player Daddy Live, movimento preciso agli
angoli e coordinate adattate all’area visibile. Note complete in README.txt.

# R. ITA TV 1.8 — SportOnline + Gecko

Basata sui sorgenti originali della versione 1.1 per schermata principale,
canali, player, guida TV e impostazioni. Aggiunta la sezione Eventi SportOnline.
DiretteCommunity e ZicoTV non sono presenti.
Pressione prolungata su Eventi nella schermata principale apre la modifica
dell’URL SportOnline: Salva, Ripristina, Annulla. L’indirizzo HTTPS è persistente;
può essere il dominio oppure il link prog.txt. Cambiandolo si elimina il vecchio
calendario salvato. Il nuovo sito deve offrire lo stesso formato del calendario.

Orari e data di aggiornamento sempre in Europe/Rome, indipendentemente dal
fuso del dispositivo, con ora legale/solare e data dopo mezzanotte.
Il fuso di origine del calendario resta quello già usato: Europe/Lisbon;
il sito non esplicita il fuso nel file del calendario.

Il calendario viene letto da https://sportsonline.st/prog.txt e raggruppato
per evento e orario. Seleziona un evento, poi la fonte/lingua: si apre subito
in GeckoView incorporato senza Chrome o Firefox. I link del calendario sono
le pagine originali dei player, senza estrazione dei flussi o header alterati.
Sport e stato hanno selezione con frecce e OK del telecomando. Gli stati sono
stimati dall'orario (tre ore di durata), non confermati dal provider.
I loghi disponibili provengono dall'indice squadra; altrimenti icona dello sport.

Gestione Gecko mantenuta dalla versione 1.6: freccia bianca, OK per cliccare
Play/unmute/fullscreen, primo BACK apre il menu, secondo BACK torna agli eventi.
Menu inizialmente nascosto e senza tre puntini. Ricarica, Schermo intero e
Clicca al centro restano disponibili. La riproduzione dipende dal sito e va
verificata su TV/tablet; i test locali non dimostrano la riproduzione reale.

Aggiornamento compatibile con le versioni precedenti: pacchetto e firma uguali,
versionCode 16. Richiede Android 8+ e ARM64/ARM32. Dimensione circa 177 MB per
il motore Mozilla incorporato. Build: Java 17, Gradle 8.11.1, Android SDK 36,
Build Tools 35, gradle assembleDebug testDebugUnitTest.



Aggiornamento TV: ricerca e categorie più compatte nella parte alta; Preferiti,
Aggiorna e impostazioni EPG nel menu. Rimossi i titoli Televisione italiana e
Diretta. Foto EPG intere, senza ritaglio, in un riquadro proporzionato 16:9;
titolo, canale e orari sotto la foto. Nel player su/giù cambiano canale,
destra/sinistra spostano di dieci secondi solo se il flusso lo consente.
Con focus sui comandi o sulla lista, le frecce navigano normalmente.

App indipendente per Android e Android TV, con catalogo Vavoo limitato ai canali italiani.
Versione 1.8, codice 16, pacchetto it.carmine.streamplayer.

## Interfaccia

Tema blu notte, focus blu e logo R. ITA TV. Su TV/tablet: menu laterale,
lista canali al centro e immagini, guida e trama a destra. Su telefono il menu
si dispone in alto. Loghi dei canali e immagini reali dei programmi quando
forniti dalle fonti; logo del canale come alternativa. Oggi e domani mostra
la programmazione e i dettagli dei singoli programmi.

Categorie: Tutti, Generalisti, Sport, Cinema e serie, Intrattenimento,
Documentari, Bambini, Notizie, Musica e Altri. Classificazione basata sul nome.
Ricerca, categoria e preferiti funzionano insieme. Pressione prolungata su
un canale aggiunge/rimuove il preferito.

## Player

Media3 ExoPlayer 1.8.0, con supporto HLS/DASH. Titolo e comandi si nascondono
dopo cinque secondi. Tocca il video o premi OK per mostrarli.
Primo BACK apre la lista senza chiudere il video; secondo BACK con lista
visibile chiude il player. La lista si nasconde dopo otto secondi di inattività.
Tocchi, scorrimento e telecomando riavviano il timer.

Categorie e filtro preferiti sono disponibili anche nel player. CH+/CH−
permettono lo zapping nell'elenco filtrato. Sono disponibili pausa, traccia
audio e formato adattato/zoom/riempimento. Avanti/indietro di dieci secondi
si attivano solo quando il flusso segnala di supportare lo spostamento.
Nessuna registrazione, timeshift locale o cache video su disco. La pausa di
una diretta e il ritorno alla diretta dipendono dalle capacità del flusso.
La riproduzione usa il normale buffer temporaneo in RAM.

## Caricamento e servizi

Il catalogo completo italiano viene salvato per sei ore; oltre questo tempo
si aggiorna in background mantenendo disponibile la copia precedente. Al
primo avvio i canali italiani compaiono progressivamente. Aggiorna forza il
rinnovo; gli URL video vengono risolti quando si apre un canale.
EPG e immagini si caricano separatamente, senza bloccare il catalogo.
La cache su disco delle sole immagini è limitata a 32 MiB.

Gli errori 401/403 sospendono le nuove richieste per cinque minuti; 429 e
Retry-After vengono rispettati. Gli errori transitori hanno tentativi limitati
e attese crescenti. Nessun proxy e nessun aggiramento dei blocchi: la continuità
dei servizi di terzi non è garantita.

## EPG e immagini

La guida mostra ora/dopo, orari, avanzamento, trama e immagini disponibili.
Orari nel fuso del dispositivo. Il pulsante EPG permette di aggiornare,
impostare un URL XMLTV HTTPS personalizzato o ripristinare la fonte.
Abbinamenti espliciti evitano guide attribuite a canali diversi.

Fonti predefinite:
- https://raw.githubusercontent.com/Belfagor2005/vavoo-player/master/epg_it.xml
- https://raw.githubusercontent.com/OwnerPlugins/vavoo/main/epg-channel-db/vavoo_channels_it.json

Le immagini dipendono dalla disponibilità delle fonti. Indici e URL dei loghi
in assets/artwork_sources.json; assets/channel_logos.json contiene la mappa
remota e assets/bundled_logos.json i loghi già inclusi. I marchi appartengono
ai rispettivi titolari.

## Installazione

Installa R-ITA-TV-v1.8.apk sopra la 1.0 o la 0.7. Pacchetto e chiave di firma sono
invariati: preferiti e impostazioni vengono mantenuti.

## Compilazione su GitHub

Estrai lo ZIP e carica il contenuto nella radice del repository, compresa
.github. build.gradle, settings.gradle, src, res e assets devono essere nella
radice. Il push su main/master avvia Actions → Compila APK; è disponibile
anche Run workflow. Scarica l'artifact R-ITA-TV-APK e installa l'APK contenuto.
Il workflow compila, esegue test e controlla l'avvio su emulatori Android 12/15.
Il workflow non è stato eseguito nel tuo account durante questa sessione.

## Build locale e verifiche

JDK 17, Gradle 8.11.1, Android SDK 36 e Build Tools 35.0.0.
Configurare ANDROID_HOME o local.properties. Eseguire:
`gradle assembleDebug testDebugUnitTest`.

25 test locali: avvio, caricamento/cache catalogo, filtri, navigazione player,
doppio BACK e timer, categorie/zapping nel player, capacità di spostamento,
abbinamento immagini XMLTV, pause dopo errori e schermata principale.
Anteprime native controllate per telefono, tablet e TV. La riproduzione dei
flussi reali richiede una prova sul dispositivo e disponibilità del servizio.

## Provenienza

Protocollo basato sul progetto MIT:
https://github.com/Haehnchen/vavoo-iptv-stream-proxy
Licenza in THIRD_PARTY_LICENSE.txt. Media3 è distribuito con licenza Apache 2.0:
https://github.com/androidx/media

La chiave local-test-key.p12 è pubblica e destinata alle build di prova
(password changeit, alias player); non utilizzarla per una distribuzione
pubblica definitiva. Nessun pannello amministrativo o controllo degli utenti.
I player HTML precedenti non sono inclusi.


## v1.15 · Grafite e ambra
Menu orizzontale unico: Canali, Categorie, Eventi, Daddy Live, Preferiti, Guida TV, Aggiorna. Logo rimosso e lista canali ampliata al 58%. Pressione lunga su Guida TV apre le impostazioni EPG; pressione lunga Canali mantiene la modifica degli URL. Palette coerente nelle attività, finestre, icone eventi e player nativo.
Daddy: il pulsante Fullscreen della pagina espande l'iframe selezionato senza ricaricarlo; usa il fullscreen HTML quando disponibile, con espansione a tutta la finestra come fallback. MENU → Schermo intero offre lo stesso comando. Indietro ripristina il player. Selezione fonti e Unmute mantengono il normale tocco Gecko. Necessaria verifica sul TIM Box per confermare input e resa reali.


## v1.16 · Rifinitura grafica
Font Inter incorporato (SIL Open Font License, Inter-OFL.txt) e pesi regolari/medi. Menu superiore senza riquadri: testo ambra e sottolineatura solo sul focus effettivo; Canali non rimane selezionato come indicatore di sezione. Stesso stile ai menu Eventi e Daddy. Poster EPG integro sopra una copia sfocata e oscurata che copre il riquadro; elaborazione su miniatura di massimo 160px, senza un secondo download. Cursore Gecko bianco con contorno ambra da 1dp. Riproduzione, input e fullscreen restano quelli della v1.15.

Focus della lista canali nel player disegnato sopra le schede opache, con bordo e tinta ambra leggera. Selezione DPAD verificata senza avviare un canale finché non si preme OK.


## v1.17
Home: destra dalla lista entra direttamente nel pannello EPG; su/giù scorre tutto il testo; sinistra torna ai canali; OK passa al pulsante Guarda. Primo Back porta al menu superiore, secondo Back consecutivo chiude la home; una nuova azione azzera la sequenza. I Back del player rimangono invariati. La selezione delle schede riutilizzate durante lo scroll viene aggiornata tramite la chiave del canale realmente associato alla scheda, non tramite un indice transitorio.
Logo R. grafite, spostata a sinistra con ITA TV piccolo accanto, su fondo ambra, generato con imagegen e incorporato in icona/banner Android. Originale in branding/R-amber-master.png. Prompt: “Square flat minimal logo, dark graphite R. left with small ITA TV beside it on warm amber #F2B245, sans serif, no decorations.”

Nomi visualizzati dei canali: iniziale di ogni parola maiuscola, resto minuscolo (es. Sky Cinema Action). Le chiavi e i nomi originali per EPG, risoluzione flussi e preferiti non vengono modificati.


## Versione 1.19 · DNS
Cloudflare DNS-over-HTTPS è attivo per richieste VOD (catalogo, lista e fonti), video VOD HLS/MP4 e pagine Gecko. Da VOD > Fonti > DNS si può scegliere il DNS del dispositivo. Le sorgenti TV native Vavoo/Gomstream conservano il trasporto precedente. Il DNS non è una VPN e non corregge server offline, errori HTTP o formati di siti non supportati. I messaggi delle fonti ora distinguono DNS, timeout, HTTPS, HTTP e catalogo non riconosciuto. La rete del TIM Box deve essere verificata sul dispositivo.

## Versione 1.20 · Due fonti VOD
Restano soltanto Streaming Community (link principale conservato) e StreamingUnity. All'apertura vengono rimosse le altre fonti già salvate. La lista automatica aggiorna solo StreamingUnity e non reintroduce siti esclusi. Modificando manualmente l'URL di StreamingUnity si mantiene quell'indirizzo anche durante gli aggiornamenti. Un elemento Riprendi proveniente da una fonte rimossa propone la nuova ricerca nelle due fonti.

## Versione 1.21 · Avvio VOD sui TV box
La lista fonti si aggiorna in parallelo e non blocca il catalogo. TMDB usa un trasporto separato con DNS del dispositivo, indipendente dai DNS scelti per le fonti. Sui dispositivi TV vengono preferiti gli indirizzi IPv4; Cloudflare richiede solo record A sui TV. Risoluzioni DNS e richieste hanno limiti di attesa; dopo 25 secondi il catalogo mostra un messaggio azionabile e scarta risposte tardive. Il comportamento sul TIM Box fisico richiede prova sul dispositivo.

Correzione di compatibilità: URLEncoder.encode(String, Charset) richiede Android 13/API 33; VOD usa ora encode(String, String), disponibile anche su Android 12 e minSdk26. I test Robolectric usano java.net della JVM host, quindi questa incompatibilità richiede anche la verifica statica delle API Android.


Aggiornamento 1.33: vedere CHANGELOG-1.33.md.
