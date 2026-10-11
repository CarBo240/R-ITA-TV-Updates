package it.carmine.streamplayer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;

/**
 * LAB ONLY: provider/host-aware inspection of PUBLIC markup and HTML5 source metadata.
 * This is not an implementation of proprietary extractors, and never decodes tokens,
 * bypasses paywalls, follows a challenge, or exposes signed stream URLs.
 */
final class ExperimentalMultiHostResolver {
 private ExperimentalMultiHostResolver() {}
 private static final String[][] HOSTS = {
  {"mixdrop", "MixDrop"}, {"maxstream", "MaxStream"},
  {"streamtape", "StreamTape"}, {"supervideo", "SuperVideo"},
  {"dood", "DoodStream"}, {"voe", "VOE"}, {"streamsb", "StreamSB"},
  {"streamhide", "StreamHide"}, {"streamwish", "StreamWish"},
  {"upstream", "UpStream"}, {"vidoza", "Vidoza"}, {"vidlox", "VidLox"},
  {"videovard", "VideoVard"}, {"fembed", "Fembed"},
  {"filemoon", "FileMoon"}, {"vixcloud", "VixCloud"},
  {"streamhg", "StreamHG"}, {"streamlare", "StreamLare"},
  {"turbovid", "TurboVid"}, {"wolfstream", "WolfStream"},
  {"dropload", "DropLoad"}, {"userload", "UserLoad"},
  {"jetload", "JetLoad"}, {"vidto", "VidTo"},
  {"altadefinizione", "AltaDefinizione (catalogo)"},
  {"cb01", "CB01 (catalogo)"}, {"guardaserie", "GuardaSerie (catalogo)"},
  {"eurostream", "EuroStreaming (catalogo)"}
 };
 private static final Pattern TAG = Pattern.compile("(?is)<(?:video|source|iframe|meta)\\b[^>]{0,4096}>");
 private static final Pattern ATTR = Pattern.compile("(?i)\\b(src|data-src|content|poster|type|property|name)\\s*=\\s*(?:\"([^\"]{1,3000})\"|'([^']{1,3000})')");
 private static final Pattern JSON_URL = Pattern.compile("(?is)\"(?:contentUrl|embedUrl)\"\\s*:\\s*\"(https://[^\"\\s]{5,1500})\"");
 private static final Pattern LINK = Pattern.compile("(?is)<a\\b[^>]*\\bhref\\s*=\\s*(?:\"([^\"]{1,1500})\"|'([^']{1,1500})')");
 private static boolean safeHost(String h) {return h!=null && h.contains(".") && !h.equals("localhost") && !h.endsWith(".local") && !h.contains(":") && !h.matches("[0-9.]+");}
 static String identify(String hostname) {
  if(hostname == null) return "Sito";
  String h=hostname.toLowerCase(Locale.ROOT);
  for(String[] spec:HOSTS) if(h.contains(spec[0])) return spec[1];
  return "Host esterno";
 }
 private static URI safe(String raw, URI base) {
  try {
   URI u=base.resolve(raw.replace("&amp;","&").replace("\\/","/").trim());
   if(!"https".equalsIgnoreCase(u.getScheme()) || u.getUserInfo()!=null || u.getPort()!=-1 || !safeHost(u.getHost()) || u.getRawQuery()!=null || u.getRawFragment()!=null) return null;
   return u;
  } catch(Exception ex){return null;}
 }
 private static boolean media(String path){return path!=null && path.toLowerCase(Locale.ROOT).matches(".*\\.(?:m3u8|mpd|mp4|webm)$");}
 private static void add(Map<String,ExperimentalHostInspector.Link> found, URI base, String raw) {
  URI link=safe(raw,base);if(link==null || found.containsKey(link.toString()) || found.size()>=60)return;
  boolean direct=media(link.getPath());String type=identify(link.getHost());
  if(!direct && "Host esterno".equals(type))return;
  // Do not label a catalogue image or catalogue navigation link as a video source.
  if(!direct && type.contains("catalogo"))return;
  found.put(link.toString(),new ExperimentalHostInspector.Link(link.toString(),direct?"Media HTTPS · "+link.getPath().substring(link.getPath().lastIndexOf('.')+1).toUpperCase(Locale.ROOT):type+" · pagina host",link.getHost(),direct?"File multimediale dichiarato nell'HTML · disponibilità da verificare":"Pagina host riconosciuta · non è un flusso video",direct));
 }
 static ExperimentalHostInspector.Result inspect(String address) throws Exception {
  // Reuse existing LAB inspection, then examine explicit HTML5 media declarations
  // and known host links in public markup. No scripts executed or credentials used.
  ExperimentalHostInspector.Result base=ExperimentalHostInspector.inspect(address);
  URI uri=new URI(address);
  LinkedHashMap<String,ExperimentalHostInspector.Link> results=new LinkedHashMap<>();
  for(ExperimentalHostInspector.Link link:base.links) if(link.direct || !link.kind.contains("catalogo")) results.put(link.url,link);
  String html="";
  HttpsURLConnection conn=(HttpsURLConnection)new URL(address).openConnection();
  conn.setInstanceFollowRedirects(false);conn.setConnectTimeout(7000);conn.setReadTimeout(10000);
  conn.setRequestProperty("Accept","text/html,application/xhtml+xml");
  try {
   if(conn.getResponseCode()==200 && conn.getContentType()!=null && conn.getContentType().toLowerCase(Locale.ROOT).contains("html")){
    try(InputStream input=conn.getInputStream();ByteArrayOutputStream buffer=new ByteArrayOutputStream()){
     byte[] bytes=new byte[4096];int n;
     while((n=input.read(bytes))!=-1 && buffer.size()<1024*1024){buffer.write(bytes,0,Math.min(n,1024*1024-buffer.size()));}
     html=buffer.toString("UTF-8");
    }
   }
  }finally{conn.disconnect();}
  Matcher tags=TAG.matcher(html);
  while(tags.find() && results.size()<60){
   String tag=tags.group();String src=null,property="";
   Matcher attr=ATTR.matcher(tag);while(attr.find()){
    String key=attr.group(1).toLowerCase(Locale.ROOT);String value=attr.group(2)!=null?attr.group(2):attr.group(3);
    if(key.equals("src")||key.equals("data-src")||key.equals("content"))src=value;
    if(key.equals("property")||key.equals("name"))property=value.toLowerCase(Locale.ROOT);
   }
   if(src!=null && (!tag.toLowerCase(Locale.ROOT).startsWith("<meta") || property.contains("video")))add(results,uri,src);
  }
  Matcher json=JSON_URL.matcher(html);while(json.find() && results.size()<60)add(results,uri,json.group(1));
  Matcher links=LINK.matcher(html);while(links.find() && results.size()<60)add(results,uri,links.group(1)!=null?links.group(1):links.group(2));
  return new ExperimentalHostInspector.Result(base.label,base.displayHost,
    "Host distinti per provider. Rilevate soltanto sorgenti pubblicamente dichiarate, senza eseguire script o superare restrizioni di accesso.",base.status,new ArrayList<>(results.values()));
 }
}
