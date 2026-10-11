package it.carmine.streamplayer.lab211;

import java.io.*;
import java.util.*;
import okhttp3.*;
import org.json.JSONObject;

/** Per-player token renewal for the public VidXGo session. TLS and access refusals stay enforced. */
public final class LabVidXSession implements Closeable {
 public interface Observer {void event(String code);}
 private final OkHttpClient control=new LabHttp().client().newBuilder().followRedirects(false).build();
 private final String id,sid=UUID.randomUUID().toString();private final LabPipeline.VidXTokens tokens;
 private final Observer observer;
 private volatile boolean closed;private String failure="";
 public LabVidXSession(String url,String refreshId,Observer observer)throws Exception {
  LabPipeline.https(url);if(!refreshId.matches("(?:tt)?[0-9]{1,12}(?:/[0-9]{1,3}/[0-9]{1,4})?"))throw new IOException("VIDXGO_REFRESH_ID");
  tokens=new LabPipeline.VidXTokens(url,System.currentTimeMillis());id=refreshId;this.observer=observer;
 }
 private void notifyCode(String code){if(observer!=null)observer.event(code);}
 private synchronized void refresh()throws IOException {
  if(closed)throw new IOException("VIDXGO_SESSION_CLOSED");if(!failure.isEmpty())throw new IOException(failure);if(System.currentTimeMillis()<tokens.due)return;
  notifyCode("VIDXGO_TOKEN_REFRESH");String endpoint="https://v.vidxgo.co/t/"+id;
  Request request=new Request.Builder().url(endpoint).header("User-Agent","Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 Chrome/128.0.0.0 Safari/537.36").header("Referer","https://v.vidxgo.co/"+id).header("X-Cs",sid).header("Accept","application/json").build();
  try(Response response=control.newCall(request).execute()){
   if(response.code()!=200)throw new IOException("VIDXGO_REFRESH_HTTP_"+response.code());
   ByteArrayOutputStream bytes=new ByteArrayOutputStream();if(response.body()==null)throw new IOException("VIDXGO_REFRESH_EMPTY");
   try(InputStream in=response.body().byteStream()){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1){if(bytes.size()+n>65536)throw new IOException("VIDXGO_REFRESH_LIMIT");bytes.write(buffer,0,n);}}
   JSONObject data=new JSONObject(bytes.toString("UTF-8"));String raw=data.optString("url","");if(raw.isEmpty())raw=data.optString("hls","");if(closed)throw new IOException("VIDXGO_SESSION_CLOSED");tokens.update(raw,System.currentTimeMillis());notifyCode("VIDXGO_TOKEN_UPDATED");
  }catch(Exception e){String code=e.getMessage();failure=code!=null&&code.startsWith("VIDXGO_")?code:"VIDXGO_REFRESH_FORMAT";notifyCode(failure);throw new IOException(failure,e);}
 }
 public OkHttpClient client(){return new LabHttp().client().newBuilder().addNetworkInterceptor(chain->{
  Request request=chain.request();if(!request.url().isHttps())throw new IOException("VIDXGO_HTTP_DOWNGRADE");if(!tokens.scoped(request.url().toString()))return chain.proceed(request);refresh();String url;
  synchronized(this){try{url=tokens.rewrite(request.url().toString());}catch(Exception e){throw new IOException("VIDXGO_REFRESH_QUERY",e);}}
  return chain.proceed(request.newBuilder().url(url).header("Referer","https://v.vidxgo.co").header("Sec-Fetch-Dest","empty").header("Sec-Fetch-Mode","cors").header("Sec-Fetch-Site","cross-site").build());
 }).build();}
 public void close(){closed=true;control.dispatcher().cancelAll();}
}
