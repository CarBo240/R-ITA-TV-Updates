# Verifica 2.1.18 LAB

| Percorso | Implementazione | Verifica eseguita |
| --- | --- | --- |
| MixDrop | Parser invariato; stessa configurazione player | Regressioni PASS; funzionamento utente confermato sulla 2.1.16 |
| Alta catalogo | Layout movie/data-link e locandine precedenti; /film e /search?q | Fixture PASS; live ancora da verificare |
| Alta → Diretto VixSrc | IMDb univoco → TMDB → resolver/player originali | Sintassi e conservazione originali PASS; richiesta live da verificare |
| Alta → VidXGo → HLS | Decoder statico delle dichiarazioni e controllo manifest | Catena con bytes media su fixture PASS |
| VidXGo rinnovo | /t/ID, UUID LAB e sostituzione token limitata al CDN/titolo | Stato token/query su JVM PASS; rete e Media3 da verificare |
| MaxStream HLS | Script m3u8 preferito a MP4; URL completo preservato | Fixture con formato URL fornito dall'utente PASS |
| Uprot → MaxStream | Redirect HEAD o link pubblico compatibile | Fixture PASS; non certificato sul link reale |
| API firmata Veezie | Non integrata | Nessuna firma/chiave di Veezie riutilizzata |
| Originali | 446 file protetti identici | SHA-256 PASS |
| Build Android | Tentata; manca AGP 8.9.1 nella cache | Nessun APK; compilazione Android non completata |

## Prova sul dispositivo

1. Applicare lo ZIP modificati alla 2.1.17 o compilare lo ZIP completo. Conservare la propria firma; non disinstallare l'app per sostituirla.
2. Controllare MixDrop e il percorso Streaming Community LAB già funzionanti.
3. AltaDefinizione: aprire catalogo, cercare Animals e selezionare la scheda. Controllare locandine e fonti separate “Diretto · VixSrc” e VidXGo; il trailer deve restare escluso.
4. Diretto: verificare VIX_IMDB → VIX_EXISTING → VIX_VERIFIED e riproduzione nel player VixSrc originale. Se manca la configurazione TMDB oppure il film non corrisponde a un'unica identità, registrare la diagnosi; nessuna associazione approssimativa.
5. VidXGo: verificare ADAPTER VidXGo → SOURCE VERIFIED → PLAYBACK FIRST_FRAME. Provare audio/sottotitoli disponibili e proseguire oltre un rinnovo token. Annotare eventuali VIDXGO_REFRESH_HTTP, SCOPE_CHANGED, EXPIRED o FORMAT. Solo una prova prolungata conferma la sessione completa.
6. MaxStream: provare prima il server dalla scheda CB01. Se fallisce Uprot, usare “Prova URL host” con la pagina finale MaxStream già fornita per distinguere intermediario e parser. Il flusso HLS finale copiato da Veezie prova soltanto l'ingresso player: non dimostra la risoluzione automatica della pagina.
7. Dopo FIRST_FRAME provare BACK, riapertura, telecomando e arresto del player. Non condividere token/credenziali nei log pubblici.

READY non equivale a FIRST_FRAME. Gli URL firmati copiati da Veezie non sono memorizzati come sorgenti permanenti. I 182 controlli JVM sono fixture, non prove di playback Android. Se un sito rifiuta l'accesso, il LAB conserva la diagnosi e non avvia una procedura di aggiramento.
