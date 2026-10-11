package it.carmine.streamplayer.lab211;

import java.io.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

/** A LAB-owned, credential-free client. No production DNS, cookies or interceptors. */
public final class LabHttp implements LabPipeline.Transport {
 private final OkHttpClient client=new OkHttpClient.Builder().cookieJar(CookieJar.NO_COOKIES)
  .followRedirects(false).followSslRedirects(false).connectTimeout(12,TimeUnit.SECONDS)
  .readTimeout(15,TimeUnit.SECONDS).callTimeout(25,TimeUnit.SECONDS).build();
 public LabPipeline.Page head(String initial,String referer)throws Exception {
  String url=initial;
  for(int redirects=0;redirects<=4;redirects++){
   LabPipeline.https(url);Request.Builder request=new Request.Builder().url(url).head().header("User-Agent","Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 Chrome/128.0.0.0 Safari/537.36");
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
 public LabPipeline.Page get(String initial,String referer,boolean media)throws Exception {
  String url=initial;
  for(int redirects=0;redirects<=4;redirects++){
   LabPipeline.https(url);Request.Builder request=new Request.Builder().url(url).header("User-Agent","Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 Chrome/128.0.0.0 Safari/537.36").header("Accept",media?"*/*":"text/html,application/json").header("Accept-Language","it-IT,it;q=0.9");
   if(referer!=null&&!referer.isEmpty()){LabPipeline.https(referer);request.header("Referer",referer);}
   if(media)request.header("Range","bytes=0-65535");
   try(Response response=client.newCall(request.build()).execute()){
    int status=response.code();if(status==301||status==302||status==303||status==307||status==308){String location=response.header("Location","");String next=LabPipeline.absolute(url,location);if(next.isEmpty()||location.isEmpty())throw new LabPipeline.Failure(LabPipeline.Stage.SOURCE,"UNSAFE_REDIRECT","Redirect non HTTPS o non valido");url=next;continue;}
    int limit=media?65536:2*1024*1024;ByteArrayOutputStream out=new ByteArrayOutputStream();
    if(response.body()!=null)try(InputStream input=response.body().byteStream()){byte[] buffer=new byte[8192];while(out.size()<limit){int n=input.read(buffer,0,Math.min(buffer.length,limit-out.size()));if(n<0)break;out.write(buffer,0,n);}}
    return new LabPipeline.Page(url,status,response.header("Content-Type",""),out.toByteArray(),response.header("Refresh",""));
   }
  }
  throw new LabPipeline.Failure(LabPipeline.Stage.SOURCE,"REDIRECT_LIMIT","Superato limite di quattro redirect HTTPS");
 }
 public LabPipeline.Page postForm(String url,Map<String,String> fields,String referer)throws Exception {
  LabPipeline.https(url);LabPipeline.https(referer);
  String host=LabPipeline.host(url);
  if((!host.equals("stayonline.pro")&&!host.equals("www.stayonline.pro"))||!LabPipeline.origin(url).equals(LabPipeline.origin(referer))||!LabPipeline.https(url).getPath().equals("/ajax/linkView.php"))throw new LabPipeline.Failure(LabPipeline.Stage.RESOLVER,"UNSAFE_PUBLIC_API","Endpoint POST fuori dal flusso pubblico StayOnline");
  FormBody.Builder form=new FormBody.Builder();for(Map.Entry<String,String> field:fields.entrySet())form.add(field.getKey(),field.getValue());
  Request request=new Request.Builder().url(url).post(form.build()).header("User-Agent","Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 Chrome/128.0.0.0 Safari/537.36").header("Referer",referer).header("Accept","application/json").header("X-Requested-With","XMLHttpRequest").build();
  // POST redirects are not replayed and no third-party credential or cookie is supplied.
  try(Response response=client.newCall(request).execute()){
   ByteArrayOutputStream out=new ByteArrayOutputStream();int limit=2*1024*1024;
   if(response.body()!=null)try(InputStream input=response.body().byteStream()){byte[] buffer=new byte[8192];while(out.size()<limit){int n=input.read(buffer,0,Math.min(buffer.length,limit-out.size()));if(n<0)break;out.write(buffer,0,n);}}
   return new LabPipeline.Page(url,response.code(),response.header("Content-Type",""),out.toByteArray());
  }
 }
 // Media3 may encounter further redirects on HLS/DASH segments. Same-scheme
 // redirects are ordinary playback requests; HTTPS downgrade stays forbidden.
 public OkHttpClient client(){return client.newBuilder().followRedirects(true).followSslRedirects(false).build();}
}
