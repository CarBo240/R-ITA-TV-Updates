(function(q,mode){
 function norm(s){return String(s||'').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/[^a-z0-9 ]/g,' ').replace(/ +/g,' ').trim()}
 var words=norm(q).split(' ').filter(Boolean),out=[],seen={},anime=/animeworld|animeunity|animesaturn/.test(location.host),seriesOnly=/guardaserie/.test(location.host);
 document.querySelectorAll('a[href]').forEach(function(a){
 var card=a.closest('article,.movie-item,.movie-card,.shortstory,.film-item,.card,.item,.anime,.film-poster,.poster,.post,.latest-anime-container')||a.parentElement;
 var im=a.querySelector('img')||(card&&card.querySelector('img'));if(!im)return;
 var h=card&&card.querySelector('h1,h2,h3,h4,h5,h6,.name,.title,.latest-anime-title'),t=(a.getAttribute('data-title')||a.getAttribute('title')||(h?h.textContent:'')||im.alt||a.textContent||'').trim();
 var u=a.href.split('#')[0];if(t.length<2||t.length>200||!/^https:/.test(u)||new URL(u).host!==location.host||u.split('?')[0]===location.href.split('?')[0]||/\/(?:tag|category|page|genre|filter)\/|[?&](?:s|q|story|keyword)=/.test(u)||!words.every(w=>norm(t).includes(w)))return;
 var context=t+' '+u+' '+(card?card.className+' '+card.textContent:'');var type=seriesOnly||/serie[ -]?tv|stagion|season|episod|\/tv-|category-series|\bTV\s*-\s*20/.test(context.toLowerCase())?'tv':anime?'unknown':'movie';if(mode===1&&type==='tv'||mode===2&&type==='movie')return;
 var image=im.getAttribute('data-src')||im.getAttribute('data-lazy-src')||im.currentSrc||im.src;try{image=new URL(image,location.href).href}catch(e){image=''};
 if(seen[u])return;seen[u]=true;out.push({title:t,url:u,image:image,type:type});
 });
 // StreamingCommunity and related Inertia catalogues carry metadata as JSON.
 document.querySelectorAll('[data-page]').forEach(function(el){try{var p=JSON.parse(el.getAttribute('data-page'));function walk(x,depth){if(!x||depth>8||out.length>150)return;if(Array.isArray(x)){x.forEach(v=>walk(v,depth+1));return}if(typeof x!=='object')return;var title=x.name||x.title;if(x.id&&title&&(x.slug||x.images||x.poster)){var type=/tv|series/.test(x.type||'')?'tv':x.type==='movie'?'movie':anime?'unknown':'movie';var u=location.origin+'/titles/'+x.id+'-'+(x.slug||'');if(words.every(w=>norm(title).includes(w))&&!seen[u]&&!(mode===1&&type==='tv'||mode===2&&type==='movie')){var image=x.poster||'';if(Array.isArray(x.images)){var i=x.images.find(i=>i.type==='poster');if(i)image=i.url||('https://cdn.'+location.hostname+'/images/'+i.filename)}seen[u]=true;out.push({title:title,url:u,image:image,type:type})}}Object.values(x).forEach(v=>walk(v,depth+1))}walk(p.props,0)}catch(e){}});

 if(/(^|\.)raiplay\.it$/.test(location.hostname)){
 function rai(x,d){if(!x||d>12)return;if(Array.isArray(x)){x.forEach(v=>rai(v,d+1));return}if(typeof x!=='object')return;var t=x.name||x.title,u=x.weblink||x.path_id;if(t&&typeof u==='string'&&/^\/(programmi|video)\//.test(u)){u=new URL(u.replace(/\.json$/,u.indexOf('/video/')===0?'.html':''),location.origin).href;var type=/serie|fiction/i.test(JSON.stringify(x.typologies||x.typology||''))||/serie/.test(location.pathname)?'tv':'movie';var imgs=x.images||{},im=imgs.portrait||imgs.portrait_logo||imgs.portrait43||imgs.landscape||x.image||'';if(typeof im==='object')im=im.url||im.path||'';if(!seen[u]&&words.every(w=>norm(t).includes(w))){seen[u]=true;out.push({title:t,url:u,image:im?new URL(im,location.origin).href:'',type:type})}}Object.values(x).forEach(v=>rai(v,d+1))}
 try{rai(window.WashiContext,0)}catch(e){}
 document.querySelectorAll('script').forEach(el=>{try{var m=el.textContent.match(/window\.WashiContext\s*=\s*([\s\S]*?)\s*;?\s*$/);if(m)rai(JSON.parse(m[1].replace(/;\s*$/,'')),0)}catch(e){}});
 document.querySelectorAll('[card],[data-json],[options]').forEach(el=>{['card','data-json','options'].forEach(a=>{try{rai(JSON.parse(el.getAttribute(a)),0)}catch(e){}})});
 document.querySelectorAll('[data-href],[data-jsonlink]').forEach(el=>{var path=el.getAttribute('data-href')||el.getAttribute('data-jsonlink'),t=el.getAttribute('data-title')||el.getAttribute('title'),img=el.querySelector('img');if(!path||!t||!/^\/(programmi|video)\//.test(path))return;var u=new URL(path.replace(/\.json$/,''),location.origin).href;if(!seen[u]&&words.every(w=>norm(t).includes(w))){seen[u]=true;out.push({title:t,url:u,image:img?new URL(img.getAttribute('data-src')||img.src,location.origin).href:'',type:/serie/.test(location.pathname)?'tv':'movie'})}});
 }
 return JSON.stringify(out.slice(0,/raiplay/.test(location.host)?600:150));
})
