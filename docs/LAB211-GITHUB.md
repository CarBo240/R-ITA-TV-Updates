# R. ITA TV 2.1.21 LAB · GitHub

Versione 2.1.21, versionCode 79, package originale `it.carmine.streamplayer`.

La patch ZIP contiene soltanto file nuovi o modificati rispetto ai sorgenti completi 2.1.20 LAB. Estrarla nella root mantenendo tutte le directory. Per una base precedente usare lo ZIP completo. Le copie root storiche vengono conservate; Gradle compila `src`.

Il workflow `.github/workflows/build-apk.yml` conserva firma e varianti originali, esegue i controlli LAB e usa il nome artifact R-ITA-TV-v2.1.21-LAB-APK. Configurare JDK 17, Android SDK e dipendenze previste dal progetto. Non usare Veezie come dipendenza. Non commettere password, keystore privati, cookie o token Infinity.

Consultare CHANGELOG-2.1.21-LAB.md e docs/LAB221-VERIFICA.md. Infinity è un esperimento web separato nel LAB, con accesso e playback da verificare sul dispositivo; non è ancora un provider nativo R. ITA o un resolver DRM Media3. Le fonti già integrate e il percorso Streaming Community sperimentale restano conservati.

Controlli disponibili: scripts/check-lab211.sh e scripts/check-lab211-preservation.py. La lista originale LAB211-PRESERVED-SHA256.json resta immutata; LAB220-AUTHORIZED-PRODUCTION-CHANGES.json registra le quattro eccezioni produzione autorizzate e l'hash corrente di PlayerChrome.

La build locale di questa consegna si arresta prima della compilazione per AGP 8.9.1 non disponibile nella cache. Nessun APK, caricamento su GitHub o installazione è stato eseguito.
