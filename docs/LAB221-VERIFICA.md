# R. ITA TV 2.1.21 LAB

Base: sorgenti completi 2.1.20 LAB. VersionCode 79. Patch incrementale sulla 2.1.20.

## Cursore del player R. ITA
- Pallino della barra VOD: 10 dp a riposo, 14 dp con focus o durante lo scorrimento. Colore bianco a riposo e ambra quando selezionato.
- Nessuna scalatura della barra completa. Il cursore torna alla misura normale quando perde focus e termina lo scorrimento.
- Dimensioni tramite i campi del DefaultTimeBar Media3 già usati nel progetto: fallback alle dimensioni disponibili se una futura versione cambia i campi.
- Conservati focus dei pulsanti ±15 e menu locandine introdotti nella 2.1.20. Il metodo PlayerChrome.apply degli altri player resta invariato.

## Mediaset Infinity: primo esperimento LAB
- Nuovo pulsante nel Laboratorio TEST: «Mediaset Infinity · TEST catalogo e accesso».
- Attività dedicata con catalogo del sito ufficiale, pulsante Catalogo / Login e collegamento alla guida TV ufficiale. Si utilizza il catalogo web del servizio: non è ancora un adattatore nativo di locandine R. ITA.
- Accesso tramite Login / Registrati del sito ufficiale: il LAB non legge password, campi account, cookie o token per estrarli o salvarli nelle preferenze R. ITA. Nessuna API autenticata o firma Veezie riutilizzata.
- Da Android 9: processo WebView dedicato `:infinity_lab`, archivio `infinity_lab`, cookie e dati sito separati dai browser/player R. ITA. Il sito mantiene la sessione nel proprio profilo WebView; salvataggio cookie dopo pagina e pausa. La presenza dei cookie non viene dichiarata prova di login riuscito.
- Android 8/8.1: apertura nel browser esterno, senza creare una WebView interna con archivio condiviso. Sessione esterna indipendente; nessun trasferimento automatico del login al LAB.
- Ripristina sessione LAB: dopo conferma cancella cookie, cache, cronologia e dati web del solo profilo dedicato. Non equivale alla revoca delle sessioni sul server: per quella usare Logout nel profilo Mediaset.
- Player web ufficiale, supporto richiesta di schermo intero e ritorno con Indietro. Il LAB non intercetta manifest protetti e non fornisce licenze DRM a Media3.
- Consentito al solo dominio ufficiale Infinity l'identificatore per contenuti protetti richiesto dalla WebView; altre risorse protette non vengono concesse. Restano licenza, diritti dell'account e compatibilità DRM del dispositivo da verificare dal servizio.
- JavaScript e storage web abilitati per il sito; accesso file/content disabilitato, nessun bridge JavaScript nativo, niente contenuti misti HTTP o prosecuzione degli errori SSL.
- Browser esterno disponibile per incompatibilità WebView o login federato. Il suo accesso non viene dichiarato condiviso col LAB. Nessuna attivazione di abbonamenti automatizzata.

## Diagnostica Infinity
- Eventi per caricamento pagina/catalogo/accesso, host (senza query o token), errore di rete e HTTP della pagina principale.
- Diagnostica su richiesta osserva solamente stato HTML5 del primo video della pagina principale: pausa, tempo, readyState ed eventuale codice errore. Non legge URL media, credenziali o cookie.
- Se il video è in un iframe, la diagnostica dichiara che non è osservabile. «Attivo» descrive lo stato HTML5, non certifica avanzamento, audio o licenza; non si dichiara automaticamente autenticato l'account.
- Nessuna modifica a RaiPlay o aggiunta di Infinity alle fonti VOD principali: l'integrazione nativa sarà un passaggio successivo dopo verifica reale del LAB.

## Verifiche e limiti
- Nuova LabInfinityActivity compilata con javac contro android-36/android.jar: nessun errore Java/API. Non equivale alla compilazione dell'app intera.
- 207 controlli Java della pipeline esistente superati; fixture sintetiche, nessuna certificazione playback live.
- Controllo sintattico Java degli altri file modificati e XML Manifest superati.
- 442 file originali conservati byte per byte; quattro eccezioni produzione già autorizzate, aggiornato esclusivamente l'hash di PlayerChrome per il nuovo cursore.
- LabPipeline, LabHttp, LabVidXSession, LabVixBridge, VixSrc, ExperimentalCatalogActivity, ExperimentalPlayerActivity e tutti i file Rai* identici alla 2.1.20. Streaming Community LAB originale e Live TV invariati.
- Tentata assembleDebug: AGP 8.9.1 assente dalla cache, arresto prima della compilazione. Nessun APK consegnato o dichiarato funzionante.
- Login Infinity, compatibilità DPAD, riproduzione e DRM non provati su Android TV in questo ambiente. WebView può essere rifiutata dal servizio o dai login Google/Apple; in tal caso usare il browser/app ufficiale. QR e pairing TV nativi non implementati.
- MaxStream/Uprot, l5hs e EuroStreaming restano nello stato documentato nella 2.1.20; questa versione non dichiara nuovi resolver o correzione dei loro blocchi.
- ZIP verificati con CRC e confronto byte: patch applicata ai sorgenti 2.1.20 riproduce il pacchetto completo 2.1.21.

## Prova sul dispositivo
1. Nel player VOD R. ITA passare il focus alla barra: cursore ambra leggermente più grande. Uscendo torna bianco e piccolo; provare telecomando e touch.
2. Nel Laboratorio TEST aprire Mediaset Infinity. Verificare caricamento catalogo e comando Login / Registrati del sito; usare direttamente lì le proprie credenziali, senza inviarle in chat.
3. Avviare un contenuto incluso nei diritti dell'account; verificare avanzamento, audio e schermo intero. Aprire Diagnostica e riportare gli errori senza password, cookie o URL con token.
4. Uscire e riaprire il LAB per verificare mantenimento della sessione. Provare Ripristina sessione e accertare che il sito torni disconnesso; gli altri provider R. ITA devono conservare le loro sessioni.
5. Provare Indietro da schermo intero, navigazione nel sito, ritorno al LAB e riproduzione di una fonte già funzionante R. ITA/SC.

## Riferimenti ufficiali consultati
- Login: https://help.mediasetinfinity.mediaset.it/hc/it/articles/360019901659-Come-effettuare-il-Login
- DRM e compatibilità: https://help.mediasetinfinity.mediaset.it/hc/it/articles/47193556478993-DRM-cos-%C3%A8-e-come-risolvere-un-errore-DRM
- Android WebView / setDataDirectorySuffix: https://developer.android.com/reference/android/webkit/WebView
- Android PermissionRequest: https://developer.android.com/reference/android/webkit/PermissionRequest
