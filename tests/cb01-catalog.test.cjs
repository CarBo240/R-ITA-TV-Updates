const assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm');
const navigation=fs.readFileSync(__dirname+'/../src/it/carmine/streamplayer/CbCatalogNavigation.java','utf8').match(/LINKS_JS\s*=\s*"""([\s\S]*?)"""/)[1];
const links=[{textContent:' Serie TV ',href:'https://cb01.test/serie-tv/?section=all',querySelector:()=>({})},{textContent:'Film',href:'https://cb01.test/'},{textContent:'Pubblicità',href:'https://ads.test/'}];
const routes=JSON.parse(vm.runInNewContext(navigation,{document:{querySelectorAll:()=>links}}));
assert.deepEqual(routes,['https://cb01.test/serie-tv/?section=all','https://cb01.test/']);
function card(title,url,context){const img={alt:title,src:'https://images.test/poster.jpg',currentSrc:'',getAttribute:()=>null};const container={className:'post',textContent:context,querySelector:selector=>selector==='img'?img:{textContent:title}};return {href:url,textContent:title,parentElement:container,closest:()=>container,querySelector:selector=>selector==='img'?img:null,getAttribute:()=>null};}
const catalog=fs.readFileSync(__dirname+'/../assets/cb01-catalog.js','utf8');
const location={href:'https://cb01.test/',host:'cb01.test',hostname:'cb01.test',origin:'https://cb01.test',pathname:'/'};
const document={querySelectorAll:selector=>selector==='a[href]'?[card('Reacher [TV - 2025]','https://cb01.test/reacher/','Reacher [TV - 2025]'),card('Film Uno','https://cb01.test/film-uno/','Film Uno')]:[]};
const parse=vm.runInNewContext(catalog,{location,document,URL});
assert.equal(JSON.parse(parse('Reacher',0))[0].type,'tv');
assert.deepEqual(JSON.parse(parse('Reacher',1)),[]);
assert.equal(JSON.parse(parse('Reacher',2)).length,1);
assert.equal(JSON.parse(parse('Film Uno',1))[0].type,'movie');
console.log('CB01: series section with icon and query preserved; movie/series search fixtures passed.');
