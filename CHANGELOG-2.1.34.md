# R. ITA TV 2.1.34 — SC e MixDrop

Base: sorgenti 2.1.33. VersionName 2.1.34, versionCode 92.

- SC: `h=1` viene aggiunto soltanto con `canPlayFHD: true`; falso o assente non lo abilita. I parametri b/h già dichiarati nella playlist non vengono sovrascritti.
- SC: parser dedicato compatibile con configurazioni multilinea/minificate, stringhe con escape e scadenza numerica. I parametri firmati già presenti vengono conservati.
- LAB/produzione: alias MixDrop gestiti per famiglia di domini, incluso mxdrop.click, con controllo delle etichette del dominio per evitare domini contraffatti.
- LAB e R. ITA condividono il parser MixDrop: wurl/vurl, accesso con punto o parentesi e packer pubblico con apici semplici/doppi. Nessuna esecuzione indiscriminata di codice remoto.
- StayOnline: rimosso il vincolo al nome del pulsante; resta richiesto l'endpoint dichiarato nella pagina accettata. HTTP rifiutati/challenge non vengono aggirati.
- Player R. ITA: MixDrop mantiene lo User-Agent del resolver e il trasporto cookie coerente.
- Proxy SC: rimosse le intestazioni Sec-Fetch aggiunte nella 2.1.33; risoluzione dei collegamenti HLS relativi rispetto all'URL finale dopo redirect.
- Diagnostica proxy: codice HTTP effettivo, playlist o segmento/chiave e dominio, senza URL firmati. Il player LAB conserva l'errore anziché sovrascriverlo con lo stato inattivo.
- Percorso storico ExperimentalCatalogActivity/ExperimentalPlayerActivity del LAB 2.1.8 invariato. Live/Eventi, RaiPlay e VixSrc invariati rispetto alla 2.1.33.

## Adattamento e limiti

L'adattamento copre cambiamenti compatibili degli alias, del markup e della configurazione dichiarata. Un nuovo protocollo, una challenge o un'API diversa richiede un aggiornamento del resolver: non viene promessa compatibilità universale né vengono usate credenziali di Veezie.

## Verifica

Compilato con il compilatore Java 17 disponibile nel runtime il nucleo parser/resolver. Superati 210 controlli pipeline e 18 regressioni SC/MixDrop con fixture sintetiche. La build Android completa non è eseguibile qui per assenza Gradle/SDK configurato. Il workflow GitHub esegue ora anche questi controlli prima della compilazione degli APK. Riproduzione reale non certificata in questo ambiente.
