# R. ITA TV 2.1.8 LAB

- LAB VixSrc: catalogo e ricerca TMDB come nel VOD R. ITA; scelta titolo/episodio e riutilizzo di VixSrc.resolve().
- Player LAB: utilizza la configurazione Media3/OkHttp già usata nel player VOD per i flussi HLS/DASH/MP4; inoltra Referer e Origin del resolver VixSrc.
- Classificazione host: evita di indicare pagine e immagini di AltaDefinizione come fonti video.
- Nessuna modifica ai sorgenti principali del VOD e Live TV, salvo versionamento e workflow.

NOTA: MixDrop, MaxStream e altri host non dispongono in questa build di resolver nativi verificati. I link host non vengono spacciati per flussi. Non sono implementate tecniche di aggiramento di protezioni di accesso.
