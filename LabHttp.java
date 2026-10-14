package it.carmine.streamplayer.lab211;

import java.io.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import it.carmine.streamplayer.VodNetwork;

/** LAB transport with selected DNS and an isolated ephemeral cookie session. */
public final class LabHttp implements LabPipeline.Transport {
 private static final class SessionCookies implements CookieJar {private final java.util.List<Cookie> cookies=new java.util.ArrayList<>();public synchronized void saveFromResponse(HttpUrl url,java.util.List<Cookie> values){for(Cookie c:values){cookies.removeIf(old->old.name().equals(c.name())&&old.domain().equals(c.domain())&&old.path().equals(c.path()));if(c.expiresAt()>System.currentTimeMillis())cookies.add(c);}if(cookies.size()>128)cookies.subList(0,cookies.size()-128).clear();}public synchronized java.util.List<Cookie> loadForRequest(HttpUrl url){java.util.List<Cookie> out=new java.util.ArrayList<>();for(Cookie c:cookies)if(c.expiresAt()>System.currentTimeMillis()&&c.matches(url))out.add(c);return out;}}
 private final SessionCookies cookies=new SessionCookies();
 private final OkHttpClient client=VodNetwork.client().newBuilder().cookieJar(cookies)
  .followRedirects(false).followSslRedirects(false).connectTimeout(12,TimeUnit.SECONDS)
  .readTimeout(15,TimeUnit.SECONDS).callTimeout(25,TimeUnit.SECONDS).build();
 public LabPipeline.Page head(String initial,String referer)throws Exception {
  String url=initial;
  for(int redirects=0;redirects<=4;redirects++){
   LabPipeline.https(url);Request.Builder request=new Request.Builder().url(url).head().header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
   if(referer!=null&&!referer.isEmpty()){LabPipeline.https(referer);request.header("Referer",referer);}
   try(Response response=client.newCall(request.build()).execute()){
    int status=response.code();if(status==301||status==302||status==303||status==307||status==308){String location=response.header("Location","");String next=LabPipeline.absolute(url,location);if(next.isEmpty()||location.isEmpty())throw new LabPipeline.Failure(LabPipeline.Stage.RESOLVER,"UNSAFE_REDIRECT","HEAD: redirect non HTTPS o non valido");url=next;continue;}
    // A server explicitly rejecting HEAD may still support ordinary GET.
    if(status==405||status==501)return null;
    return new LabPipeline.Page(url,status,response.header("Content-Type",""),new byte[0],response.header("Refresh",""));
   }
  }
  throw new LabPipeline.Failure(LabPipeline.Stage.RESOLVER,"REDIRECT_LIMIT","HEAD: superato limite di quattro redirect HTTPS");
 }
 public LabPipeline.Page get(String initial,String referer,boolean media)throws Exception{return getWithHeaders(initial,referer,media,java.util.Collections.emptyMap());}
 public LabPipeline.Page getWithHeaders(String initial,String referer,boolean media,Map<String,String> headers)throws Exception {
  String url=initial;
  for(int redirects=0;redirects<=4;redirects++){
   LabPipeline.https(url);Request.Builder request=new Request.Builder().url(url).header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36").header("Accept",media?"*/*":"text/html,application/json").header("Accept-Language","it-IT,it;q=0.9");
   if(referer!=null&&!referer.isEmpty()){LabPipeline.https(referer);request.header("Referer",referer);}
   for(Map.Entry<String,String> header:headers.entrySet())request.header(header.getKey(),header.getValue());if(media)request.header("Range","bytes=0-65535");
   if(LabPipeline.https(url).getPath().startsWith("/pass_md5/"))request.header("Range","bytes=0-").header("Accept","*/*");
   if(LabVodPolicy.adapterKind(url).equals("animeunity"))request.header("User-Agent",PlaybackHeaders.userAgent("VidXGo"));
   if(LabPipeline.host(url).equals("v.vidxgo.co"))request.header("User-Agent",PlaybackHeaders.userAgent("VidXGo"));
   if(media&&"https://v.vidxgo.co".equals(referer))request.header("Sec-Fetch-Dest","empty").header("Sec-Fetch-Mode","cors").header("Sec-Fetch-Site","cross-site");
   else if(LabPipeline.host(url).equals("v.vidxgo.co"))request.header("Sec-Fetch-Dest","iframe");
   try(Response response=client.newCall(request.build()).execute()){
    int status=response.code();if(status==301||status==302||status==303||status==307||status==308){String location=response.header("Location","");String next=LabPipeline.absolute(url,location);if(next.isEmpty()||location.isEmpty())throw new LabPipeline.Failure(LabPipeline.Stage.SOURCE,"UNSAFE_REDIRECT","Redirect non HTTPS o non valido");url=next;continue;}
    int limit=media?65536:2*1024*1024;ByteArrayOutputStream out=new ByteArrayOutputStream();
    if(response.body()!=null)try(InputStream input=response.body().byteStream()){byte[] buffer=new byte[8192];while(out.size()<limit){int n=input.read(buffer,0,Math.min(buffer.length,limit-out.size()));if(n<0)break;out.write(buffer,0,n);}}
    return new LabPipeline.Page(url,status,response.header("Content-Type",""),out.toByteArray(),response.header("Refresh",""));
   }
  }
  throw new LabPipeline.Failure(LabPipeline.Stage.SOURCE,"REDIRECT_LIMIT","Superato limite di quattro redirect HTTPS");
 }
 public LabPipeline.Page postJson(String url,String body,String referer,String csrf)throws Exception {
  if(!LabVodPolicy.adapterKind(url).equals("animeunity")||!LabPipeline.https(url).getPath().equals("/archivio/get-animes")||!LabPipeline.origin(url).equals(LabPipeline.origin(referer))||!LabPipeline.https(referer).getPath().equals("/archivio")||csrf.isEmpty()||csrf.length()>1024||body.length()>8192)throw new LabPipeline.Failure(LabPipeline.Stage.CATALOG,"UNITY_API_INVALID","Endpoint di ricerca non valido");
  Request request=new Request.Builder().url(url).post(RequestBody.create(MediaType.parse("application/json"),body)).header("X-CSRF-TOKEN",csrf).header("Referer",referer).header("Accept","application/json").header("User-Agent",PlaybackHeaders.userAgent("VidXGo")).build();
  try(Response response=client.newCall(request).execute()){ByteArrayOutputStream out=new ByteArrayOutputStream();if(response.body()!=null)try(InputStream input=response.body().byteStream()){byte[] buffer=new byte[8192];while(out.size()<2*1024*1024){int n=input.read(buffer,0,Math.min(buffer.length,2*1024*1024-out.size()));if(n<0)break;out.write(buffer,0,n);}}return new LabPipeline.Page(url,response.code(),response.header("Content-Type",""),out.toByteArray());}
 }
 public LabPipeline.Page postForm(String url,Map<String,String> fields,String referer)throws Exception {
  LabPipeline.https(url);LabPipeline.https(referer);
  String host=LabPipeline.host(url);
  boolean stay=(host.equals("stayonline.pro")||host.equals("www.stayonline.pro"))&&LabPipeline.https(url).getPath().equals("/ajax/linkEmbedView.php");boolean doo=LabVodPolicy.adapterKind(url).equals("streamingita")&&LabPipeline.https(url).getPath().equals("/wp-admin/admin-ajax.php")&&"doo_player_ajax".equals(fields.get("action"))&&fields.getOrDefault("post","").matches("[0-9]+")&&fields.getOrDefault("nume","").matches("[0-9]{1,2}");if((!stay&&!doo)||!LabPipeline.origin(url).equals(LabPipeline.origin(referer)))throw new LabPipeline.Failure(LabPipeline.Stage.RESOLVER,"UNSAFE_PUBLIC_API","Endpoint POST fuori dal flusso dichiarato del provider");
  FormBody.Builder form=new FormBody.Builder();for(Map.Entry<String,String> field:fields.entrySet())form.add(field.getKey(),field.getValue());
  Request request=new Request.Builder().url(url).post(form.build()).header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36").header("Referer",referer).header("Accept","application/json").header("X-Requested-With","XMLHttpRequest").build();
  // POST redirects are not replayed and no third-party credential or cookie is supplied.
  try(Response response=client.newCall(request).execute()){
   ByteArrayOutputStream out=new ByteArrayOutputStream();int limit=2*1024*1024;
   if(response.body()!=null)try(InputStream input=response.body().byteStream()){byte[] buffer=new byte[8192];while(out.size()<limit){int n=input.read(buffer,0,Math.min(buffer.length,limit-out.size()));if(n<0)break;out.write(buffer,0,n);}}
   return new LabPipeline.Page(url,response.code(),response.header("Content-Type",""),out.toByteArray());
  }
 }
 // Media3 may encounter further redirects on HLS/DASH segments. Same-scheme
 // redirects are ordinary playback requests; HTTPS downgrade stays forbidden.
 public Map<String,String> playbackHeaders(String url){StringBuilder value=new StringBuilder();for(Cookie c:cookies.loadForRequest(HttpUrl.get(url))){if(value.length()>0)value.append("; ");value.append(c.name()).append("=").append(c.value());}return value.length()==0?java.util.Collections.emptyMap():java.util.Collections.singletonMap("Cookie",value.toString());}
 public OkHttpClient client(){return client.newBuilder().followRedirects(true).followSslRedirects(false).build();}
}
