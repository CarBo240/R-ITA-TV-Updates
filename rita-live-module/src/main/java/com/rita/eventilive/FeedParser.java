package com.rita.eventilive;
import java.net.URI;
import java.util.*;
import java.util.regex.*;
/** Parses every calendar row, preserving separate providers for the same event. */
public final class FeedParser {
 public static final class Entry {
  public final String day,time,title,url,language;
  Entry(String d,String t,String n,String u,String l){day=d;time=t;title=n;url=u;language=l;}
 }
 public static List<Entry> parse(String html){
  String text=html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>","").replaceAll("(?i)<br\\s*/?>","\n").replaceAll("<[^>]+>","");
  text=text.replace("&amp;","&").replace("&nbsp;"," ").replace("&quot;","\"").replace("&#39;","'").replace("&lt;","<").replace("&gt;",">");
  List<Entry> parsed=new ArrayList<>();String day="EVENTI";Map<String,String> langs=new HashMap<>();Set<String> keys=new HashSet<>();
  Pattern ev=Pattern.compile("^(\\d{1,2}:\\d{2})\\s+(.+?)\\s*\\|\\s*(https?://\\S+)");Pattern lang=Pattern.compile("^(HD\\d+|BR\\d+)\\s+(.+)$",Pattern.CASE_INSENSITIVE);Pattern channel=Pattern.compile("/(hd\\d+|br\\d+)\\.php",Pattern.CASE_INSENSITIVE);
  for(String line:text.split("\n")){line=line.trim();if(line.matches("(?i)MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY")){day=line.toUpperCase(Locale.ROOT);langs.clear();continue;}Matcher l=lang.matcher(line);if(l.matches()){langs.put(l.group(1).toLowerCase(Locale.ROOT),l.group(2));continue;}Matcher m=ev.matcher(line);if(!m.find())continue;String url=m.group(3);try{URI u=new URI(url);if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null)continue;}catch(Exception e){continue;}Matcher ch=channel.matcher(url);String language=ch.find()?langs.getOrDefault(ch.group(1).toLowerCase(Locale.ROOT),"Non indicata"):"Non indicata";String key=day+m.group(1)+m.group(2)+url;if(keys.add(key))parsed.add(new Entry(day,m.group(1),m.group(2),url,language));}
  return parsed;
 }
}
