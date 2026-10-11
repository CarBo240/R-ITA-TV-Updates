# R. ITA TV 2.1.3 — diagnostica Streaming Community

- Riparata la distribuzione dei file Java diagnostici: le modifiche ora risiedono in `src/it/carmine/streamplayer/`, cioè nel percorso effettivamente compilato da Gradle.
- Messaggi di errore con prefisso `D213-` per individuare la fase: Pagina riproduzione, Pagina player, Iframe video, Playlist HLS, RESOLVE oppure MEDIA.
- Errori HTTP durante Media3 mostrati come `D213-MEDIA · HTTP <codice>` se disponibile; per altri errori Media3 mostrato un identificativo non sensibile.
- Aggiornato il nome dell'Artifact GitHub Actions a `R-ITA-TV-v2.1.3-APK`.
- `versionName=2.1.3`, `versionCode=61`. Nessuna modifica a modalità di autenticazione, URL firmati, User-Agent, cookie, DNS, limiti di accesso o altri provider.

**Nota:** diagnosticare un HTTP 403 non equivale a rimuovere il blocco imposto dal servizio. La compilazione e la prova reale restano necessarie.
