# R. ITA TV 2.1.9 LAB

- Base: 2.1.8 LAB; versione Android 2.1.9 (code 67).
- Il percorso Streaming Community nel LAB usa ancora ExperimentalHostInspector senza modifiche. Nessuna modifica al VOD principale.
- Per altri provider, nuovo ExperimentalMultiHostResolver: riconosce host distinti, link nei tag video/source/iframe, metadati video HTML e contentUrl/embedUrl dichiarati, riconosce flussi multimediali diretti solo da URL HTTPS pubblici senza query/token.
- Le pagine host non sono spacciate per flussi diretti; collegamenti di immagini e cataloghi esclusi.
- Non sono implementati resolver equivalenti a tutte le funzioni Veezie: nessuna decodifica di script proprietari, né bypass di token, restrizioni o sessioni.
- Test javac indipendente superato; build Android GitHub Actions ancora da verificare.
