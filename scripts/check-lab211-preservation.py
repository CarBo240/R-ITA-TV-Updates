#!/usr/bin/env python3
"""Validate the exact 2.1.10 bytes retained in the 2.1.11 package."""
from pathlib import Path
import hashlib,json
root=Path(__file__).resolve().parent.parent
expected=json.loads((root/'docs/LAB211-PRESERVED-SHA256.json').read_text())
# The user explicitly authorized these four production changes on 2026-10-11.
allowed_file=root/'docs/LAB220-AUTHORIZED-PRODUCTION-CHANGES.json'
allowed=json.loads(allowed_file.read_text()) if allowed_file.exists() else {}
approved={'src/it/carmine/streamplayer/'+name for name in ('PlayerChrome.java','VodActivity.java','VodCatalogueActivity.java','VodPlayerActivity.java')}
if not set(allowed).issubset(approved):raise SystemExit('Unexpected production exception')
for path,entry in allowed.items():
    if entry['original_sha256']!=expected.get(path):raise SystemExit('Original hash mismatch: '+path)
errors=[]
for path,digest in expected.items():
    file=root/path
    if not file.is_file() or hashlib.sha256(file.read_bytes()).hexdigest()!=allowed.get(path,{}).get('sha256',digest):
        errors.append(path)
if errors:
    raise SystemExit('CHANGED OR MISSING: '+', '.join(errors))
print(f'PASS: {len(expected)-len(allowed)} original files byte-identical; {len(allowed)} explicitly authorized production changes match their reviewed hashes.')
