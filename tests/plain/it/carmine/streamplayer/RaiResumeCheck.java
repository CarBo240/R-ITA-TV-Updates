package it.carmine.streamplayer;
import org.json.*;
public class RaiResumeCheck {
 static void check(boolean ok){if(!ok)throw new AssertionError();}
 public static void main(String[] args)throws Exception{
 VodSource.Title series=RaiLibrary.title("Serie","https://www.raiplay.it/programmi/serie","","tv");
 String first="https://www.raiplay.it/video/first.json",second="https://www.raiplay.it/video/second.json";
 JSONObject a=VodLibrary.progressRecord(series,first,"Episodio 1",123000,3600000,1);
 JSONObject b=VodLibrary.progressRecord(series,second,"Episodio 2",45000,3600000,2);
 check(!VodLibrary.key("raiplay",series.id,first).equals(VodLibrary.key("raiplay",series.id,second)));
 check(a.getLong("position")==123000&&b.getLong("position")==45000);
 check(a.getString("episode").equals(first)&&b.getJSONObject("title").getString("_rai_page").equals("https://www.raiplay.it/programmi/serie"));
 check(VodLibrary.progressRecord(series,first,"E1",3599000,3600000,3).getBoolean("completed"));
 check(VodLibrary.progressRecord(series,first,"E1",3599000,3600000,3).getLong("position")==0);
 System.out.println("5 RaiPlay resume checks passed");
 }
}
