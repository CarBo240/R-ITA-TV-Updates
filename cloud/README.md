# R. ITA TV 1.25 — configurazione della sincronizzazione personale

Il VOD funziona senza account. La configurazione pubblica del tuo progetto Firebase **r-ita-757c6** è inclusa nell’APK 1.25. Il progetto è stato creato dall’utente; non sono state modificate risorse remote durante questo aggiornamento. Nessuna password è incorporata nell’APK. La sincronizzazione reale tra dispositivi va verificata dopo avere abilitato il proprio UID. I passaggi seguenti restano come riferimento per nuove configurazioni.

1. Apri https://console.firebase.google.com/ nel tuo account e crea un progetto. Analytics non serve a questa funzione.
2. Registra un’app Android con il nome pacchetto **it.carmine.streamplayer** e scarica **google-services.json**. Non serve ricompilare l’APK.
3. In **Authentication → Sign-in method**, abilita Email/Password. In **Users**, crea il tuo solo account personale con email e password. L’app non ha un pulsante di registrazione.
4. Crea **Cloud Firestore**, scegli la regione e usa la modalità produzione. In **Rules**, copia il file **cloud/firestore.rules** incluso nei sorgenti e pubblica le regole. Non utilizzare regole aperte o la modalità di test.
5. Copia l’UID del tuo utente da Authentication. In Firestore crea la raccolta **owners**, documento con quel preciso UID, campo **enabled** di tipo booleano impostato a **true**. Le regole impediscono all’app di creare o modificare questa autorizzazione.
6. La configurazione è già inclusa nella 1.25. Per importare un’altra configurazione compatibile, nell’app: **VOD → ⚙ → Account e sincronizzazione → Configura cloud · importa file**, seleziona il google-services.json. Se il TIM Box non ha un selettore file, usa il pulsante per incollare la configurazione semplificata descritta sotto.
7. Accedi con la stessa email e password su ciascun TIM Box o tablet. Puoi scegliere se unire i salvataggi locali al profilo personale. Chi riceve soltanto l’APK non può leggere il tuo profilo senza credenziali e autorizzazione.
8. Assegna un nome ai dispositivi. **Dispositivi collegati** mostra quelli che hanno effettuato l’accesso; **Esci dall’account su questo dispositivo** scollega quello in uso. **Password dimenticata** invia l’email di recupero.

La configurazione semplificata contiene i valori del tuo google-services.json:

```json
{"projectId":"IL_TUO_PROJECT_ID","apiKey":"LA_CHIAVE_CLIENT_FIREBASE","applicationId":"IL_MOBILESDK_APP_ID"}
```

Questa è configurazione del client Firebase, non la password. Non inserire chiavi private di account di servizio. Se restringi la chiave client, segui le istruzioni Firebase per Authentication e Firestore e verifica l’accesso su entrambe le architetture.

## Dati e conflitti

Vengono sincronizzati metadati, preferiti, Da vedere e punto di ripresa VOD. Non vengono caricati video. Film e serie sono collegati all’ID TMDB; stagione ed episodio usano la numerazione, conservando anche la fonte precedente.

L’app salva localmente ogni 10 secondi e prova a inviare i progressi al cloud circa ogni 60 secondi, oltre che su pausa e uscita. Le modifiche offline rimangono in coda. L’uscita dall’account può lasciare modifiche in coda, che vengono ritentate al successivo accesso dello stesso account.

Ogni voce ha una revisione: se un altro dispositivo ha già aggiornato quella voce, un salvataggio basato su una revisione vecchia accetta il valore remoto e non lo sovrascrive. Non viene scelto automaticamente il minuto più alto. La rimozione crea una marca di cancellazione, così un dispositivo offline non può ripristinare accidentalmente il titolo. La ripartenza dall’inizio è una scelta esplicita. Fonti con montaggi diversi possono avere punti di ripresa leggermente differenti.

I salvataggi cloud sono separati da quelli del profilo locale. Uscendo dall’account torna visibile la libreria locale precedente. Il backup esporta la libreria del profilo attualmente aperto, ma esclude credenziali, sessioni, identità del dispositivo e configurazione cloud.

## Verifica dopo l’attivazione

Avvia un film sul primo dispositivo, metti in pausa e attendi che la sincronizzazione abbia finito. Sul secondo apri Riprendi e verifica il minuto. Ripeti con un episodio, un preferito, Da vedere e una rimozione. Prova poi a modificare un titolo offline mentre l’altro dispositivo lo aggiorna: alla riconnessione il salvataggio vecchio deve accettare quello remoto.

## Dispositivi e revoca

L’elenco dispositivi è informativo: rimuovere una voce non revocherebbe i token. Questa versione consente l’uscita sul dispositivo in uso. Per bloccare l’intero profilo imposta **owners/UID.enabled = false** in Firestore: le regole bloccano i successivi accessi ai dati e i dispositivi connessi escono dal profilo. Per una revoca selettiva sicura di un solo dispositivo con un account condiviso serve una successiva integrazione server dedicata. QR, account per altre persone e profili familiari restano idee future.

Controlla le quote e i costi del tuo progetto nella console. Non è stato attivato alcun servizio a pagamento per tuo conto.

## Accesso con Google

Il google-services.json fornito non contiene client OAuth. L’accesso email/password è disponibile. Per aggiungere Google occorre abilitare il provider Google nella console Firebase Authentication, aggiungere le impronte SHA-1 e SHA-256 della firma usata per questi APK e scaricare il file aggiornato con il client OAuth web. Non inserire mai chiavi private di account di servizio nell’app.

Impronte della firma utilizzata per questi APK (coincidono con la 1.24):

- SHA-1: `F6:6E:B0:0A:C8:F0:A9:69:E5:C1:3E:5C:33:6B:DB:03:AA:43:C4:6B`
- SHA-256: `E4:AF:ED:40:98:A8:BB:35:43:E5:84:A7:7D:E5:40:68:1C:88:7F:B2:BF:A1:CE:CD:27:26:D6:A2:A4:44:14:AB`
