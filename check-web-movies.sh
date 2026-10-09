#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
task_classes=$(mktemp -d)
trap 'rm -rf "$task_classes"' EXIT
java com.sun.tools.javac.Main -d "$task_classes" src/it/carmine/streamplayer/WebMovieLinks.java tests/plain/it/carmine/streamplayer/WebMovieLinksCheck.java
java -cp "$task_classes" it.carmine.streamplayer.WebMovieLinksCheck
