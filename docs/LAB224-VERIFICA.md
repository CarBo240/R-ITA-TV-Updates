# Verifica R. ITA TV 2.1.24 LAB

## Modifica funzionale

Il player R. ITA usa `LabScResolver` e `LabHlsProxy` soltanto quando la fonte di riproduzione ha tipo `streamingcommunity`. Tutte le altre fonti mantengono il ramo precedente.

## Controlli statici

- `VodPlayerActivity`, `LabHlsProxy`, `LabScResolver` e `LabScProxyPlayerActivity`: delimitatori lessicali bilanciati.
- Nessun accesso residuo al campo inesistente `PlaybackException.errorCodeName`.
- Diff rispetto alla 2.1.23: `VodPlayerActivity.java`, `build.gradle`, changelog e questo documento.
- `ExperimentalCatalogActivity`, `ExperimentalPlayerActivity`, `VodSource` e l'intero percorso Streaming Community LAB 2.1.8 non modificati.

## Prova dispositivo

1. Aprire R. ITA VOD e scegliere Streaming Community come fonte video.
2. Avviare un film già verificato nel catalogo LAB.
3. Controllare avvio, seek ±15 secondi, pausa/ripresa e tracce.
4. Aprire una serie, scegliere stagione ed episodio e ripetere la prova.
5. Premere **Riprova** durante un errore: il vecchio relay deve essere chiuso e il token deve essere risolto nuovamente.
6. Selezionare **Altra fonte** e verificare che VixSrc/RaiPlay/altre fonti continuino a usare il proprio resolver.

La compilazione deve essere eseguita dalla workflow GitHub con Java 17. Se fallisce, allegare soltanto il blocco a partire da `compileDebugJavaWithJavac` o il primo task segnato `FAILED`.
