package it.carmine.streamplayer;
import android.app.Activity;import android.content.*;import android.widget.Toast;
final class GeckoCompanion {
 static boolean open(Activity a,String url,String title,boolean daddy){Intent i=new Intent().setComponent(new ComponentName("it.carmine.ritagecko","it.carmine.ritagecko.GeckoEventActivity")).putExtra("url",url).putExtra("title",title).putExtra("daddyLive",daddy);try{a.startActivity(i);return true;}catch(ActivityNotFoundException|SecurityException e){Toast.makeText(a,"Installa R. ITA Gecko per aprire questa sorgente",Toast.LENGTH_LONG).show();return false;}}
}
