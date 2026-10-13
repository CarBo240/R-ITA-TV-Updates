package com.rita.eventilive;
import java.net.URI;import java.util.Locale;
final class StreamCandidate {
 static boolean playerOrigin(String text){try{URI u=new URI(text);String h=u.getHost();if(h==null)return false;h=h.toLowerCase(Locale.ROOT);return "https".equalsIgnoreCase(u.getScheme())&&u.getUserInfo()==null&&(h.equals("missflight.net")||h.endsWith(".missflight.net")||h.endsWith(".dyntheme.net"));}catch(Exception e){return false;}}
 static String mediaMime(String url,String method){try{URI u=new URI(url);String p=u.getPath();if(!"GET".equals(method)||!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null||p==null)return null;p=p.toLowerCase(Locale.ROOT);return p.endsWith(".m3u8")?"application/x-mpegURL":p.endsWith(".mpd")?"application/dash+xml":null;}catch(Exception e){return null;}}
 static String mime(String url,String method,String referer){try{URI u=new URI(url);String p=u.getPath();if(!"GET".equals(method)||!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null||!playerOrigin(referer)||p==null)return null;p=p.toLowerCase(Locale.ROOT);return p.endsWith(".m3u8")?"application/x-mpegURL":p.endsWith(".mpd")?"application/dash+xml":null;}catch(Exception e){return null;}}
}
