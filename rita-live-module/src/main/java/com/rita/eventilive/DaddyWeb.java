package com.rita.eventilive;
import android.webkit.*;import java.io.*;import java.net.*;import java.nio.charset.StandardCharsets;import java.util.*;

/** Preserve the selector's origin while using the available HTTPS versions of its sources. */
final class DaddyWeb {
 static String withUi(MainActivity owner,String html){String script="<script>"+owner.daddyUiScript()+"</script>";int end=html.toLowerCase(Locale.ROOT).lastIndexOf("</body>");return end<0?html+script:html.substring(0,end)+script+html.substring(end);}
 static WebResourceResponse selector(MainActivity owner,MainActivity.Run run,WebResourceRequest request){
  if(!run.daddy||!request.isForMainFrame()||!"GET".equals(request.getMethod())||request.getUrl().getPath()==null||!request.getUrl().getPath().endsWith("/player/embed.php"))return null;
  String url=request.getUrl().toString();HttpURLConnection c=null;
  try{c=DnsTransport.connection(owner,run,url);c.setUseCaches(false);c.setRequestProperty("Cache-Control","no-cache");c.setConnectTimeout(15000);c.setReadTimeout(25000);c.setRequestProperty("User-Agent",owner.daddyUserAgent());String cookie=android.webkit.CookieManager.getInstance().getCookie(url);if(cookie!=null)c.setRequestProperty("Cookie",cookie);int code=c.getResponseCode();if(code!=200)return null;
   Map<String,String> headers=new LinkedHashMap<>();for(Map.Entry<String,List<String>> h:c.getHeaderFields().entrySet()){if(h.getKey()==null||h.getValue()==null)continue;if(h.getKey().equalsIgnoreCase("Set-Cookie")){for(String value:h.getValue())android.webkit.CookieManager.getInstance().setCookie(url,value);}else if(!h.getKey().matches("(?i)content-length|content-encoding|transfer-encoding|connection"))headers.put(h.getKey(),String.join(", ",h.getValue()));}
   String html=new String(MainActivity.readBytes(c.getInputStream(),4*1024*1024),StandardCharsets.UTF_8);html=withUi(owner,DaddyPlayers.upgradeSources(html));owner.log(run,"daddy.webSelector",owner.obj("httpsSources",true));return new WebResourceResponse("text/html","UTF-8",200,"OK",headers,new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
  }catch(Exception e){owner.log(run,"daddy.webSelectorError",owner.obj("message",e.toString()));return null;}finally{if(c!=null)c.disconnect();}
 }
}
