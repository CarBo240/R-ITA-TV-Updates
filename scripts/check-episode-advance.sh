#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
task_classes=$(mktemp -d)
trap 'rm -rf "$task_classes"' EXIT
javac -encoding UTF-8 -d "$task_classes" src/it/carmine/streamplayer/EpisodeAdvanceGate.java src/it/carmine/streamplayer/EpisodeOrder.java tests/plain/it/carmine/streamplayer/EpisodeAdvanceGateCheck.java tests/plain/it/carmine/streamplayer/EpisodeOrderCheck.java
java -cp "$task_classes" it.carmine.streamplayer.EpisodeAdvanceGateCheck
java -cp "$task_classes" it.carmine.streamplayer.EpisodeOrderCheck
