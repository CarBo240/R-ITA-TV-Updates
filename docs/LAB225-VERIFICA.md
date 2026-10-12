# Verifica R. ITA TV 2.1.25 LAB

1. Aprire LAB → CB01 → `Animals [HD] (2026)`.
2. Selezionare MaxStream proveniente da `uprot.net/msf/9r5lmsrtcmi7`.
3. Controllare in Diagnostica la sequenza Uprot → intermediario `maxmon*.site/freeswatcee` → `maxstream.video/cast2` → sorgente HLS verificata.
4. Confermare che il player interno Media3 raggiunga `STATE_READY` e che seek, audio e sottotitoli disponibili funzionino.
5. Se appare `HTTP_403` o `ACCESS_CHALLENGE`, annotare fase, dominio e codice: non equivale a un errore del parser e non viene aggirato.

Fixture osservata durante lo sviluppo:

- Uprot: `https://uprot.net/msf/9r5lmsrtcmi7`
- intermediario: dominio numerato `maxmon683.site`, percorso `freeswatcee`
- player: `https://maxstream.video/cast2/...`

I token completi non sono riportati nella documentazione perché sono temporanei.
