package it.carmine.streamplayer;

import org.json.*;import java.util.*;

/** Explicit provider markers only: never guesses an intro from elapsed time. */
final class SkipSegments {
 static final class Segment {final String label;final long start,end;Segment(String l,long s,long e){label=l;start=s;end=e;}boolean active(long p){return p>=start&&p<end;}}
 private SkipSegments(){}
 static List<Segment> read(JSONObject title,String episode){ArrayList<Segment> out=new ArrayList<>();JSONObject all=title.optJSONObject("_skipSegments");if(all==null)return out;JSONArray data=all.optJSONArray(episode);if(data==null)data=all.optJSONArray("default");append(out,data);return out;}
 static void attachRai(JSONObject title,JSONObject data,String episode){JSONArray found=new JSONArray();scan(data,found,0);if(found.length()==0)return;JSONObject all=title.optJSONObject("_skipSegments");if(all==null){all=new JSONObject();try{title.put("_skipSegments",all);}catch(Exception ignored){}}try{all.put(episode,found);}catch(Exception ignored){}}
 private static void scan(Object value,JSONArray out,int depth){if(value==null||depth>10)return;if(value instanceof JSONArray){JSONArray a=(JSONArray)value;for(int i=0;i<a.length();i++)scan(a.opt(i),out,depth+1);return;}if(!(value instanceof JSONObject))return;JSONObject o=(JSONObject)value;String type=(o.optString("type")+" "+o.optString("title")+" "+o.optString("label")+" "+o.optString("name")).toLowerCase(Locale.ROOT);String label=type.matches(".*(?:intro|sigla).* ".trim())?"Salta intro":type.matches(".*(?:recap|riassunto|riepilogo).* ".trim())?"Salta il riassunto":"";if(!label.isEmpty()){long start=time(o,"start","start_ms","startTime"),end=time(o,"end","end_ms","endTime");if(valid(start,end))try{out.put(new JSONObject().put("label",label).put("start",start).put("end",end));}catch(Exception ignored){}}for(Iterator<String> it=o.keys();it.hasNext();)scan(o.opt(it.next()),out,depth+1);}
 private static void append(List<Segment> out,JSONArray a){for(int i=0;a!=null&&i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;long s=o.optLong("start",-1),e=o.optLong("end",-1);if(valid(s,e))out.add(new Segment(o.optString("label","Salta intro"),s,e));}}
 private static long time(JSONObject o,String...keys){for(String key:keys)if(o.has(key)){double v=o.optDouble(key,-1);if(v>=0)return v<10000?Math.round(v*1000):Math.round(v);}return -1;}
 private static boolean valid(long s,long e){return s>=0&&e>s&&e-s<=15*60*1000L;}
}
