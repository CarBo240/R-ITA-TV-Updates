package com.rita.eventilive;
import org.json.*;
import java.net.URI;
import java.util.*;
import java.util.regex.*;

/** Reads the provider's actual PLAYERS JSON array; no guessed player endpoints. */
final class DaddyPlayers {
 static final class Choice {
  final int number;final String source,url;final boolean hls,upgraded;
  Choice(int n,String src,String u,boolean direct){number=n;source=src;url=u;hls=direct;upgraded=!src.equals(u);}
  String label(){return "Player "+number;}
 }
 static String upgradeSources(String html)throws JSONException {Matcher m=Pattern.compile("(?:const|let|var)\\s+PLAYERS\\s*=\\s*(\\[.*?\\])\\s*;",Pattern.DOTALL).matcher(html);if(!m.find())return html;JSONArray slots=new JSONArray(m.group(1));for(int i=0;i<slots.length();i++){JSONObject slot=slots.optJSONObject(i);if(slot!=null){String src=slot.optString("src","");if(src.startsWith("http://"))slot.put("src","https://"+src.substring(7));}}return html.substring(0,m.start(1))+slots.toString()+html.substring(m.end(1));}
 static String select(String html,Choice choice)throws JSONException {
  Matcher players=Pattern.compile("(?:const|let|var)\\s+PLAYERS\\s*=\\s*(\\[.*?\\])\\s*;",Pattern.DOTALL).matcher(html);
  if(!players.find())throw new JSONException("PLAYERS array missing");JSONArray slots=new JSONArray(players.group(1));JSONObject selected=slots.optJSONObject(choice.number-1);if(selected==null)throw new JSONException("Selected player missing");selected.put("src",choice.url);
  html=html.substring(0,players.start(1))+slots.toString()+html.substring(players.end(1));
  html=html.replaceAll("((?:let|var|const)\\s+currentPlayer\\s*=\\s*)[0-9]+", "$1"+choice.number);
  Matcher frame=Pattern.compile("<iframe\\b(?=[^>]*\\bid\\s*=\\s*[\"']media-player[\"'])[^>]*>",Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(html);
  if(frame.find()){String tag=frame.group();Matcher src=Pattern.compile("\\bsrc\\s*=\\s*([\"'])[^\"']*\\1",Pattern.CASE_INSENSITIVE).matcher(tag);String attribute="src=\""+choice.url.replace("&","&amp;").replace("\"","&quot;")+"\"";
   tag=src.find()?src.replaceFirst(Matcher.quoteReplacement(attribute)):tag.substring(0,tag.length()-1)+" "+attribute+">";
   html=html.substring(0,frame.start())+tag+html.substring(frame.end());
  }return html;
 }

 static List<Choice> parse(String html)throws JSONException {
  Matcher m=Pattern.compile("(?:const|let|var)\\s+PLAYERS\\s*=\\s*(\\[.*?\\])\\s*;",Pattern.DOTALL).matcher(html);
  if(!m.find())return Collections.emptyList();JSONArray array=new JSONArray(m.group(1));List<Choice> out=new ArrayList<>();
  for(int i=0;i<array.length();i++){JSONObject slot=array.optJSONObject(i);if(slot==null)continue;String raw=slot.optString("src","");
   try{URI uri=new URI(raw);String scheme=uri.getScheme();if(uri.getHost()==null||uri.getUserInfo()!=null||!("https".equalsIgnoreCase(scheme)||"http".equalsIgnoreCase(scheme)))continue;
    String url="http".equalsIgnoreCase(scheme)?"https"+raw.substring(raw.indexOf(':')):raw;
    out.add(new Choice(i+1,raw,url,slot.optBoolean("hls",false)));
   }catch(Exception ignored){}
  }return out;
 }
}
