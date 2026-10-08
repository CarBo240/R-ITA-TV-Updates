package it.carmine.streamplayer;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.mozilla.geckoview.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Event playback using Mozilla's embedded browser and normal public site navigation. */
public class GeckoEventActivity extends Activity {
    private String url;private WebAdPolicy adPolicy;private boolean daddyPage;
    protected boolean engineEnabled(){return true;}
    private static GeckoRuntime runtime;
    private GeckoSession session;
    private GeckoView browser;
    private FrameLayout root;
    private LinearLayout tools;
    private TextView status;
    private Button fullscreenButton;
    private org.mozilla.geckoview.WebExtension.Port fullscreenPort;
    private boolean pageFullscreen;
    private Pointer cursor;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean remoteHovering,remotePressed,mouseClicks;
    private Button clickModeButton;private final Set<Integer> heldKeys=new HashSet<>();
    private long moveStarted,lastFrame,lastHover=-1,pressedAt;private float pressedX,pressedY;private boolean pressedMouse;
    private final Runnable releaseClick=()->finishClick(false);
    private final Runnable moveFrame=new Runnable(){public void run(){
        if(destroyed||heldKeys.isEmpty()||tools.getVisibility()==View.VISIBLE)return;
        long now=SystemClock.uptimeMillis(),age=now-moveStarted;float dt=Math.min(48,Math.max(1,now-lastFrame))/1000f;lastFrame=now;
        float speed=dp(1)*(age<250?280:age<650?700:1200),dx=0,dy=0;
        if(heldKeys.contains(KeyEvent.KEYCODE_DPAD_LEFT))dx--;if(heldKeys.contains(KeyEvent.KEYCODE_DPAD_RIGHT))dx++;
        if(heldKeys.contains(KeyEvent.KEYCODE_DPAD_UP))dy--;if(heldKeys.contains(KeyEvent.KEYCODE_DPAD_DOWN))dy++;
        if(dx!=0&&dy!=0){dx*=.7071f;dy*=.7071f;}moveBy(dx*speed*dt,dy*speed*dt);
        handler.postDelayed(this,16);
    }};
    private final Runnable hideCursor=()->{endRemoteHover();if(cursor!=null)cursor.setVisibility(View.GONE);};
    
    private boolean htmlFullscreen, destroyed, playing;
    private String phase="Caricamento evento…";
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private Button button(String label){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(14);b.setTextColor(Color.WHITE);b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(dp(8),0,dp(8),0);android.graphics.drawable.StateListDrawable colors=new android.graphics.drawable.StateListDrawable();for(boolean focus:new boolean[]{true,false}){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(focus?0xff40331e:0xff202125);d.setCornerRadius(dp(6));d.setStroke(dp(2),focus?Color.WHITE:0xff38393d);colors.addState(focus?new int[]{android.R.attr.state_focused}:new int[]{},d);}b.setBackground(colors);b.setFocusable(true);return b;}
    private void record(String text){}
    private void host(String uri){}
    private void phase(String text){phase=text;if(status!=null)status.setText(text);}

    @Override public void onCreate(Bundle state){super.onCreate(state);url=getIntent().getStringExtra("url");if(url==null||!(url.startsWith("https://")||url.startsWith("http://"))){finish();return;}adPolicy=new WebAdPolicy(this);daddyPage=getIntent().getBooleanExtra("daddyLive",false);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        root=new FrameLayout(this);root.setFitsSystemWindows(true);root.setBackgroundColor(Color.BLACK);setContentView(root);
        browser=new GeckoView(this);root.addView(browser,new FrameLayout.LayoutParams(-1,-1));
        tools=new LinearLayout(this);tools.setOrientation(LinearLayout.VERTICAL);tools.setPadding(dp(8),dp(4),dp(8),dp(4));tools.setBackgroundColor(0xee151619);root.addView(tools,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP));
        LinearLayout row=new LinearLayout(this);tools.addView(row,new LinearLayout.LayoutParams(-1,dp(46)));
        Button reload=button("Ricarica");row.addView(reload,new LinearLayout.LayoutParams(0,-1,1));reload.setOnClickListener(v->{if(session!=null){phase("Ricaricamento evento…");session.loadUri(url);}});
        fullscreenButton=button("Schermo intero");row.addView(fullscreenButton,new LinearLayout.LayoutParams(0,-1,1));fullscreenButton.setOnClickListener(v->{showTools(false);if(daddyPage&&fullscreenPort!=null)fullscreenCommand("toggle");else centerCursor();});
        Button center=button("Clicca al centro");row.addView(center,new LinearLayout.LayoutParams(0,-1,1));center.setOnClickListener(v->{showTools(false);centerCursor();clickCursor();});
        status=new TextView(this);status.setTextColor(Color.WHITE);status.setTextSize(12);status.setPadding(dp(5),dp(4),dp(5),dp(5));tools.addView(status,new LinearLayout.LayoutParams(-1,-2));status.setText("Frecce: cursore · OK: clic · MENU: comandi · clicca Play e Click to unmute");
        clickModeButton=button("Clic: tocco");row.addView(clickModeButton,new LinearLayout.LayoutParams(0,-1,1));mouseClicks=daddyPage&&getSharedPreferences("geckoInput",0).getBoolean("daddyMouseClicks",false);updateClickMode();clickModeButton.setTag("geckoClickMode");clickModeButton.setOnClickListener(v->{mouseClicks=!mouseClicks;if(daddyPage)getSharedPreferences("geckoInput",0).edit().putBoolean("daddyMouseClicks",mouseClicks).apply();updateClickMode();});
        cursor=new Pointer(this);cursor.setFocusable(false);cursor.setClickable(false);root.addView(cursor,new FrameLayout.LayoutParams(dp(30),dp(40)));cursor.setVisibility(View.GONE);
        cursor.setTag("geckoCursor");tools.setTag("geckoMenu");fullscreenButton.setTag("geckoFullscreen");showTools(false);
        if(!engineEnabled())return;
        try{
            if(runtime==null){GeckoRuntimeSettings settings=new GeckoRuntimeSettings.Builder().javaScriptEnabled(true).build();settings.getContentBlocking().setCookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_ALL);runtime=GeckoRuntime.create(getApplicationContext(),settings);}
            session=new GeckoSession();
            session.setContentDelegate(new GeckoSession.ContentDelegate(){
                @Override public void onFullScreen(GeckoSession s,boolean full){htmlFullscreen=full;record("Fullscreen HTML: "+full);if(full)showTools(false);}
                @Override public void onCrash(GeckoSession s){phase("Il motore ha interrotto la pagina · premi Ricarica");showTools(true);}
                @Override public void onKill(GeckoSession s){phase("Processo pagina terminato · premi Ricarica");showTools(true);}
                @Override public void onCloseRequest(GeckoSession s){record("Richiesta chiusura pagina ignorata");}
                @Override public void onFocusRequest(GeckoSession s){if(tools.getVisibility()!=View.VISIBLE)browser.requestFocus();}
            });
            session.setProgressDelegate(new GeckoSession.ProgressDelegate(){
                @Override public void onPageStart(GeckoSession s,String uri){host(uri);phase("Caricamento evento…");}
                @Override public void onPageStop(GeckoSession s,boolean success){phase(success?(playing?"Pagina caricata · riproduzione segnalata dal motore":"Pagina caricata · riproduzione non ancora confermata"):"Errore caricamento pagina · premi Ricarica");}
            });
            session.setNavigationDelegate(new GeckoSession.NavigationDelegate(){
                @Override public GeckoResult<AllowOrDeny> onLoadRequest(GeckoSession s,LoadRequest request){if(request.target==TARGET_WINDOW_NEW){record("Nuova finestra bloccata: "+safeHost(request.uri));return GeckoResult.fromValue(AllowOrDeny.DENY);}host(request.uri);return GeckoResult.fromValue(adPolicy.allowNavigation(request.uri,url,daddyPage)?AllowOrDeny.ALLOW:AllowOrDeny.DENY);}
                @Override public GeckoResult<AllowOrDeny> onSubframeLoadRequest(GeckoSession s,LoadRequest request){host(request.uri);return GeckoResult.fromValue(adPolicy.allowNavigation(request.uri,url,false)?AllowOrDeny.ALLOW:AllowOrDeny.DENY);}
                @Override public GeckoResult<GeckoSession> onNewSession(GeckoSession s,String uri){record("Pop-up bloccato: "+safeHost(uri));return GeckoResult.fromValue(null);}
                @Override public GeckoResult<String> onLoadError(GeckoSession s,String uri,WebRequestError error){record("Errore Gecko "+error.code+" · categoria "+error.category+" · "+safeHost(uri));phase("Errore rete/pagina · premi Ricarica");showTools(true);return null;}
            });
            session.setPermissionDelegate(new GeckoSession.PermissionDelegate(){
                @Override public GeckoResult<Integer> onContentPermissionRequest(GeckoSession s,ContentPermission permission){boolean allow=permission.permission==PERMISSION_AUTOPLAY_AUDIBLE||permission.permission==PERMISSION_AUTOPLAY_INAUDIBLE||permission.permission==PERMISSION_MEDIA_KEY_SYSTEM_ACCESS;record("Permesso web "+permission.permission+": "+(allow?"consentito":"negato"));return GeckoResult.fromValue(allow?ContentPermission.VALUE_ALLOW:ContentPermission.VALUE_DENY);}
                @Override public void onAndroidPermissionsRequest(GeckoSession s,String[] requested,Callback callback){callback.reject();}
                @Override public void onMediaPermissionRequest(GeckoSession s,String uri,MediaSource[] video,MediaSource[] audio,MediaCallback callback){callback.reject();}
            });
            session.setMediaSessionDelegate(new MediaSession.Delegate(){
                @Override public void onActivated(GeckoSession s,MediaSession media){record("Sessione multimediale rilevata");}
                @Override public void onPlay(GeckoSession s,MediaSession media){playing=true;phase("Riproduzione segnalata dal motore");}
                @Override public void onPause(GeckoSession s,MediaSession media){playing=false;phase("Riproduzione in pausa");}
                @Override public void onStop(GeckoSession s,MediaSession media){playing=false;phase("Riproduzione terminata");}
                @Override public void onFullscreen(GeckoSession s,MediaSession media,boolean full,MediaSession.ElementMetadata meta){record("Video fullscreen: "+full);}
            });
            session.open(runtime);browser.setSession(session);session.setActive(true);GeckoDns.configure(this,()->{if(!destroyed){runtime.getWebExtensionController().ensureBuiltIn("resource://android/assets/adblock/","adfilter@rita.tv").accept(extension->{if(!destroyed){if(daddyPage)installFullscreenBridge(extension);session.loadUri(url);browser.requestFocus();}},error->{if(!destroyed){Toast.makeText(this,"Filtro pubblicità non disponibile · popup comunque bloccati",Toast.LENGTH_LONG).show();session.loadUri(url);browser.requestFocus();}});}},()->{if(!destroyed){phase("Impossibile impostare i DNS · torna a VOD > Fonti > DNS");showTools(true);}});record("GeckoView 152 · cookie di terze parti consentiti · pop-up bloccati");
        }catch(RuntimeException|LinkageError error){phase("Motore non disponibile: "+error.getClass().getSimpleName());record(String.valueOf(error.getMessage()));showTools(true);}
    }
    private void installFullscreenBridge(org.mozilla.geckoview.WebExtension extension){
        session.getWebExtensionController().setMessageDelegate(extension,new org.mozilla.geckoview.WebExtension.MessageDelegate(){
            @Override public void onConnect(org.mozilla.geckoview.WebExtension.Port port){
                fullscreenPort=port;port.setDelegate(new org.mozilla.geckoview.WebExtension.PortDelegate(){
                    @Override public void onPortMessage(Object message,org.mozilla.geckoview.WebExtension.Port source){if(message instanceof org.json.JSONObject){pageFullscreen=((org.json.JSONObject)message).optBoolean("fullscreen");if(pageFullscreen)showTools(false);}}
                    @Override public void onDisconnect(org.mozilla.geckoview.WebExtension.Port source){if(fullscreenPort==source){fullscreenPort=null;pageFullscreen=false;}}
                });
            }
        },"ritaFullscreen");
    }
    private void fullscreenCommand(String command){if(fullscreenPort==null)return;org.json.JSONObject message=new org.json.JSONObject();try{message.put("command",command);fullscreenPort.postMessage(message);}catch(org.json.JSONException ignored){}}
    private static boolean allowed(String uri){String scheme=Uri.parse(uri).getScheme();return "https".equals(scheme)||"http".equals(scheme)||"about".equals(scheme)||"blob".equals(scheme)||"data".equals(scheme);}
    private static String safeHost(String uri){String h=Uri.parse(uri).getHost();return h==null?"pagina interna":h;}
    private void updateClickMode(){if(clickModeButton!=null)clickModeButton.setText(mouseClicks?"Clic: mouse":"Clic: tocco");}
    private void showTools(boolean show){stopCursorMotion();finishClick(true);tools.setVisibility(show?View.VISIBLE:View.GONE);getWindow().getDecorView().setSystemUiVisibility(show?0:5894);handler.removeCallbacks(hideCursor);endRemoteHover();cursor.setVisibility(View.GONE);if(show)fullscreenButton.requestFocus();else browser.requestFocus();}
    private void centerCursor(){if(browser.getWidth()<=0)return;cursor.x=browser.getWidth()/2f;cursor.y=browser.getHeight()/2f;hoverCursor(true);revealCursor();}
    private void positionCursor(){cursor.setX(browser.getLeft()+cursor.x);cursor.setY(browser.getTop()+cursor.y);}
    private void revealCursor(){positionCursor();cursor.setVisibility(View.VISIBLE);if(heldKeys.isEmpty()){handler.removeCallbacks(hideCursor);handler.postDelayed(hideCursor,3500);}}
    private void moveBy(float dx,float dy){if(browser.getWidth()<=0||browser.getHeight()<=0)return;
        if(cursor.x<0){cursor.x=browser.getWidth()/2f;cursor.y=browser.getHeight()/2f;}
        cursor.x=Math.max(1,Math.min(browser.getWidth()-1,cursor.x+dx));cursor.y=Math.max(1,Math.min(browser.getHeight()-1,cursor.y+dy));
        if(!remotePressed)hoverCursor(false);revealCursor();
    }
    private void startCursorMotion(int key){if(!heldKeys.add(key))return;
        handler.removeCallbacks(hideCursor);float step=dp(10);moveBy(key==KeyEvent.KEYCODE_DPAD_LEFT?-step:key==KeyEvent.KEYCODE_DPAD_RIGHT?step:0,key==KeyEvent.KEYCODE_DPAD_UP?-step:key==KeyEvent.KEYCODE_DPAD_DOWN?step:0);
        if(heldKeys.size()==1){moveStarted=lastFrame=SystemClock.uptimeMillis();handler.postDelayed(moveFrame,48);}
    }
    private void releaseCursorMotion(int key){heldKeys.remove(key);if(heldKeys.isEmpty()){handler.removeCallbacks(moveFrame);if(cursor.getVisibility()==View.VISIBLE)revealCursor();}}
    private void stopCursorMotion(){heldKeys.clear();handler.removeCallbacks(moveFrame);}
    /** Sends real Gecko input, including hover so web player controls appear before OK. */
    protected void sendRemotePointer(MotionEvent event,boolean mouse){
        if(session==null||!session.isOpen())return;
        if(mouse){int action=event.getActionMasked();if(action==MotionEvent.ACTION_HOVER_ENTER||action==MotionEvent.ACTION_HOVER_MOVE||action==MotionEvent.ACTION_HOVER_EXIT)session.getPanZoomController().onMotionEvent(event);else session.getPanZoomController().onMouseEvent(event);}
        else browser.dispatchTouchEvent(event);
    }
    static MotionEvent remoteEvent(long downTime,int action,float x,float y,boolean mouse,boolean pressed){
        MotionEvent.PointerProperties pointer=new MotionEvent.PointerProperties();
        pointer.id=0;pointer.toolType=mouse?MotionEvent.TOOL_TYPE_MOUSE:MotionEvent.TOOL_TYPE_FINGER;
        MotionEvent.PointerCoords coords=new MotionEvent.PointerCoords();
        coords.x=x;coords.y=y;coords.pressure=pressed?1:0;coords.size=1;
        return MotionEvent.obtain(downTime,SystemClock.uptimeMillis(),action,1,
            new MotionEvent.PointerProperties[]{pointer},new MotionEvent.PointerCoords[]{coords},
            0,mouse&&pressed?MotionEvent.BUTTON_PRIMARY:0,1,1,0,0,
            mouse?InputDevice.SOURCE_MOUSE:InputDevice.SOURCE_TOUCHSCREEN,0);
    }
    private void emitRemote(int action,float x,float y,long downTime,boolean pressed){
        boolean mouse=action==MotionEvent.ACTION_HOVER_ENTER||action==MotionEvent.ACTION_HOVER_MOVE||action==MotionEvent.ACTION_HOVER_EXIT||pressedMouse;
        MotionEvent event=remoteEvent(downTime,action,x,y,mouse,pressed);
        try{sendRemotePointer(event,mouse);}finally{event.recycle();}
    }
    private void hoverCursor(boolean force){
        if(!daddyPage||cursor.x<0||destroyed)return;
        long now=SystemClock.uptimeMillis();if(!force&&lastHover>=0&&now-lastHover<50)return;lastHover=now;
        if(!remoteHovering){emitRemote(MotionEvent.ACTION_HOVER_ENTER,cursor.x,cursor.y,now,false);remoteHovering=true;}
        emitRemote(MotionEvent.ACTION_HOVER_MOVE,cursor.x,cursor.y,now,false);
    }
    private void endRemoteHover(){
        if(remoteHovering){emitRemote(MotionEvent.ACTION_HOVER_EXIT,cursor.x,cursor.y,SystemClock.uptimeMillis(),false);remoteHovering=false;}
    }
    private void clickCursor(){
        if(destroyed||remotePressed||browser.getWidth()<=0||browser.getHeight()<=0)return;
        stopCursorMotion();if(cursor.x<0){cursor.x=browser.getWidth()/2f;cursor.y=browser.getHeight()/2f;}
        cursor.x=Math.max(1,Math.min(browser.getWidth()-1,cursor.x));cursor.y=Math.max(1,Math.min(browser.getHeight()-1,cursor.y));
        browser.requestFocus();hoverCursor(true);pressedX=cursor.x;pressedY=cursor.y;pressedAt=SystemClock.uptimeMillis();pressedMouse=mouseClicks;
        remotePressed=true;emitRemote(MotionEvent.ACTION_DOWN,pressedX,pressedY,pressedAt,true);revealCursor();handler.postDelayed(releaseClick,40);
    }
    private void finishClick(boolean cancel){handler.removeCallbacks(releaseClick);if(!remotePressed)return;
        emitRemote(cancel?MotionEvent.ACTION_CANCEL:MotionEvent.ACTION_UP,pressedX,pressedY,pressedAt,false);remotePressed=false;pressedMouse=false;
        if(!cancel&&!destroyed&&tools.getVisibility()!=View.VISIBLE){hoverCursor(true);revealCursor();}
    }
    private final class Pointer extends View {
        float x=-1,y=-1;final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final Path arrow=new Path();
        Pointer(Context context){super(context);setWillNotDraw(false);arrow.moveTo(0,0);arrow.lineTo(0,29);arrow.lineTo(7,22);arrow.lineTo(13,35);arrow.lineTo(19,32);arrow.lineTo(12,20);arrow.lineTo(25,20);arrow.close();}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(x<0)return;canvas.save();canvas.scale(getResources().getDisplayMetrics().density,getResources().getDisplayMetrics().density);paint.setStyle(Paint.Style.FILL);paint.setColor(Color.WHITE);canvas.drawPath(arrow,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1f);paint.setColor(UiTheme.AMBER);canvas.drawPath(arrow,paint);canvas.restore();}
    }
    @Override public void onBackPressed(){if(pageFullscreen||htmlFullscreen){fullscreenCommand("exit");if(session!=null&&htmlFullscreen)session.exitFullScreen();pageFullscreen=false;return;}if(tools.getVisibility()!=View.VISIBLE)showTools(true);else finish();}
    @Override public boolean dispatchKeyEvent(KeyEvent event){int key=event.getKeyCode();if(key==KeyEvent.KEYCODE_MENU){if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0)showTools(tools.getVisibility()!=View.VISIBLE);return true;}if(tools.getVisibility()!=View.VISIBLE){if(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT||key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_DOWN){if(event.getAction()==KeyEvent.ACTION_DOWN)startCursorMotion(key);else if(event.getAction()==KeyEvent.ACTION_UP)releaseCursorMotion(key);return true;}if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_NUMPAD_ENTER){if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0)clickCursor();return true;}}return super.dispatchKeyEvent(event);}
    @Override protected void onResume(){super.onResume();if(session!=null&&session.isOpen())session.setActive(true);}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(!focused){stopCursorMotion();finishClick(true);}}
    @Override protected void onPause(){stopCursorMotion();finishClick(true);endRemoteHover();handler.removeCallbacks(hideCursor);if(cursor!=null)cursor.setVisibility(View.GONE);if(session!=null&&session.isOpen())session.setActive(false);super.onPause();}
    @Override protected void onDestroy(){stopCursorMotion();finishClick(true);endRemoteHover();destroyed=true;handler.removeCallbacksAndMessages(null);if(session!=null&&session.isOpen()){browser.releaseSession();session.close();}super.onDestroy();}
}
