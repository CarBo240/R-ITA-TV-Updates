# Verifica 2.1.13 LAB

| Controllo | Esito | Cosa dimostra |
| --- | --- | --- |
| Compilazione JVM LabPipeline e test | PASS, Java 17 | Tipi e sintassi del nucleo indipendente da Android |
| Test pipeline | PASS, 81 controlli | Catalogo, intermediario, resolver e firma del media su fixture |
| Sintassi sorgenti toccati | PASS | Non sostituisce compilazione Android/OkHttp |
| File originali protetti | PASS, 446 SHA-256 identici | Nessuna modifica al VOD, Live, SC o VixSrc ereditati dalla 2.1.10 |
| Layout AltaDefinizione | Osservato nel browser pubblico | Anchor data-title con poster w300; vecchio parser leggeva i sei hero article |
| Ricerca AltaDefinizione | Percorso pubblico osservato | /archivio?search=Milonga; non prova il rendering senza JavaScript |
| CB01 live | Non verificabile nell'ambiente | Browser: Site Unavailable; lettura web: 403 |
| StayOnline | Root pubblica accessibile | Non dimostra il comportamento di un link /l/ specifico |
| VidXGo | Non risolto | HTTP 403 riportato prima del media; nessuna causa dedotta |
| Compilazione Android | BLOCCATA | AGP 8.9.1 non disponibile nella cache; risoluzione online non disponibile |
| Media3 FIRST_FRAME | NON ESEGUITO | Occorre APK installato e una sorgente realmente accessibile |

## Come verificare sul TV

1. Applicare la patch alla 2.1.12 oppure usare i sorgenti completi. Compilare il workflow build-apk.
2. Nel LAB aprire AltaDefinizione: controllare una griglia con locandine delle schede e cercare un titolo. Il numero dipende dal markup pubblico ricevuto, fino a 100. Se il sito restituisce risultati soltanto tramite JavaScript, la ricerca non è certificata da questo adattatore.
3. Su CB01 selezionare una riga: verificare HTTP dell'intermediario, PUBLIC_LINK al dominio finale, ADAPTER/REDISPATCH, SOURCE VERIFIED, infine PLAYBACK FIRST_FRAME.
4. INTERMEDIARY_NO_PUBLIC_LINK indica che la pagina è stata letta ma non contiene una destinazione HTTPS supportata. HTTP_403 indica che la pagina è stata rifiutata. ACCESS_CHALLENGE indica una verifica interattiva. Nessuno di questi esiti va dichiarato come video risolto.

Per completare la verifica CB01 servono l'URL completo della scheda Animals e almeno un collegamento esatto a uprot.net o stayonline.pro; i nomi dei domini negli screenshot non identificano la risorsa. Non inviare credenziali o token privati.

## Analisi Veezie aggiuntiva

La separazione catalogo → host manager → resolver è confermata nei tre script estratti dall'APK. Il flusso StayOnline fa un POST AJAX e passa la destinazione al resolver scelto. Alcuni percorsi Uprot vengono convertiti da un servizio con chiave incorporata nell'app. La 2.1.13 non usa tali credenziali e non copia quelle funzioni. Implementa autonomamente solo destinazioni esplicitamente esposte nel markup e redirect normali. Non è ancora un'integrazione completa dei due servizi reali.
