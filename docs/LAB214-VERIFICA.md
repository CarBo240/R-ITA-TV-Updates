# Verifica 2.1.14 LAB

| Componente | Implementazione | Verifica disponibile |
| --- | --- | --- |
| MixDrop su mxdrop.top | Alias esatto, pagina /f/, iframe /e/, packer, wurl, media | Pagina ed embed reali osservati; catena completa su fixture; riproduzione live non riuscita nel browser |
| StayOnline | GET pagina, ID pubblico, POST AJAX, lettura data.value, dispatch finale | Test locale completo con verifica dei bytes; link StayOnline reale non disponibile per la prova |
| MaxStream su maxsun623.site | Alias esatto, sorgenti pubbliche, packer, Continue sullo stesso sito | Test locale completo; link reale bloccato dal sito in questo browser con Cloudflare 1005 |
| Uprot | Redirect e link pubblici; nessuna chiave Veezie | Il link fornito restituisce Cloudflare 1005 in questo browser; servizio con chiave non integrato |
| VidXGo | Nessun resolver aggiunto | Screenshot: HTTP 403 prima del media; non attribuito al player |
| Player Media3 LAB | Intent e client dedicato ereditati; ingresso solo dopo SOURCE VERIFIED | Nessuna installazione/FIRST_FRAME 2.1.14 |
| Nucleo Java e test | PASS | 108 controlli; compilazione Java 17 |
| Android | Compilazione bloccata | AGP 8.9.1 non presente nella cache; sintassi verificata separatamente |
| Originali protetti | PASS | 446 file identici alla base 2.1.10 |

## Passaggi osservati in Veezie

Lo studio locale di resolveStayOnline, mixdropExtract, maxstreamExtract e parseMaxstreamLink distingue tre operazioni: risolvere l'intermediario, leggere la pagina effettiva dell'host e recuperare il flusso. StayOnline usa un POST con ID pubblico e restituisce data.value; MixDrop legge l'embed e la dichiarazione MDCore dopo decodifica del packer. Alcuni rami Uprot richiedono un servizio con chiave incorporata nell'app. Quel servizio non viene riutilizzato. I resolver R. ITA sono implementazioni autonome limitate alle operazioni pubbliche descritte nel changelog.

## Prova sul TV/tablet

1. Applicare lo ZIP dei file modificati alla 2.1.13 oppure usare lo ZIP completo, quindi compilare su GitHub/Android Studio con le dipendenze originali.
2. Nel catalogo LAB premere «Prova URL host» e incollare, separatamente, il collegamento finale MixDrop fornito. Deve apparire ADAPTER MixDrop, HOST_EMBED, SOURCE VERIFIED e infine PLAYBACK FIRST_FRAME. SOURCE VERIFIED da solo non certifica la riproduzione.
3. Provare poi la riga MixDrop della scheda CB01. La catena prevista include STAYONLINE_PUBLIC_POST e PUBLIC_API_LINK prima del resolver MixDrop. Un HTTP 403, challenge o errore JSON resta visibile e interrompe il passaggio.
4. Provare la destinazione MaxStream fornita direttamente. ACCESS_ASN_BLOCK descrive esclusivamente una risposta che contiene il blocco Cloudflare 1005; non attiva proxy, cambio rete o altri aggiramenti.
5. Verificare separatamente riproduzione, tracce audio/sottotitoli disponibili, BACK e telecomando. Il percorso Streaming Community e VixSrc continua nelle attività originali.

Gli URL incollati nel messaggio dell'utente terminavano con due collegamenti uniti senza spazio. Per le verifiche sono stati separati al secondo prefisso https://: maxsun623.site e mxdrop.top. Non sono stati modificati i loro identificativi o parametri.
