# Verifica R. ITA TV 2.1.23 LAB

## Esito statico

- Versione: 2.1.23 / versionCode 81.
- Diff contro i sorgenti completi 2.1.22: soltanto manifest, build, hub LAB, changelog e quattro classi nuove del proxy SC.
- `ExperimentalCatalogActivity.java`: invariato, SHA-256 `c7063e2440214a4688009b8d973dec2bc5ef5257351ec4dbbf04fe9e112d7c17`.
- `ExperimentalPlayerActivity.java`: invariato, SHA-256 `d34de32083664f82114deba388216472e11d4b3c5d2cdc57375225725f44fef1`.
- `VodSource.java`: invariato, SHA-256 `93ba235e3029e8ba1b216ec1481f2732532d91896501dd43ceab906b016e0f5e`.
- Controllo lessicale delle parentesi delle nuove classi: superato.

## Prova su Android TV

1. Aprire Laboratorio TEST e selezionare il provider Streaming Community.
2. Scegliere **Catalogo SC → proxy HLS LAB**.
3. Selezionare un film dalla griglia e poi **Riproduci**.
4. Verificare che lo stato passi da `Avvio proxy HLS` a `Proxy HLS · caricamento` e infine `VixCloud pronto`.
5. Verificare seek, cambio qualità e tracce HLS disponibili.
6. Ripetere dal menu **Percorso Streaming Community 2.1.8 conservato** per confermare che il percorso precedente non sia cambiato.

Se il nuovo player restituisce ancora 403, registrare se l'errore avviene sulla playlist iniziale o dopo l'avvio. Un 403 iniziale indica che il server ha vincolato il token a un requisito ulteriore; un errore dopo alcuni secondi indica invece una variante, chiave o segmento che richiede un'intestazione diversa.

## Compilazione

Non eseguita localmente: nell'ambiente della sessione non sono disponibili `javac`, Gradle o il Gradle wrapper. È necessario eseguire la workflow GitHub/Android SDK prima della distribuzione.
