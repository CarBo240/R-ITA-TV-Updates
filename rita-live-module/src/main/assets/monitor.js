(function(){
if(window.__ritaDiagInstalled)return;window.__ritaDiagInstalled=true;
const clean=u=>{try{let x=new URL(u,location.href);if(x.search)x.search='?REDACTED';x.hash='';x.username='';x.password='';return x.href}catch(e){return String(u||'').slice(0,1000)}};
const emit=(kind,data={})=>{try{if(window.__ritaResolverOnly===true&&kind.startsWith('media.'))kind='resolver.'+kind;const p={kind,frame:clean(location.href),top:window===window.top,timestamp:Date.now(),...data};if(window.RitaDiag&&window.RitaDiag.postMessage)window.RitaDiag.postMessage(JSON.stringify(p));else window.postMessage({ritaDiag:true,payload:p},'*')}catch(e){}};
emit('page.capabilities',{ua:navigator.userAgent,mediaSource:typeof MediaSource!=='undefined',webRTC:typeof RTCPeerConnection!=='undefined',secureContext:window.isSecureContext,cookiesEnabled:navigator.cookieEnabled,eme:typeof navigator.requestMediaKeySystemAccess==='function',mseH264:typeof MediaSource!=='undefined'&&MediaSource.isTypeSupported('video/mp4; codecs="avc1.42E01E"'),h264:document.createElement('video').canPlayType('video/mp4; codecs="avc1.42E01E"'),hls:document.createElement('video').canPlayType('application/vnd.apple.mpegurl')});
window.addEventListener('error',e=>emit('page.error',{message:e.message||'resource load error',source:clean(e.filename||e.target?.src||e.target?.href),line:e.lineno}),true);
window.addEventListener('unhandledrejection',e=>emit('page.rejection',{message:String(e.reason).slice(0,1200)}));
window.addEventListener('securitypolicyviolation',e=>emit('page.csp',{directive:e.violatedDirective,blocked:clean(e.blockedURI)}));
const fetch0=window.fetch;if(fetch0)window.fetch=function(...a){const url=clean(a[0]?.url||a[0]);emit('fetch.request',{url});return fetch0.apply(this,a).then(r=>{emit('fetch.response',{url:clean(r.url),status:r.status,type:r.type});return r},e=>{emit('fetch.error',{url,message:String(e)});throw e})};
const open0=XMLHttpRequest.prototype.open,send0=XMLHttpRequest.prototype.send;
XMLHttpRequest.prototype.open=function(m,u,...a){this.__ritaURL=clean(u);return open0.call(this,m,u,...a)};
XMLHttpRequest.prototype.send=function(...a){this.addEventListener('loadend',()=>emit('xhr.result',{url:clean(this.responseURL||this.__ritaURL),status:this.status}));return send0.apply(this,a)};
for(const level of ['error','warn']){const orig=console[level];console[level]=function(...a){emit('console.'+level,{message:a.map(x=>String(x)).join(' ').slice(0,1500)});return orig.apply(console,a)}}
const seen=new WeakSet();let sample=0;
let nativeHandoff=false;
function resolverMedia(v){if(window.__ritaResolverOnly!==true)return;v.muted=true;if(nativeHandoff&&!v.paused)v.pause();}
window.addEventListener('message',event=>{if(window.__ritaResolverOnly!==true||event.data!=='rita-native-handoff'||(event.source!==window&&event.source!==window.parent))return;
 nativeHandoff=true;document.querySelectorAll('video,audio').forEach(resolverMedia);
 document.querySelectorAll('iframe').forEach(frame=>{try{frame.contentWindow.postMessage('rita-native-handoff','*')}catch(e){}});
 emit('internal.webMediaStopped',{pageSessionRetained:true});
});

function videoInfo(v){let q;try{q=v.getVideoPlaybackQuality?.()}catch(e){}return {src:clean(v.currentSrc||v.src),paused:v.paused,muted:v.muted,volume:v.volume,currentTime:v.currentTime,readyState:v.readyState,networkState:v.networkState,width:v.videoWidth,height:v.videoHeight,error:v.error?{code:v.error.code,message:v.error.message}:null,buffered:Array.from({length:v.buffered.length},(_,i)=>[v.buffered.start(i),v.buffered.end(i)]).slice(-5),totalFrames:q?.totalVideoFrames,droppedFrames:q?.droppedVideoFrames}}
// BEGIN AUTO AUDIO
const audioStates=new WeakMap();
const audioFrame=window.__ritaAutoAudio===true&&location.protocol==='https:'&&location.pathname.startsWith('/e/')&&(location.hostname.endsWith('.dyntheme.net')||location.hostname==='missflight.net'||location.hostname.endsWith('.missflight.net'));
function autoAudio(v){
 if(!audioFrame||v.readyState<2||v.error)return;
 let state=audioStates.get(v);if(!state){state={attempts:0,last:0,done:false,stable:0};audioStates.set(v,state)}
 if(state.done)return;
 if(!v.paused&&!v.muted&&v.volume>0&&v.currentTime>0){
  if(++state.stable>=2){state.done=true;emit('audio.auto.success',{attempts:state.attempts,muted:v.muted,volume:v.volume,outputVerified:false})}return;
 }
 state.stable=0;
 if(state.attempts>=4){state.done=true;emit('audio.auto.manualRequired',{reason:'Player still paused or muted after four attempts',...videoInfo(v)});return;}
 const now=Date.now();if(state.attempts>0&&now-state.last<2500)return;
 state.last=now;state.attempts++;
 try{
  v.defaultMuted=false;v.removeAttribute('muted');v.muted=false;if(v.volume===0)v.volume=1;
  const controls=Array.from(document.querySelectorAll('button,a,[role="button"],span,div')).slice(0,500);
  const target=controls.find(el=>{const text=(el.innerText||el.textContent||'').trim().replace(/\s+/g,' ');const rect=el.getBoundingClientRect();return /^(?:click|tap)(?: here)? to unmute[.!]?$/i.test(text)&&rect.width>0&&rect.height>0;});
  if(target){target.click();emit('audio.auto.controlClick',{label:'Click to unmute',synthetic:true})}
  // The site handler may have changed the media state again.
  v.muted=false;if(v.volume===0)v.volume=1;
  emit('audio.auto.attempt',{attempt:state.attempts,controlFound:!!target,...videoInfo(v)});
  if(v.paused){const promise=v.play();if(promise&&promise.catch)promise.catch(e=>emit('audio.auto.playRejected',{name:e.name,message:String(e.message).slice(0,300),manualGestureMayBeRequired:e.name==='NotAllowedError'}));}
 }catch(e){emit('audio.auto.error',{name:e.name,message:String(e.message).slice(0,300)})}
}
// END AUTO AUDIO
function scan(){document.querySelectorAll('video,audio').forEach(v=>{if(!seen.has(v)){seen.add(v);emit('media.found',videoInfo(v));for(const ev of ['loadstart','loadedmetadata','canplay','playing','pause','waiting','stalled','error','ended','volumechange','seeking','encrypted','waitingforkey'])v.addEventListener(ev,()=>{resolverMedia(v);emit('media.'+ev,videoInfo(v));if(ev==='playing'||ev==='canplay')autoAudio(v)});}resolverMedia(v);autoAudio(v);if(sample%5===0)emit('media.sample',videoInfo(v));});sample++}
let frames=new Set(),lastStatus='';const timer=setInterval(()=>{scan();const text=document.body?.innerText||'';const found=['This stream is domain protected','Not allowed','STREAM IS OFFLINE','CLICK HERE TO UNMUTE'].filter(x=>text.includes(x)).join(' | ');if(found&&found!==lastStatus){lastStatus=found;emit('dom.status',{message:found})}document.querySelectorAll('iframe').forEach(f=>{const url=clean(f.src);if(!frames.has(url)){frames.add(url);emit('iframe',{url,allow:f.allow,sandbox:String(f.sandbox)})}})},1000);
window.addEventListener('pagehide',()=>clearInterval(timer));
try{new PerformanceObserver(list=>list.getEntries().slice(-100).forEach(e=>emit('resource',{url:clean(e.name),initiator:e.initiatorType,durationMs:Math.round(e.duration),transferSize:e.transferSize,responseStatus:e.responseStatus}))).observe({type:'resource',buffered:true})}catch(e){}
emit('page.hooksReady',{coverage:'page JavaScript hooks installed; CORS and cross-origin timing can limit observations'});
})();
