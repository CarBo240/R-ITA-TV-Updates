const assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm');
const script=fs.readFileSync(__dirname+'/../assets/rai-catalog.js','utf8');
const location={host:'www.raiplay.it',hostname:'www.raiplay.it',origin:'https://www.raiplay.it',href:'https://www.raiplay.it/',pathname:'/'};
function card(name,path){const im={alt:name,src:'/placeholder.png',getAttribute:k=>k==='data-src'?'/logo.png':null};const parent={className:'card',textContent:name,querySelector:k=>k==='img'?im:{textContent:name}};return {href:location.origin+path,textContent:name,closest:()=>parent,parentElement:parent,querySelector:k=>k==='img'?im:null,getAttribute:()=>null};}
const document={querySelectorAll:k=>k==='a[href]'?[card('Rai 1','/dirette/rai1'),card('Rai 2','/dirette/rai2'),card('Film Uno','/programmi/film-uno'),card('Puntata','/video/2026/puntata.html')]:[]};
const parse=vm.runInNewContext(script,{location,document,URL,window:{}});
assert.deepEqual(JSON.parse(parse('',0)).map(x=>x.title),['Film Uno','Puntata']);
assert.deepEqual(JSON.parse(parse('',3)).map(x=>x.title),['Rai 1','Rai 2']);
assert.deepEqual(JSON.parse(parse('Rai 2',3)).map(x=>x.title),['Rai 2']);
assert.equal(JSON.parse(parse('',3))[0].image,'https://www.raiplay.it/logo.png');
assert.deepEqual(JSON.parse(parse('Rai 1',0)),[]);
console.log('Rai: on-demand excludes live cards; live request/search only returns channels, with absolute logos.');
