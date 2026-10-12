# R. ITA TV 2.1.30 LAB

Versione Android 2.1.30, versionCode 88.

## Correzioni

- Corretto il risultato CB01 nella ricerca nascosta **Altre fonti** del VOD principale.
- CB01 non viene più eliminato solo perché il flusso non è ancora stato risolto durante la ricerca globale: viene mostrato esclusivamente quando la ricerca del titolo restituisce una scheda valida; la verifica dei server video avviene dopo la selezione della fonte.
- Aggiornato il nome dell'Artifact GitHub Actions a `R-ITA-TV-v2.1.30-LAB-APK`, evitando che il download venga presentato con il vecchio nome 2.1.22.

## Componenti conservati

- Nessuna modifica al percorso sperimentale Streaming Community del LAB.
- Nessuna modifica ai resolver e al player LAB già funzionanti.
- Nessuna modifica a Live TV, RaiPlay, VixSrc, Media3 o agli altri componenti VOD.

## Base e verifica

- La cartella sorgenti disponibile nell'ambiente corrisponde alla 2.1.24 più la presente correzione. Per un repository GitHub già aggiornato alla 2.1.29 usare preferibilmente lo ZIP dei soli file modificati, così da non sovrascrivere altri file più recenti.
- Verifica statica eseguita. La compilazione completa deve essere confermata dal workflow GitHub Actions perché Gradle non è installato nell'ambiente locale.
