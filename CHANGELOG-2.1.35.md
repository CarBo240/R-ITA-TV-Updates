# R. ITA TV 2.1.35 — fonti alternative italiane

VersionName 2.1.35, versionCode 93. Il pacchetto contiene soltanto file nuovi o modificati rispetto allo ZIP sorgente 2.1.32 fornito dall'utente; comprende anche le correzioni 2.1.33 e 2.1.34. Copiare i percorsi nella radice del repository, mantenendo src, scripts, tests e .github. Non creare una cartella project aggiuntiva.

## Cataloghi e player

- GuardaFlix/GuardaPlay: schede film, ricerca, wrapper iframe e server dichiarati.
- GuardaSerie/GuardoSerie: schede e collegamenti degli episodi.
- StreamingIta: schede WordPress e server DooPlayer tramite il POST pubblico della pagina.
- AnimeWorld: catalogo, episodi e API pubblica che restituisce il grabber.
- AnimeSaturn: catalogo, episodi e configurazione embed/playlist dichiarata dal sito.
- AnimeUnity: catalogo JSON con cookie e CSRF della propria sessione, ricerca, episodi dichiarati e recupero embed. Per cataloghi molto lunghi la lettura degli episodi è limitata a 1000.
- AltaDefinizione-01: riconoscimento delle schede boxgrid e ricerca DLE.
- EuroStreaming.design: conservato il percorso film/identità TMDB/VixSrc già presente; il dominio yachts non diventa automaticamente compatibile con il layout design.
- Fonti nuove aggiunte una sola volta nelle impostazioni, rispettando fonti esistenti e rimozioni. Gli URL impostati dall'utente non vengono sovrascritti.
- Altre fonti cerca in background e mostra i provider per cui il resolver verifica almeno un media. Per le serie, il menu episodi elenca gli ID dichiarati e risolve quello selezionato; non scarica preventivamente ogni episodio.
- Cataloghi e video nell'interfaccia R. ITA; gli adattatori sono disponibili anche nel LAB multi-provider. Il LAB storico Streaming Community 2.1.8 rimane invariato.

## Resolver

- Streaming Community: percorso iframe, fallback watch solo su 404; parser della configurazione masterPlaylist con params annidati; parametri firmati preservati. User-Agent desktop e Referer origin coerenti con il percorso osservato in Streamflix.
- MixDrop: alias aggiornati, parser pubblico wurl/vurl e packer; contesto di riproduzione senza Referer aggiunto al MP4.
- StayOnline: endpoint linkEmbedView.php, sessione propria e associazione al server.
- Dropload, SuperVideo, VOE, StreamTape, StreamWish, DoodStream, Vidoza: configurazioni pubbliche, alias controllati e intestazioni distinte per host.
- VOE: supportata anche la configurazione JSON codificata moderna.
- DoodStream: handshake pass_md5 dichiarato nella pagina, cookie della propria sessione e contesto media.
- VixCloud: configurazione pubblica condivisa col parser SC; proxy HLS nei player LAB e R. ITA.
- Sottotitoli HTML e tracce captions VTT/SRT dichiarate nei player gestiti.
- Verifica del contenuto media e risoluzione fresca all'apertura; un dominio riconosciuto da solo non equivale a video disponibile.

## Limiti e verifica

MaxStream escluso da queste nuove integrazioni, come richiesto. Nessuna chiave o credenziale di altre app. PlutoTV Italia è un provider live e non è stato aggiunto alla ricerca alternativa VOD. Non vengono importati indiscriminatamente i provider di altre lingue.

Non esiste un resolver universale: sono coperti alias, configurazioni e protocolli descritti qui. Una nuova API o una challenge può richiedere un aggiornamento. HTTP 403 e verifiche interattive restano errori espliciti.

Superati 271 controlli Java con fixture sintetiche, 5 test JavaScript e parsing sintattico di 118 sorgenti Java. Compilazione del nucleo Java riuscita. Build Android completa non verificata: il wrapper Gradle non può scaricare la distribuzione a causa della rete del runtime. Il workflow GitHub compila gli APK dopo i controlli dei resolver. Nessuna certificazione della riproduzione reale dei provider dal box.

Riferimento: Streamflix Reborn2, commit 6dca4ea (14 ottobre 2026), https://git.gay/streamflix-reborn2/streamflix.git. Progetto Apache-2.0; vedere docs/streamflix-reference/LICENSE-2.0.txt e NOTICE.md. Gli adattatori Java sono stati scritti per la pipeline R. ITA; non includono le API private di Streamflix/Veezie.
