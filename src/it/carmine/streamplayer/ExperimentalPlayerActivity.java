package it.carmine.streamplayer;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import android.net.Uri;
import androidx.media3.common.*;
import androidx.media3.exoplayer.*;
import androidx.media3.datasource.*;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.ui.PlayerView;
import java.net.URI;

/** Isolated native player for user-authorized direct media URLs. */
public final class ExperimentalPlayerActivity extends Activity {
 private ExoPlayer player;
 private TextView diagnostics;
 @Override public void onCreate(Bundle bundle){super.onCreate(bundle);
  getWindow().getDecorView().setSystemUiVisibility(5894);
  LinearLayout column=new LinearLayout(this);column.setOrientation(LinearLayout.VERTICAL);column.setBackgroundColor(Color.BLACK);
  diagnostics=new TextView(this);diagnostics.setTextColor(Color.WHITE);diagnostics.setTextSize(14);diagnostics.setPadding(20,18,20,18);diagnostics.setText("LAB · Preparazione flusso…");
  column.addView(diagnostics,new LinearLayout.LayoutParams(-1,-2));
  PlayerView view=new PlayerView(this);view.setUseController(true);column.addView(view,new LinearLayout.LayoutParams(-1,0,1));
  Button close=new Button(this);close.setText("← Torna al laboratorio");close.setOnClickListener(v->finish());column.addView(close,new LinearLayout.LayoutParams(-1,-2));setContentView(column);
  String video=getIntent().getStringExtra("stream_url");
  try{
   URI uri=new URI(video);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null)throw new Exception("URL non valido");
   player=new ExoPlayer.Builder(this).build();view.setPlayer(player);
   player.addListener(new Player.Listener(){
    @Override public void onPlaybackStateChanged(int state){if(state==Player.STATE_READY)diagnostics.setText("LAB · Riproduzione pronta");else if(state==Player.STATE_BUFFERING)diagnostics.setText("LAB · Caricamento flusso…");else if(state==Player.STATE_ENDED)diagnostics.setText("LAB · Fine riproduzione");}
    @Override public void onPlayerError(PlaybackException error){
     String message="LAB · Errore riproduzione ("+error.errorCode+")";
     for(Throwable t=error;t!=null;t=t.getCause())if(t instanceof HttpDataSource.InvalidResponseCodeException){message="LAB · HTTP "+((HttpDataSource.InvalidResponseCodeException)t).responseCode+" · accesso non disponibile";break;}
     diagnostics.setText(message);
    }
   });
   java.util.HashMap<String,String> headers=new java.util.HashMap<>();
   String referer=getIntent().getStringExtra("stream_referer"),origin=getIntent().getStringExtra("stream_origin");
   if(referer!=null&&!referer.isEmpty())headers.put("Referer",referer);
   if(origin!=null&&!origin.isEmpty())headers.put("Origin",origin);
   androidx.media3.datasource.okhttp.OkHttpDataSource.Factory http=new androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(VodNetwork.client()).setUserAgent(VodSource.USER_AGENT).setDefaultRequestProperties(headers);
   String lower=uri.getPath()==null?"":uri.getPath().toLowerCase(java.util.Locale.ROOT);
   String mime=lower.endsWith(".mpd")?MimeTypes.APPLICATION_MPD:lower.endsWith(".mp4")?MimeTypes.VIDEO_MP4:MimeTypes.APPLICATION_M3U8;
   MediaItem item=new MediaItem.Builder().setUri(Uri.parse(video)).setMimeType(mime).build();
   if(mime.equals(MimeTypes.APPLICATION_MPD))player.setMediaSource(new androidx.media3.exoplayer.dash.DashMediaSource.Factory(http).createMediaSource(item));
   else if(mime.equals(MimeTypes.VIDEO_MP4))player.setMediaSource(new androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(http).createMediaSource(item));
   else player.setMediaSource(new androidx.media3.exoplayer.hls.HlsMediaSource.Factory(http).createMediaSource(item));
   player.prepare();player.play();
  }catch(Exception e){diagnostics.setText("LAB · URL video non valido");}
 }
 @Override protected void onStop(){if(player!=null){player.release();player=null;}super.onStop();}
}
