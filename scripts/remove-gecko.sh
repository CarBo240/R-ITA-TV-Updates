#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
rm -f src/it/carmine/streamplayer/GeckoEventActivity.java src/it/carmine/streamplayer/GeckoDns.java src/it/carmine/streamplayer/EventsActivity.java src/it/carmine/streamplayer/DaddyLiveActivity.java src/it/carmine/streamplayer/EventSource.java src/it/carmine/streamplayer/EventSettings.java src/it/carmine/streamplayer/SportIcons.java src/it/carmine/streamplayer/EventFilterSpinner.java src/it/carmine/streamplayer/WebAdPolicy.java
rm -rf assets/adblock
rm -f assets/team_logos.json
rm -f tests/it/carmine/streamplayer/EventSourceTest.java tests/it/carmine/streamplayer/GeckoNavigationTest.java tests/it/carmine/streamplayer/EventsActivityTest.java tests/it/carmine/streamplayer/EventSettingsTest.java tests/it/carmine/streamplayer/GeckoRemoteInputTest.java tests/it/carmine/streamplayer/DaddyLiveTest.java tests/it/carmine/streamplayer/DaddyLiveSettingsTest.java
rm -f tests/adblock.test.cjs tests/adblock-content.test.cjs tests/fullscreen.test.cjs
