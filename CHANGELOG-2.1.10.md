# R. ITA TV 2.1.10 LAB

- Base 2.1.9 LAB, versione Android 2.1.10 / versionCode 68.
- NUOVO `ExperimentalHostAdapters`: instradamento riconoscimento per host (MixDrop, MaxStream, StreamTape, VOE, DoodStream, SuperVideo e altri 17 alias).
- Per i collegamenti a host riconosciuti, analisi secondaria dell'HTML pubblico della pagina host (massimo cinque host per titolo), con raccolta dei file multimediali dichiarati apertamente e loro distinzione dalle pagine host.
- Messaggi diagnostici con numero di host ispezionati, fonti dichiarate e host non analizzabili.
- Nessuna esecuzione di script anti-bot, aggiramento restrizioni, decodifica di link protetti o copia di estrattori proprietari Veezie. Gli host senza link pubblici continueranno a non avere una fonte nativa.
- Percorso Streaming Community LAB ESATTAMENTE INVARIATO (continua a usare `ExperimentalHostInspector` e il resolver R. ITA solo su pulsante specifico).
- Codice VOD principale, Live TV, Firebase, player originale invariati.
- Compilazione Java isolata delle 3 classi di analisi: superata. Build Android completa da verificare in GitHub Actions.
