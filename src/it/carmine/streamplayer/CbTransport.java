package it.carmine.streamplayer;
import android.webkit.CookieManager;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

final class CbTransport {
 static final String UA="Mozilla/5.0 (Linux; Android 12; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Mobile Safari/537.36";
 static final OkHttpClient HTTP=new OkHttpClient.Builder().connectTimeout(10,TimeUnit.SECONDS).readTimeout(15,TimeUnit.SECONDS).callTimeout(20,TimeUnit.SECONDS).cookieJar(new CookieJar(){
  public void saveFromResponse(HttpUrl url,List<Cookie> cookies){for(Cookie c:cookies)CookieManager.getInstance().setCookie(url.toString(),c.toString());}
  public List<Cookie> loadForRequest(HttpUrl url){List<Cookie> out=new ArrayList<>();String raw=CookieManager.getInstance().getCookie(url.toString());if(raw!=null)for(String part:raw.split(";")){int eq=part.indexOf('=');if(eq>0)try{out.add(new Cookie.Builder().name(part.substring(0,eq).trim()).value(part.substring(eq+1).trim()).domain(url.host()).path("/").build());}catch(Exception ignored){}}return out;}
 }).build();
 static final OkHttpClient PLAYBACK=HTTP.newBuilder().callTimeout(0,TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS).build();
}
