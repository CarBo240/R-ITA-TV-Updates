package it.carmine.streamplayer;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.util.*;
import org.json.JSONObject;

/** Official-web experiment only. No credential extraction, native token or DRM resolver. */
public final class LabInfinityActivity extends Activity {
 private static final String HOME="https://mediasetinfinity.mediaset.it/";
 private static boolean profileReady;
 private WebView web;
 private FrameLayout frame;
 private LinearLayout panel;
 private TextView status;
 private View fullscreen;
 private WebChromeClient.CustomViewCallback fullscreenCallback;
 private final ArrayList<String> events=new ArrayList<>();
 private boolean closed;
 private boolean pageFailed;
 private int probeGeneration;
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private void note(String s){if(closed)return;status.setText(s);if(events.size()>=40)events.remove(0);events.add(s);}
 private Button button(LinearLayout row,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setFocusable(true);row.addView(b,new LinearLayout.LayoutParams(0,-2,1));b.setOnClickListener(v->action.run());return b;}
 private static boolean official(Uri u){String host=u.getHost();return "https".equalsIgnoreCase(u.getScheme())&&host!=null&&(host.equals("mediasetinfinity.mediaset.it")||host.endsWith(".mediasetinfinity.mediaset.it"));}
 @Override public void onCreate(Bundle saved){super.onCreate(saved);
  frame=new FrameLayout(this);panel=new LinearLayout(this);panel.setOrientation(1);panel.setPadding(dp(12),dp(8),dp(12),0);panel.setBackgroundColor(0xff10151c);frame.addView(panel);setContentView(frame);
  TextView heading=new TextView(this);heading.setText("LAB 2.1.22 · Mediaset Infinity · fallback ufficiale");heading.setTextColor(Color.WHITE);heading.setTextSize(21);panel.addView(heading);
  TextView help=new TextView(this);help.setText("Test sito ufficiale. Per accedere usa Login / Registrati nel sito. Catalogo e video restano nel player Mediaset; compatibilità TV da verificare.");help.setTextColor(0xffc5d3dc);help.setTextSize(14);panel.addView(help);
  status=new TextView(this);status.setTextColor(0xfff2b245);panel.addView(status);
  LinearLayout row=new LinearLayout(this);panel.addView(row);
  button(row,"← LAB",()->finish());button(row,"Catalogo / Login",()->{if(web!=null)web.loadUrl(HOME);else external();});button(row,"Dirette",()->{if(web!=null)web.loadUrl(HOME+"guidatv");else external();});button(row,"Diagnostica",this::diagnostics);
  LinearLayout actions=new LinearLayout(this);panel.addView(actions);
  button(actions,"Apri nel browser",this::external);button(actions,"Ripristina sessione LAB",this::resetSession);
  if(Build.VERSION.SDK_INT<28){note("SESSIONE · Android 8: test disponibile nel browser esterno. Sessione separata interna richiede Android 9 o superiore.");return;}
  try{
   // Must precede every android.webkit call in this dedicated process.
   if(!profileReady){WebView.setDataDirectorySuffix("infinity_lab");profileReady=true;}
   web=new WebView(this);panel.addView(web,new LinearLayout.LayoutParams(-1,0,1));
   WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);s.setMediaPlaybackRequiresUserGesture(true);s.setSupportMultipleWindows(false);
   CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
   web.setWebViewClient(new WebViewClient(){
    @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){Uri u=request.getUrl();if("https".equalsIgnoreCase(u.getScheme()))return false;note("NAVIGAZIONE · Collegamento esterno non aperto nel LAB.");return true;}
    @Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap icon){probeGeneration++;pageFailed=false;note("CATALOGO / ACCESSO · Caricamento · "+safeHost(url));}
    @Override public void onPageFinished(WebView view,String url){CookieManager.getInstance().flush();if(!pageFailed)note("PAGINA · Caricata · "+safeHost(url)+" · login e riproduzione da verificare");}
    @Override public void onReceivedError(WebView view,WebResourceRequest request,WebResourceError error){if(request.isForMainFrame()){pageFailed=true;note("PAGINA · Errore rete / WebView · "+error.getErrorCode());}}
    @Override public void onReceivedHttpError(WebView view,WebResourceRequest request,WebResourceResponse response){if(request.isForMainFrame()){pageFailed=true;note("PAGINA · HTTP "+response.getStatusCode()+" · "+safeHost(request.getUrl().toString()));}}
    // Default SSL-error handling cancels. Never proceed past a certificate error.
   });
   web.setWebChromeClient(new WebChromeClient(){
    @Override public void onPermissionRequest(PermissionRequest request){runOnUiThread(()->{
     if(closed){request.deny();return;}
     boolean media=false;for(String resource:request.getResources())if(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID.equals(resource))media=true;
     if(media&&official(request.getOrigin())){request.grant(new String[]{PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID});note("DRM · Identificatore protetto consentito al sito ufficiale; licenza e riproduzione da verificare.");}else request.deny();
    });}
    @Override public void onShowCustomView(View view,CustomViewCallback callback){if(fullscreen!=null){callback.onCustomViewHidden();return;}fullscreen=view;fullscreenCallback=callback;panel.setVisibility(View.GONE);frame.addView(view,new FrameLayout.LayoutParams(-1,-1));getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().getDecorView().setSystemUiVisibility(5894);}
    @Override public void onHideCustomView(){hideFullscreen();}
   });
   note("SESSIONE · Profilo LAB dedicato; credenziali gestite dal sito ufficiale.");web.loadUrl(HOME);
  }catch(RuntimeException error){if(web!=null){panel.removeView(web);web.destroy();web=null;}note("WEBVIEW · Avvio non disponibile · "+error.getClass().getSimpleName()+". Usa il browser esterno.");}
 }
 private String safeHost(String url){try{String h=Uri.parse(url).getHost();return h==null?"pagina":h;}catch(Exception e){return "pagina";}}
 private void hideFullscreen(){if(fullscreen==null)return;frame.removeView(fullscreen);fullscreen=null;panel.setVisibility(View.VISIBLE);getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().getDecorView().setSystemUiVisibility(0);WebChromeClient.CustomViewCallback cb=fullscreenCallback;fullscreenCallback=null;if(cb!=null)cb.onCustomViewHidden();}
 private void external(){String url=web==null?HOME:web.getUrl();if(url==null||!official(Uri.parse(url)))url=HOME;try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));note("BROWSER · Sessione esterna indipendente dal LAB.");}catch(ActivityNotFoundException e){note("BROWSER · Nessuna applicazione disponibile.");}}
 private void resetSession(){if(web==null){note("SESSIONE · La sessione esterna si gestisce nel browser.");return;}new AlertDialog.Builder(this).setTitle("Ripristinare la sessione Infinity del LAB?").setMessage("Cancella i dati locali di questo test. Per uscire anche dal servizio usa Logout nel profilo Mediaset.").setNegativeButton("Annulla",null).setPositiveButton("Ripristina",(d,n)->{
  probeGeneration++;web.stopLoading();hideFullscreen();web.loadUrl("about:blank");CookieManager.getInstance().removeAllCookies(done->{if(closed)return;CookieManager.getInstance().flush();WebStorage.getInstance().deleteAllData();web.clearCache(true);web.clearHistory();note("SESSIONE · Dati locali LAB cancellati.");web.loadUrl(HOME);});
 }).show();}
 private void diagnostics(){if(web==null){showDiagnostics();return;}final int current=probeGeneration;
  // Observe public HTML5 state only. No media URLs, cookies, account fields or passwords.
  web.evaluateJavascript("(function(){var v=document.querySelector('video');return JSON.stringify(v?{found:true,paused:v.paused,time:v.currentTime,ready:v.readyState,error:v.error?v.error.code:0}:{found:false});})()",value->{
   if(closed||current!=probeGeneration)return;
   try{String decoded=new org.json.JSONTokener(value).nextValue().toString();JSONObject result=new JSONObject(decoded);if(!result.optBoolean("found"))note("PLAYER · Nessun video HTML5 osservabile nella pagina principale; eventuali iframe non verificati.");else if(result.optInt("error")!=0)note("PLAYER · Errore HTML5 "+result.optInt("error"));else note("PLAYER · "+(result.optBoolean("paused")?"In pausa":"Attivo")+" · tempo "+(int)result.optDouble("time")+" s · readyState "+result.optInt("ready")+" · avanzamento e audio da verificare sulla TV.");}catch(Exception e){note("PLAYER · Stato non osservabile.");}showDiagnostics();
  });
 }
 private void showDiagnostics(){StringBuilder s=new StringBuilder("Accesso: gestito dal sito, non verificato automaticamente.\nPlayer: web ufficiale, nessun resolver Media3 Infinity.\nSessione: "+(web==null?"browser esterno":"profilo LAB separato")+".\n\n");for(String line:events)s.append(line).append('\n');new AlertDialog.Builder(this).setTitle("Diagnostica Infinity LAB").setMessage(s.toString()).setPositiveButton("Chiudi",null).show();}
 @Override public void onBackPressed(){if(fullscreen!=null)hideFullscreen();else if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onPause(){if(web!=null){web.onPause();CookieManager.getInstance().flush();}super.onPause();}
 @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
 @Override protected void onDestroy(){closed=true;probeGeneration++;hideFullscreen();if(web!=null){panel.removeView(web);web.stopLoading();web.destroy();web=null;}super.onDestroy();}
}
