package it.carmine.streamplayer;

import android.content.SharedPreferences;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/** Backs off on service errors. Never retries an access denial on another host. */
final class RequestGuard {
    interface Clock {long now();}
    private final SharedPreferences prefs;private final Clock clock;
    private final Map<String,Integer> failures=new HashMap<>();private final Map<String,Long> until=new HashMap<>();
    private long serviceUntil;
    RequestGuard(SharedPreferences p){this(p,System::currentTimeMillis);}
    RequestGuard(SharedPreferences p,Clock c){prefs=p;clock=c;serviceUntil=p==null?0:p.getLong("serviceUntil",0);}
    static final class Wait extends IOException {
        Wait(long ms){super("Il servizio richiede una pausa. Riprova tra "+Math.max(1,(ms+999)/1000)+" secondi.");}
    }
    synchronized void check(String host)throws Wait {
        long ready=Math.max(serviceUntil,until.getOrDefault(host,prefs==null?0:prefs.getLong("until:"+host,0)));
        if(ready>clock.now())throw new Wait(ready-clock.now());
    }
    synchronized void success(String host){failures.remove(host);until.remove(host);if(prefs!=null)prefs.edit().remove("until:"+host).apply();}
    synchronized void failure(String host,int status,String retryAfter){
        long now=clock.now(),delay=0;
        if(status==401||status==403)delay=300000;
        else if(status==429||((status==503)&&retryAfter!=null&&!retryAfter.isEmpty()))delay=Math.max(60000,retryDelay(retryAfter,now));
        if(delay>0){serviceUntil=Math.max(serviceUntil,now+delay);if(prefs!=null)prefs.edit().putLong("serviceUntil",serviceUntil).apply();return;}
        int count=failures.getOrDefault(host,0)+1;failures.put(host,count);
        if(count>=2){long ready=now+Math.min(120000,15000L<<(Math.min(count-2,3)));until.put(host,ready);if(prefs!=null)prefs.edit().putLong("until:"+host,ready).apply();}
    }
    static long retryDelay(String header,long now){
        if(header==null)return 0;
        try{return Math.min(7*86400000L,Math.max(0,Long.parseLong(header.trim()))*1000);}catch(Exception ignored){}
        try{SimpleDateFormat f=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.US);f.setLenient(false);return Math.max(0,Math.min(7*86400000L,f.parse(header.trim()).getTime()-now));}catch(Exception e){return 0;}
    }
}
