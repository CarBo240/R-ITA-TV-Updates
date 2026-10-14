package it.carmine.streamplayer;

import android.app.*;import android.graphics.Color;import android.net.Uri;import android.os.*;import android.view.*;import android.widget.*;
import androidx.media3.common.*;import androidx.media3.exoplayer.*;import androidx.media3.exoplayer.hls.HlsMediaSource;import androidx.media3.ui.PlayerView;

/** Separate LAB player for the VixCloud loopback transport. */
public final class LabScProxyPlayerActivity extends Activity {
 private ExoPlayer player;private LabHlsProxy proxy;private TextView status;private boolean failed;
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(5894);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.BLACK);status=new TextView(this);status.setTextColor(Color.WHITE);status.setPadding(20,18,20,18);status.setText("LAB · Avvio proxy HLS…");root.addView(status);PlayerView view=new PlayerView(this);root.addView(view,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
  try{String remote=getIntent().getStringExtra("stream_url"),referer=getIntent().getStringExtra("stream_referer");proxy=new LabHlsProxy(referer);String local=proxy.url(remote);player=new ExoPlayer.Builder(this).build();view.setPlayer(player);player.addListener(new Player.Listener(){@Override public void onPlaybackStateChanged(int s){if(failed)return;status.setText(s==Player.STATE_READY?"LAB · VixCloud pronto":s==Player.STATE_BUFFERING?"LAB · Proxy HLS · caricamento…":"LAB · VixCloud");}@Override public void onPlayerError(PlaybackException e){failed=true;status.setText("LAB · Proxy HLS · "+e.getErrorCodeName()+(proxy==null||proxy.diagnostic().isEmpty()?"":" · "+proxy.diagnostic()));}});MediaItem item=new MediaItem.Builder().setUri(Uri.parse(local)).setMimeType(MimeTypes.APPLICATION_M3U8).build();player.setMediaSource(new HlsMediaSource.Factory(new androidx.media3.datasource.DefaultHttpDataSource.Factory()).createMediaSource(item));player.prepare();player.play();}catch(Exception e){status.setText("LAB · Proxy non avviato: "+e.getMessage());}
 }
 @Override protected void onDestroy(){if(player!=null)player.release();if(proxy!=null)proxy.close();super.onDestroy();}
}
