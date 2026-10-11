#!/usr/bin/env python3
"""Validate the exact 2.1.10 bytes retained in the 2.1.11 package."""
from pathlib import Path
import hashlib,json
root=Path(__file__).resolve().parent.parent
expected=json.loads((root/'docs/LAB211-PRESERVED-SHA256.json').read_text())
errors=[]
for path,digest in expected.items():
    file=root/path
    if not file.is_file() or hashlib.sha256(file.read_bytes()).hexdigest()!=digest:
        errors.append(path)
if errors:
    raise SystemExit('CHANGED OR MISSING: '+', '.join(errors))
print(f'PASS: {len(expected)} original files byte-identical to attached 2.1.10.')
