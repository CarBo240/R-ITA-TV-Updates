package com.rita.eventilive;

import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Controlled HTML transport experiment; video and script requests stay in WebView. */
public final class NativeFetchClient {
 static final String UA="Mozilla/5.0 (Android 15; Mobile; rv:149.0) Gecko/149.0 Firefox/149.0";
 static final int MAX_BYTES=4*1024*1024;
 static boolean eligible(String engine,String method,URI uri){
  String host=uri.getHost(),path=uri.getPath();
  if(host==null||path==null)return false;
  host=host.toLowerCase(Locale.ROOT);
  return ("WebView assistita".equals(engine)||InternalPlayer.MODE.equals(engine))&&"GET".equals(method)&&"https".equalsIgnoreCase(uri.getScheme())&&uri.getUserInfo()==null&&path.startsWith("/e/")&&(host.endsWith(".dyntheme.net")||host.equals("missflight.net")||host.endsWith(".missflight.net"));
 }
 static String header(Map<String,String> headers,String name){for(Map.Entry<String,String> e:headers.entrySet())if(name.equalsIgnoreCase(e.getKey()))return e.getValue();return null;}
 static byte[] readBounded(InputStream input)throws IOException{
  if(input==null)return new byte[0];
  try(InputStream in=input;ByteArrayOutputStream out=new ByteArrayOutputStream()){
   byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(out.size()+n>MAX_BYTES)throw new IOException("HTML exceeds 4 MiB limit");out.write(buffer,0,n);}return out.toByteArray();
  }
 }
 public static WebResourceResponse intercept(MainActivity owner,MainActivity.Run run,WebResourceRequest request){
  InternalPlayer.capture(owner,run,request);
  String url=request.getUrl().toString();
  try{if(!eligible(run.engine,request.getMethod(),new URI(url)))return DnsTransport.intercept(owner,run,request);}catch(Exception e){return null;}
  long start=System.nanoTime();HttpURLConnection connection=null;
  try{
   connection=DnsTransport.connection(owner,run,url);
   connection.setConnectTimeout(15000);connection.setReadTimeout(15000);connection.setInstanceFollowRedirects(false);connection.setUseCaches(false);
   Map<String,String> sent=new LinkedHashMap<>();sent.put("User-Agent",UA);sent.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");sent.put("Accept-Language","it-IT,it;q=0.9,en;q=0.7");sent.put("Accept-Encoding","gzip");
   String referer=header(request.getRequestHeaders(),"Referer");if(referer==null||referer.isEmpty())referer=run.url;sent.put("Referer",referer);
   for(Map.Entry<String,String> e:sent.entrySet())connection.setRequestProperty(e.getKey(),e.getValue());
   String cookie=CookieManager.getInstance().getCookie(url);boolean hasCookie=cookie!=null&&!cookie.isEmpty();if(hasCookie)connection.setRequestProperty("Cookie",cookie);
   owner.log(run,"assisted.request",owner.obj("url",url,"transport",DnsTransport.enabled(owner)?"OkHttp + Cloudflare DoH":"Android HttpURLConnection","headers",owner.safeHeaders(sent),"cookiePresent",hasCookie,"scope","player HTML only"));
   int status=connection.getResponseCode();Map<String,String> response=new LinkedHashMap<>();int cookieCount=0;
   for(Map.Entry<String,List<String>> e:connection.getHeaderFields().entrySet()){
    String name=e.getKey();List<String> values=e.getValue();if(name==null||values==null)continue;
    if(name.equalsIgnoreCase("Set-Cookie")){for(String value:values){CookieManager.getInstance().setCookie(url,value);cookieCount++;}continue;}
    if(name.matches("(?i)content-length|content-encoding|transfer-encoding|connection"))continue;
    response.put(name,String.join(", ",values));
   }
   if(status>=300&&status<400){owner.log(run,"assisted.redirectFallback",owner.obj("url",url,"status",status,"location",connection.getHeaderField("Location"),"elapsedMs",(System.nanoTime()-start)/1000000));return null;}
   if(status<100||status>599)throw new IOException("Invalid HTTP status: "+status);
   InputStream input=status>=400?connection.getErrorStream():connection.getInputStream();
   if(input!=null&&"gzip".equalsIgnoreCase(connection.getContentEncoding()))input=new GZIPInputStream(input);
   byte[] bytes=readBounded(input);String type=connection.getContentType(),mime="text/html",encoding="UTF-8";
   if(type!=null){String[] parts=type.split(";");mime=parts[0].trim();for(int i=1;i<parts.length;i++){String part=parts[i].trim();if(part.toLowerCase(Locale.ROOT).startsWith("charset="))encoding=part.substring(8).replace("\"","").trim();}}
   if(!Charset.isSupported(encoding))encoding="UTF-8";
   String sample=new String(bytes,0,Math.min(bytes.length,65536),Charset.forName(encoding)).toLowerCase(Locale.ROOT);
   owner.log(run,"assisted.response",owner.obj("url",url,"status",status,"bytes",bytes.length,"mime",mime,"elapsedMs",(System.nanoTime()-start)/1000000,"headers",owner.safeHeaders(response),"cookiesReceived",cookieCount,"notAllowedText",sample.contains("not allowed"),"videoMarkup",sample.contains("<video")||sample.contains("jwplayer")||sample.contains("m3u8")));
   String reason=connection.getResponseMessage();if(reason==null||!reason.matches("[ -~]+"))reason="HTTP "+status;
   return new WebResourceResponse(mime,encoding,status,reason,response,new ByteArrayInputStream(bytes));
  }catch(Exception e){owner.log(run,"assisted.error",owner.obj("url",url,"errorType",e.getClass().getSimpleName(),"message",e.getMessage(),"elapsedMs",(System.nanoTime()-start)/1000000,"fallback",DnsTransport.enabled(owner)?"Cloudflare error response":"WebView"));return DnsTransport.enabled(owner)?DnsTransport.failure():null;}
  finally{if(connection!=null)connection.disconnect();}
 }
}
