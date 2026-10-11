# Applicazione della patch e build

## ZIP

Lo ZIP dei soli file modificati contiene nuovi file e sostituzioni rispetto alla 2.1.15. Estrarre nella root del repository mantenendo le directory. Lo ZIP completo conserva anche tutte le copie root già presenti nell'archivio originale; Gradle compila `src`, come prima. Non usare l'APK Veezie come dipendenza.

## Android Studio / GitHub

`local.properties` non contiene più il percorso SDK assoluto di una vecchia sessione; Android Studio lo configura localmente e GitHub usa ANDROID_HOME. Il workflow build-apk conserva firma e varianti originali, aggiunge i 137 controlli LAB prima della build e usa il nome artifact 2.1.16 LAB.

Aprire il progetto con JDK 17, Android SDK 36 e Build Tools 35.0.0. Conservare le dipendenze e le esclusioni originali. È necessaria la connettività verso Google Maven e Maven Central.

```bash
gradle compileDebugJavaWithJavac
gradle assembleRelease
```

Il progetto allegato non contiene Gradle wrapper né chiave di firma. `assembleRelease` mantiene il tipo release non firmato della base. Firmare con la **stessa chiave usata per la propria app** prima di installare come aggiornamento; non sostituire la firma con una chiave nuova. I parametri debug originali richiedono `RITA_KEYSTORE_FILE`, `RITA_KEYSTORE_PASSWORD`, `RITA_KEY_ALIAS`, `RITA_KEY_PASSWORD` se si vuole produrre un APK debug firmato. Nessuna credenziale è inclusa.

La versione è 2.1.16, codice 74, package originale `it.carmine.streamplayer`. Nessuna pubblicazione GitHub o installazione è stata effettuata qui.

## Test indipendente dal SDK

Con Java 17 nel PATH:

```bash
bash scripts/check-lab211.sh
```

Questo test non effettua richieste a host esterni. Verifica i parser e la pipeline usando risposte sintetiche controllate. Le prove della suite Android originale richiedono inoltre le dipendenze Robolectric.

## Configurazione e prova su TV

Nel Laboratorio TEST scegliere il provider web e l'adattatore adatto al suo layout. Oppure aggiungere un provider LAB con URL HTTPS e uno degli adattatori disponibili. Le nuove configurazioni risiedono solo nelle preferenze LAB.

I file JSON di esempio in docs usano domini `.example`: sono **schemi da compilare**, non provider reali. Per usare JSON LAB, pubblicare un catalogo proprio/autorizzato con `labSchema: 1`, `titles` e campi `name`, `poster`, `detail`. La scheda `detail` deve rispondere con `sources`, contenenti `label` e `url`. Gli URL relativi e le query HTTPS sono ammessi; immagini e schede devono avere URL HTTPS.

Sul dispositivo verificare l'intera sequenza: locandine → scheda → fonti → SOURCE VERIFIED → PLAYBACK FIRST_FRAME. READY significa che Media3 è pronto, non certifica da solo l'immagine visibile. Provare audio/sottotitoli dichiarati dal media, BACK e telecomando. Registrare il provider, l'adattatore, il dominio del server e il codice diagnostico, senza condividere token o credenziali.

Limitazioni: prima pagina/catalogo fino a 100 titoli, nessuna paginazione automatica; serie senza un adattatore episodio dedicato elencano i link dichiarati nella scheda; nessun login nel nuovo client; cookie/challenge, wrapper dinamici, DRM o API riservate non sono risolti. Gli intermediari generici possono essere attraversati soltanto quando espongono un collegamento pubblico HTTPS univoco o un redirect ordinario. Streaming Community e VixSrc continuano nelle attività precedenti.

La 2.1.16 aggiunge l'alias esatto m1xdrop.net al resolver MixDrop effettivo, correggendo il rifiuto della destinazione StayOnline mostrato nel nuovo screenshot. Consultare CHANGELOG-2.1.16-LAB.md e docs/LAB216-VERIFICA.md. Le correzioni 2.1.15 e Prova URL host sono conservate.
