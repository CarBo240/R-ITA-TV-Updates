# R. ITA TV 1.42

Base 1.41, versionCode 50.

- Catalogo scelto dalle impostazioni letto e mantenuto anche dopo il riavvio: non viene ripristinato automaticamente TMDB. La preferenza del catalogo è indipendente dalla fonte video, che conserva VixSrc come predefinita.
- Home instradata al catalogo selezionato: TMDB conserva la Home attuale; gli altri cataloghi aprono la relativa schermata.
- Aggiunto CB01 ai cataloghi: locandine, ricerca del nome, selezione del film e lista fonti. URL modificabile e salvato con identità stabile.
- Mixdrop diretto e collegamenti StayOnline verso Mixdrop risolti nativamente come nel prototipo 0.10 confermato dall’utente. Configurazione letta senza eseguire gli script; video verificato con richiesta Range e vincolato all’ID selezionato.
- Riproduzione in VodPlayerActivity con Media3, ripresa/progresso esistenti. User-Agent Chrome passato una sola volta; timeout totale disattivato per lo streaming.
- Focus sulle locandine e sui pulsanti per il telecomando. BACK ritorna alle fonti/locandine.
- Le altre fonti restano elencate ma mostrano “estrazione non ancora supportata”. Maxstream e Altadefinizione non sono inclusi in questa integrazione.
- Firma e Secrets GitHub invariati.

Validazione: compilazione Android locale riuscita; 3 test Robolectric superati sulla persistenza del catalogo e sui filtri Mixdrop/fonti. Integrazione R. ITA da verificare su TV; la conferma di riproduzione riguarda il prototipo 0.10 sul tablet.
