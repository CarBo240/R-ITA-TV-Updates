# R. ITA TV 2.1.18 LAB

VersionName 2.1.18, versionCode 76. Patch rispetto alla 2.1.17. I quattro script aggiornati di Veezie forniti dall'utente sono stati usati per analisi del comportamento; non sono inclusi nell'app o negli ZIP. Nessuna chiave, firma API, libreria di decrittazione proprietaria o funzione di aggiramento è stata importata.

## Problema e comportamento nuovo

La sola lettura degli iframe non ricostruiva le due fonti che Veezie mostra per Animals su AltaDefinizione. La fonte “Diretto” è VixSrc, ottenuta dall'identità IMDb, mentre VidXGo dichiara una playlist HLS negli script della pagina e rinnova i parametri durante la visione. MaxStream può dichiarare HLS separatamente dall'MP4.

### AltaDefinizione: catalogo e fonti

- Aggiunto il layout `div.movie` con `data-link`, `data-title` e immagine `layer-image`, mantenendo i precedenti layout con ancore/locandine e DLE.
- Per un provider configurato alla root, il catalogo iniziale usa `/film`; ricerca `/search?q=...`. Solo un HTTP 404 permette il ritorno al vecchio percorso root o `/archivio?search=...`. Un 403 non attiva tentativi su percorsi alternativi.
- Il referer dell'iframe continua a rispettare `referrerpolicy=origin`; i trailer sono separati.
- Una sola identità IMDb dichiarata nella scheda o nell'iframe VidXGo crea una fonte candidata VixSrc. Identità discordanti non producono associazioni approssimative.
- Nuovo `LabVixBridge`: IMDb → ricerca esterna esatta TMDB → `VixSrc.resolve` originale. Riutilizza la configurazione TMDB di R. ITA, senza modificare preferenze o resolver. Il player è lo stesso `ExperimentalPlayerActivity` già usato dal percorso VixSrc LAB, con gli stessi referer/origin.
- La fonte candidata non significa disponibilità: soltanto il resolver VixSrc originale che verifica il manifest consente di aprire il player. Diagnostica `TMDB_KEY_MISSING`, `VIX_ID_UNRESOLVED`, `VIX_EXISTING`, `VIX_VERIFIED`.

### VidXGo: resolver e sessione Media3

- Resolver dedicato al dominio esatto `v.vidxgo.co`, indipendente dal catalogo. Non è un riconoscitore di nomi: estrae una dichiarazione HLS e la pipeline verifica i bytes `#EXTM3U` prima del player.
- Decodifica statica, limitata, delle dichiarazioni pubbliche base64/XOR presenti nell'embed. Nessun `eval`, esecuzione di JavaScript remoto o chiave riservata. Più playlist distinte, payload oltre limite o dichiarazioni prive di HLS producono errori specifici.
- Conservazione dei parametri della sorgente e dell'identificativo film/episodio. Referer del media `https://v.vidxgo.co` e intestazioni di richiesta HLS coerenti con il percorso analizzato.
- Gestore di sessione dedicato solo a VidXGo. Media3 usa HTTPS direttamente: nessun indirizzo privato di Veezie o proxy esterno è incorporato.
- Rinnovo tramite il percorso pubblico `/t/<identificativo>`, con identificativo client UUID generato dal LAB. Il rinnovo è verificato quando arrivano richieste Media3, prima della scadenza del token, senza servizio persistente in background.
- Aggiornamento dei parametri osservati `t`, `e`, `b` su manifest e segmenti appartenenti al CDN e alla directory originali; altri parametri (audio/qualità) restano intatti. Nessun trasferimento di token a host o titoli differenti.
- Una risposta di rinnovo che cambia CDN o percorso master, manca dei parametri necessari, è scaduta o rifiuta l'accesso interrompe la sessione con diagnosi. Non vengono ricostruite firme, cercate alternative per evitare rifiuti o disabilitate verifiche TLS.
- La sessione viene chiusa quando il player si ferma. Le risposte di una vecchia sessione non aggiornano la schermata di quella nuova.
- Diagnostica player: `VIDXGO_TOKEN_REFRESH`, `VIDXGO_TOKEN_UPDATED`, `VIDXGO_REFRESH_HTTP_<status>`, `VIDXGO_REFRESH_SCOPE_CHANGED`, `VIDXGO_REFRESH_EXPIRED`, oltre a SOURCE VERIFIED / PLAYBACK READY / FIRST_FRAME.

### MaxStream

- Lettura della playlist HTTPS nello script `id="m3u8"`, preferita alla sorgente MP4 dichiarata in `cccc`.
- Il percorso HLS fornito dall'utente, con virgole e `.urlset/master.m3u8`, viene conservato integralmente. Non si aggiunge `.mp4` alla playlist.
- Restano il redirect pubblico Uprot con HEAD, i collegamenti pubblici univoci, Continue sullo stesso host e la lettura degli script compattati successivi.
- La conversione MaxStream degli ID effettuata tramite API firmata di Veezie non è stata importata. Se Uprot non espone un redirect/link pubblico compatibile, la catena continua a fallire con una diagnosi intermediario: questa versione non certifica una soluzione universale Uprot.

## Preservazione

- 446 file originali protetti: byte identici alla base allegata 2.1.10, controllati con SHA-256.
- Streaming Community LAB 2.1.8, il suo resolver e player, VOD principale, Live TV e resolver VixSrc originali invariati.
- Parser MixDrop identico alla 2.1.17/2.1.16, la cui riproduzione è stata confermata dall'utente. MixDrop continua nella stessa configurazione Media3; il nuovo gestore di token si attiva soltanto con metadata VidXGo.
- Nessun cambio a credenziali, cookie o impostazioni del VOD principale.

## Verifiche e limiti

- Java 17: nucleo compilato e 182 controlli PASS. Fixture catalogo → fonti → resolver → bytes media; MaxStream HLS con il formato URL fornito; VidXGo XOR/HLS; query firmate, rinnovo e scope; regressioni MixDrop/StayOnline; rifiuti 403 e identità discordanti.
- Sintassi Java dei sei file nuovi/modificati: PASS. Questo controllo non sostituisce la compilazione Android o la verifica delle API Android/OkHttp.
- Build `compileDebugJavaWithJavac` tentata: AGP 8.9.1 non presente nella cache offline. Il precedente tentativo online nello stesso ambiente non riusciva a scaricarlo. Fallimento in configurazione prima del compilatore; nessun APK 2.1.18 prodotto.
- Non eseguiti: FIRST_FRAME reale MaxStream/VidXGo/VixSrc su dispositivo, richiesta live di rinnovo e riproduzione prolungata. L'utente conferma che Veezie riproduce entrambe le fonti Alta; questo non certifica ancora il LAB.
- Il nuovo ponte VixSrc copre i film con identità IMDb univoca. La navigazione stagioni/episodi Alta non è stata ricostruita. Gli iframe di episodi già presenti possono essere risolti da VidXGo se usano le dichiarazioni supportate.
- Cookie/login, CAPTCHA, API riservate, DRM, formati dinamici diversi e cambi CDN durante rinnovo restano non supportati. DoodStream resta limitato a media HTML5 pubblicamente dichiarati; gli altri resolver conservano le capacità documentate, senza nuove dichiarazioni di compatibilità.

Vedere `docs/LAB218-VERIFICA.md` per la prova sul dispositivo. Lo ZIP modificati va applicato alla 2.1.17; lo ZIP completo contiene tutto il progetto pronto per la build GitHub con la propria firma.
