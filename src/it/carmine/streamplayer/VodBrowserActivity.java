package it.carmine.streamplayer;

import android.app.*;import android.graphics.*;import android.net.Uri;import android.os.*;import android.view.*;import android.webkit.*;import android.widget.*;import java.io.*;import java.net.URI;import java.util.*;import org.json.*;

/** Visible Android System WebView used as a normal in-app browser. */
public final class VodBrowserActivity extends Activity {
 private final java.util.concurrent.ExecutorService resolver=java.util.concurrent.Executors.newSingleThreadExecutor();
 private final Set<String> inspected=java.util.Collections.synchronizedSet(new HashSet<>());
 private volatile boolean destroyed,handedOff,captureMedia;private boolean nativeAttempt,searching,querySubmitted,findingPlayer,manualSearch;private int searchGeneration,pollCount;private TextView hint;private final Handler searchUi=new Handler(Looper.getMainLooper());
 private WebView web;private FrameLayout root,fullscreen;private View custom;private WebChromeClient.CustomViewCallback customCallback;private String home;private CheckBox remember;private VodSettings.Source playbackSource;private VodSource.Title playbackTitle;private String episode="",label="";private long position,duration,lastSaved;
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private Button button(String label){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(14);b.setTextColor(Color.WHITE);b.setMinWidth(0);b.setMinimumWidth(0);UiTheme.navigation(b);return b;}
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(5894);home=valid(getIntent().getStringExtra("url"));if(home==null){finish();return;}try{String source=getIntent().getStringExtra("source"),title=getIntent().getStringExtra("titleJson");if(source!=null&&title!=null){JSONObject s=new JSONObject(source);playbackSource=new VodSettings.Source(s.getString("id"),s.getString("name"),s.getString("url"),s.optString("type","web"));playbackTitle=new VodSource.Title(new JSONObject(title),"");episode=getIntent().getStringExtra("episode");label=getIntent().getStringExtra("label");}}catch(Exception ignored){playbackSource=null;playbackTitle=null;}
  nativeAttempt=getIntent().getBooleanExtra("nativeAttempt",false)&&playbackSource!=null&&playbackTitle!=null&&!playbackTitle.series()&&WebMovieLinks.supported(playbackSource.url);searching=playbackTitle!=null&&playbackSource!=null&&WebMovieLinks.supported(playbackSource.url)&&playbackTitle.json.optBoolean("_web_search");VodNetwork.initialize(this);root=new FrameLayout(this);root.setBackgroundColor(Color.BLACK);setContentView(root);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);root.addView(page,new FrameLayout.LayoutParams(-1,-1));
  LinearLayout bar=new LinearLayout(this);bar.setPadding(dp(8),dp(6),dp(8),dp(6));page.addView(bar,new LinearLayout.LayoutParams(-1,dp(56)));Button back=button("‹ VOD");Button site=button("Home");Button reload=button("↻ Ricarica");TextView title=new TextView(this);hint=title;title.setText(getIntent().getStringExtra("title"));title.setTextColor(UiTheme.TEXT);title.setTextSize(17);title.setGravity(Gravity.CENTER_VERTICAL);title.setPadding(dp(14),0,0,0);bar.addView(back,new LinearLayout.LayoutParams(dp(110),-1));bar.addView(site,new LinearLayout.LayoutParams(dp(100),-1));bar.addView(reload,new LinearLayout.LayoutParams(dp(130),-1));bar.addView(title,new LinearLayout.LayoutParams(0,-1,1));remember=new CheckBox(this);remember.setText("Ricorda accesso");remember.setTextColor(UiTheme.TEXT);remember.setChecked(getSharedPreferences("browserSessions",0).getBoolean(origin(home),true));bar.addView(remember,new LinearLayout.LayoutParams(dp(155),-1));remember.setOnCheckedChangeListener((v,checked)->getSharedPreferences("browserSessions",0).edit().putBoolean(origin(home),checked).apply());
  web=new WebView(this);web.setBackgroundColor(Color.BLACK);web.setFocusable(true);web.setFocusableInTouchMode(true);page.addView(web,new LinearLayout.LayoutParams(-1,0,1));CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);if(nativeAttempt||searching){s.setUserAgentString(VodSource.USER_AGENT);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);}s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setMediaPlaybackRequiresUserGesture(false);s.setSupportMultipleWindows(false);s.setJavaScriptCanOpenWindowsAutomatically(false);s.setLoadsImagesAutomatically(true);s.setBuiltInZoomControls(false);s.setDisplayZoomControls(false);if(playbackSource!=null&&playbackSource.vixsrc())web.addJavascriptInterface(new VixBridge(),"RitaVix");
  web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){UriGuard g=guard(r.getUrl().toString());if(playbackSource!=null&&playbackSource.vixsrc()&&!sameVix(r.getUrl()))return true;if(g.ok){v.loadUrl(g.url);return true;}return true;}@Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){if(blocked(r.getUrl()))return empty();if(nativeAttempt&&captureMedia)detectMedia(r);return super.shouldInterceptRequest(v,r);}@Override public void onPageFinished(WebView v,String url){title.setText(searching?(manualSearch?"Seleziona il titolo nel sito":"Ricerca nel sito · "+playbackTitle.name):nativeAttempt?(captureMedia?"Preparazione player interno · avvia il video se richiesto":"Player non riconosciuto · torna a Fonti"):playbackSource!=null&&playbackSource.vixsrc()?"VixSrc · "+label:v.getTitle()==null?"Streaming Community":v.getTitle());if(searching||findingPlayer)scheduleScan();if(playbackSource!=null&&playbackSource.vixsrc())v.evaluateJavascript("(function(){if(window.__ritaVix)return;window.__ritaVix=true;window.open=function(){return null};window.addEventListener('message',function(e){try{if(e.data&&e.data.type==='PLAYER_EVENT')RitaVix.event(JSON.stringify(e.data.data));}catch(x){}});document.addEventListener('click',function(e){var a=e.target&&e.target.closest?e.target.closest('a[href]'):null;if(a){try{var u=new URL(a.href,location.href);if(u.hostname!==location.hostname&&!u.hostname.endsWith('.vixsrc.to')){e.preventDefault();e.stopImmediatePropagation();}}catch(x){}}},true);function clean(){document.querySelectorAll('ins.adsbygoogle,[data-ad-client],[id*=popup i],[class*=popup i],[id*=advert i],[class*=advert i]').forEach(function(n){n.remove()})}clean();new MutationObserver(clean).observe(document.documentElement,{childList:true,subtree:true});})()",null);}});
  web.setWebChromeClient(new WebChromeClient(){@Override public boolean onCreateWindow(WebView view,boolean dialog,boolean gesture,Message resultMsg){return false;}@Override public void onShowCustomView(View view,CustomViewCallback callback){if(custom!=null){callback.onCustomViewHidden();return;}custom=view;customCallback=callback;fullscreen=new FrameLayout(VodBrowserActivity.this);fullscreen.setBackgroundColor(Color.BLACK);fullscreen.addView(view,new FrameLayout.LayoutParams(-1,-1));root.addView(fullscreen,new FrameLayout.LayoutParams(-1,-1));getWindow().getDecorView().setSystemUiVisibility(5894|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}@Override public void onHideCustomView(){hideCustom();}});
  back.setOnClickListener(v->{if(custom!=null)hideCustom();else if(web.canGoBack())web.goBack();else finish();});site.setOnClickListener(v->{if(nativeAttempt||searching){startSearch();}else loadHome();});reload.setOnClickListener(v->{if((nativeAttempt||searching)&&!handedOff){inspected.clear();if(playbackTitle.json.optBoolean("_web_search"))startSearch();else prepareNative();}else web.reload();});if(searching)startSearch();else if(nativeAttempt)prepareNative();else loadHome();web.requestFocus();if(nativeAttempt)web.postDelayed(()->{if(!destroyed&&!handedOff&&!searching&&!findingPlayer)hint.setText("Video non rilevato · Ricarica o scegli un’altra fonte");},35000);
 }
 private void prepareNative(){
  hint.setText("Ricerca del player · "+playbackTitle.name);captureMedia=false;findingPlayer=false;int request=++searchGeneration;
  resolver.submit(()->{try{
   String html=VodSource.HTTP.get(home,playbackSource.url+"/");
   java.util.List<String> links=WebMovieLinks.players(html,home);
   if(links.isEmpty())throw new IOException("Player non riconosciuto");String target=links.get(0);
   runOnUiThread(()->{if(destroyed||request!=searchGeneration)return;captureMedia=true;web.loadUrl(target,Collections.singletonMap("Referer",home));});
  }catch(Exception error){runOnUiThread(()->{if(destroyed||request!=searchGeneration)return;hint.setText("Verifica del player nel sito…");findingPlayer=true;pollCount=0;loadHome();scheduleScan();});}});
 }
 private void startSearch(){
  ++searchGeneration;searchUi.removeCallbacksAndMessages(null);captureMedia=false;findingPlayer=false;searching=true;querySubmitted=false;manualSearch=false;pollCount=0;
  hint.setText("Ricerca nel sito · "+playbackTitle.name);web.loadUrl(playbackSource.url+"/");scheduleScan();
 }
 private void scheduleScan(){searchUi.removeCallbacks(scanPage);searchUi.postDelayed(scanPage,manualSearch?1800:900);}
 private final Runnable scanPage=()->{
  if(destroyed||handedOff||(!searching&&!findingPlayer))return;final int request=searchGeneration;
  if(++pollCount==50){manualSearch=true;querySubmitted=true;hint.setText("Ricerca non confermata · cerca o seleziona il titolo nel sito");}
  web.evaluateJavascript(VodWebSearchScript.snapshot(),raw->{
   if(destroyed||request!=searchGeneration||handedOff)return;
   try{JSONObject page=new JSONObject(new JSONArray("["+raw+"]").getString(0));String url=page.optString("url"),html=page.optString("html");
    if(!WebMovieLinks.supported(url)){scheduleScan();return;}
    if(searching){
     String match=VodWebSource.currentTitlePage(html,url,playbackTitle);if(match.isEmpty())match=VodWebSource.titlePage(html,url,playbackTitle);
     if(!match.isEmpty()){searching=false;findingPlayer=nativeAttempt;pollCount=0;home=match;playbackTitle.json.remove("_web_search");playbackTitle.json.put("_web_page",match);if(!match.equals(url))web.loadUrl(match);if(findingPlayer)scheduleScan();return;}
     if(!querySubmitted){web.evaluateJavascript(VodWebSearchScript.submit(JSONObject.quote(playbackTitle.name)),submitted->{if(!destroyed&&request==searchGeneration)querySubmitted="true".equals(submitted);});}
    }else if(findingPlayer){
     List<String> links=WebMovieLinks.players(html,url);
     if(!links.isEmpty()){findingPlayer=false;captureMedia=true;web.loadUrl(links.get(0),Collections.singletonMap("Referer",home));return;}
    }
   }catch(Exception ignored){}
   if(!destroyed&&request==searchGeneration)scheduleScan();
  });
 };
 private void detectMedia(WebResourceRequest request){
  String url=request.getUrl().toString();if(!WebMovieLinks.media(url)||handedOff||destroyed||inspected.size()>=12||!inspected.add(url))return;
  Map<String,String> headers=new HashMap<>();
  for(Map.Entry<String,String> h:request.getRequestHeaders().entrySet())for(String allowed:new String[]{"Referer","Origin","User-Agent"})if(allowed.equalsIgnoreCase(h.getKey()))headers.put(allowed,h.getValue());
  if(!headers.containsKey("Referer"))return;
  resolver.submit(()->{try{
   String cookies=CookieManager.getInstance().getCookie(url);if(cookies!=null&&!cookies.isEmpty())headers.put("Cookie",cookies);
   okhttp3.Request.Builder builder=new okhttp3.Request.Builder().url(url);
   for(Map.Entry<String,String> h:headers.entrySet())builder.header(h.getKey(),h.getValue());
   boolean mp4=new java.net.URI(url).getPath().toLowerCase(Locale.ROOT).endsWith(".mp4");
   if(mp4)builder.header("Range","bytes=0-1023");
   try(okhttp3.Response response=VodNetwork.client().newCall(builder.build()).execute()){
    if(!response.isSuccessful()||response.body()==null)return;
    if(mp4){String type=response.header("Content-Type","").toLowerCase(Locale.ROOT);if(!type.startsWith("video/")&&!type.contains("octet-stream"))return;}
    else {String first=response.body().source().readUtf8Line();if(first==null||!first.replace("\uFEFF", "").trim().equals("#EXTM3U"))return;}
   }
   runOnUiThread(()->{if(destroyed||handedOff)return;handedOff=true;captureMedia=false;web.stopLoading();
    android.content.Intent player=new android.content.Intent(this,VodPlayerActivity.class).putExtra("source",playbackSource.json().toString()).putExtra("titleJson",playbackTitle.json.toString()).putExtra("episode", "").putExtra("label",playbackTitle.name).putExtra("episodes", "[]").putExtra("index",-1).putExtra("startPosition",getIntent().getLongExtra("startPosition",-1)).putExtra("resolvedWebUrl",url).putExtra("resolvedWebHeaders",new JSONObject(headers).toString());
    startActivityForResult(player,138);
   });
  }catch(Exception ignored){}});
 }
 @Override protected void onActivityResult(int request,int result,android.content.Intent data){super.onActivityResult(request,result,data);if(request==138){setResult(result,data);finish();}}
 private void loadHome(){if(home.startsWith("https://www.youtube.com/embed/"))web.loadUrl(home,java.util.Collections.singletonMap("Referer","https://"+getPackageName()));else web.loadUrl(home);}
 private static String origin(String url){try{return new URI(url).getHost();}catch(Exception e){return "";}}
 private static final String[] AD_HOSTS={"doubleclick.net","googlesyndication.com","googleadservices.com","adservice.google.com","popads.net","popcash.net","adsterra.com","adsterra.net","propellerads.com","monetag.com","onclicka.com","onclickalgo.com","onclkds.com","intellipopup.com","xtpdsreqdoz.com","gzmftfhecexxm.com","chewsever.com"};
 static boolean blocked(Uri uri){String host=uri==null?null:uri.getHost(),path=uri==null?"":uri.getPath();if(host!=null)for(String bad:AD_HOSTS)if(host.equals(bad)||host.endsWith("."+bad))return true;return path!=null&&(path.endsWith("/fmatrix.min.js")||path.endsWith("/ejquery.jscroll.min.js")||path.endsWith("/vjquery.jscroll.min.js"));}
 private static boolean sameVix(Uri uri){String host=uri==null?null:uri.getHost();return host!=null&&(host.equals("vixsrc.to")||host.endsWith(".vixsrc.to"));}
 private static WebResourceResponse empty(){return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));}
 private void forgetSession(){try{String cookies=CookieManager.getInstance().getCookie(home);if(cookies!=null)for(String cookie:cookies.split(";")){String key=cookie.split("=",2)[0].trim();CookieManager.getInstance().setCookie(home,key+"=; Max-Age=0; Path=/; Secure");}}catch(Exception ignored){}}
 private static final class UriGuard{final boolean ok;final String url;UriGuard(boolean o,String u){ok=o;url=u;}}
 private static UriGuard guard(String raw){try{URI u=new URI(raw);boolean ok=("https".equals(u.getScheme())||"http".equals(u.getScheme()))&&u.getHost()!=null&&u.getUserInfo()==null;return new UriGuard(ok,raw);}catch(Exception e){return new UriGuard(false,raw);}}
 private static String valid(String raw){UriGuard g=guard(raw);return g.ok?g.url:null;}
 private void hideCustom(){if(custom==null)return;root.removeView(fullscreen);custom=null;fullscreen=null;if(customCallback!=null)customCallback.onCustomViewHidden();customCallback=null;}
 private final class VixBridge {@JavascriptInterface public void event(String raw){try{JSONObject e=new JSONObject(raw);position=Math.max(0,(long)(e.optDouble("currentTime")*1000));duration=Math.max(0,(long)(e.optDouble("duration")*1000));String type=e.optString("event",e.optString("type"));long now=System.currentTimeMillis();if("pause".equals(type)||"ended".equals(type)||now-lastSaved>=10000){lastSaved=now;saveProgress();}}catch(Exception ignored){}}}
 private void saveProgress(){if(playbackSource!=null&&playbackTitle!=null&&position>=1000){VodLibrary.linkedProgress(this,playbackSource,playbackTitle,episode==null?"":episode,label==null?playbackTitle.name:label,position,duration);CloudSync.flush(this,false);}}
 @Override public void onBackPressed(){if(custom!=null)hideCustom();else if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onPause(){saveProgress();if(web!=null)web.onPause();if(remember!=null&&!remember.isChecked())forgetSession();CookieManager.getInstance().flush();super.onPause();}
 @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
 @Override protected void onDestroy(){destroyed=true;++searchGeneration;searchUi.removeCallbacksAndMessages(null);resolver.shutdownNow();if(web!=null){web.stopLoading();web.loadUrl("about:blank");web.destroy();web=null;}super.onDestroy();}
}
