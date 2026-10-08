const assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm');
const code=fs.readFileSync(require('node:path').resolve(__dirname,'../assets/adblock/fullscreen.js'),'utf8');
function fixture(path='/player/embed.php',native=true){
 const listeners={},messages=[];let receiver;const styles=new Map();
 const parent={parentElement:null,getAttribute:()=>null,removeAttribute(){this.restored=true;},style:{setProperty(){}}};
 const frame={parentElement:parent,src:'https://player.example/selected-4',getAttribute:()=> 'width:80%',setAttribute(k,v){this.restored=v;},removeAttribute(){},style:{setProperty(k,v){styles.set(k,v);}},getBoundingClientRect:()=>({width:1000,height:560})};
 if(native)frame.requestFullscreen=()=>{frame.requests=(frame.requests||0)+1;return Promise.reject(new Error('No activation'));};
 const document={querySelectorAll:()=>[frame],fullscreenElement:null,addEventListener:(key,fn)=>listeners[key]=fn,exitFullscreen:()=>Promise.resolve()};
 const window={};window.top=window;
 vm.runInNewContext(code,{window,document,location:{pathname:path},browser:{runtime:{connectNative:()=>({postMessage:m=>messages.push(m),onMessage:{addListener:fn=>receiver=fn},onDisconnect:{addListener(){}}})}}});
 return {listeners,messages,frame,parent,styles,command:c=>receiver({command:c})};
}
(async()=>{
 const f=fixture();const click=label=>{let stopped=false;f.listeners.click({target:{closest:()=>({textContent:label})},isTrusted:true,preventDefault(){stopped=true;},stopImmediatePropagation(){}});return stopped;};
 assert.equal(click('Player - 4'),false);assert.equal(click('UNMUTE'),false);
 assert.equal(click('⛶ Fullscreen'),true);await new Promise(setImmediate);
 assert.equal(f.frame.requests,1);assert.equal(f.styles.get('width'),'100vw');assert.equal(f.frame.src,'https://player.example/selected-4');assert.equal(f.messages.at(-1).fullscreen,true);
 f.command('exit');assert.equal(f.frame.restored,'width:80%');assert.equal(f.parent.restored,true);assert.equal(f.messages.at(-1).fullscreen,false);
 f.command('toggle');assert.equal(f.messages.at(-1).fullscreen,true);assert.equal(f.frame.requests,1);f.listeners.keydown({key:'Escape'});assert.equal(f.messages.at(-1).fullscreen,false);
 const unsupported=fixture('/player/embed.php',false);unsupported.command('toggle');assert.equal(unsupported.styles.get('height'),'100vh');unsupported.command('exit');
 assert.deepEqual(Object.keys(fixture('/other').listeners),[]);
 console.log('Daddy fullscreen: trusted click, rejected native request, menu fallback, restoration; other controls preserved.');
})().catch(e=>{console.error(e);process.exitCode=1;});
