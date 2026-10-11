# R. ITA TV 2.1.6 LAB — analizzatore host e cataloghi

- Base stabile 2.1.1 mantenuta: VOD e player principali non modificati.
- In Impostazioni > Laboratorio TEST: nuovo comando «Analizza pagina / host».
- Riconoscimento di collegamenti HTML pubblici a MixDrop, MaxStream e AltaDefinizione, visualizzati separatamente dai flussi video diretti.
- Possibilità di analizzare ulteriori pagine, aprire il sito nel browser TEST e provare nel player LAB soltanto URL HTTPS diretti (HLS/DASH/MP4) non firmati.
- Limitazioni intenzionali: non esegue script dei siti, non deoffusca codici, non acquisisce credenziali, non aggira 403/DRM; nessun resolver completo per host protetti.
- Nessun salvataggio delle preferenze dei provider principali.
- VersionCode 64; compilazione e prove da effettuare su GitHub Actions.
