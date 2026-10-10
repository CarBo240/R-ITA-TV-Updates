package it.carmine.streamplayer;

import java.net.URI;import java.util.*;import java.util.regex.*;

/** Extracts only explicit CB01 season/episode navigation from the chosen series page. */
final class CbSeriesNavigation {
 static final class Link {final String url,label;final int season,episode;Link(String u,String l,int s,int e){url=u;label=l;season=s;episode=e;}}
 static final class Result {final List<Link> seasons=new ArrayList<>(),episodes=new ArrayList<>();}
 private static final Pattern TAG=Pattern.compile("(?is)<(?:a|button|option)\\b[^>]*>.*?</(?:a|button|option)>");
 static Result parse(String html,String page){Result out=new Result();Set<String> seen=new HashSet<>();Matcher m=TAG.matcher(html==null?"":html);while(m.find()){String tag=m.group(),label=tag.replaceAll("(?is)<[^>]+>"," ").replaceAll("&nbsp;"," ").replaceAll("\\s+"," ").trim();String raw=first(tag,"href","value","data-href","data-url","data-link");if(raw.isEmpty())continue;try{URI base=URI.create(page),u=base.resolve(raw);if(!"https".equalsIgnoreCase(u.getScheme())||!Objects.equals(base.getHost(),u.getHost())||u.equals(base)||CbCatalogNavigation.blocked(u.toString()))continue;int[] number=numbers(label+" "+tag);boolean seasonWord=(label+" "+tag).matches("(?is).*(?:stagion|season).*"),episodeWord=(label+" "+tag).matches("(?is).*(?:episod|puntat|s\\s*\\d+\\s*e\\s*\\d+|\\d+\\s*[x×]\\s*\\d+).*");if(!seasonWord&&!episodeWord)continue;String key=u.toString();if(!seen.add(key))continue;if(episodeWord&&number[1]>0)out.episodes.add(new Link(key,episodeLabel(label,number),number[0],number[1]));else if(seasonWord)out.seasons.add(new Link(key,seasonLabel(label,number[0]),number[0],0));}catch(Exception ignored){}}out.seasons.sort(Comparator.comparingInt(a->a.season<=0?999:a.season));out.episodes.sort(Comparator.comparingInt((Link a)->a.season<=0?999:a.season).thenComparingInt(a->a.episode));return out;}
 private static String first(String tag,String...names){for(String n:names){Matcher m=Pattern.compile("(?is)\\b"+Pattern.quote(n)+"\\s*=\\s*(['\"])(.*?)\\1").matcher(tag);if(m.find())return m.group(2);}return "";}
 private static int[] numbers(String s){Matcher se=Pattern.compile("(?i)s(?:tagione|eason)?\\s*(\\d{1,2})\\D{0,10}e(?:pisodio)?\\s*(\\d{1,3})|\\b(\\d{1,2})\\s*[x×]\\s*(\\d{1,3})").matcher(s);if(se.find())return new int[]{integer(se.group(1)!=null?se.group(1):se.group(3)),integer(se.group(2)!=null?se.group(2):se.group(4))};Matcher ep=Pattern.compile("(?i)(?:episod(?:io)?|puntata)\\s*(\\d{1,3})").matcher(s);int episode=ep.find()?integer(ep.group(1)):0;Matcher season=Pattern.compile("(?i)(?:stagion(?:e)?|season)\\s*(\\d{1,2})").matcher(s);return new int[]{season.find()?integer(season.group(1)):0,episode};}
 private static int integer(String s){try{return Integer.parseInt(s);}catch(Exception e){return 0;}}
 private static String seasonLabel(String label,int n){return n>0?"Stagione "+n:label;}
 private static String episodeLabel(String label,int[] n){return n[1]>0?(n[0]>0?"S"+n[0]+" · ":"")+"E"+n[1]+(label.matches("(?i).*(?:episod|puntat|s\\s*\\d+|\\d+[x×]\\d+).*" )?"":" · "+label):label;}
}
