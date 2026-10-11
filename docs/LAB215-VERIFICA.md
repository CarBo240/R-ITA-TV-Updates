# Verifica 2.1.15 LAB

| Passaggio | Esito |
| --- | --- |
| Nucleo Java 17 | Compilato, 125 controlli PASS |
| StayOnline originale → redirect root → POST → MixDrop embed → MP4 verificato | PASS su fixture; link reale dello screenshot non disponibile |
| Header Refresh → resolver → media verificato | PASS su fixture; HTTPS e rifiuti controllati |
| MaxStream script cccc → bytes MP4 | PASS su fixture; URL preservato senza aggiunte |
| Sintassi Java modificati | PASS, non equivale alla compilazione Android |
| File originali protetti | 446 hash identici alla base 2.1.10 |
| Riproduzione Media3 sul dispositivo | Non verificata; nessun FIRST_FRAME documentato |
| VidXGo 403 e Uprot senza link pubblico | Ancora irrisolti |

## Prova da effettuare sul dispositivo

1. Applicare la patch alla 2.1.14, oppure compilare i sorgenti completi, mantenendo la propria firma Android.
2. Sulla scheda CB01 tenere premuta la riga MixDrop e copiare il collegamento StayOnline completo. Il solo dominio stayonline.pro non permette di riprodurre il problema.
3. Aprire normalmente la fonte. Diagnostica: STAYONLINE_ID_SOURCE → STAYONLINE_PUBLIC_POST → PUBLIC_API_LINK → ADAPTER MixDrop → eventuale HOST_EMBED → SOURCE VERIFIED → PLAYBACK FIRST_FRAME. L'ultima fase richiede il dispositivo; i test locali si fermano all'ingresso Media3 verificato.
4. Se STAYONLINE_NO_ID persiste, fornire il collegamento selezionato e la diagnostica. Se compare un conflitto, non vengono effettuati POST con ID scelti arbitrariamente.
5. Usare separatamente Prova URL host sul collegamento finale MixDrop già fornito per distinguere il resolver dall'intermediario. Un successo diretto non certifica il passaggio StayOnline.

Streaming Community e VixSrc mantengono i percorsi ereditati.

## Compilazione Android

Tentativo Gradle 8.11.1/JDK 17 compileDebugJavaWithJavac: fallimento nella configurazione, prima del compilatore Android. AGP 8.9.1 non disponibile nella cache offline; Google Maven e Maven Central restituiscono Network is unreachable nel tentativo online. Nessun APK 2.1.15 prodotto. Il workflow GitHub mantiene la compilazione e la firma originali ed esegue prima i 125 controlli LAB.
