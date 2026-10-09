"use strict";
globalThis.RitaAdFilter = {
  blocked(raw, rules, type) {
    try {
      const url = new URL(raw);
      const host = url.hostname.toLowerCase().replace(/\.$/, "");
      if (rules.domains.some(domain => host === domain || host.endsWith("." + domain))) return true;
      return type === "script" && rules.scriptPaths.some(path => url.pathname.toLowerCase().endsWith(path));
    } catch (_) { return false; }
  }
};
