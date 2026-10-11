# Rapporto di verifica — 2.1.11 LAB

| Verifica | Esito | Limite |
| --- | --- | --- |
| Test JVM della pipeline | PASS: 47 controlli | Fixture sintetiche, non siti reali |
| Compilazione LabPipeline e JsonCatalog | PASS | JDK 17; SDK 36 per org.json |
| Sintassi dei sei file Java nuovi/modificati principali | PASS | Non sostituisce il type checking Android/Media3 |
| Integrità file originali | PASS: 446 file identici | Confronto SHA-256/byte con archivio 2.1.10 allegato |
| Dispatch hub SC/VixSrc | PASS sul codice | Entrambe le fonti continuano a ExperimentalCatalogActivity |
| Sorgente → Intent → Media3 | Presente, ispezionato | URL, MIME, referer, sottotitoli trasferiti al nuovo player |
| Gradle Android offline | BLOCCATA | AGP 8.9.1 assente dalla cache |
| Gradle Android online | BLOCCATA | Google/Maven: Network is unreachable |
| APK / installazione | NON ESEGUITI | Nessun APK 2.1.11 creato |
| Riproduzione reale / FIRST_FRAME / tracce | NON ESEGUITI | Richiedono build e dispositivo |
| Cataloghi/host live | NON ESEGUITI | Richiedono URL accessibili e autorizzati |
| Suite Android/Robolectric originale | NON ESEGUITA | Risoluzione dipendenze bloccata prima delle task |

## Copertura dei 47 controlli

Locandine/titolo/URL relativo, lettura host dalla scheda, dispatch specifico, estrazione effettiva dei formati pubblici supportati, referer e sottotitoli, query conservate, host falso con nome incluso, packer radix 6 e 62, VOE Base64, link vuoto respinto, alias DoodStream, link diretto, redirect host→media, firma HLS/DASH/MP4, HTML falso media, HTTP 403, challenge, cataloghi WordPress/DLE e query ricerca.

Il controllo end-to-end arriva al risultato da passare al player. Il test non avvia Media3; l'effettiva riproduzione richiede una prova Android dopo la build.

## File originali modificati

- `.github/workflows/build-apk.yml`
- `AndroidManifest.xml`
- `build.gradle`
- `local.properties`
- `src/it/carmine/streamplayer/ExperimentalHubActivity.java`

Tutti gli altri 446 file dell'input sono identici. `scripts/check-lab211-preservation.py` controlla il manifesto di hash. Nessun archivio storico 2.1.1 o LAB 2.1.8 separato è stato fornito: i relativi percorsi sono conservati nella forma ereditata dalla 2.1.10.

## Errori della build

Offline: `No cached version of com.android.tools.build:gradle:8.9.1 available for offline mode` (BUILD FAILED in 3s).

Online: tentativi GET verso `dl.google.com/dl/android/maven2` e `repo.maven.apache.org/maven2` falliti con `Network is unreachable` (BUILD FAILED in 10s).

La build non è arrivata al compilatore Android; non si possono escludere errori di tipo/API non rilevabili con il parser di sintassi. Le firme Media3 sono state confrontate con la documentazione ufficiale, ma la compilazione con le dipendenze esatte rimane da eseguire in Android Studio/GitHub.
