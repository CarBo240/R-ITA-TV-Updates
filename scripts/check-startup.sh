#!/usr/bin/env bash
set -euo pipefail
mkdir -p diagnostics
apk_path=$(find apk -maxdepth 1 -name '*.apk' -print -quit)
if [[ -z "$apk_path" ]]; then echo 'APK mancante'; exit 1; fi
adb install -r "$apk_path"
adb logcat -c
adb shell am start -W -n it.carmine.streamplayer/it.carmine.streamplayer.MainActivity > diagnostics/start.txt
sleep 8
adb logcat -d > diagnostics/logcat.txt
adb shell uiautomator dump /sdcard/startup.xml >/dev/null
adb pull /sdcard/startup.xml diagnostics/startup.xml >/dev/null
adb exec-out screencap -p > diagnostics/startup.png
if ! adb shell pidof it.carmine.streamplayer >/dev/null; then
  echo 'Il processo si è chiuso dopo l’avvio'; exit 1
fi
if grep -q 'R. ITA TV — errore di avvio' diagnostics/startup.xml; then
  echo 'L’app ha mostrato un errore iniziale'; exit 1
fi
if grep -q 'Process: it.carmine.streamplayer,' diagnostics/logcat.txt; then
  echo 'È stato rilevato un crash durante l’avvio'; exit 1
fi
echo 'Avvio verificato: processo attivo, nessun errore iniziale rilevato.'
