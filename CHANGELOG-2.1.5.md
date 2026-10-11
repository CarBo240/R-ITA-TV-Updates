# R. ITA TV 2.1.5 — base 2.1.1 + Laboratorio TEST

- Base ripristinata integralmente dai sorgenti originali 2.1.1 (non dalla linea diagnostica 2.1.2–2.1.4).
- VersionCode 63 per consentire l’aggiornamento della versione 2.1.4 installata senza downgrade.
- Nuova voce **Laboratorio TEST · provider e player sperimentali** nelle Impostazioni.
- LAB isolato: fonti dal catalogo locale in sola lettura; possibile lettura remota senza salvarla; ricerca prova solo per provider con catalogo riconosciuto; accesso alle pagine dei provider; lettore Media3 per URL video HTTPS diretti autorizzati.
- Non copia il codice proprietario Veezie, non implementa i suoi resolver specifici né promette accesso a tutti i provider. Il laboratorio è una base incrementale per successive prove.
- Non tocca preferenze VOD, lettore principale, live TV, Firebase, RaiPlay, Eventi o fonti esistenti. Il browser di consultazione riusa VodBrowserActivity; questa attività, come ogni browser, può gestire cookie propri.
- Nessun aggiramento di sessioni o protezioni.
- Build Android da verificare su GitHub Actions.
