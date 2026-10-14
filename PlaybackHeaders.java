package it.carmine.streamplayer.lab211;
import java.util.*;
/** Host-specific playback context; credentials are never borrowed from another app. */
public final class PlaybackHeaders {
 public static String userAgent(String host){if("VixCloud".equals(host))return it.carmine.streamplayer.ScPlaylistConfig.USER_AGENT;if("DoodStream".equals(host))return "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1";String version="VidXGo".equals(host)?"120":"SuperVideo".equals(host)?"121":"StreamWish".equals(host)?"131":"124";return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/"+version+".0.0.0 Safari/537.36";}
 public static Map<String,String> forHost(String host,String referer){Map<String,String> h=new LinkedHashMap<>();h.put("User-Agent",userAgent(host));h.put("Accept","*/*");if(!"MixDrop".equals(host)&&referer!=null&&!referer.isEmpty())h.put("Referer",referer);if("DoodStream".equals(host)||"StreamWish".equals(host))try{h.put("Origin",LabPipeline.origin(referer));}catch(Exception ignored){}return Collections.unmodifiableMap(h);}
 private PlaybackHeaders(){}
}
