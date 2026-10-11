#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
task_classes=$(mktemp -d)
trap 'rm -rf "$task_classes"' EXIT
javac -encoding UTF-8 -d "$task_classes" src/it/carmine/streamplayer/lab211/LabPipeline.java src/it/carmine/streamplayer/lab211/LabVodPolicy.java tests/lab211/LabPipelineCheck.java
java -cp "$task_classes" it.carmine.streamplayer.lab211.LabPipelineCheck
