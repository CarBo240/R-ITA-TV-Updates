package it.carmine.streamplayer;

import android.app.*;import android.content.*;import android.os.*;import android.graphics.Color;import android.view.*;import android.widget.*;
import androidx.media3.common.*;import androidx.media3.datasource.*;import androidx.media3.exoplayer.*;import androidx.media3.exoplayer.hls.HlsMediaSource;import androidx.media3.ui.PlayerView;
import java.util.concurrent.*;

/** Native full-screen player with source re-resolution after a playback failure. */
public class GomstreamPlayerActivity extends Activity {
 private ExoPlayer player;private PlayerView view;private FrameLayout root;private LinearLayout tools;private TextView status;private ProgressBar progress;
 private final ExecutorService worker=Executors.newSingleThreadExecutor();private Future<?> task;private final Handler ui=new Handler(Looper.getMainLooper());
 private boolean destroyed,resumePlayback,active,consumeOkUp;private int generation,retries;private GomstreamSource.Channel channel;
 protected boolean engineEnabled(){return true;}
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setMinWidth(0);b.setMinimumWidth(0);return b;}
 @Override public void onCreate(Bundle state){super.onCreate(state);String id=getIntent().getStringExtra("id"),name=getIntent().getStringExtra("title");if(id==null||!id.matches("[0-9]{1,8}")){finish();return;}channel=new GomstreamSource.Channel(id,name==null?"Gomstream":name);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().getDecorView().setSystemUiVisibility(5894);root=new FrameLayout(this);root.setBackgroundColor(Color.BLACK);setContentView(root);
  view=new PlayerView(this);view.setKeepScreenOn(true);view.setControllerShowTimeoutMs(3500);view.setControllerAutoShow(false);view.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);root.addView(view,new FrameLayout.LayoutParams(-1,-1));
  progress=new ProgressBar(this);progress.setTag("gomBuffering");root.addView(progress,new FrameLayout.LayoutParams(dp(56),dp(56),Gravity.CENTER));
  tools=new LinearLayout(this);tools.setOrientation(1);tools.setPadding(dp(12),dp(10),dp(12),dp(10));tools.setBackgroundColor(0xee151619);tools.setTag("gomPlayerMenu");root.addView(tools,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP));
  status=new TextView(this);status.setTextColor(Color.WHITE);status.setTextSize(16);status.setText(channel.name+" · apertura…");tools.addView(status);
  LinearLayout row=new LinearLayout(this);tools.addView(row,new LinearLayout.LayoutParams(-1,dp(48)));Button reload=button("Riprova");row.addView(reload,new LinearLayout.LayoutParams(0,-1,1));reload.setOnClickListener(v->{retries=0;resolve();});Button web=button("Apri con Gecko");row.addView(web,new LinearLayout.LayoutParams(0,-1,1));web.setOnClickListener(v->{if(player!=null)player.pause();startActivity(new Intent(this,GeckoEventActivity.class).putExtra("url",new DaddyLiveSource.Link(channel.name,channel.id).embedUrl(DaddyLiveSettings.home(this))).putExtra("title",channel.name).putExtra("daddyLive",true));});Button close=button("Chiudi");row.addView(close,new LinearLayout.LayoutParams(0,-1,1));close.setOnClickListener(v->finish());
  tools.setVisibility(View.GONE);view.requestFocus();if(!engineEnabled())return;
  player=new ExoPlayer.Builder(this).build();view.setPlayer(player);
  player.addListener(new Player.Listener(){
   @Override public void onPlaybackStateChanged(int state){if(state==Player.STATE_READY){progress.setVisibility(View.GONE);retries=0;status.setText(channel.name);}else if(state==Player.STATE_BUFFERING)progress.setVisibility(View.VISIBLE);}
   @Override public void onPlayerError(PlaybackException error){if(destroyed)return;status.setText(channel.name+" · recupero della sorgente…");progress.setVisibility(View.VISIBLE);if(retries++<2&&active){ui.postDelayed(()->{if(!destroyed&&active)resolve();},retries*2000L);}else showError("Riproduzione non disponibile · Riprova oppure Apri con Gecko");}
  });resolve();
 }
 private void showError(String s){progress.setVisibility(View.GONE);status.setText(channel.name+" · "+s);tools.setVisibility(View.VISIBLE);tools.getChildAt(1).requestFocus();}
 private void resolve(){if(player==null||destroyed)return;final int request=++generation;final String home=DaddyLiveSettings.home(this),gom=GomstreamSettings.home(this);progress.setVisibility(View.VISIBLE);tools.setVisibility(View.GONE);player.stop();status.setText(channel.name+" · aggiornamento sorgente…");
  if(task!=null)task.cancel(true);
  task=worker.submit(()->{try{String url=GomstreamSource.resolve(channel,home,gom,LiveNetwork.gom(this));runOnUiThread(()->{if(destroyed||request!=generation)return;if(!home.equals(DaddyLiveSettings.home(this))||!gom.equals(GomstreamSettings.home(this))){resolve();return;}
   androidx.media3.datasource.okhttp.OkHttpDataSource.Factory http=new androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(LiveNetwork.client(this)).setUserAgent("R-ITA-TV/1.25");
   HlsMediaSource.Factory factory=new HlsMediaSource.Factory(type->{DataSource upstream=http.createDataSource();return type==C.DATA_TYPE_MEDIA?new GomstreamDataSource(upstream):type==C.DATA_TYPE_MANIFEST?new GomstreamManifestDataSource(upstream):upstream;});
   MediaItem item=new MediaItem.Builder().setUri(url).setMimeType(MimeTypes.APPLICATION_M3U8).setMediaId(channel.id).setLiveConfiguration(new MediaItem.LiveConfiguration.Builder().setTargetOffsetMs(18000).setMinPlaybackSpeed(1f).setMaxPlaybackSpeed(1f).build()).build();
   player.setMediaSource(factory.createMediaSource(item));player.prepare();player.setPlayWhenReady(active);resumePlayback=!active;view.requestFocus();
  });}catch(Exception e){runOnUiThread(()->{if(destroyed||request!=generation)return;showError("Fonte non disponibile · "+e.getMessage());});}});
 }
 @Override public void onBackPressed(){if(tools.getVisibility()!=View.VISIBLE){progress.setVisibility(View.GONE);status.setText(channel.name);tools.setVisibility(View.VISIBLE);tools.getChildAt(1).requestFocus();}else finish();}
 @Override public boolean dispatchKeyEvent(KeyEvent e){int key=e.getKeyCode();if((key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER)&&consumeOkUp){if(e.getAction()==KeyEvent.ACTION_UP)consumeOkUp=false;return true;}if(key==KeyEvent.KEYCODE_MENU){if(e.getAction()==KeyEvent.ACTION_DOWN&&e.getRepeatCount()==0){boolean show=tools.getVisibility()!=View.VISIBLE;tools.setVisibility(show?View.VISIBLE:View.GONE);if(show)tools.getChildAt(1).requestFocus();else view.requestFocus();}return true;}
  if(tools.getVisibility()!=View.VISIBLE&&e.getAction()==KeyEvent.ACTION_DOWN&&(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER)&&!view.isControllerFullyVisible()){consumeOkUp=true;view.showController();return true;}return super.dispatchKeyEvent(e);
 }
 @Override protected void onResume(){super.onResume();active=true;if(player!=null&&resumePlayback){player.play();resumePlayback=false;}}
 @Override protected void onPause(){active=false;if(player!=null){resumePlayback=player.getPlayWhenReady();player.pause();}super.onPause();}
 @Override protected void onDestroy(){destroyed=true;++generation;ui.removeCallbacksAndMessages(null);if(task!=null)task.cancel(true);worker.shutdownNow();if(player!=null){view.setPlayer(null);player.release();player=null;}super.onDestroy();}
}
