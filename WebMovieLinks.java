package it.carmine.streamplayer;

import java.net.URI;
import java.util.*;
import java.util.regex.*;

/** Bounded, ordinary HTML player links. No script evaluation or token decoding. */
final class WebMovieLinks {
 static boolean supported(String url) {
  try { String h=new URI(url).getHost(); if(h==null)return false;
   h=h.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
   return h.matches("(?:cb01[a-z0-9-]*|cineblog[a-z0-9-]*|altadefinizione[a-z0-9-]*)\\.[a-z]{2,24}$");
  } catch(Exception e) { return false; }
 }
 static String attr(String tag,String key) {
  Matcher m=Pattern.compile("(?i)(?<![\\w-])"+Pattern.quote(key)+"\\s*=\\s*(['\"])(.*?)\\1",Pattern.DOTALL).matcher(tag);
  return m.find()?m.group(2).replace("&amp;","&").replace("&#038;","&").replace("&quot;","\"").replace("&#39;","'"):"";
 }
 static boolean trailer(String value) { return value.toLowerCase(Locale.ROOT).matches("(?s).*(?:trailer|youtube\\.com|youtu\\.be).*"); }
 static boolean media(String value) {
  try { URI u=new URI(value); return "https".equals(u.getScheme())&&u.getHost()!=null&&u.getUserInfo()==null&&u.getPath()!=null&&u.getPath().toLowerCase(Locale.ROOT).matches(".*\\.(?:m3u8|mp4)$")&&!trailer(value); }
  catch(Exception e){return false;}
 }
 static List<String> players(String html,String page) {
  LinkedHashSet<String> out=new LinkedHashSet<>();
  Matcher tags=Pattern.compile("(?is)<iframe\\b[^>]*>|<a\\b[^>]*>.*?</a>").matcher(html);
  while(tags.find()&&out.size()<8){String tag=tags.group();if(trailer(tag))continue;
   boolean frame=tag.regionMatches(true,1,"iframe",0,6);
   String raw=attr(tag,frame?"src":"href");if(frame&&raw.isEmpty())raw=attr(tag,"data-src");
   if(!frame&&!tag.toLowerCase(Locale.ROOT).matches("(?s).*(?:player|streaming|guarda|riproduci|watch|supervideo|dropload|mixdrop|streamtape).*"))continue;
   try{URI u=new URI(page).resolve(raw);String h=u.getHost();if(raw.isEmpty()||!"https".equals(u.getScheme())||h==null||u.getUserInfo()!=null||trailer(u.toString()))continue;
    if(h.matches("(?i).*(?:doubleclick|googlesyndication|popads|popcash).*"))continue;
    if(!u.toString().equals(page))out.add(u.toString());
   }catch(Exception ignored){}
  }
  return new ArrayList<>(out);
 }
}
