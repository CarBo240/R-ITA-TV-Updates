# R. ITA TV 2.1.25 LAB

Versione Android 2.1.25, versionCode 83.

## MaxStream LAB

- Aggiunto il riconoscimento controllato degli intermediari numerati `maxmon*.site` e `maxsun*.site` osservati nella catena pubblica MaxStream.
- Implementato il passaggio dall'intermediario `freeswatcee` alla destinazione pubblica `maxstream.video/cast2/...`, conservando integralmente identificativo e token opaco.
- Supportati sia il collegamento `C0NTINUE` sia l'iframe player verso `cast2`, `castv` o `video` esclusivamente tra domini MaxStream ammessi.
- La pagina finale continua a essere risolta tramite dichiarazioni HLS/MP4 pubbliche e il contenuto viene verificato prima di essere consegnato a Media3.
- Aggiunte regressioni per la catena `maxmon683.site` → `maxstream.video/cast2` → HLS e per il rifiuto dei domini imitazione.

## Limiti e sicurezza

- Nessuna chiave, firma o API privata di Veezie è stata copiata.
- Il resolver non decifra il token MaxStream e non aggira Cloudflare o CAPTCHA: segue soltanto destinazioni pubbliche esposte dalla pagina.
- Se l'intermediario restituisce HTTP 403 sul dispositivo, la diagnostica continua a riportare il rifiuto reale senza dichiarare la fonte riproducibile.

## Conservazione

- Streaming Community LAB e R. ITA, VixSrc, Live TV, RaiPlay, Mediaset Infinity e gli altri componenti non sono stati modificati.
