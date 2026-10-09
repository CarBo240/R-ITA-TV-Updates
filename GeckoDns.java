package it.carmine.streamplayer;
import android.app.Activity;import android.os.*;import java.util.*;
import org.mozilla.geckoview.GeckoPreferenceController;
/** Configure the existing Gecko runtime before loading any requested page. */
final class GeckoDns {
 private static final HandlerThread thread=new HandlerThread("GeckoDnsSettings");private static Handler handler;
 static synchronized Handler handler(){if(handler==null){thread.start();handler=new Handler(thread.getLooper());}return handler;}
 static Map<String,Object> prefs(boolean cloudflare){return prefs(cloudflare?"cloudflare":"system");}
 static Map<String,Object> prefs(String provider){Map<String,Object> p=new LinkedHashMap<>();p.put("network.trr.uri",VodNetwork.endpoint(provider));p.put("network.trr.bootstrapAddr",VodNetwork.bootstrap(provider));p.put("network.trr.mode","system".equals(provider)?5:3);return p;}
 static void configure(Activity activity,Runnable ready,Runnable failed){String selected=VodNetwork.provider(activity);handler().post(()->{try{List<GeckoPreferenceController.SetGeckoPreference<?>> values=new ArrayList<>();for(Map.Entry<String,Object> entry:prefs(selected).entrySet()){if(entry.getValue() instanceof Integer)values.add(GeckoPreferenceController.SetGeckoPreference.setIntPref(entry.getKey(),(Integer)entry.getValue(),GeckoPreferenceController.PREF_BRANCH_USER));else values.add(GeckoPreferenceController.SetGeckoPreference.setStringPref(entry.getKey(),entry.getValue().toString(),GeckoPreferenceController.PREF_BRANCH_USER));}GeckoPreferenceController.setGeckoPrefs(values).accept(result->activity.runOnUiThread(()->{if(result!=null&&result.size()==values.size()&&!result.containsValue(false))ready.run();else failed.run();}),error->activity.runOnUiThread(failed));}catch(Exception e){activity.runOnUiThread(failed);}});}
}
