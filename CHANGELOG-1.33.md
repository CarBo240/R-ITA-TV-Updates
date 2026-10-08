# R. ITA TV 1.33

- Impostazioni: nascosto il riferimento visibile al repository GitHub. Il controllo aggiornamenti conserva la configurazione interna.
- Rimosso il campo CineSearch API dalla schermata Impostazioni (le altre funzioni restano disponibili).
- DNS VOD e Gecko: sistema, Cloudflare, Google, Quad9, AdGuard. Conservata retrocompatibilità con cloudflareDns e fallback DNS di sistema per le richieste VOD. Pulsante verifica DNS.
- Informazioni: voce Versione dell’app letta da BuildConfig, con crediti TMDB mantenuti nella schermata informativa per rispettare l’attribuzione.
- Incremento versione a 1.33 / versionCode 41.

## Verifiche richieste prima della pubblicazione
- Compilazione Android GitHub Actions, controllo firma e apk universal.
- Test DNS anche dentro GeckoView (le preferenze Gecko necessitano un nuovo caricamento sessione).
- Test VOD e regressioni ereditate dalla 1.32.
- Verifica updater su box 1.32 e accessibilità del repository.
