"use strict";
// Only the Daddy outer player page: leave iframe playback, Unmute and source selection alone.
if (window === window.top && /\/player\/embed\.php$/.test(location.pathname)) {
  let expanded = null, savedStyle = null, ancestors = [], port = null;
  function report() { if (port) port.postMessage({fullscreen: !!(expanded || document.fullscreenElement)}); }
  function player() {
    return Array.from(document.querySelectorAll('iframe')).filter(frame => {
      const r = frame.getBoundingClientRect(); return r.width > 100 && r.height > 100;
    }).sort((a,b) => b.getBoundingClientRect().width*b.getBoundingClientRect().height-a.getBoundingClientRect().width*a.getBoundingClientRect().height)[0];
  }
  function restore() {
    if (expanded) {
      if (savedStyle === null) expanded.removeAttribute('style'); else expanded.setAttribute('style', savedStyle);
      ancestors.forEach(([node, style]) => { if (style === null) node.removeAttribute('style'); else node.setAttribute('style',style); });
      expanded = null; ancestors = []; report();
    }
  }
  function fill(frame) {
    if (expanded === frame) return;
    restore(); expanded = frame; savedStyle = frame.getAttribute('style');
    for (let node = frame.parentElement; node; node = node.parentElement) {
      ancestors.push([node,node.getAttribute('style')]);
      for (const [key,value] of Object.entries({transform:'none',filter:'none',perspective:'none',overflow:'visible',contain:'none',isolation:'auto',position:'static',zIndex:'auto'})) node.style.setProperty(key.replace(/[A-Z]/g,c=>'-'+c.toLowerCase()),value,'important');
    }
    for (const [key,value] of Object.entries({position:'fixed',left:'0',top:'0',width:'100vw',height:'100vh',maxWidth:'none',maxHeight:'none',margin:'0',border:'0',zIndex:'2147483647',background:'#000'})) frame.style.setProperty(key.replace(/[A-Z]/g,c=>'-'+c.toLowerCase()),value,'important');
    report();
  }
  function exit() { restore(); if(document.fullscreenElement) document.exitFullscreen().catch(()=>{}); }
  function toggle(trusted) {
    if (expanded || document.fullscreenElement) { exit(); return; }
    const frame = player(); if (!frame) return;
    // Expands the existing iframe; it never reloads the stream or loses the selected player.
    fill(frame);
    if (trusted && frame.requestFullscreen) {
      try { const result = frame.requestFullscreen(); if (result && result.catch) result.catch(()=>{}); } catch (_) {}
    }
  }
  document.addEventListener('click', event => {
    const button = event.target.closest('button, a, [role="button"]');
    if (!button || !/^(?:⛶|\s)*fullscreen\s*$/i.test(button.textContent.trim()) || !player()) return;
    event.preventDefault(); event.stopImmediatePropagation(); toggle(event.isTrusted);
  },true);
  document.addEventListener('fullscreenchange',()=>{ if(!document.fullscreenElement) restore(); report(); });
  document.addEventListener('keydown',event=>{if(event.key==='Escape')exit();});
  try {
    port = browser.runtime.connectNative('ritaFullscreen');
    port.onMessage.addListener(message=>{if(message.command==='exit')exit();else if(message.command==='toggle')toggle(false);});
    port.onDisconnect.addListener(()=>{port=null;}); report();
  } catch (_) {}
}
