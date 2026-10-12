# R. ITA TV 2.1.28 LAB

## DNS interno R. ITA

- Corretto il bootstrap DNS-over-HTTPS: Cloudflare, Google, Quad9 e AdGuard vengono ora raggiunti tramite i rispettivi IP iniziali, senza dipendere dal DNS configurato sul TIM Box.
- Il DNS selezionato in R. ITA viene applicato anche al trasporto CB01, alle immagini del catalogo, ai resolver StayOnline/MixDrop e alle verifiche delle sorgenti.
- Il nuovo laboratorio multi-provider usa il trasporto DNS scelto in R. ITA mantenendo separati cookie, redirect e diagnostica LAB.
- Il cambio DNS ricrea i client dinamici: non è necessario modificare il DNS di Android TV o del router.
- Conservato senza modifiche funzionali il percorso sperimentale Streaming Community 2.1.8 e il relativo resolver/player.

## Correzioni precedenti incluse

- CB01 torna tra i risultati di “Altre fonti” anche quando una singola sorgente non viene risolta immediatamente.
- Streaming Community nel VOD principale effettua un solo rinnovo automatico del collegamento in caso di errore temporaneo.
- Diagnostica Media3 più precisa dopo il tentativo automatico.

## Verifica

- Controllo statico completato sui riferimenti ai vecchi client CB01.
- Compilazione locale non eseguita: il runtime disponibile non contiene Gradle/Gradle Wrapper. Il progetto resta predisposto per il workflow GitHub con Java 17 e Gradle 8.11.1.
