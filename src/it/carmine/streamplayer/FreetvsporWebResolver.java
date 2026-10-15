package it.carmine.streamplayer;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.webkit.*;
import android.widget.FrameLayout;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Executes the selected Daddy provider and hands the observed HLS session to the main player. */
final class FreetvsporWebResolver {
 interface Callback {void resolved(String url,String referer,String cookie);void failed(Exception error);}
 private static final String UA="Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
 private final MainActivity owner;private final FrameLayout parent;private final String target,selector,selectedHtml;private final Callback callback;
 private final Handler main=new Handler(Looper.getMainLooper());private final AtomicBoolean delivered=new AtomicBoolean();
 private WebView web;private int attempt;private boolean selectorPrimed,serveSelected,closed;
 private final Runnable timeout=()->{if(closed||delivered.get())return;if(attempt<3)loadSelected();else finishError(new java.io.IOException("FreeTVSpor non ha generato il flusso dopo 3 tentativi"));};

 FreetvsporWebResolver(MainActivity a,FrameLayout p,String t,String s,String h,Callback c){owner=a;parent=p;target=t;selector=s;selectedHtml=h;callback=c;}

 @SuppressLint({"SetJavaScriptEnabled","AddJavascriptInterface"}) void start(){
  CookieManager cookies=CookieManager.getInstance();cookies.setAcceptCookie(true);
  web=new WebView(owner);web.setBackgroundColor(Color.TRANSPARENT);web.setAlpha(.01f);web.setFocusable(false);web.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
  parent.addView(web,new FrameLayout.LayoutParams(2,2,Gravity.BOTTOM|Gravity.RIGHT));
  WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setCacheMode(WebSettings.LOAD_NO_CACHE);settings.setMediaPlaybackRequiresUserGesture(false);settings.setUserAgentString(UA);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
  cookies.setAcceptThirdPartyCookies(web,true);
  web.addJavascriptInterface(new Object(){@JavascriptInterface public void media(String url,String referer){main.post(()->capture(url,referer));}},"RitaLive");
  web.setWebChromeClient(new WebChromeClient(){@Override public boolean onCreateWindow(WebView v,boolean dialog,boolean gesture,Message result){return false;}});
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){String url=request.getUrl().toString();if(serveSelected&&request.isForMainFrame()&&same(url,selector))return new WebResourceResponse("text/html","UTF-8",200,"OK",Collections.emptyMap(),new ByteArrayInputStream(selectedHtml.getBytes(StandardCharsets.UTF_8)));if(serveSelected&&hls(url)){String ref=header(request.getRequestHeaders(),"Referer");main.post(()->capture(url,ref));}return null;}
   @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){return !"https".equalsIgnoreCase(request.getUrl().getScheme());}
   @Override public void onPageFinished(WebView view,String url){if(!selectorPrimed&&same(url,selector)){selectorPrimed=true;loadSelected();return;}if(serveSelected)inject();}
  });
  Map<String,String> headers=new LinkedHashMap<>();headers.put("Referer",origin(selector)+"/");web.loadUrl(selector,headers);main.removeCallbacks(timeout);main.postDelayed(timeout,12000);
 }

 private void loadSelected(){if(closed||web==null||delivered.get())return;attempt++;serveSelected=true;main.removeCallbacks(timeout);web.stopLoading();web.clearCache(false);Map<String,String> headers=new LinkedHashMap<>();headers.put("Referer",origin(selector)+"/");web.loadUrl(selector,headers);main.postDelayed(timeout,18000);}
 private void inject(){if(web==null||closed)return;web.evaluateJavascript("(function(){if(window.__ritaLive)return;window.__ritaLive=1;function r(u){try{u=new URL(u,location.href).href;if(/\\.m3u8(?:[?#]|$)/i.test(u))RitaLive.media(u,location.href)}catch(e){}};function q(){document.querySelectorAll('video').forEach(function(v){v.muted=true;r(v.src)});document.querySelectorAll('source').forEach(function(e){r(e.src)});try{performance.getEntriesByType('resource').forEach(function(e){r(e.name)})}catch(e){}}var f=window.fetch;if(f)window.fetch=function(){r(arguments[0]&&arguments[0].url||arguments[0]);return f.apply(this,arguments)};var o=XMLHttpRequest.prototype.open;XMLHttpRequest.prototype.open=function(m,u){r(u);return o.apply(this,arguments)};setInterval(q,1000);q()})();",null);}
 private void capture(String url,String referer){if(closed||!hls(url)||!delivered.compareAndSet(false,true))return;main.removeCallbacks(timeout);String ref=referer==null||referer.isEmpty()?(web==null?target:web.getUrl()):referer;if(ref==null||ref.isEmpty())ref=target;String cookie=CookieManager.getInstance().getCookie(url);callback.resolved(url,ref,cookie);}
 private void finishError(Exception error){if(closed||!delivered.compareAndSet(false,true))return;callback.failed(error);close();}
 void close(){if(closed)return;closed=true;main.removeCallbacks(timeout);if(web!=null){WebView old=web;web=null;parent.removeView(old);old.stopLoading();old.loadUrl("about:blank");old.removeAllViews();old.destroy();}}
 private static boolean hls(String value){try{Uri u=Uri.parse(value);return "https".equalsIgnoreCase(u.getScheme())&&u.getHost()!=null&&u.getPath()!=null&&u.getPath().toLowerCase(Locale.ROOT).endsWith(".m3u8");}catch(Exception e){return false;}}
 private static String header(Map<String,String> headers,String key){if(headers==null)return null;for(Map.Entry<String,String> e:headers.entrySet())if(e.getKey()!=null&&e.getKey().equalsIgnoreCase(key))return e.getValue();return null;}
 private static boolean same(String a,String b){try{Uri x=Uri.parse(a),y=Uri.parse(b);return x.getHost()!=null&&x.getHost().equalsIgnoreCase(y.getHost())&&Objects.equals(x.getPath(),y.getPath());}catch(Exception e){return false;}}
 private static String origin(String value){Uri u=Uri.parse(value);return u.getScheme()+"://"+u.getAuthority();}
}
