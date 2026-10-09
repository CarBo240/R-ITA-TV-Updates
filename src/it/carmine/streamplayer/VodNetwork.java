package it.carmine.streamplayer;

import android.app.*;import android.widget.Toast;import android.content.*;import android.webkit.CookieManager;
import java.io.*;import java.net.*;import java.util.*;import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import java.util.concurrent.*;
import okhttp3.*;import okhttp3.dnsoverhttps.DnsOverHttps;

/** Shared VOD transport. DNS changes never replace URLs or relax TLS checks. */
final class VodNetwork {
 static final String ENDPOINT="https://cloudflare-dns.com/dns-query";
 static final String[] PROVIDERS={"system","cloudflare","google","quad9","adguard"};
 static final String[] DNS_LABELS={"Automatico (DNS dispositivo)","Cloudflare · Privacy","Google Public DNS","Quad9 · Protezione malware","AdGuard · Blocco pubblicità e tracker"};
 static String provider(Context c){String p=VodSettings.prefs(c).getString("dnsProvider",null);if(p!=null)for(String known:PROVIDERS)if(known.equals(p))return p;return enabledLegacy(c)?"cloudflare":"system";}
 private static boolean enabledLegacy(Context c){return VodSettings.prefs(c).getBoolean("cloudflareDns",true);}
 static String endpoint(String provider){switch(provider){case "google":return "https://dns.google/dns-query";case "quad9":return "https://dns.quad9.net/dns-query";case "adguard":return "https://dns.adguard-dns.com/dns-query";default:return ENDPOINT;}}
 static String bootstrap(String provider){switch(provider){case "google":return "8.8.8.8";case "quad9":return "9.9.9.9";case "adguard":return "94.140.14.14";default:return "1.1.1.1";}}
 static String[] bootstraps(String provider){switch(provider){case "google":return new String[]{"8.8.8.8","8.8.4.4"};case "quad9":return new String[]{"9.9.9.9","149.112.112.112"};case "adguard":return new String[]{"94.140.14.14","94.140.15.15"};default:return new String[]{"1.1.1.1","1.0.0.1"};}}

 private static final CookieJar COOKIES=new CookieJar(){final Map<String,List<Cookie>> jar=new HashMap<>();public synchronized void saveFromResponse(HttpUrl url,List<Cookie> cookies){jar.put(url.host(),new ArrayList<>(cookies));}public synchronized List<Cookie> loadForRequest(HttpUrl url){List<Cookie> saved=jar.get(url.host());if(saved==null)return Collections.emptyList();List<Cookie> valid=new ArrayList<>();for(Cookie cookie:saved)if(cookie.matches(url))valid.add(cookie);return valid;}};
 private static volatile OkHttpClient client;private static volatile String activeProvider="cloudflare";private static volatile boolean television;private static volatile OkHttpClient metadataClient;
 static boolean enabled(Context c){return !provider(c).equals("system");}
 static boolean television(Context c){UiModeManager ui=(UiModeManager)c.getSystemService(Context.UI_MODE_SERVICE);return (ui!=null&&ui.getCurrentModeType()==android.content.res.Configuration.UI_MODE_TYPE_TELEVISION)||c.getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_LEANBACK);}
 static synchronized void initialize(Context c){String wanted=provider(c);boolean tv=television(c);if(client==null||!activeProvider.equals(wanted)||television!=tv){activeProvider=wanted;television=tv;client=buildProvider(wanted,tv);metadataClient=build(false,tv).newBuilder().callTimeout(20,TimeUnit.SECONDS).build();}}
 static synchronized OkHttpClient client(){if(client==null)client=buildProvider(activeProvider,television);return client;}
 static synchronized OkHttpClient transport(HttpUrl target){if(target.host().equals("api.themoviedb.org")){if(metadataClient==null)metadataClient=build(false,television).newBuilder().callTimeout(20,TimeUnit.SECONDS).build();return metadataClient;}return client();}
 static OkHttpClient build(boolean cloudflare){return build(cloudflare,false);}
 static OkHttpClient build(boolean cloudflare,boolean tv){return buildProvider(cloudflare?"cloudflare":"system",tv);}
 static OkHttpClient buildProvider(String provider,boolean tv){
  Dns system=tv?new BoundedDns(new PreferIpv4(Dns.SYSTEM),6000):Dns.SYSTEM;
  OkHttpClient base=new OkHttpClient.Builder().dns(system).cookieJar(COOKIES).connectTimeout(8,TimeUnit.SECONDS).readTimeout(20,TimeUnit.SECONDS).callTimeout(30,TimeUnit.SECONDS).followSslRedirects(false).build();
  if("system".equals(provider))return base;
  try{
   OkHttpClient bootstrap=new OkHttpClient.Builder().connectTimeout(4,TimeUnit.SECONDS).readTimeout(6,TimeUnit.SECONDS).callTimeout(8,TimeUnit.SECONDS).build();
   DnsOverHttps.Builder builder=new DnsOverHttps.Builder().client(bootstrap).url(HttpUrl.get(endpoint(provider))).includeIPv6(!tv);
   String[] ips=bootstraps(provider);
   builder.bootstrapDnsHosts(InetAddress.getByName(ips[0]),InetAddress.getByName(ips[1]));
   Dns doh=builder.build();
   return base.newBuilder().dns(new ShortCache(new FallbackDns(new BoundedDns(doh,6000),system))).build();
  }catch(UnknownHostException impossible){throw new IllegalStateException(impossible);}
 }
 static final class FallbackDns implements Dns{final Dns selected,fallback;FallbackDns(Dns a,Dns b){selected=a;fallback=b;}public List<InetAddress> lookup(String host)throws UnknownHostException{try{return selected.lookup(host);}catch(UnknownHostException e){return fallback.lookup(host);}}}
 static final class PreferIpv4 implements Dns{final Dns delegate;PreferIpv4(Dns d){delegate=d;}public List<InetAddress> lookup(String host)throws UnknownHostException{List<InetAddress> result=new ArrayList<>(delegate.lookup(host));result.sort(Comparator.comparing(a->!(a instanceof Inet4Address)));return result;}}
 static final ThreadPoolExecutor dnsWorkers=new ThreadPoolExecutor(0,4,20,TimeUnit.SECONDS,new SynchronousQueue<>(),r->{Thread t=new Thread(r,"VodDns");t.setDaemon(true);return t;});
 static final class BoundedDns implements Dns{final Dns delegate;final long limit;BoundedDns(Dns d,long timeout){delegate=d;limit=timeout;}public List<InetAddress> lookup(String host)throws UnknownHostException{Future<List<InetAddress>> future;try{future=dnsWorkers.submit(()->delegate.lookup(host));}catch(RejectedExecutionException e){throw failure(host,e);}try{return future.get(limit,TimeUnit.MILLISECONDS);}catch(InterruptedException e){future.cancel(true);Thread.currentThread().interrupt();throw failure(host,e);}catch(TimeoutException e){future.cancel(true);throw failure(host,new SocketTimeoutException("DNS scaduto"));}catch(ExecutionException e){if(e.getCause() instanceof UnknownHostException)throw (UnknownHostException)e.getCause();throw failure(host,e.getCause());}}private UnknownHostException failure(String host,Throwable cause){UnknownHostException e=new UnknownHostException(host);e.initCause(cause);return e;}}
 static final class ShortCache implements Dns {
  final Dns resolver;final Map<String,Entry> entries=new LinkedHashMap<>();ShortCache(Dns d){resolver=d;}
  static final class Entry{final long until;final List<InetAddress> addresses;Entry(List<InetAddress> a){until=System.nanoTime()+TimeUnit.SECONDS.toNanos(60);addresses=Collections.unmodifiableList(new ArrayList<>(a));}}
  public List<InetAddress> lookup(String host)throws UnknownHostException{
   // Numeric and local addresses must remain local; public hosts use only selected DNS.
   if(host.matches("[0-9.]+")||host.contains(":"))return Arrays.asList(InetAddress.getAllByName(host));
   if(!host.contains(".")||host.endsWith(".local"))return Dns.SYSTEM.lookup(host);
   synchronized(entries){Entry e=entries.get(host);if(e!=null&&e.until>System.nanoTime())return e.addresses;}
   List<InetAddress> result=resolver.lookup(host);if(result.isEmpty())throw new UnknownHostException(host);
   synchronized(entries){if(entries.size()>=256)entries.remove(entries.keySet().iterator().next());entries.put(host,new Entry(result));}return result;
  }
 }
 static final class HttpError extends IOException{final int status;HttpError(int n){super("Il sito risponde HTTP "+n);status=n;}}
 static String fetch(String url,String referer)throws Exception{
  HttpUrl target=HttpUrl.get(url);if(!target.isHttps())throw new IOException("È richiesto HTTPS");Request.Builder request=new Request.Builder().url(target).header("User-Agent",VodSource.USER_AGENT).header("Accept","text/html,application/json;q=0.9,*/*;q=0.8").header("Accept-Language","it-IT,it;q=0.9,en;q=0.7").header("Cache-Control","no-cache").header("Pragma","no-cache").header("Upgrade-Insecure-Requests","1");if(referer!=null&&!referer.isEmpty())request.header("Referer",referer);try{String browserCookies=CookieManager.getInstance().getCookie(url);if(browserCookies!=null&&!browserCookies.isEmpty())request.header("Cookie",browserCookies);}catch(Throwable ignored){}
  try(Response response=transport(target).newBuilder().callTimeout(20,TimeUnit.SECONDS).build().newCall(request.build()).execute()){if(response.code()!=200)throw new HttpError(response.code());if(response.body()==null)throw new IOException("Risposta vuota");try(InputStream in=response.body().byteStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();out.write(b,0,n);if(out.size()>12*1024*1024)throw new IOException("Risposta troppo grande");}return out.toString("UTF-8");}}
 }
 static String error(Throwable e){if(e instanceof LinkageError)return "Errore di compatibilità Android";for(Throwable t=e;t!=null;t=t.getCause()){if(t instanceof HttpError){int code=((HttpError)t).status;return code==403?"Accesso rifiutato dal sito (HTTP 403)":code==401?"Il sito richiede accesso":"Sito risponde HTTP "+code;}if(t instanceof UnknownHostException)return "DNS: dominio non risolto";if(t instanceof SocketTimeoutException)return "Connessione scaduta";if(t instanceof SSLException)return "Errore connessione HTTPS";if(t instanceof ConnectException)return "Connessione al sito fallita";}
  if(e instanceof org.json.JSONException)return "Formato del sito non compatibile";String message=e.getMessage();if(message!=null&&(message.contains("Catalogo non riconosciuto")||message.contains("getJSONObject")))return "Catalogo del sito non riconosciuto";return "Fonte non disponibile";
 }
 static void settings(Activity a,Runnable changed){
  String selected=provider(a);int index=0;for(int i=0;i<PROVIDERS.length;i++)if(PROVIDERS[i].equals(selected))index=i;
  new AlertDialog.Builder(a).setTitle("DNS VOD").setSingleChoiceItems(DNS_LABELS,index,(d,n)->{
   String choice=PROVIDERS[n];VodSettings.prefs(a).edit().putString("dnsProvider",choice).putBoolean("cloudflareDns",!"system".equals(choice)).apply();initialize(a);d.dismiss();changed.run();
  }).setNeutralButton("Verifica connessione",(d,w)->{
   new Thread(()->{String outcome;try{OkHttpClient c=buildProvider(provider(a),television(a));java.util.List<java.net.InetAddress> ips=c.dns().lookup("www.themoviedb.org");outcome=ips.isEmpty()?"DNS non disponibile":"DNS funzionante · "+provider(a);}catch(Exception e){outcome="Verifica DNS fallita: "+error(e);}final String text=outcome;a.runOnUiThread(()->Toast.makeText(a,text,Toast.LENGTH_LONG).show());},"RitaDnsCheck").start();
  }).setNegativeButton("Chiudi",null).show();
 }
}
