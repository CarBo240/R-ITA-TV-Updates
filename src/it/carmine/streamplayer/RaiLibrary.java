package it.carmine.streamplayer;
import android.content.Context;import org.json.*;import java.util.*;
final class RaiLibrary {
 static VodSettings.Source source(){return new VodSettings.Source("raiplay","RaiPlay","https://www.raiplay.it","raiplay");}
 static List<VodSource.Title> history(Context c){List<VodSource.Title> out=new ArrayList<>();for(VodSource.Title t:VodLibrary.titles(c,source(),true)){JSONObject h=VodLibrary.latest(c,"raiplay",t.id);if(h!=null&&!h.optBoolean("completed")&&h.optLong("position")>0)out.add(t);}return out;}
 static void confirmRemove(android.app.Activity a,VodSource.Title title,Runnable changed){new android.app.AlertDialog.Builder(a).setTitle(title.name).setMessage("Rimuovere questo contenuto da Continua a guardare?").setPositiveButton("Rimuovi",(dialog,n)->{VodLibrary.removeHistory(a,"raiplay",title.id);changed.run();}).setNegativeButton("Annulla",null).show();}
 static void open(android.app.Activity a,VodSource.Title t){a.startActivity(new android.content.Intent(a,RaiPlayActivity.class).putExtra("titleJson",t.json.toString()).putExtra("resume",true));}
 static VodSource.Title title(String name,String url,String poster,String type)throws Exception{return new VodSource.Title(new JSONObject().put("id",String.valueOf(Integer.toUnsignedLong(url.hashCode()))).put("slug","raiplay").put("name",name).put("type",type).put("_rai",true).put("_rai_page",url).put("_poster_url",poster),"");}
}
