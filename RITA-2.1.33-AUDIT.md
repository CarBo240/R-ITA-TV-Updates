# Audit statico R. ITA TV 2.1.33

## Verificato

- Build principale: `src/it/carmine/streamplayer`.
- Modulo Eventi: `rita-live-module`, incluso come libreria senza launcher separato.
- Versione: 2.1.33 / code 91.
- Artifact: `R-ITA-TV-v2.1.33-APK`.
- Streaming Community R. ITA: `LabScResolver` → `LabHlsProxy` → Media3.
- Streaming Community LAB storico: non modificato.
- MixDrop: percorso specifico con StayOnline opzionale e resolver effettivo.
- Live principale: un solo ingresso Eventi; Daddy Live interno al modulo Eventi.

## Limiti della verifica

- Nessuna chiamata live ai provider è stata certificata come riproducibile dall'ambiente di sviluppo.
- Nessun controllo di accesso, challenge, DRM o token privato viene aggirato.
- Le copie Java nella radice sono materiale storico e non sono compilate dal source set Gradle; per modifiche future usare sempre `src/`.
- GitHub Actions e prova sul dispositivo restano obbligatorie prima di definire la release stabile.
