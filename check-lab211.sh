#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
task_classes=$(mktemp -d)
trap 'rm -rf "$task_classes"' EXIT
javac -encoding UTF-8 -d "$task_classes" src/it/carmine/streamplayer/CbLinkTargets.java src/it/carmine/streamplayer/CbMixdropConfig.java src/it/carmine/streamplayer/WebMovieLinks.java src/it/carmine/streamplayer/ScPlaylistConfig.java src/it/carmine/streamplayer/lab211/PlaybackHeaders.java src/it/carmine/streamplayer/lab211/AnimeUnityCatalog.java src/it/carmine/streamplayer/lab211/AnimeCatalog.java src/it/carmine/streamplayer/lab211/StreamingItaCatalog.java src/it/carmine/streamplayer/lab211/ItalianCatalog.java src/it/carmine/streamplayer/lab211/LabPipeline.java src/it/carmine/streamplayer/lab211/LabVodPolicy.java tests/lab211/LabPipelineCheck.java tests/plain/it/carmine/streamplayer/Resolver234Check.java tests/plain/it/carmine/streamplayer/Resolver235Check.java
java -cp "$task_classes" it.carmine.streamplayer.lab211.LabPipelineCheck

java -cp "$task_classes" it.carmine.streamplayer.Resolver234Check

java -cp "$task_classes" it.carmine.streamplayer.Resolver235Check
