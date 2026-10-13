package com.rita.eventilive;
import android.content.Context;import android.webkit.*;
import java.io.*;import java.net.*;import java.nio.charset.StandardCharsets;import java.util.*;import java.util.concurrent.TimeUnit;
import okhttp3.*;

public final class DnsTransport {
 public static boolean enabled(Context c){return c.getSharedPreferences("dns_settings",0).getBoolean("cloudflare",false);}
 public static void record(MainActivity o,MainActivity.Run r){o.log(r,"dns.config",o.obj("selected",enabled(o)?"Cloudflare DoH":"Sistema","endpoint",enabled(o)?DohClientFactory.ENDPOINT:"system","bootstrap",enabled(o)?"1.1.1.1, 1.0.0.1":"system","webViewCoverage","HTTPS GET through native client; POST/ServiceWorker and redirect fallback may use system DNS","tlsValidation",true));}
 public static OkHttpClient client(MainActivity o,MainActivity.Run r)throws IOException {
  OkHttpClient base=DohClientFactory.cloud();Dns actual=base.dns();
  return base.newBuilder().dns(host->{long t=System.nanoTime();try{List<InetAddress> addresses=actual.lookup(host);o.log(r,"dns.lookup",o.obj("host",host,"resolver","Cloudflare DoH","addresses",addresses.toString(),"elapsedMs",(System.nanoTime()-t)/1000000));return addresses;}catch(UnknownHostException e){o.log(r,"dns.error",o.obj("host",host,"resolver","Cloudflare DoH","message",e.toString()));throw e;}})
   .addNetworkInterceptor(chain->{Request request=chain.request();String cookie=android.webkit.CookieManager.getInstance().getCookie(request.url().toString());Request.Builder b=request.newBuilder().removeHeader("Cookie");if(cookie!=null&&!cookie.isEmpty())b.header("Cookie",cookie);Response result=chain.proceed(b.build());storeCookies(request.url().toString(),result);return result;}).build();
 }
 public static HttpURLConnection connection(MainActivity o,MainActivity.Run r,String url)throws IOException {
  URL u=new URL(url);if(!enabled(o))return (HttpURLConnection)u.openConnection();
  if(!"https".equalsIgnoreCase(u.getProtocol()))throw new IOException("Cloudflare transport requires HTTPS");return new Bridge(u,client(o,r));
 }
 /** Implements only the GET operations used by feed and player HTML loading. */
 static final class Bridge extends HttpURLConnection {
  private final OkHttpClient base;private final Map<String,String> headers=new LinkedHashMap<>();private Response response;private Call call;
  Bridge(URL url,OkHttpClient client){super(url);base=client;}
  public void setRequestProperty(String k,String v){if(connected)throw new IllegalStateException("Already connected");headers.put(k,v);}
  public String getRequestProperty(String k){return headers.get(k);}
  public void connect()throws IOException {if(connected)return;if(!"GET".equals(getRequestMethod())||getDoOutput())throw new ProtocolException("GET only");
   Request.Builder b=new Request.Builder().url(url).get();for(Map.Entry<String,String> e:headers.entrySet())b.header(e.getKey(),e.getValue());
   OkHttpClient c=base.newBuilder().connectTimeout(getConnectTimeout()>0?getConnectTimeout():15000,TimeUnit.MILLISECONDS).readTimeout(getReadTimeout()>0?getReadTimeout():15000,TimeUnit.MILLISECONDS).followRedirects(getInstanceFollowRedirects()).followSslRedirects(false).build();call=c.newCall(b.build());response=call.execute();connected=true;
  }
  public int getResponseCode()throws IOException{connect();return response.code();}
  public String getResponseMessage()throws IOException{connect();String s=response.message();return s.isEmpty()?"HTTP "+response.code():s;}
  public Map<String,List<String>> getHeaderFields(){try{connect();return response.headers().toMultimap();}catch(IOException e){return Collections.emptyMap();}}
  public String getHeaderField(String key){try{connect();return response.header(key);}catch(IOException e){return null;}}
  public InputStream getInputStream()throws IOException{connect();if(response.code()>=400)throw new IOException("HTTP "+response.code());return response.body()==null?new ByteArrayInputStream(new byte[0]):response.body().byteStream();}
  public InputStream getErrorStream(){try{connect();return response.code()>=400&&response.body()!=null?response.body().byteStream():null;}catch(IOException e){return null;}}
  public void disconnect(){if(response!=null)response.close();if(call!=null)call.cancel();}public boolean usingProxy(){return false;}
 }
 public static WebResourceResponse failure(){return new WebResourceResponse("text/plain","UTF-8",502,"Cloudflare request failed",Collections.emptyMap(),new ByteArrayInputStream("Caricamento Cloudflare non riuscito. Consulta il report oppure scegli DNS Automatico.".getBytes(StandardCharsets.UTF_8)));}
 public static WebResourceResponse intercept(MainActivity o,MainActivity.Run r,WebResourceRequest req){
  if(!enabled(o)||!"GET".equals(req.getMethod())||!"https".equalsIgnoreCase(req.getUrl().getScheme()))return null;
  String url=req.getUrl().toString();Response response=null;
  try{Request.Builder b=new Request.Builder().url(url).get();for(Map.Entry<String,String> e:req.getRequestHeaders().entrySet())if(!e.getKey().matches("(?i)host|cookie|accept-encoding|connection"))b.header(e.getKey(),e.getValue());
   response=client(o,r).newBuilder().followRedirects(false).build().newCall(b.build()).execute();int status=response.code();
   storeCookies(url,response);o.log(r,"dns.web.response",o.obj("url",url,"status",status,"resolver","Cloudflare DoH","mainFrame",req.isForMainFrame()));
   if(status>=300&&status<400){o.log(r,"dns.redirectFallback",o.obj("url",url,"status",status,"location",response.header("Location"),"dnsCoverage","browser redirect may use system DNS"));response.close();return null;}
   Map<String,String> headers=new LinkedHashMap<>();for(String name:response.headers().names())if(!name.matches("(?i)set-cookie|content-length|content-encoding|transfer-encoding|connection"))headers.put(name,String.join(", ",response.headers(name)));
   MediaType type=response.body()==null?null:response.body().contentType();String mime=type==null?"application/octet-stream":type.type()+"/"+type.subtype(),charset=type==null||type.charset()==null?null:type.charset().name();
   final Response held=response;InputStream raw=response.body()==null?new ByteArrayInputStream(new byte[0]):response.body().byteStream();InputStream stream=new FilterInputStream(raw){public void close()throws IOException{try{super.close();}finally{held.close();}}};
   String reason=response.message();return new WebResourceResponse(mime,charset,status,reason.isEmpty()?"HTTP "+status:reason,headers,stream);
  }catch(Exception e){if(response!=null)response.close();o.log(r,"dns.web.error",o.obj("url",url,"resolver","Cloudflare DoH","message",e.toString()));return failure();}
 }
 private static void storeCookies(String url,Response response){for(String value:response.headers("Set-Cookie"))android.webkit.CookieManager.getInstance().setCookie(url,value);}

}
