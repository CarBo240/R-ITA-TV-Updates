# R. ITA TV 1.39 — correzioni rispetto alla 1.38

- Barra laterale a icone e menu espandibile soltanto nella Home VOD, rimossi da catalogo/schede e Impostazioni.
- Chiusura del menu con DPAD destra e ritorno del focus al contenuto anziché alle icone del menu (evita riaperture involontarie).
- Schede serie: un solo pulsante «Episodi», il pulsante principale resta «Guarda».
- Card piattaforme: separazione visiva di 12 dp e invio esplicito del clic via telecomando; loghi forniti da TMDB dove disponibili, fallback testuale negli altri casi.
- TMDB: richieste con lingua it-IT e regione IT; per i cataloghi di piattaforma rimane il filtro watch_region=IT. Questo non garantisce la disponibilità dell'audio italiano.
- Catalogo temporaneo: la scelta della fonte non viene più azzerata entrando in MainActivity; resta di sessione come concordato in precedenza.
- Ricerca visibile in Film e Serie TV, con anno e generi; il filtro di tipo mantiene Film o Serie.

VersionCode 47, versionName 1.39. Sorgenti non compilati in questo ambiente: verificare con GitHub Actions prima di pubblicare la Release.
