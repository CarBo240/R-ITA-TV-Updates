package it.carmine.streamplayer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import javax.net.ssl.HttpsURLConnection;

/** LAB-only HTML link inspector. Does not execute scripts or resolve protected streams. */
final class ExperimentalHostInspector {
 static final class Link {
  final String url,kind,displayHost,hint;final boolean direct;
  Link(String u,String k,String h,String description,boolean d){url=u;kind=k;displayHost=h;hint=description;direct=d;}
 }
 static final class Result {
  final String label,displayHost,message;final int status;final List<Link> links;
  Result(String l,String host,String m,int s,List<Link> items){label=l;displayHost=host;message=m;status=s;links=items;}
 }
 private static final Pattern ATTR=Pattern.compile("(?is)\\b(?:href|src|data-src|data-url|data-link)\\s*=\\s*(?:\\\"([^\\\"]{1,3000})\\\"|'([^']{1,3000})')");
 private static final Pattern PLAIN=Pattern.compile("https://[^\\s<>\\\"']{5,1000}",Pattern.CASE_INSENSITIVE);
 private static boolean has(String s,String part){return s.toLowerCase(Locale.ROOT).contains(part);}
 private static String classify(String host){
  if(has(host,"mixdrop"))return "MixDrop · host video";
  if(has(host,"maxstream")||has(host,"maxstream.video"))return "MaxStream · host video";
  if(has(host,"altadefinizione"))return "Collegamento pagina";
  return "Collegamento pagina";
 }
 private static boolean direct(String path){String p=path.toLowerCase(Locale.ROOT);return p.matches(".*\\.(m3u8|mpd|mp4)(?:$|[?].*)");}
 private static boolean publicHost(String host){
  if(host==null||!host.contains(".")||host.endsWith(".local")||host.equals("localhost"))return false;
  if(host.matches("[0-9.]+")||host.startsWith("[")||host.contains(":"))return false;
  return true;
 }
 private static URI allowed(String source)throws Exception{
  URI uri=new URI(source);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getPort()!=-1||!publicHost(uri.getHost()))throw new IOException("Indirizzo non consentito");return uri;
 }
 static Result inspect(String source)throws Exception{
  URI page=allowed(source);
  HttpsURLConnection conn=(HttpsURLConnection)page.toURL().openConnection();conn.setInstanceFollowRedirects(false);conn.setRequestMethod("GET");conn.setConnectTimeout(8000);conn.setReadTimeout(12000);
  conn.setRequestProperty("Accept","text/html,application/xhtml+xml");conn.setRequestProperty("Accept-Language","it-IT,it;q=0.8");
  int status;String html="";try{status=conn.getResponseCode();String ct=conn.getContentType();
   if(status==200&&ct!=null&&(ct.toLowerCase(Locale.ROOT).contains("text/html")||ct.toLowerCase(Locale.ROOT).contains("application/xhtml"))){
    try(InputStream in=conn.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
     byte[] chunk=new byte[4096];int size;while((size=in.read(chunk))!=-1){if(out.size()+size>1024*1024)break;out.write(chunk,0,size);}html=out.toString("UTF-8");
    }
   }
  }finally{conn.disconnect();}
  LinkedHashMap<String,Link> found=new LinkedHashMap<>();
  Matcher matcher=ATTR.matcher(html);while(matcher.find()&&found.size()<80){String raw=matcher.group(1)==null?matcher.group(2):matcher.group(1);add(found,page,raw);}
  matcher=PLAIN.matcher(html);while(matcher.find()&&found.size()<80)add(found,page,matcher.group());
  String category=classify(page.getHost());String message=status!=200?"Il server non ha restituito una pagina HTML accessibile. Nessun controllo di accesso viene aggirato.":"Sono elencati soltanto collegamenti dichiarati nell'HTML ricevuto. Nessun flusso è garantito; i link contenenti credenziali o token vengono esclusi.";
  return new Result(category,page.getHost(),message,status,new ArrayList<>(found.values()));
 }
 private static void add(Map<String,Link> found,URI base,String raw){
  try{
   if(raw==null||raw.startsWith("#")||raw.startsWith("javascript:")||raw.startsWith("data:"))return;
   String cleaned=raw.replace("&amp;","&").trim().replace("\\/","/");
   URI resolved=base.resolve(cleaned);if(!"https".equalsIgnoreCase(resolved.getScheme())||resolved.getUserInfo()!=null||resolved.getPort()!=-1||!publicHost(resolved.getHost()))return;
   if(resolved.getRawQuery()!=null||resolved.getRawFragment()!=null)return; // No secrets or signed URLs stored/displayed.
   String host=resolved.getHost();String kind=classify(host);boolean media=direct(resolved.getPath());
   if(!media&&kind.equals("Collegamento pagina"))return;
   String url=resolved.toString();if(found.containsKey(url))return;
   String desc=media?"Flusso diretto dichiarato · formato da verificare":"Pagina/host riconosciuto · richiede verifica della fonte";
   found.put(url,new Link(url,media?"Media HTTPS":kind,host,desc,media));
  }catch(Exception ignored){}
 }
 static String safeError(Exception e){if(e instanceof java.net.SocketTimeoutException)return "timeout";if(e instanceof java.net.UnknownHostException)return "DNS non disponibile";return e instanceof IOException?"Risposta non accessibile o non supportata":"Pagina non analizzabile";}
}
