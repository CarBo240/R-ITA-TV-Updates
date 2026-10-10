#!/usr/bin/env python3
"""Verify a production APK contains no embedded Gecko engine or extension."""
import sys,zipfile
for filename in sys.argv[1:]:
 with zipfile.ZipFile(filename) as apk:
  names=apk.namelist()
  forbidden=[n for n in names if n.startswith('assets/adblock/') or n.endswith(('/libxul.so','/libmozglue.so','/omni.ja'))]
  for name in names:
   if name.endswith('.dex') and b'org/mozilla/geckoview' in apk.read(name):forbidden.append(name+' (Mozilla classes)')
  if forbidden:raise SystemExit('Gecko ancora incorporato: '+', '.join(forbidden))
 print(filename+': Gecko assente')
