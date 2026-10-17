const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const path=require('node:path');
const assets=path.resolve(__dirname,'../../rita-live-module/src/main/assets');
const script=fs.readFileSync(path.join(assets,'monitor.js'),'utf8');
const definition=script.match(/function resolverMedia\(v\)\{[^\n]+\}/)[0];
function run(eventMode,handoff,resolver=true){const state={window:{__ritaResolverOnly:resolver,__ritaMuteAfterHandoff:eventMode},nativeHandoff:handoff};vm.createContext(state);vm.runInContext(definition,state);const video={muted:false,paused:false,pause(){this.paused=true;}};state.resolverMedia(video);return video;}
assert.equal(run(true,false).muted,false,'Eventi: audio consentito prima del passaggio al player interno');
const transitioned=run(true,true);assert.equal(transitioned.muted,true);assert.equal(transitioned.paused,true);
assert.equal(run(false,false).muted,true,'Daddy: comportamento precedente conservato');
assert.equal(run(true,false,false).muted,false,'WebView: nessun muto imposto');
const full=fs.readFileSync(path.join(assets,'events-ui.js'),'utf8');
function element(parent,w,h){const classes=new Set();return {parentElement:parent,classes,classList:{add(...a){a.forEach(x=>classes.add(x));},remove(...a){a.forEach(x=>classes.delete(x));},toggle(x,v){v?classes.add(x):classes.delete(x);}},getBoundingClientRect(){return {width:w,height:h};}};}
const root=element(null,1000,600),body=element(root,1000,600),frame=element(body,800,450);
const doc={documentElement:root,head:{appendChild(){}},createElement(){return {};},querySelectorAll(selector){return selector==='video,iframe'?[frame]:[body,frame].filter(x=>x.classes.size);}};
const context={window:{},document:doc};vm.createContext(context);vm.runInContext(full,context);
context.window.__ritaSetAppFullscreen(true);assert(root.classes.has('rita-event-fullscreen'));assert(frame.classes.has('rita-event-full-target'));
context.window.__ritaSetAppFullscreen(false);assert(!root.classes.has('rita-event-fullscreen'));assert.equal(frame.classes.size,0);
console.log('PASS: audio Eventi, handoff, Daddy invariato e fullscreen reversibile');
