package it.carmine.streamplayer;

import android.os.Build;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.*;

/** Independent Android client for the protocol documented by
 * https://github.com/Haehnchen/vavoo-iptv-stream-proxy (MIT).
 * Uses standard TLS verification and does not disable certificate checks. */
public final class VavooClient {
    static final String[] BASES={"https://vavoo.to","https://kool.to"};
    private String[] bases=BASES.clone();
    private volatile String signature;
    private volatile long signatureTime;
    static final long RENEW_INTERVAL_MS=8*60*1000L;
    private android.content.Context context;
    private final String deviceId;
    interface Transport {Object post(String url,JSONObject payload,String sig)throws Exception;}
    private Transport transport;
    private RequestGuard guard=new RequestGuard(null);
    public VavooClient(String id,android.content.Context context){this(id);this.context=context.getApplicationContext();bases=VavooSettings.bases(context);guard=new RequestGuard(context.getSharedPreferences("request_limits",0));}
    static final class HttpFailure extends IOException {
        final int status;final String retryAfter;
        HttpFailure(int code,String retry){super(code==401||code==403?"Il servizio non autorizza l'accesso. Attendi prima di riprovare.":"Il servizio ha risposto HTTP "+code);status=code;retryAfter=retry;}
    }
    VavooClient(String id,android.content.Context context,Transport transport){this(id,context);this.transport=transport;}
    VavooClient(String deviceId,Transport transport){this(deviceId);this.transport=transport;}
    public VavooClient(String deviceId){this.deviceId=deviceId;}
    public static final class Channel {
        public final String name, country, url;
        public final ChannelSource source;
        Channel(String n,String c,String u){this(n,c,u,ChannelSource.VAVOO);}
        Channel(String n,String c,String u,ChannelSource s){name=n;country=c;url=u;source=s;}
        public String key(){return source==ChannelSource.GOMSTREAM?"gomstream|"+country+"|"+url:country+"|"+name;}
        public boolean isItalian(){return "Italy".equalsIgnoreCase(country.trim())||"Italia".equalsIgnoreCase(country.trim())||"IT".equalsIgnoreCase(country.trim());}
    }
    void reportPlaybackFailure(int status,String retry){guard.failure("video",status,retry);}
    private final java.util.concurrent.ConcurrentHashMap<Thread,HttpURLConnection> active=new java.util.concurrent.ConcurrentHashMap<>();
    private volatile Thread resolving;
    public void cancelResolve(){Thread thread=resolving;if(thread!=null){thread.interrupt();HttpURLConnection connection=active.get(thread);if(connection!=null)connection.disconnect();okhttp3.Call call=calls.get(thread);if(call!=null)call.cancel();}}
    private Object post(String url,JSONObject data,String sig)throws Exception {
        String host=new URI(url).getHost();guard.check(host);
        try{Object result=rawPost(url,data,sig);guard.success(host);return result;}
        catch(HttpFailure e){guard.failure(host,e.status,e.retryAfter);if(e.status==401||e.status==403){signature=null;signatureTime=0;}throw e;}
        catch(IOException e){if(!Thread.currentThread().isInterrupted())guard.failure(host,0,null);throw e;}
    }
    private static boolean stopRetry(Exception e){return Thread.currentThread().isInterrupted()||e instanceof RequestGuard.Wait||e instanceof HttpFailure&&(((HttpFailure)e).status==401||((HttpFailure)e).status==403||((HttpFailure)e).status==429||((HttpFailure)e).retryAfter!=null);}
    private final java.util.concurrent.ConcurrentHashMap<Thread,okhttp3.Call> calls=new java.util.concurrent.ConcurrentHashMap<>();
    private Object rawPost(String url,JSONObject data,String sig) throws Exception {
        if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Operazione annullata");
        if(transport!=null)return transport.post(url,data,sig);
        if(context!=null){okhttp3.Request.Builder b=new okhttp3.Request.Builder().url(url).post(okhttp3.RequestBody.create(data.toString(),okhttp3.MediaType.get("application/json; charset=utf-8"))).header("Accept","application/json").header("Accept-Language","it").header("User-Agent",sig==null?"TVSatPlayer/0.1":"MediaHubMX/2");if(sig!=null)b.header("mediahubmx-signature",sig);okhttp3.Call call=LiveNetwork.client(context).newCall(b.build());calls.put(Thread.currentThread(),call);try(okhttp3.Response response=call.execute()){if(!response.isSuccessful())throw new HttpFailure(response.code(),response.header("Retry-After"));if(response.body()==null)throw new IOException("Risposta vuota");try(InputStream in=response.body().byteStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();out.write(buffer,0,n);if(out.size()>12*1024*1024)throw new IOException("Risposta troppo grande");}return new JSONTokener(out.toString("UTF-8")).nextValue();}}finally{calls.remove(Thread.currentThread());}}
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
        c.setConnectTimeout(15000); c.setReadTimeout(20000);
        c.setRequestMethod("POST");c.setDoOutput(true);
        c.setRequestProperty("Content-Type","application/json; charset=utf-8");
        c.setRequestProperty("Accept","application/json");
        c.setRequestProperty("User-Agent",sig==null?"TVSatPlayer/0.1":"MediaHubMX/2");
        c.setRequestProperty("Accept-Language","it");
        if(sig!=null)c.setRequestProperty("mediahubmx-signature",sig);
        active.put(Thread.currentThread(),c);
        try {
            if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Operazione annullata");
            byte[] bytes=data.toString().getBytes("UTF-8");
            c.setFixedLengthStreamingMode(bytes.length);
            try(OutputStream out=c.getOutputStream()){out.write(bytes);}
            int status=c.getResponseCode();
            if(status<200||status>=300){
                throw new HttpFailure(status,c.getHeaderField("Retry-After"));
            }
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            try(InputStream in=c.getInputStream()){
                byte[] buffer=new byte[8192];int n;
                while((n=in.read(buffer))!=-1){
                    if(Thread.currentThread().isInterrupted())throw new IOException("Operazione annullata");
                    out.write(buffer,0,n);
                    if(out.size()>12*1024*1024)throw new IOException("Risposta troppo grande");
                }
            }
            return new JSONTokener(out.toString("UTF-8")).nextValue();
        } finally {active.remove(Thread.currentThread());c.disconnect();}
    }
    public synchronized void renewSession() throws Exception {signatureTime=0;signature();}
    private synchronized String signature() throws Exception {
        long now=System.currentTimeMillis();
        if(signature!=null&&now-signatureTime<RENEW_INTERVAL_MS)return signature;
        JSONObject device=new JSONObject().put("type","android").put("uniqueId",deviceId);
        JSONObject os=new JSONObject().put("name","android").put("version",Build.VERSION.RELEASE)
            .put("abis",new JSONArray(Arrays.asList(Build.SUPPORTED_ABIS))).put("host","android");
        JSONObject version=new JSONObject().put("package","tv.vavoo.app").put("binary","3.1.8").put("js","3.1.8");
        JSONObject metadata=new JSONObject().put("device",device).put("os",os)
            .put("app",new JSONObject().put("platform","android")).put("version",version);
        JSONObject payload=new JSONObject().put("reason","app-focus").put("locale","it").put("theme","dark")
            .put("metadata",metadata).put("appFocusTime",0).put("playerActive",false).put("playDuration",0)
            .put("devMode",false).put("hasAddon",true).put("castConnected",false)
            .put("package","tv.vavoo.app").put("version","3.1.8").put("process","app")
            .put("firstAppStart",now).put("lastAppStart",now).put("ipLocation",JSONObject.NULL)
            .put("adblockEnabled",true).put("proxy",new JSONObject().put("supported",new JSONArray().put("ss"))
                .put("engine","Mu").put("enabled",false).put("autoServer",true))
            .put("iap",new JSONObject().put("supported",false));
        Object result=post("https://www.vavoo.tv/api/app/ping",payload,null);
        if(!(result instanceof JSONObject))throw new IOException("Risposta iniziale non valida");
        signature=((JSONObject)result).optString("addonSig","");
        if(signature.length()==0){signature=null;throw new IOException("Il servizio non ha concesso una sessione");}
        signatureTime=now;return signature;
    }
    private JSONObject common()throws JSONException{
        return new JSONObject().put("language","it").put("region","US").put("clientVersion","3.0.2");
    }
    public interface Progress {
        void update(int count);
        default void updateChannels(List<Channel> channels){}
    }
    public List<Channel> catalog(Progress progress)throws Exception {
        signature();Exception last=null;
        for(String base:bases){
            try{
                LinkedHashMap<String,Channel> channels=new LinkedHashMap<>();
                Object cursor=JSONObject.NULL;HashSet<String> seen=new HashSet<>();int reported=0;
                for(int page=0;page<200;page++){
                    JSONObject p=common().put("catalogId","iptv").put("id","iptv").put("adult",false)
                        .put("search","").put("sort","").put("filter",new JSONObject()).put("cursor",cursor);
                    Object result=post(base+"/mediahubmx-catalog.json",p,signature());
                    if(!(result instanceof JSONObject))throw new IOException("Catalogo non valido");
                    JSONObject b=(JSONObject)result;JSONArray items=b.optJSONArray("items");
                    if(items==null)throw new IOException("Il catalogo non contiene un elenco di canali");
                    for(int i=0;i<items.length();i++){
                        JSONObject item=items.optJSONObject(i);if(item==null)continue;
                        String url=item.optString("url","");
                        if(!"iptv".equals(item.optString("type"))||url.isEmpty())continue;
                        String country=item.optString("group","Altro").trim();
                        for(String sep:new String[]{"➾","⟾","->","→","»","›"}){
                            int pos=country.indexOf(sep);if(pos>=0){country=country.substring(0,pos).trim();break;}
                        }
                        Channel c=new Channel(item.optString("name","Canale"),country,url);
                        if(c.isItalian())channels.put(c.key(),c);
                    }
                    if(progress!=null&&channels.size()!=reported){reported=channels.size();progress.update(reported);List<Channel> partial=new ArrayList<>(channels.values());Collections.sort(partial,(a,b2)->a.name.compareToIgnoreCase(b2.name));progress.updateChannels(partial);}
                    cursor=b.opt("nextCursor");
                    if(cursor==null||cursor==JSONObject.NULL||cursor.toString().isEmpty()){
                        if(channels.isEmpty())throw new IOException("Catalogo vuoto");
                        List<Channel> out=new ArrayList<>(channels.values());
                        Collections.sort(out,(a,b2)->a.name.compareToIgnoreCase(b2.name));return out;
                    }
                    if(!seen.add(cursor.toString()))throw new IOException("Il catalogo ripete la stessa pagina");
                }
                throw new IOException("Catalogo oltre il limite di pagine");
            }catch(Exception e){if(stopRetry(e))throw e;last=e;}
        }
        throw last==null?new IOException("Catalogo non disponibile"):last;
    }
    public String resolve(Channel channel)throws Exception {
        Thread owner=Thread.currentThread();resolving=owner;
        try{return resolveInternal(channel);}finally{if(resolving==owner)resolving=null;}
    }
    private String resolveInternal(Channel channel)throws Exception {
        String sig=signature();Exception last=null;
        for(String base:bases){
            try{
                Object b=post(base+"/mediahubmx-resolve.json",common().put("url",channel.url),sig);
                if(b instanceof JSONArray)b=((JSONArray)b).optJSONObject(0);
                if(!(b instanceof JSONObject))throw new IOException("Risposta video non valida");
                JSONObject o=(JSONObject)b;
                String url=o.optString("url",o.optString("streamUrl",""));
                URI u=new URI(url);
                if(!("https".equalsIgnoreCase(u.getScheme())||"http".equalsIgnoreCase(u.getScheme()))||u.getHost()==null)
                    throw new IOException("Indirizzo video non valido");
                return url;
            }catch(Exception e){if(stopRetry(e))throw e;last=e;}
        }
        throw last==null?new IOException("Video non disponibile"):last;
    }
}
