(function(){
if(window.__ritaEventsUi)return;window.__ritaEventsUi=true;
window.__ritaSetAppFullscreen=function(full){
 document.querySelectorAll('.rita-event-full-path,.rita-event-full-target').forEach(el=>{el.classList.remove('rita-event-full-path','rita-event-full-target')});
 document.documentElement.classList.toggle('rita-event-fullscreen',full);
 if(!full)return;
 let best=null,area=0;
 document.querySelectorAll('video,iframe').forEach(el=>{const r=el.getBoundingClientRect(),a=r.width*r.height;if(a>area&&r.width>40&&r.height>40){best=el;area=a}});
 if(!best)return;
 best.classList.add('rita-event-full-target');
 for(let el=best;el&&el!==document.documentElement;el=el.parentElement)el.classList.add('rita-event-full-path');
};
const style=document.createElement('style');style.textContent='html.rita-event-fullscreen,html.rita-event-fullscreen body{margin:0!important;padding:0!important;width:100%!important;height:100%!important;overflow:hidden!important;background:#000!important}html.rita-event-fullscreen .rita-event-full-path{position:fixed!important;inset:0!important;width:100%!important;height:100%!important;max-width:none!important;max-height:none!important;margin:0!important;padding:0!important;border:0!important;background:#000!important}html.rita-event-fullscreen .rita-event-full-path>:not(.rita-event-full-path){display:none!important}html.rita-event-fullscreen .rita-event-full-target{z-index:2147483646!important;object-fit:contain!important}';
 (document.head||document.documentElement).appendChild(style);
})();
