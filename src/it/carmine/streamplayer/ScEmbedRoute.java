package it.carmine.streamplayer;
import java.net.*;import java.io.*;import java.util.regex.*;
/** Distinguishes a wrapper from an HTTP redirect that has already reached the player. */
public final class ScEmbedRoute {
 public static String next(String page,String html)throws Exception {
  URI current=new URI(page);if(!"https".equalsIgnoreCase(current.getScheme())||current.getHost()==null)throw new IOException("SC · URL player non valido");
  if(Pattern.compile("(?:window\\.)?masterPlaylist\\s*=\\s*\\{").matcher(html).find())return page;
  Matcher frames=Pattern.compile("(?is)<iframe\\b([^>]*)>").matcher(html);
  while(frames.find()){java.util.Map<String,String> attrs=it.carmine.streamplayer.lab211.LabPipeline.attrs(frames.group(1));String raw=attrs.getOrDefault("data-src",attrs.getOrDefault("src",""));String url=it.carmine.streamplayer.lab211.LabPipeline.absolute(page,raw);if(!url.isEmpty())return url;}
  throw new IOException("SC · nessun embed o configurazione playlist nella risposta di "+current.getHost());
 }
 private ScEmbedRoute(){}
}
