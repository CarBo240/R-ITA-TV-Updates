"use strict";
const ready = fetch(browser.runtime.getURL("rules.json")).then(response => response.json());
browser.webRequest.onBeforeRequest.addListener(
  details => ready.then(rules => ({cancel: RitaAdFilter.blocked(details.url, rules, details.type)})),
  {urls: ["http://*/*", "https://*/*"]},
  ["blocking"]
);
