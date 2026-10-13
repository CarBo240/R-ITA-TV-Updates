(() => {
 'use strict';
 if(window.__ritaDaddyObserver)return;window.__ritaDaddyObserver=true;
 const seen=new WeakSet(),audio=new WeakMap();let handed=false;
 const send=(kind,v,extra={})=>{try{window.RitaDiag.postMessage(JSON.stringify(Object.assign({kind,frame:location.href,top:window===top},v?{paused:v.paused,muted:v.muted,volume:v.volume,currentTime:v.currentTime,width:v.videoWidth,height:v.videoHeight,readyState:v.readyState,error:v.error?{code:v.error.code}:null}:{},extra)));return true;}catch(e){return false;}};
 const stop=()=>{handed=true;document.querySelectorAll('video,audio').forEach(v=>{v.muted=true;v.pause();});Array.from(window.frames).forEach(f=>{try{f.postMessage('rita-native-handoff','*');}catch(e){}});};
 window.addEventListener('message',e=>{if(e.data==='rita-native-handoff'&&(e.source===window||e.source===parent))stop();});
 const attach=()=>document.querySelectorAll('video,audio').forEach(v=>{if(seen.has(v))return;seen.add(v);send(window.__ritaResolverOnly?'resolver.media.found':'media.found',v);['playing','waiting','error'].forEach(k=>v.addEventListener(k,()=>send((window.__ritaResolverOnly?'resolver.media.':'media.')+k,v)));});
 const tick=()=>{attach();document.querySelectorAll('video,audio').forEach(v=>{if(handed){v.muted=true;v.pause();return;}if(window.__ritaAutoAudio&&v.readyState>=2){let a=audio.get(v);if(!a){a={tries:0,confirmed:0,done:false};audio.set(v,a);}if(!a.done){if(!v.muted&&v.volume>0&&!v.paused){if(++a.confirmed>=2)a.done=true;}else if(a.tries<4){a.tries++;a.confirmed=0;const b=document.querySelector('button#unmute');if(b&&b.getClientRects().length)b.click();v.muted=false;if(v.volume===0)v.volume=1;if(v.paused){try{const p=v.play();if(p&&p.catch)p.catch(()=>{});}catch(e){}}}}}send(window.__ritaResolverOnly?'resolver.media.sample':'media.sample',v);});};
 new MutationObserver(attach).observe(document,{childList:true,subtree:true});attach();setInterval(tick,2500);
})();
