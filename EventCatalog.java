package com.rita.eventilive;
import java.net.URI;import java.util.*;import java.time.*;import java.time.format.TextStyle;
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
 public static String italianTiming(String englishDay,String time){
  try{DayOfWeek wanted=DayOfWeek.valueOf(englishDay.toUpperCase(Locale.ROOT));LocalTime clock=LocalTime.parse(time);ZoneId source=ZoneId.of("Europe/Lisbon"),italy=ZoneId.of("Europe/Rome");ZonedDateTime now=ZonedDateTime.now(source);int add=(wanted.getValue()-now.getDayOfWeek().getValue()+7)%7;ZonedDateTime converted=ZonedDateTime.of(now.toLocalDate().plusDays(add),clock,source).withZoneSameInstant(italy);String day=converted.getDayOfWeek().getDisplayName(TextStyle.FULL,Locale.ITALIAN);return Character.toUpperCase(day.charAt(0))+day.substring(1)+" · "+String.format(Locale.ITALY,"%02d:%02d",converted.getHour(),converted.getMinute())+" · ora italiana";}catch(Exception error){return italianDay(englishDay)+" · "+time+" · ora italiana";}
 }
 public static String italianDay(String englishDay){try{String day=DayOfWeek.valueOf(englishDay.toUpperCase(Locale.ROOT)).getDisplayName(TextStyle.FULL,Locale.ITALIAN);return Character.toUpperCase(day.charAt(0))+day.substring(1);}catch(Exception error){return englishDay;}}
}
