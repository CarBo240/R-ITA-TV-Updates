# R. ITA TV 2.1.16 LAB

Versione 2.1.16, versionCode 74. Patch rispetto alla 2.1.15.

## Errore confermato sul dispositivo

Lo screenshot mostra STAYONLINE_UNSUPPORTED_TARGET con destinazione m1xdrop.net. L'ID XNnym è di cinque caratteri: la correzione 2.1.15 permette ora di superare l'estrazione ID e ricevere una destinazione. Il rifiuto successivo è dovuto al registro host, che accettava mxdrop.top ma non m1xdrop.net. Questa correzione non richiede chiavi Veezie.

## Correzione MixDrop

- Aggiunti gli alias esatti m1xdrop.net e www.m1xdrop.net, coerenti con il riconoscimento MixDrop osservato in switchHost dell'APK Veezie. Nessun codice Veezie copiato.
- L'alias è usato sia dalla classificazione delle fonti sia dal resolver MixDrop. StayOnline può ora passare la destinazione a quel resolver, senza sostituire il dominio o alterare l'identificativo.
- Se il sito esegue un normale redirect HTTPS verso mxdrop.top, il LAB conserva l'URL finale e segue l'iframe /e/ con il resolver esistente.
- Se m1xdrop.net dichiara direttamente MDCore.wurl/vurl, la sorgente viene letta dal parser MixDrop esistente. Restano obbligatorie verifica dei bytes MP4/HLS/DASH e trasmissione del referer al player.
- Un alias riconosciuto senza sorgente produce NO_PUBLIC_MEDIA; HTTP 403 interrompe la catena. Non è dichiarato un resolver funzionante sulla sola base del dominio. Domini contraffatti come m1xdrop.net.evil.example ed evil-m1xdrop.net sono esclusi.

## Verifica del collegamento reale

Il collegamento StayOnline fornito, /l/XNnym/, è stato aperto nel browser di questo ambiente. La pagina dichiara linkId XNnym e l'endpoint /ajax/linkView.php. Il normale pulsante Click to continue ha portato alla pagina mxdrop.top/f/pjkrwgqdaljdo0, con iframe /e/pjkrwgqdaljdo0. Il browser non ha mostrato una CAPTCHA interattiva durante questo passaggio.

Il pulsante Play Video nell'embed ha restituito Unable to play media. Il video DOM ha readyState 0, nessuna currentSrc attiva e videoWidth 0. Quindi la navigazione reale è verificata, ma non una riproduzione riuscita. Lo screenshot del dispositivo conferma la destinazione m1xdrop.net dell'AJAX; il percorso di redirect intermedio di questo browser non è stato ricostruito attraverso richieste nascoste.

## Test e compilazione

- 137 controlli JVM PASS con Java 17. Nuova catena su fixture: ID XNnym → POST pubblico StayOnline → m1xdrop.net → redirect mxdrop.top → embed → MP4 verificato.
- Verificati anche parser m1xdrop senza redirect, alias www, domini contraffatti, pagina priva di sorgente e HTTP 403. Le risposte media dei test sono sintetiche, non copie o URL firmati del titolo reale.
- Sintassi dei quattro Java modificati verificata; questo non sostituisce la compilazione Android.
- 446 file originali protetti byte per byte rispetto alla base 2.1.10. VOD, Live, Streaming Community LAB e VixSrc invariati.
- Compilazione Android tentata con Gradle/JDK originali; esito riportato in docs/LAB216-VERIFICA.md. Nessun APK/FIRST_FRAME sul dispositivo dichiarato.

## Limiti ancora aperti

- Uprot /msf/9r5lmsrtcmi7 continua a fallire sul dispositivo con INTERMEDIARY_NO_PUBLIC_LINK. Questa patch non introduce una conversione API Uprot con chiave. Le verifiche precedenti in questo browser erano bloccate dal sito con Cloudflare 1005/ASN; non sono stati cercati percorsi per aggirarlo.
- VidXGo HTTP 403 resta irrisolto; nessun cambiamento ad AltaDefinizione o al player per quel rifiuto.
- L'avvio effettivo in Media3 va verificato installando la build sul dispositivo: SOURCE VERIFIED e PLAYBACK FIRST_FRAME sono fasi distinte.

Lo ZIP dei soli file modificati si applica alla 2.1.15. Lo ZIP completo contiene il progetto e la cronologia delle versioni precedenti. Nessuna chiave, cookie Veezie, APK Veezie o sorgente video a scadenza inclusa.
