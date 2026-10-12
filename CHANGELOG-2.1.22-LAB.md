# R. ITA TV 2.1.22 LAB

Base: sorgenti completi 2.1.21 LAB. VersionCode 80. Patch incrementale sulla 2.1.21.

## Account RaiPlay

- Dopo un accesso riuscito, email e password vengono cifrate localmente con AES/GCM e una chiave non esportabile di Android Keystore.
- Alla scadenza della sessione i campi vengono precompilati: per rinnovarla è sufficiente selezionare **Accedi**.
- La password usa esplicitamente `PasswordTransformationMethod` oltre al tipo input password; il testo non è leggibile sullo schermo.
- La password non viene cancellata dal campo dopo un errore di rete o di accesso, così è possibile riprovare senza riscriverla.
- **Esci e dimentica credenziali** cancella sessione, email e password dal dispositivo. Una sessione corrotta viene rimossa senza cancellare credenziali ancora decifrabili.
- Credenziali e sessione non entrano nel backup dell'app e non vengono stampate nella diagnostica.

## Altre fonti R. ITA

- La lista dei provider non viene più mostrata durante la ricerca. Una finestra comunica soltanto che la verifica è in corso.
- Al termine appaiono esclusivamente provider con un risultato video accettato dal percorso integrato.
- I provider senza titolo, le semplici pagine esterne e le ricerche dinamiche non confermate vengono esclusi.
- Gli adattatori LAB devono trovare almeno un server riproducibile; una scheda con soli trailer, MaxStream/Uprot non risolto o host non integrati non viene proposta.
- Dopo la scelta di un provider LAB, l'elenco server mostra soltanto le fonti riproducibili. Le righe grigie «non integrata» non vengono più visualizzate.
- VixSrc conserva la verifica API già presente per i film. I provider nativi conservano il controllo titolo/player esistente. La disponibilità può comunque cambiare tra ricerca e avvio: il player continua a risolvere nuovamente la fonte.
- Annullando la ricerca vengono chiusi i caricamenti CB/WebView e i risultati tardivi non riaprono finestre.

## Mediaset Infinity

- Il pulsante 2.1.21 è rinominato **sito ufficiale (fallback)**: non viene più presentato come catalogo LAB nativo.
- L'analisi delle pagine ufficiali conferma che titoli, puntate e locandine sono pubblicati sul sito, ma la pagina vieta espressamente mezzi automatizzati di scraping.
- Non è stata individuata una API pubblica/documentata equivalente all'integrazione RaiPlay, né un flusso ufficiale documentato per consegnare catalogo, sessione e licenza Widevine a un player Media3 di terze parti.
- Per questo non vengono copiati HTML/catalogo, token, cookie, chiavi o richieste DRM del sito. Il fallback web ufficiale resta isolato nel processo LAB e non viene portato nel VOD principale.
- L'integrazione nativa rimane sospesa fino a disponibilità di endpoint autorizzati o documentazione ufficiale. Non viene dichiarata implementata.

## MaxStream

- Il collegamento Uprot osservato non espone una destinazione pubblica. La configurazione Veezie ricevuta usa `msdec` con parametri firmati e sincronizzazione del relativo servizio privato.
- Le chiavi/firme di Veezie e il suo backend non vengono copiati. Non vengono aggirati captcha, controlli di accesso o challenge.
- Il parser pubblico MaxStream per pagine finali e manifest HLS resta presente nel LAB; Uprot → MaxStream rimane non risolto e non viene mostrato tra le fonti R. ITA riproducibili.

## Conservazione e verifiche

- PlayerChrome 2.1.21 conservato: cursore 10 dp bianco a riposo e 14 dp ambra quando selezionato.
- Live TV, file Rai catalogo/player/DRM, VixSrc, LabPipeline, LabHttp, LabVidXSession, LabVixBridge, ExperimentalCatalogActivity ed ExperimentalPlayerActivity invariati rispetto alla 2.1.21.
- Streaming Community sperimentale LAB 2.1.8 resta separato e byte-identico.
- 207 controlli della pipeline superati su fixture sintetiche.
- Verifica di conservazione: 440 file originali byte-identici e 6 modifiche produzione espressamente autorizzate con hash registrati.
- Analisi del compilatore Java non rileva errori sintattici nei file modificati; la compilazione Android completa non è disponibile perché SDK/AGP 8.9.1 non sono presenti nella cache di questa sessione. Nessun APK dichiarato compilato.
- ZIP controllati con CRC e ricostruzione byte per byte dalla base 2.1.21.

## Prova sul dispositivo

1. Uscire da RaiPlay con **Esci e dimentica credenziali**, accedere una volta e verificare che la password resti mascherata.
2. Dopo l'accesso, chiudere e riaprire l'app. Per simulare una sessione scaduta attendere la scadenza reale: devono ricomparire i campi già compilati e deve bastare **Accedi**.
3. In un film scegliere **Altre fonti**: durante la verifica non deve comparire la lista completa dei provider; al termine devono apparire solo risultati utilizzabili.
4. Se nessuna fonte è risolvibile deve apparire il messaggio «Nessun'altra fonte video disponibile», senza righe di siti o server non integrati.
5. Aprire CB01/MixDrop, AltaDefinizione/VidXGo o VixSrc già funzionanti e verificare riproduzione, ritorno ad Altre fonti e posizione di ripresa.

## Riferimenti ufficiali Mediaset consultati

- Catalogo/puntata: https://mediasetinfinity.mediaset.it/video/buongiornomamma3/prima-puntata_F313727301000101
- Login: https://help.mediasetinfinity.mediaset.it/hc/it/articles/360019901659-Come-effettuare-il-Login
- DRM: https://help.mediasetinfinity.mediaset.it/hc/it/articles/47193556478993-DRM-cos-%C3%A8-e-come-risolvere-un-errore-DRM
