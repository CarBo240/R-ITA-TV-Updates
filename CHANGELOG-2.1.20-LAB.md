# R. ITA TV 2.1.20 LAB

Base: sorgenti completi 2.1.19 LAB. VersionCode 78. Patch incrementale sulla 2.1.19.

## Locandine R. ITA
- Pressione lunga sulla Home: apre il menu Da guardare, Preferiti, Continua a guardare; non aggiunge più direttamente alla lista.
- Stesso menu condiviso nelle griglie del catalogo. Le prime due azioni aggiungono/rimuovono; Continua a guardare rimuove dalla cronologia, come nel precedente menu del catalogo. Non si crea una ripresa fittizia.
- Le modifiche avvengono solo dopo la scelta; Chiudi non modifica dati. Restano scope TMDB/provider e sincronizzazione esistente.

## Player VOD R. ITA
- Barra di avanzamento bianca a riposo, ambra con focus o durante lo scorrimento. Buffer e parte non riprodotta conservano una diversa opacità per distinguere l'avanzamento.
- Rimossa la deformazione del focus (prima scala verticale 1.85); pallino 10 dp, 12 dp durante lo scorrimento anziché 20/28 dp. Dimensioni tramite lo stesso meccanismo già presente; se il campo Media3 non è disponibile resta il default, senza deformazione.
- Pulsanti ±15 secondi: gestiti anche exo_rew_with_amount/exo_ffwd_with_amount, prima esclusi dallo stile. Focus con fondo ambra, bordo bianco, testo contrastato e crescita moderata; frecce originali conservate.
- Stile applicato al solo VodPlayerActivity. Metodo PlayerChrome.apply originale conservato per gli altri player.

## Altre fonti: prima integrazione LAB nel VOD
- Ricerca sui provider R. ITA e sui provider CB01/AltaDefinizione già configurati nel LAB. La lettura delle configurazioni LAB non cambia fonte principale, catalogo predefinito o preferenze salvate.
- Per i film: ricerca per titolo italiano e titolo originale, concordanza del nome e dell'anno se dichiarato, deduplicazione per scheda; titoli omonimi e anni discordanti non vengono associati automaticamente. Anno mancante esplicitamente indicato nella riga del risultato.
- La ricerca legge anche la scheda e conta le fonti candidate integrate. Selezione provider → server effettivi della scheda → player VOD R. ITA, con i colori e il focus R. ITA; nessun passaggio all'attività grafica LAB.
- Attivati CB01/MixDrop (compreso StayOnline pubblico), AltaDefinizione/VidXGo e Diretto VixSrc, oltre ai collegamenti media HTTPS dichiarati. Riutilizzati gli adattatori e resolver LAB confermati dall'utente.
- Trailer separati e non proposti come film. Uprot/MaxStream e gli altri resolver non confermati rimangono visibili come non integrati e non selezionabili nel nuovo percorso.
- Prima del playback e quando si preme Riprova/riprende il titolo, la scheda viene riletta e la fonte risolta nuovamente. Non si salva un manifest temporaneo come collegamento permanente; se la fonte è cambiata e non è univoca si richiede Altre fonti.
- Manifest HLS/DASH e MP4 verificati dal resolver prima del player. Stesso trasporto LAB per MixDrop; per VidXGo stessa sessione di rinnovo token, scope CDN e intestazioni. Sottotitoli dichiarati trasmessi a Media3; scelta audio/sottotitoli e preferenza italiano del player R. ITA conservate.
- Ripresa, Continua a guardare e sincronizzazione usano i metadati del titolo e il sistema R. ITA esistente. Il pulsante Altra fonte torna alla ricerca provider mantenendo la posizione.
- Primo passaggio limitato ai FILM: stagioni ed episodi CB01/Alta nel bridge non sono dichiarati implementati. I percorsi serie esistenti su VixSrc/Streaming Community restano quelli originali.
- EuroStreaming non è abilitato nel VOD come provider confermato: il nuovo catalogo .design necessita ancora di verifica sul dispositivo.

## EuroStreaming e MaxStream: diagnosi ricevuta
- La schermata dell'utente mostra CATALOG/HTTP_403 su eurostreamings.yachts. È un dominio distinto da eurostreaming.design, cui si riferisce l'adattatore 2.1.19; non si tratta di un catalogo vuoto dopo parsing.
- In caso di quel preciso errore nel LAB, un dialogo consente di provare eurostreaming.design nella sola sessione corrente. Nessuna sostituzione silenziosa dell'URL salvato o aggiramento del 403. Il funzionamento di .yachts non viene dichiarato ripristinato.
- La schermata MaxStream mostra RESOLVER/INTERMEDIARY_NO_PUBLIC_LINK su uprot.net: l'errore precede la pagina finale MaxStream e il manifest. La modifica al parser HLS 2.1.19 non può da sola correggere la conversione Uprot.
- Nessuna copia delle credenziali/firme Veezie o uso del suo backend privato. MaxStream e k9vo restano non integrati nel nuovo bridge VOD.

## Conservazione, verifiche e limiti
- Modifiche alla produzione autorizzate esplicitamente dall'utente: quattro file, VodActivity, VodCatalogueActivity, VodPlayerActivity, PlayerChrome. Eccezioni limitate e motivate in LAB220-AUTHORIZED-PRODUCTION-CHANGES.json; la lista SHA256 originale non viene riscritta.
- 442 altri file originali byte-identici. Live TV invariata. LabPipeline, LabHttp, LabVidXSession, LabVixBridge, VixSrc, ExperimentalCatalogActivity e ExperimentalPlayerActivity identici alla 2.1.19: il percorso Streaming Community LAB 2.1.8 resta separato e intatto.
- 207 controlli Java superati su fixture: catalogo → host → resolver → sorgente verificata; policy del bridge produzione, nomi/anni, esclusione serie, trailer, host non integrati e HTTP. Non equivalgono a playback live.
- Controllo sintattico dei file Java Android modificati e validazione XML. Non è una compilazione Android né una prova DPAD sul dispositivo.
- Tentata assembleDebug locale: blocco prima della compilazione perché AGP 8.9.1 non è nella cache e i repository non sono accessibili dall'ambiente. Nessun APK dichiarato compilato. Workflow GitHub aggiornato alla 2.1.20.
- ZIP completi e patch verificati con CRC e confronto byte: patch + sorgenti 2.1.19 riproducono il pacchetto completo 2.1.20.

## Prova consigliata sul dispositivo
1. Tenere premuta una locandina in Home e in catalogo: verificare il menu e che Chiudi non modifichi le liste.
2. Avviare un film VOD: verificare barra bianca/ambra, dimensione del pallino e focus ±15 con telecomando e touch.
3. Nella scheda film selezionare Altre fonti. CB01/Alta già configurati nelle fonti R. ITA o nei provider salvati LAB devono mostrare risultati e server. Se un provider era usato soltanto temporaneamente e non salvato, aggiungerlo nelle impostazioni Fonti VOD oppure nella configurazione LAB.
4. Provare CB01/MixDrop e Alta/Diretto-VixSrc o VidXGo, poi Altra fonte e Riprendi. Verificare posizione, audio italiano disponibile e sottotitoli.
5. Euro .yachts: verificare il dialogo dopo 403; provare .design esplicitamente e controllare il catalogo, senza considerare corretto .yachts.
