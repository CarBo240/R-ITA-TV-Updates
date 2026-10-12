# R. ITA TV 2.1.24 LAB

Versione Android 2.1.24, versionCode 82.

## Streaming Community nel VOD R. ITA

- Importato nel player VOD principale il trasporto VixCloud confermato funzionante nel nuovo percorso LAB 2.1.23.
- La fonte Streaming Community usa ora `LabScResolver`: la playlist viene risolta fresca e non viene consumata da una verifica HTTP preliminare.
- Media3 riceve un URL del relay HLS locale su `127.0.0.1`; master, varianti, audio, sottotitoli, chiavi e segmenti mantengono Referer, Origin, cookie, User-Agent e richieste Range.
- Il relay viene chiuso e ricreato su retry, cambio episodio/cambio fonte e viene sempre arrestato all'uscita dal player.
- L'integrazione vale per film ed episodi Streaming Community e non viene applicata a VixSrc, RaiPlay, fonti web o provider LAB alternativi.

## Conservazione

- Il percorso Streaming Community LAB 2.1.8 resta byte-identico.
- Il nuovo percorso proxy LAB 2.1.23 resta disponibile e invariato.
- Live TV, RaiPlay, Mediaset Infinity e gli altri resolver/player non sono stati modificati.

## Verifica richiesta

Compilare con Java 17/Gradle 8.11.1 e provare in R. ITA un film e un episodio Streaming Community, quindi verificare retry, seek, cambio qualità, audio e sottotitoli.
