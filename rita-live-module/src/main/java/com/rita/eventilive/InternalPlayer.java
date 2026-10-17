package com.rita.eventilive;

import android.app.AlertDialog;import android.graphics.Color;import android.view.*;import android.webkit.*;import android.widget.*;
import androidx.media3.common.*;import androidx.media3.datasource.*;import androidx.media3.exoplayer.ExoPlayer;import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import org.json.*;import java.io.IOException;import java.util.*;import java.util.concurrent.atomic.AtomicBoolean;

/** ExoPlayer 1.10.1, as in R. ITA 2.1.30, with a dedicated diagnostic panel. */
public final class InternalPlayer {
 public static final String MODE="WebView → Player interno";
 private static volatile InternalPlayer active;
 private final MainActivity owner;private final MainActivity.Run run;
 private final AtomicBoolean captured=new AtomicBoolean();private volatile boolean released;
 private final List<WebResourceRequest> pendingEvents=Collections.synchronizedList(new ArrayList<>());
 private ExoPlayer player;private TextView info,position;private Button pause;private FrameLayout stage;private SurfaceView surface;
 private final Runnable hideControls=()->{if(this.controlsScroll!=null)this.controlsScroll.setVisibility(View.GONE);if(info!=null)info.setVisibility(View.GONE);if(position!=null)position.setVisibility(View.GONE);};
 private void showControls(){if(controlsScroll!=null)controlsScroll.setVisibility(View.VISIBLE);if(info!=null)info.setVisibility(View.VISIBLE);if(position!=null)position.setVisibility(View.VISIBLE);owner.main.removeCallbacks(hideControls);owner.main.postDelayed(hideControls,5000);}
 private LinearLayout controls;private HorizontalScrollView controlsScroll;private boolean packedSegments;private VideoSize size=VideoSize.UNKNOWN;private long captureAt;private String signedUrl,mime,referer,origin;
 private final Runnable sample=new Runnable(){public void run(){
  if(released||player==null)return;
  owner.log(run,"media.sample",owner.obj("source","internal","currentTime",player.getCurrentPosition()/1000.0,"paused",!player.isPlaying(),"muted",player.getVolume()==0,"volume",player.getVolume(),"width",size.width,"height",size.height,"readyState",player.getPlaybackState()==Player.STATE_READY?4:0,"bufferedPositionMs",player.getBufferedPosition(),"live",player.isCurrentMediaItemLive()));
  position.setText((player.isCurrentMediaItemLive()?"LIVE • ":"")+String.format(Locale.ROOT,"%02d:%02d",player.getCurrentPosition()/60000,(player.getCurrentPosition()/1000)%60));pause.setText(player.getPlayWhenReady()?"Pausa":"Riprendi");owner.main.postDelayed(this,5000);
 }};
 private InternalPlayer(MainActivity o,MainActivity.Run r){owner=o;run=r;}
 public static void arm(MainActivity owner,MainActivity.Run run){
  if(!MODE.equals(run.engine))return;InternalPlayer expected=new InternalPlayer(owner,run);active=expected;
  owner.log(run,"internal.resolverStart",owner.obj("version","Media3/ExoPlayer 1.10.1","url",run.url,"webViewPolicy","kept for session; muted and paused after capture","rawTokens","in memory only"));
  owner.main.postDelayed(()->{if(active==expected&&!expected.released&&!expected.captured.get()){owner.onNoStream(run);owner.log(run,"internal.noStream",owner.obj("waitSeconds",run.daddy?90:45,"reason","no recognized HLS/DASH request captured"));}},run.daddy?90000:45000);
 }
 public static void direct(MainActivity owner,MainActivity.Run run,DaddyResolver.Result result){InternalPlayer c=active;if(c==null||c.owner!=owner||c.run!=run||c.released||!c.captured.compareAndSet(false,true))return;c.signedUrl=result.playlist;c.mime=StreamCandidate.mediaMime(result.playlist,"GET");c.referer=result.referer;java.net.URI ref=java.net.URI.create(result.referer);c.origin=ref.getScheme()+"://"+ref.getAuthority();c.packedSegments=result.packed&&"application/x-mpegURL".equals(c.mime);owner.log(run,"daddy.directResolved",owner.obj("url",result.playlist,"referer",result.referer,"packedSegments",result.packed));c.open();}
 public static void capture(MainActivity owner,MainActivity.Run run,WebResourceRequest request){
  InternalPlayer current=active;if(current==null||current.owner!=owner||current.run!=run||current.released)return;
  String url=request.getUrl().toString(),ref=NativeFetchClient.header(request.getRequestHeaders(),"Referer");String type=StreamCandidate.mime(url,request.getMethod(),ref);if(type==null&&run.daddy&&url.equals(run.url))type=StreamCandidate.mediaMime(url,request.getMethod());if(type==null&&run.daddy&&owner.isDaddyMediaOrigin(ref))type=StreamCandidate.mediaMime(url,request.getMethod());
  if(type==null&&!run.daddy){String candidate=StreamCandidate.mediaMime(url,request.getMethod());if(candidate!=null){if(owner.isEventMediaOrigin(ref))type=candidate;else{synchronized(current.pendingEvents){if(current.pendingEvents.size()<16&&!current.pendingEvents.contains(request))current.pendingEvents.add(request);}return;}}}
  if(type==null||!current.captured.compareAndSet(false,true))return;
  current.signedUrl=url;current.mime=type;current.referer=ref==null?run.url:ref;current.origin=NativeFetchClient.header(request.getRequestHeaders(),"Origin");
  owner.log(run,"internal.streamCaptured",owner.obj("url",url,"mime",type,"referer",ref,"queryPresent",request.getUrl().getQuery()!=null,"candidatePolicy","first HLS/DASH GET from recognized player origin"));
  owner.main.post(()->{if(active==current&&owner.current==run&&!current.released)current.open();});
 }
 public static void retryObserved(MainActivity owner,MainActivity.Run run){InternalPlayer c=active;if(c==null||c.owner!=owner||c.run!=run||run.daddy||c.released||c.captured.get())return;List<WebResourceRequest> pending;synchronized(c.pendingEvents){pending=new ArrayList<>(c.pendingEvents);c.pendingEvents.clear();}for(WebResourceRequest request:pending)capture(owner,run,request);}
 public static void closeWeb(MainActivity owner){InternalPlayer c=active;if(c!=null&&c.owner==owner&&c.player!=null)c.closeResolver();}
 public static void pauseForBackground(MainActivity owner){InternalPlayer c=active;if(c!=null&&c.owner==owner&&c.player!=null)c.player.pause();}
 public static boolean key(MainActivity owner,KeyEvent e){InternalPlayer c=active;if(c==null||c.owner!=owner||c.player==null)return false;
  if(e.getAction()!=KeyEvent.ACTION_DOWN)return false;int k=e.getKeyCode();
  if(k==KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE){if(c.player.isPlaying())c.player.pause();else c.player.play();return true;}
  if(k==KeyEvent.KEYCODE_MEDIA_REWIND){c.seek(-15000);return true;}if(k==KeyEvent.KEYCODE_MEDIA_FAST_FORWARD){c.seek(15000);return true;}
  if(c.controlsScroll!=null&&c.controlsScroll.getVisibility()!=View.VISIBLE&&(k==21||k==22)){c.seek(k==21?-15000:15000);return true;}
  if(c.controlsScroll!=null&&c.controlsScroll.getVisibility()!=View.VISIBLE&&(k==19||k==20||k==23)){c.showControls();c.pause.requestFocus();return true;}return false;
 }
 public static void release(MainActivity owner){InternalPlayer current=active;if(current==null||current.owner!=owner)return;active=null;current.released=true;owner.main.removeCallbacks(current.sample);owner.main.removeCallbacks(current.hideControls);if(current.player!=null){current.player.release();current.player=null;}current.signedUrl=null;}
 private void open(){captureAt=System.currentTimeMillis();try{
  Map<String,String> defaults=new LinkedHashMap<>();defaults.put("Referer",referer);if(origin!=null&&StreamCandidate.playerOrigin(origin))defaults.put("Origin",origin);
  DefaultHttpDataSource.Factory http=new DefaultHttpDataSource.Factory().setUserAgent(run.daddy?owner.daddyUserAgent():NativeFetchClient.UA).setConnectTimeoutMs(15000).setReadTimeoutMs(run.daddy?30000:15000).setDefaultRequestProperties(defaults);
  DataSource.Factory upstream=DnsTransport.enabled(owner)?new androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(DnsTransport.client(owner,run).newBuilder().readTimeout(run.daddy?30:15,java.util.concurrent.TimeUnit.SECONDS).build()).setUserAgent(run.daddy?owner.daddyUserAgent():NativeFetchClient.UA).setDefaultRequestProperties(defaults):http;
  DataSource.Factory data=new ResolvingDataSource.Factory(upstream,spec->{
   if(!"https".equalsIgnoreCase(spec.uri.getScheme()))throw new IOException("Internal player permits HTTPS media requests only");
   Map<String,String> headers=new LinkedHashMap<>(spec.httpRequestHeaders);headers.remove("Cookie");String cookie=CookieManager.getInstance().getCookie(spec.uri.toString());if(cookie!=null&&!cookie.isEmpty())headers.put("Cookie",cookie);
   owner.log(run,"internal.networkRequest",owner.obj("url",spec.uri.toString(),"method",spec.getHttpMethodString(),"cookiePresent",cookie!=null&&!cookie.isEmpty()));return spec.withRequestHeaders(headers);
  });
  player=new ExoPlayer.Builder(owner).setMediaSourceFactory(new DefaultMediaSourceFactory(data)).setSeekBackIncrementMs(15000).setSeekForwardIncrementMs(15000).build();
  player.setAudioAttributes(new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(),true);player.setHandleAudioBecomingNoisy(true);player.setVolume(1);player.setTrackSelectionParameters(player.getTrackSelectionParameters().buildUpon().setPreferredAudioLanguage("it").build());
  buildPanel();player.setVideoSurfaceView(surface);
  player.addListener(new Player.Listener(){
   public void onPlaybackStateChanged(int state){String label=state==Player.STATE_READY?"Pronto":state==Player.STATE_BUFFERING?"Caricamento…":state==Player.STATE_ENDED?"Terminato":"In attesa";info.setText("Player interno • "+label);owner.status.setText("Player interno • "+run.title+" • "+label);owner.log(run,"internal.state",owner.obj("state",state,"label",label));}
   public void onIsPlayingChanged(boolean value){owner.log(run,value?"media.playing":"internal.paused",owner.obj("source","internal","paused",!value,"muted",player.getVolume()==0,"volume",player.getVolume(),"elapsedFromCaptureMs",System.currentTimeMillis()-captureAt));}
   public void onRenderedFirstFrame(){owner.main.postDelayed(()->{if(active==InternalPlayer.this&&!released)closeResolver();},1200);owner.log(run,"internal.firstFrame",owner.obj("elapsedFromCaptureMs",System.currentTimeMillis()-captureAt));}
   public void onVideoSizeChanged(VideoSize v){size=v;fit();owner.log(run,"internal.videoSize",owner.obj("width",v.width,"height",v.height));}
   public void onTracksChanged(Tracks v){recordTracks(v);}
   public void onPlayerError(PlaybackException error){showControls();int status=0;for(Throwable c=error;c!=null;c=c.getCause())if(c instanceof HttpDataSource.InvalidResponseCodeException)status=((HttpDataSource.InvalidResponseCodeException)c).responseCode;if(run.daddy&&owner.retryDaddyStartup(run,error.errorCode,status))return;info.setText("Player interno • "+error.getErrorCodeName()+" • torna a WebView o riprova");owner.log(run,"internal.error",owner.obj("code",error.errorCode,"name",error.getErrorCodeName(),"status",status,"message",error.getMessage(),"webViewRetained",owner.web!=null));}
  });
  MediaItem item=new MediaItem.Builder().setUri(signedUrl).setMimeType(mime).build();if(packedSegments){androidx.media3.exoplayer.hls.HlsMediaSource.Factory adapted=new androidx.media3.exoplayer.hls.HlsMediaSource.Factory(type->{DataSource raw=data.createDataSource();return type==C.DATA_TYPE_MEDIA?new GomstreamDataSource(raw):type==C.DATA_TYPE_MANIFEST?new GomstreamManifestDataSource(raw):raw;});player.setMediaSource(adapted.createMediaSource(item));}else player.setMediaItem(item);player.prepare();player.play();owner.main.post(sample);owner.log(run,"internal.open",owner.obj("version","1.10.1","seekIncrementMs",15000,"volume",1,"audioFocus",true,"webViewRetained",owner.web!=null));
 }catch(Exception error){initializationError(error);}catch(LinkageError error){initializationError(error);}}
 private void initializationError(Throwable error){owner.dismissResolverCover();owner.setPlayerFullscreen(false);owner.log(run,"internal.error",owner.obj("stage","initialization","message",error.toString()));if(player!=null){player.release();player=null;}if(owner.web!=null)owner.web.setVisibility(View.VISIBLE);owner.status.setText("Player interno non avviato • prova WebView assistita");}
 private void buildPanel(){
  owner.dismissResolverCover();owner.setPlayerFullscreen(true);
  if(owner.web!=null){owner.web.evaluateJavascript("window.postMessage(\"rita-native-handoff\",\"*\");",null);owner.web.setVisibility(View.GONE);}
  FrameLayout panel=new FrameLayout(owner);panel.setBackgroundColor(Color.BLACK);
  stage=new FrameLayout(owner);panel.addView(stage,new FrameLayout.LayoutParams(-1,-1));surface=new SurfaceView(owner);stage.addView(surface,new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER));stage.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(r-l!=or-ol||b-t!=ob-ot)fit();});
  info=owner.label(run.title+" • apertura…",15);info.setBackgroundColor(0xb3151619);panel.addView(info,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP));
  LinearLayout chrome=new LinearLayout(owner);chrome.setOrientation(LinearLayout.VERTICAL);chrome.setBackgroundColor(0xdd151619);panel.addView(chrome,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));
  controls=new LinearLayout(owner);controlsScroll=new HorizontalScrollView(owner);controlsScroll.setHorizontalScrollBarEnabled(false);controlsScroll.addView(controls);chrome.addView(controlsScroll);
  pause=owner.button("Pausa",()->{if(player!=null){if(player.getPlayWhenReady())player.pause();else player.play();}showControls();});controls.addView(pause);controls.addView(owner.button("−15s",()->{seek(-15000);showControls();}));controls.addView(owner.button("+15s",()->{seek(15000);showControls();}));controls.addView(owner.button("Diretta",()->{if(player!=null)player.seekToDefaultPosition();showControls();}));controls.addView(owner.button("Audio",this::audio));controls.addView(owner.button("Menu",()->owner.showPlaybackMenu()));controls.addView(owner.button("WebView",()->owner.start("WebView assistita")));controls.addView(owner.button("Riprova",()->owner.start(MODE)));
  for(int i=0;i<controls.getChildCount();i++){View view=controls.getChildAt(i);View.OnFocusChangeListener original=view.getOnFocusChangeListener();view.setOnFocusChangeListener((v,focus)->{if(original!=null)original.onFocusChange(v,focus);if(focus)showControls();});}
  position=owner.label("LIVE",12);chrome.addView(position);surface.setOnClickListener(v->{if(controlsScroll.getVisibility()==View.VISIBLE){owner.main.removeCallbacks(hideControls);hideControls.run();}else showControls();});owner.browserBox.addView(panel,new FrameLayout.LayoutParams(-1,-1));showControls();
 }
 private void fit(){if(surface==null||stage==null||size.width<=0||size.height<=0)return;int w=stage.getWidth(),h=stage.getHeight();if(w<=0||h<=0)return;double ratio=(double)size.width*size.pixelWidthHeightRatio/size.height;int tw=w,th=(int)(w/ratio);if(th>h){th=h;tw=(int)(h*ratio);}surface.setLayoutParams(new FrameLayout.LayoutParams(tw,th,Gravity.CENTER));}
 private void seek(long amount){if(player==null||!player.isCurrentMediaItemSeekable())return;long value=Math.max(0,player.getCurrentPosition()+amount),duration=player.getDuration();if(duration!=C.TIME_UNSET)value=Math.min(value,duration);player.seekTo(value);}
 private void recordTracks(Tracks tracks){JSONArray formats=new JSONArray();for(Tracks.Group group:tracks.getGroups())for(int i=0;i<group.length;i++){Format f=group.getTrackFormat(i);formats.put(owner.obj("type",group.getType(),"selected",group.isTrackSelected(i),"supported",group.isTrackSupported(i),"language",f.language,"mime",f.sampleMimeType,"codecs",f.codecs,"channels",f.channelCount,"width",f.width,"height",f.height));}owner.log(run,"internal.tracks",owner.obj("tracks",formats,"audioOutputVerified",false));}
 private void audio(){if(player==null)return;List<String> labels=new ArrayList<>();List<TrackSelectionOverride> choices=new ArrayList<>();labels.add("Automatico • italiano preferito");choices.add(null);for(Tracks.Group group:player.getCurrentTracks().getGroups())if(group.getType()==C.TRACK_TYPE_AUDIO)for(int i=0;i<group.length;i++)if(group.isTrackSupported(i)){Format f=group.getTrackFormat(i);labels.add((f.label==null?"Traccia "+labels.size():f.label)+" • "+(f.language==null?"lingua non indicata":f.language));choices.add(new TrackSelectionOverride(group.getMediaTrackGroup(),i));}new AlertDialog.Builder(owner).setTitle("Audio").setItems(labels.toArray(new String[0]),(d,index)->{if(player==null)return;TrackSelectionParameters.Builder b=player.getTrackSelectionParameters().buildUpon().clearOverridesOfType(C.TRACK_TYPE_AUDIO).setPreferredAudioLanguage("it");if(choices.get(index)!=null)b.setOverrideForType(choices.get(index));player.setTrackSelectionParameters(b.build());}).setNegativeButton("Chiudi",null).show();}
 private void closeResolver(){if(owner.web==null)return;WebView web=owner.web;owner.web=null;owner.browserBox.removeView(web);if(owner.pointer!=null){owner.browserBox.removeView(owner.pointer);owner.pointer=null;}web.stopLoading();web.destroy();owner.log(run,"internal.resolverClosed",owner.obj("playerContinues",true,"warning","page session renewal stops; later expiry remains observable"));info.setText(run.title+" • Player interno");}
}
