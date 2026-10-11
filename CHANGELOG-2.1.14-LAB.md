# R. ITA TV 2.1.14 LAB

Versione 2.1.14, versionCode 72. Patch rispetto alla 2.1.13. Integrazioni scritte autonomamente dopo lo studio dei passaggi Veezie; nessuna chiave incorporata o funzione proprietaria copiata.

## MixDrop: alias e pagina contenitore

- Aggiunto l'alias esatto mxdrop.top, osservato sul collegamento fornito dall'utente. Il controllo del dominio non accetta suffissi contraffatti o nomi contenenti semplicemente «mixdrop».
- La pagina /f/ dichiara un iframe /e/: il resolver ora segue quell'embed pubblico sullo stesso dominio quando la pagina contenitore non dichiara direttamente un media.
- L'embed viene letto dal resolver MixDrop esistente, che decodifica il packer letterale e recupera MDCore.wurl/vurl senza eseguire codice remoto. Il formato radix 10 è stato osservato nell'embed reale; il test usa valori sintetici.
- Referer dell'embed e query della sorgente sono conservati. La destinazione deve ancora superare la verifica della firma MP4/HLS/DASH prima di essere passata al player interno.
- Gli iframe pubblicitari esterni e i pulsanti download non sono usati come embed video. Più embed validi producono HOST_EMBED_AMBIGUOUS.

## StayOnline: resolver AJAX separato

- Implementato il passaggio che mancava al semplice attraversamento dei link HTML: prima GET della pagina /l/, poi POST all'endpoint /ajax/linkView.php sullo stesso sito StayOnline.
- Sono inviati soltanto l'ID pubblico del collegamento e ref vuoto. L'ID viene letto dall'URL o dalla dichiarazione linkId della pagina. Nessuna chiave, cookie di Veezie, token di autenticazione o conversione riservata viene utilizzata.
- La risposta JSON viene analizzata con un lettore limitato: data.value può comparire insieme ad altri campi, ma chiavi duplicate, JSON non valido o strutture troppo profonde vengono rifiutati.
- La destinazione viene validata HTTPS, controllata nel registro degli host e passata al resolver appropriato. Nessuna sostituzione di dominio della destinazione, nessuna scelta arbitraria dei due link che causavano INTERMEDIARY_AMBIGUOUS nello screenshot.
- Una pagina rifiutata o con challenge interrompe la catena prima del POST. Il POST non segue redirect e non viene ripetuto su altri domini.
- Diagnostica STAYONLINE_PUBLIC_POST, PUBLIC_API_LINK, STAYONLINE_NO_ID, STAYONLINE_JSON, STAYONLINE_NO_DESTINATION e STAYONLINE_UNSUPPORTED_TARGET.
- Stato live: il collegamento StayOnline esatto di Animals non è stato fornito e la scheda CB01 non è accessibile da questo ambiente. L'endpoint e il flusso sono ricavati dall'analisi dell'APK Veezie; il successo del servizio reale non è certificato dai test locali.

## MaxStream e Uprot

- Aggiunto l'alias esatto maxsun623.site indicato dall'utente. Usa il parser MaxStream di sorgenti pubbliche e packer; non viene dichiarata una sorgente soltanto perché il dominio è riconosciuto.
- Supportata la normale navigazione di link Continue/C0NTINUE sullo stesso dominio, mantenendo limiti di catena, rilevamento cicli e arresto sulle challenge.
- Nessun servizio Uprot con la chiave incorporata in Veezie viene utilizzato. Rimane disponibile soltanto la catena di redirect/link HTTPS pubblici.
- Nella verifica del collegamento Uprot specifico e della destinazione maxsun623.site, entrambi i siti hanno restituito a questo browser Cloudflare Error 1005, indicando un blocco della rete ASN. Nessun tentativo di aggiramento effettuato. Questo non dimostra che gli stessi link siano bloccati sul dispositivo dell'utente.
- Aggiunta diagnostica ACCESS_ASN_BLOCK quando la risposta contiene esplicitamente Cloudflare 1005 e il riferimento al blocco ASN. Un normale 403 resta distinto e non viene automaticamente attribuito a Cloudflare.

## Prova diretta nel LAB

- Nuovo pulsante «Prova URL host» nella schermata del catalogo. Permette di inserire un singolo collegamento HTTPS a host/media e verificarlo con la stessa pipeline fino al player.
- È utile per provare il collegamento finale mxdrop.top fornito dall'utente, separando l'esito del resolver da quello dell'intermediario.
- Nessun URL di Animals è inserito nel codice, nessuna sorgente a scadenza è memorizzata o distribuita.
- Il pulsante Cerca chiude la tastiera prima di avviare la ricerca, evitando che copra l'elenco sul tablet.

## Verifiche e componenti preservati

- 108 controlli JVM superati, incluse catene complete con POST StayOnline → mxdrop /f/ → iframe /e/ → sorgente MP4 verificata su risposte sintetiche.
- Compilazione del nucleo e dei test con Java 17 superata. Sintassi dei sorgenti Android toccati controllata; non sostituisce la compilazione Android completa.
- 446 file originali conservati byte per byte rispetto alla base allegata 2.1.10. VOD principale, Live TV, percorso sperimentale Streaming Community e VixSrc invariati.
- Il browser ha raggiunto il file e l'embed MixDrop e mostrato la dichiarazione packer pubblica. Il tentativo di avvio non ha ottenuto una sorgente attiva nel video DOM: nessuna riproduzione riuscita viene dichiarata.
- VidXGo di AltaDefinizione rimane non integrato: il 403 segnalato precede il recupero del media. Le correzioni catalogo/locandine della 2.1.13 sono mantenute.
- Build Android bloccata prima della compilazione: AGP 8.9.1 assente dalla cache e dipendenze non recuperabili nell'ambiente. Nessun APK 2.1.14 prodotto; nessuna prova Media3 FIRST_FRAME sul dispositivo.

Consultare docs/LAB214-VERIFICA.md per la distinzione tra codice implementato, prove locali e limiti live. I rapporti delle versioni precedenti sono documenti storici.
