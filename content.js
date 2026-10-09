"use strict";
fetch(browser.runtime.getURL("rules.json")).then(response => response.json()).then(rules => {
  const selector = "ins.adsbygoogle, [data-ad-client], iframe[src], a[href]";
  const dirty = new Set(); let pending = false;
  function removeAd(node) {
    if (node.matches("ins.adsbygoogle, [data-ad-client]") || RitaAdFilter.blocked(node.src || node.href, rules, "sub_frame")) node.remove();
  }
  function clean(root) {
    if (root.nodeType === 1 && root.matches(selector)) removeAd(root);
    if (root.querySelectorAll) root.querySelectorAll(selector).forEach(removeAd);
  }
  function flush() {
    pending = false; const batch = Array.from(dirty); dirty.clear();
    // Scan only attached changed subtrees, once even when several nested nodes changed.
    batch.forEach(node => { if (!node.isConnected) return; for (let parent = node.parentElement; parent; parent = parent.parentElement) if (dirtyRoots.has(parent)) return; clean(node); });
  }
  let dirtyRoots;
  function schedule(records) {
    records.forEach(record => {
      if (record.type === "attributes") dirty.add(record.target);
      else record.addedNodes.forEach(node => { if (node.nodeType === 1) dirty.add(node); });
    });
    if (dirty.size && !pending) { pending = true; setTimeout(() => { dirtyRoots = new Set(dirty); flush(); dirtyRoots = null; }, 150); }
  }
  clean(document);
  const observer = new MutationObserver(schedule);
  observer.observe(document, {childList: true, subtree: true, attributes: true, attributeFilter: ["src", "href", "data-ad-client"]});
});
