# Diagnosi delle schermate 1513 / 1514

## CB01 / Animals

Osservazione diretta dello screenshot: HOST_SEARCH segnala una fonte, ma la riga contiene soltanto www.youtube.com e viene etichettata HTML5 pubblico.

Problema nel codice 2.1.11: `declaredSources` ammette indiscriminatamente tutti gli iframe e non separa trailer/piattaforme esterne. Inoltre scarta i link a intermediari non presenti nel registro dei resolver. Questo spiega un possibile elenco incompleto, ma non dimostra quali link manchino nella scheda reale di Animals.

Correzione: YouTube separato e non passato al player del titolo; iframe/pagine candidate non indicati come supportati; lettura delle dichiarazioni data-url/data-link e dei link pubblici a intermediari. Questi ultimi sono visibili ma non risolti. Per completare l'adattatore CB01 serve l'URL completo della scheda interessata o una risposta HTML accessibile. Nessun URL di dettaglio è inventato e nessun host mancante è dedotto dal solo nome del film.

## AltaDefinizione / Neagley

Lo screenshot distingue Neagley - Trailer e Neagley - Player, entrambi su v.vidxgo.co, e segnala RESOLVER HTTP_403.

La pagina pubblica controllata distingue Trailer, selezione stagione/episodio e Player. Entrambi i collegamenti esterni controllati restituiscono 403 prima dell'estrazione del media. Il 403 non è una prova di un bug nel decoder Media3 e non rivela da solo la causa del rifiuto.

Correzione: separazione del trailer, classificazione del player come pagina da verificare e diagnostica esplicita «nessuna sorgente letta». Nessun aggiramento, ricostruzione di token o resolver VidXGo completo è stato aggiunto. Il player di Neagley rimane non disponibile con questo metodo.

Fonti primarie controllate:

- https://altadefinizionex.surf/serie-tv/44881-neagley-streaming.html
- https://v.vidxgo.co/ (i collegamenti specifici dalla scheda restituiscono 403)

## Regressioni aggiunte

18 controlli aggiuntivi su markup sintetico: YouTube escluso dalle fonti principali e dal resolver; trailer e player separati; pagine sconosciute classificate come da verificare; mantenimento del 403; intermediari visibili ma non supportati; testo anchor; data-url; lazy iframe; fallback da data-src vuoto; nomi di film contenenti Trailer; suffix trailer esplicito; corrispondenza esatta del dominio YouTube; diagnostica TRAILER_ONLY; link esplicito Player.

I 65 controlli JVM non costituiscono una prova di riproduzione Android o dei servizi reali. L'analisi e il rapporto 2.1.11 inclusi nel progetto descrivono la versione precedente; questo documento e il changelog 2.1.12 descrivono le correzioni successive.
