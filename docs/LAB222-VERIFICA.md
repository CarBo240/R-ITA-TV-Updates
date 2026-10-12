# Verifica R. ITA TV 2.1.22 LAB

Il dettaglio funzionale, i limiti e la procedura di prova sono in `CHANGELOG-2.1.22-LAB.md`.

Esito locale:

- 207 controlli pipeline: PASS;
- conservazione sorgenti originali: PASS, 440 invariati e 6 eccezioni autorizzate;
- nessuna modifica al percorso Streaming Community LAB, Live TV, VixSrc o player/DRM Rai;
- sintassi dei file modificati analizzata dal compilatore senza errori di parsing;
- build Android completa non eseguita: dipendenze SDK/AGP non disponibili nella cache;
- login RaiPlay, navigazione DPAD e playback live richiedono prova sul dispositivo.

Mediaset Infinity non è dichiarato provider nativo. Il sito ufficiale resta un fallback separato; non è stato eseguito scraping né recupero di token/licenze.

MaxStream via Uprot non è dichiarato risolto: il passaggio osservato dipende da un servizio privato firmato non riutilizzato.
