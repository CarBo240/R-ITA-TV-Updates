#!/usr/bin/env python3
"""Reject incomplete replacement uploads before building an APK with an old path."""
from pathlib import Path
import hashlib,json,re
root=Path(__file__).resolve().parents[1]
data=json.loads((root/'vendor/streamflix/IMPORT-MANIFEST.json').read_text())
for item in data['files']:
 path=root/'vendor/streamflix'/item['path']
 if not path.exists() or hashlib.sha256(path.read_bytes()).hexdigest()!=item['imported_sha256']:
  raise SystemExit('Importazione Streamflix incompleta o modificata: '+item['path'])
required={
 'src/it/carmine/streamplayer/CbMixdropResolver.java':['StreamflixBridge.resolve'],
 'src/it/carmine/streamplayer/LabScResolver.java':['StreamflixBridge.resolve'],
 'src/it/carmine/streamplayer/lab211/LabHttp.java':['StreamflixBridge.resolve'],
 'src/it/carmine/streamplayer/ExperimentalHubActivity.java':['AnimeWorld · anime','AnimeUnity · anime','AnimeSaturn · anime','StreamingIta · DooPlayer','BuildConfig.VERSION_NAME'],
 'src/it/carmine/streamplayer/StreamflixBridge.kt':['selected.extract(url)','video.extraBuffering','headers,true'],
 'vendor/streamflix/src/main/java/com/streamflixreborn/streamflix/utils/NetworkClient.kt':['CookieManager.getInstance()','VodNetwork.client()'],
 'build.gradle':['vendor/streamflix/src/main/java','kotlin-gradle-plugin:2.2.10',"versionName '2.1.37'",'versionCode 95'],
}
for name,tokens in required.items():
 text=(root/name).read_text()
 for token in tokens:
  if token not in text:raise SystemExit('Sorgente 2.1.37 mancante o vecchio: '+name)
print('PASS: imported Streamflix source hashes, production/LAB routes and release 2.1.37')
