# R. ITA TV 2.1.32 — R. ITA Live 1.5

Base: R. ITA TV 2.1.31, versionCode 89. Nuova versione 2.1.32, versionCode 90.

- Integrato `rita-live-module` come sottoprogetto libreria Android senza launcher.
- Live TV mantiene Canali, Categorie, Preferiti, Guida e VOD, aggiungendo i pulsanti **Eventi** e **Daddy Live**; DPAD, focus e sfondo seguono lo stile esistente di R. ITA.
- Ogni pulsante apre `com.rita.eventilive.MainActivity` nella rispettiva sezione e passa `dns_cloudflare` dalle impostazioni DNS VOD host. Quando è scelto un altro provider (Google/Quad9/AdGuard o sistema), il booleano Cloudflare è disattivato nel modulo (il modulo non implementa tali DNS).
- Il modulo conserva selezione WebView/Media3, cursore per telecomando, gestione e selezione dei canali alternativi Eventi 1.5; non modifica il suo motore di riproduzione.
- Media3 1.10.1 e OkHttp 4.12.0 coincidono fra host e modulo; WebKit 1.12.1 viene aggiunto dal modulo.
- Gecko e funzionalità pregresse del VOD, LAB, RaiPlay, account/Firebase, updater e Live TV non sono stati eliminati o sostituiti.

## Da verificare con build e prove fisiche

Il modulo non è stato ricompilato qui né provato su dispositivo. Eseguire GitHub Actions e confermare su TIM Box/tablet Eventi (caricamento e tutte le alternative), Daddy Live (fullscreen WebView e avvio player 4), riproduzione Media3, telecomando/cursore e DNS. Le pagine HTTP raggiungibili **non** dimostrano la riproduzione. Servono i report diagnostici delle sessioni che falliscono.
