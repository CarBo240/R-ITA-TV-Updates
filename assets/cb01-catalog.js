(function(q){
 function norm(s){return s.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/[^a-z0-9 ]/g,' ').replace(/ +/g,' ').trim()}
 var words=norm(q).split(' ').filter(Boolean),out=[],seen={};
 document.querySelectorAll('a[href]').forEach(function(a){
  var card=a.closest('article,.movie-item,.movie-card,.shortstory,.card,[data-title]')||a;
  var im=a.querySelector('img')||card.querySelector('img'),h=a.querySelector('h1,h2,h3,h4,h5,h6');
  var t=(a.getAttribute('data-title')||card.getAttribute('data-title')||(h?h.textContent:'')||(im?im.alt:'')||a.getAttribute('title')||a.textContent||'').trim().replace(/\s+streaming(?:\s+ita)?$/i,'');
  var u=a.href.split('#')[0];
  if(t.length<3||t.length>200||!/^https:/.test(u)||new URL(u).hostname!==location.hostname||u.split('?')[0]===location.href.split('?')[0]||/\/(tag|category|page)\/|[?&]s=/.test(u)||!words.every(function(w){return norm(t).indexOf(w)>=0}))return;
  if(!q&&!im)return;
  var image=im?(im.getAttribute('data-src')||im.getAttribute('data-lazy-src')||im.currentSrc||im.src):'';
  try{image=image?new URL(image,location.href).href:''}catch(e){image=''}
  if(seen[u]!==undefined){if(image&&!out[seen[u]].image)out[seen[u]].image=image;return}
  seen[u]=out.length;out.push({title:t,url:u,image:image});
 });
 return JSON.stringify(out.slice(0,80));
})
