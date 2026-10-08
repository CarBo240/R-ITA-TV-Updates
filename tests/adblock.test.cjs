const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const base = path.resolve(__dirname, "../assets/adblock");
const rules = JSON.parse(fs.readFileSync(path.join(base, "rules.json")));
let listener;
const context = vm.createContext({URL, Promise, fetch: async () => ({json: async () => rules}), browser: {runtime: {getURL: name => name}, webRequest: {onBeforeRequest: {addListener(fn, filter, options) {listener = fn; assert.deepEqual(Array.from(options), ["blocking"]);}}}}});
for (const script of ["filter.js", "background.js"]) vm.runInContext(fs.readFileSync(path.join(base, script), "utf8"), context);
(async () => {
  for (const url of ["https://sub.doubleclick.net/ad.js", "https://chewsever.com/ad.js", "https://cdn.cloudfront.net/fmatrix.min.js"]) assert.equal((await listener({url, type: "script"})).cancel, true);
  for (const url of ["https://video.cloudfront.net/live.m3u8", "https://notdoubleclick.net/video.m3u8", "https://cdn.jsdelivr.net/npm/hls.js", "https://new-daddy.example/player/embed.php?id=664"]) assert.equal((await listener({url, type: "media"})).cancel, false);
  assert.equal((await listener({url: "https://video.example/fmatrix.min.js", type: "media"})).cancel, false);
  const manifest = JSON.parse(fs.readFileSync(path.join(base, "manifest.json")));
  assert.ok(manifest.content_scripts[0].all_frames);
  console.log("Ad filter: blocked ads; preserved video CDNs, media and player scripts.");
})().catch(error => {console.error(error); process.exitCode = 1;});
