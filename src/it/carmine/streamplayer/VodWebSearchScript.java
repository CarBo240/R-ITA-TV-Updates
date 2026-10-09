package it.carmine.streamplayer;
/** DOM access in the existing WebView; no JavaScript bridge exposed to sites. */
final class VodWebSearchScript {
 static String submit(String quotedQuery){return SUBMIT+"("+quotedQuery+")";}
 static String snapshot(){return SNAPSHOT;}
 private static final String SUBMIT="(function(q){\n var inputs=Array.from(document.querySelectorAll('input:not([type=\"hidden\"]):not([type=\"email\"]):not([type=\"password\"])'));\n var input=inputs.find(function(n){return /(?:search|story|keyword|cerca|^s$|^q$)/i.test([n.name,n.id,n.placeholder,n.getAttribute('aria-label')].join(' '));});\n if(!input)return false;\n var setter=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;\n setter.call(input,q);input.dispatchEvent(new Event('input',{bubbles:true}));input.dispatchEvent(new Event('change',{bubbles:true}));\n setTimeout(function(){var form=input.form;if(form){if(form.requestSubmit)form.requestSubmit();else form.dispatchEvent(new Event('submit',{bubbles:true,cancelable:true}));}else{input.dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',code:'Enter',keyCode:13,bubbles:true}));}},300);\n return true;\n})";
 private static final String SNAPSHOT="(function(){return JSON.stringify({url:location.href,html:document.documentElement?document.documentElement.outerHTML.slice(0,1000000):''});})()";
}
