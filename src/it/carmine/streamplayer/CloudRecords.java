package it.carmine.streamplayer;
import org.json.*;
/** Causal revisions prevent an old offline checkpoint from overwriting newer progress. */
final class CloudRecords {
 static boolean keyAllowed(String key){return key!=null&&key.length()<240&&key.matches("(?:fav|watch|history):[a-zA-Z0-9_-]+:[a-zA-Z0-9:_-]+");}
 static boolean payloadAllowed(String key,String payload){if(!keyAllowed(key)||payload==null||payload.length()>200000)return false;try{JSONObject j=new JSONObject(payload);JSONObject title=key.startsWith("history:")?j.optJSONObject("title"):j;return title!=null&&title.optString("id").matches("[0-9]+")&&(!key.startsWith("history:")||j.optLong("position",0)>=0);}catch(Exception e){return false;}}
 static boolean canCommit(long expected,long current){return expected==current;}
 static String episodeKey(int season,int episode,String fallback){return season>0&&episode>0?"s"+season+"e"+episode:fallback;}
}
