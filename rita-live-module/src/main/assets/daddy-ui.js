(() => {
 'use strict';
 if(window.__ritaDaddyUi)return;window.__ritaDaddyUi=true;
 const root=window===top;let played=false,retries=0,timer=0,started=Date.now(),errorSince=0;
 const log=(kind,extra={})=>{try{window.RitaDiag.postMessage(JSON.stringify({kind,...extra}));}catch(_){}};
 const children=()=>Array.from(document.querySelectorAll('iframe'));
 const relay=(kind)=>{const data={ritaDaddy:kind,sentAt:Date.now()};if(root)receive(data);else parent.postMessage(data,'*');};
 const cancel=()=>{clearTimeout(timer);timer=0;};
 function receive(data){
  if(data.sentAt<started)return;
  if(data.ritaDaddy==='playing'){played=true;cancel();return;}
  if(data.ritaDaddy!=='startup-error'||played||retries>=2||timer||Date.now()-started>60000)return;
  const frame=document.getElementById('media-player');if(!frame||frame.tagName!=='IFRAME')return;
  const src=frame.src;
  timer=setTimeout(()=>{timer=0;if(played||frame!==document.getElementById('media-player')||frame.src!==src)return;
   retries++;log('daddy.webStartupRetry',{attempt:retries});
   // Fresh wrapper, same selected source. Never reload a stream that has played.
   const next=new URL(src);next.searchParams.set('_rita_retry',String(Date.now()));frame.src=next.href;
  },4000);
 }
 window.addEventListener('message',e=>{if(!e.data||!['playing','startup-error'].includes(e.data.ritaDaddy)||!children().some(f=>f.contentWindow===e.source))return;if(root)receive(e.data);else parent.postMessage(e.data,'*');});
 const style=()=>{if(!document.documentElement)return;let s=document.getElementById('rita-daddy-ui-style');if(!s){s=document.createElement('style');s.id='rita-daddy-ui-style';s.textContent='iframe#thatframe{position:fixed!important;inset:0!important;width:100vw!important;height:100vh!important;border:0!important}html.rita-app-fullscreen,html.rita-app-fullscreen body{margin:0!important;width:100%!important;height:100%!important;overflow:hidden!important}html.rita-app-fullscreen .embed-container{position:fixed!important;inset:0!important;width:100vw!important;height:100vh!important;z-index:2147483646!important}html.rita-app-fullscreen .video-wrapper{width:100%!important;height:100%!important;flex:1!important;min-height:0!important}html.rita-app-fullscreen #media-player{width:100%!important;height:100%!important}html.rita-app-fullscreen .controls-bar{display:none!important}';(document.head||document.documentElement).appendChild(s);}};
 if(root)window.__ritaSetAppFullscreen=full=>{style();document.documentElement.classList.toggle('rita-app-fullscreen',!!full);};
 document.addEventListener('click',e=>{
  const t=e.target&&e.target.closest?e.target:null;if(!t)return;
  if(root&&t.closest('.player-btn')){cancel();played=false;retries=0;started=Date.now();}
  if(window.__ritaResolverOnly)return;
  if(t.closest('.fullscreen-btn,.jw-icon-fullscreen,[data-plyr="fullscreen"],.vjs-fullscreen-control,[data-fullscreen]')&&window.RitaDaddyUi){
   try{window.RitaDaddyUi.toggleFullscreen();e.preventDefault();e.stopImmediatePropagation();}catch(_){}
  }
 },true);
 const videos=new WeakSet();
 const tick=()=>{
  style();document.querySelectorAll('video').forEach(v=>{
   if(!videos.has(v)){videos.add(v);v.addEventListener('playing',()=>relay('playing'));v.addEventListener('error',()=>relay('startup-error'));}
   if(!v.paused&&v.readyState>=2)relay('playing');
  });
  const msg=document.getElementById('msg');
  if(msg&&msg.classList.contains('show')&&/network error|playback error|failed to (?:load|start)|stream unavailable/i.test(msg.textContent)){
   if(!errorSince)errorSince=Date.now();if(Date.now()-errorSince>=4000){relay('startup-error');errorSince=0;}
  }else errorSince=0;
 };
 tick();setInterval(tick,1000);
})();
