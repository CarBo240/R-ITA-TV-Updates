package com.rita.eventilive;
import java.net.URI;import java.util.*;
/** Group exact calendar events without discarding any provider URL. */
public final class EventCatalog {
 public static final class Event {
  public final String day,time,title;public final List<FeedParser.Entry> sources;
  Event(FeedParser.Entry first,List<FeedParser.Entry> rows){day=first.day;time=first.time;title=first.title;sources=Collections.unmodifiableList(new ArrayList<>(rows));}
  public boolean italian(){for(FeedParser.Entry e:sources)if(e.language.toUpperCase(Locale.ROOT).contains("ITALIAN"))return true;return false;}
 }
 public static List<Event> group(List<FeedParser.Entry> rows){
  Map<String,List<FeedParser.Entry>> groups=new LinkedHashMap<>();
  for(FeedParser.Entry e:rows){String key=e.day+"\n"+e.time+"\n"+e.title.trim().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT);groups.computeIfAbsent(key,k->new ArrayList<>()).add(e);}
  List<Event> events=new ArrayList<>();for(List<FeedParser.Entry> sources:groups.values())events.add(new Event(sources.get(0),sources));return events;
 }
 public static String channel(FeedParser.Entry e){try{String path=URI.create(e.url).getPath();String name=path.substring(path.lastIndexOf('/')+1).replaceFirst("(?i)\\.php$","").toUpperCase(Locale.ROOT);return name.replaceFirst("^(HD|BR|SPORTTV|ELEVEN)([0-9]+)$","$1 $2").replace("SPORTTV","SPORT TV");}catch(Exception error){return e.url;}}
}
