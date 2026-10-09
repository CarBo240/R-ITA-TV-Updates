const assert = require('node:assert/strict');
const fs = require('node:fs');const vm = require('node:vm');const path = require('node:path');
const base = path.resolve(__dirname,'../assets/adblock');const rules=JSON.parse(fs.readFileSync(path.join(base,'rules.json')));
let observer, timers=[];
class Node {
 constructor(kind,src=''){this.nodeType=1;this.kind=kind;this.src=src;this.href=src;this.children=[];this.isConnected=true;this.parentElement=null;this.scans=0;this.removed=false;}
 matches(selector){return selector==='ins.adsbygoogle, [data-ad-client]'?this.kind==='ad':['ad','iframe','a'].includes(this.kind);}
 querySelectorAll(){this.scans++;let all=[];for(const c of this.children){if(c.matches('candidates'))all.push(c);all.push(...c.querySelectorAll());}return all;}
 remove(){this.removed=true;this.isConnected=false;}
 add(c){c.parentElement=this;this.children.push(c);return c;}
}
const document=new Node('document');document.nodeType=9;
const initial=document.add(new Node('iframe','https://doubleclick.net/ad'));
const media=document.add(new Node('iframe','https://player.example/live'));
const context=vm.createContext({URL,Set,fetch:async()=>({json:async()=>rules}),browser:{runtime:{getURL:n=>n}},document,MutationObserver:class{constructor(fn){observer=fn;}observe(){}},setTimeout:fn=>timers.push(fn)});
for(const file of ['filter.js','content.js'])vm.runInContext(fs.readFileSync(path.join(base,file),'utf8'),context);
(async()=>{
 await new Promise(setImmediate);assert.equal(initial.removed,true);assert.equal(media.removed,false);assert.equal(document.scans,1);
 const root=document.add(new Node('div'));const child=root.add(new Node('iframe','https://doubleclick.net/new-ad'));
 observer([{type:'childList',addedNodes:[root,child]}]);assert.equal(timers.length,1);timers.shift()();assert.equal(child.removed,true);assert.equal(document.scans,1);assert.equal(root.scans,1);assert.equal(child.scans,1);
 observer([{type:'childList',addedNodes:[],removedNodes:[child]}]);assert.equal(timers.length,0);
 media.src='https://doubleclick.net/replaced-ad';observer([{type:'attributes',target:media}]);timers.shift()();assert.equal(media.removed,true);assert.equal(document.scans,1);
 console.log('DOM ad filter: initial scan once; changed subtrees only; video preserved.');
})().catch(e=>{console.error(e);process.exitCode=1;});
