package it.carmine.streamplayer;
import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.net.Uri;
import android.widget.*;
import androidx.media3.common.*;
import androidx.media3.exoplayer.*;
import androidx.media3.ui.PlayerView;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import it.carmine.streamplayer.lab211.*;
import java.util.*;

/** Separate player: no changes to ExperimentalPlayerActivity used by the original SC route. */
public final class LabMultiPlayerActivity extends Activity {
 private ExoPlayer player;private TextView status;private PlayerView view;private boolean ready;
 private long position;private boolean playing=true;private LabVidXSession vidSession;private String refreshFailure="";private int sessionGeneration;
 @Override public void onCreate(Bundle state){super.onCreate(state);if(state!=null){position=state.getLong("position",0);playing=state.getBoolean("playing",true);}getWindow().getDecorView().setSystemUiVisibility(5894);LinearLayout column=new LinearLayout(this);column.setOrientation(1);column.setBackgroundColor(Color.BLACK);status=new TextView(this);status.setTextColor(Color.WHITE);status.setPadding(18,10,18,10);status.setText("PLAYBACK · preparazione");column.addView(status);view=new PlayerView(this);view.setUseController(true);view.setControllerShowTimeoutMs(4000);column.addView(view,new LinearLayout.LayoutParams(-1,0,1));Button tracks=new Button(this);tracks.setText("Audio e sottotitoli disponibili");tracks.setOnClickListener(v->chooseTracks());column.addView(tracks);setContentView(column);}
 @Override protected void onStart(){super.onStart();start();}
 private void start(){if(player!=null)return;try{
  String url=getIntent().getStringExtra("stream_url"),mime=getIntent().getStringExtra("stream_mime"),referer=getIntent().getStringExtra("stream_referer");LabPipeline.https(url);if(!Arrays.asList(LabPipeline.HLS,LabPipeline.DASH,LabPipeline.MP4).contains(mime))throw new Exception();
  Map<String,String> headers=new HashMap<>();if(referer!=null&&!referer.isEmpty()){LabPipeline.https(referer);headers.put("Referer",referer);}
  String vidId=getIntent().getStringExtra("stream_vidx_id");okhttp3.OkHttpClient client=new LabHttp().client();
  if(vidId!=null&&!vidId.isEmpty()){int current=++sessionGeneration;vidSession=new LabVidXSession(url,vidId,code->runOnUiThread(()->{if(current!=sessionGeneration)return;if(code.startsWith("VIDXGO_REFRESH_"))refreshFailure=code;status.setText("SOURCE · "+code);}));client=vidSession.client();}
  OkHttpDataSource.Factory http=new OkHttpDataSource.Factory(client).setUserAgent("Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 Chrome/128.0.0.0 Safari/537.36").setDefaultRequestProperties(headers);
  player=new ExoPlayer.Builder(this).setMediaSourceFactory(new DefaultMediaSourceFactory(http)).build();view.setPlayer(player);player.setTrackSelectionParameters(player.getTrackSelectionParameters().buildUpon().setPreferredAudioLanguage("it").build());
  MediaItem.Builder item=new MediaItem.Builder().setUri(url).setMimeType(mime);List<MediaItem.SubtitleConfiguration> subtitles=new ArrayList<>();ArrayList<String> urls=getIntent().getStringArrayListExtra("sub_urls"),langs=getIntent().getStringArrayListExtra("sub_langs"),labels=getIntent().getStringArrayListExtra("sub_labels"),mimes=getIntent().getStringArrayListExtra("sub_mimes");if(urls!=null&&langs!=null&&labels!=null&&mimes!=null&&urls.size()==langs.size()&&urls.size()==labels.size()&&urls.size()==mimes.size())for(int i=0;i<urls.size();i++){LabPipeline.https(urls.get(i));subtitles.add(new MediaItem.SubtitleConfiguration.Builder(Uri.parse(urls.get(i))).setMimeType(mimes.get(i)).setLanguage(langs.get(i)).setLabel(labels.get(i)).build());}item.setSubtitleConfigurations(subtitles);
  player.addListener(new Player.Listener(){public void onPlaybackStateChanged(int state){if(state==Player.STATE_READY){ready=true;status.setText("PLAYBACK · READY · "+trackSummary());}else if(state==Player.STATE_BUFFERING)status.setText("PLAYBACK · BUFFERING");else if(state==Player.STATE_ENDED)status.setText("PLAYBACK · ENDED");}public void onRenderedFirstFrame(){status.setText("PLAYBACK · FIRST_FRAME · "+trackSummary());}public void onTracksChanged(Tracks tracks){if(ready)status.setText("PLAYBACK · READY · "+trackSummary());}public void onPlayerError(PlaybackException e){String code=e.getErrorCodeName();for(Throwable t=e;t!=null;t=t.getCause())if(t instanceof HttpDataSource.InvalidResponseCodeException)code="HTTP_"+((HttpDataSource.InvalidResponseCodeException)t).responseCode;status.setText("PLAYBACK · "+(refreshFailure.isEmpty()?code:refreshFailure));}});
  player.setMediaItem(item.build(),position);player.prepare();player.setPlayWhenReady(playing);
 }catch(Exception e){status.setText("PLAYBACK · configurazione non valida · "+e.getClass().getSimpleName());}}
 private String trackSummary(){int audio=0,text=0;if(player!=null)for(Tracks.Group group:player.getCurrentTracks().getGroups())for(int i=0;i<group.length;i++)if(group.isTrackSupported(i)){if(group.getType()==C.TRACK_TYPE_AUDIO)audio++;if(group.getType()==C.TRACK_TYPE_TEXT)text++;}return audio+" tracce audio · "+text+" sottotitoli";}
 private void chooseTracks(){if(player==null)return;List<String> labels=new ArrayList<>();List<TrackSelectionOverride> choices=new ArrayList<>();labels.add("Sottotitoli disattivati");choices.add(null);for(Tracks.Group group:player.getCurrentTracks().getGroups())if(group.getType()==C.TRACK_TYPE_AUDIO||group.getType()==C.TRACK_TYPE_TEXT)for(int i=0;i<group.length;i++)if(group.isTrackSupported(i)){Format f=group.getTrackFormat(i);labels.add((group.getType()==C.TRACK_TYPE_AUDIO?"Audio":"Sottotitoli")+" · "+(f.language==null?"und":f.language)+" · "+(f.label==null?"Traccia "+(i+1):f.label));choices.add(new TrackSelectionOverride(group.getMediaTrackGroup(),i));}new AlertDialog.Builder(this).setTitle("Tracce dichiarate dal media").setItems(labels.toArray(new String[0]),(d,index)->{if(player==null)return;TrackSelectionParameters.Builder b=player.getTrackSelectionParameters().buildUpon();TrackSelectionOverride choice=choices.get(index);if(choice==null)b.setTrackTypeDisabled(C.TRACK_TYPE_TEXT,true).clearOverridesOfType(C.TRACK_TYPE_TEXT);else b.setTrackTypeDisabled(choice.getType(),false).setOverrideForType(choice);player.setTrackSelectionParameters(b.build());}).setNegativeButton("Chiudi",null).show();}
 @Override protected void onSaveInstanceState(Bundle out){out.putLong("position",player==null?position:player.getCurrentPosition());out.putBoolean("playing",player==null?playing:player.getPlayWhenReady());super.onSaveInstanceState(out);}
 @Override protected void onStop(){sessionGeneration++;if(vidSession!=null){vidSession.close();vidSession=null;}if(player!=null){position=player.getCurrentPosition();playing=player.getPlayWhenReady();view.setPlayer(null);player.release();player=null;ready=false;}refreshFailure="";super.onStop();}
 @Override public void onBackPressed(){if(view.isControllerFullyVisible()){view.hideController();}else super.onBackPressed();}
}
