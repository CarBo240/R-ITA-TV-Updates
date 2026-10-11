package it.carmine.streamplayer.lab211;
import java.text.Normalizer;
import java.util.regex.*;
/** Pure policy for the production bridge; host recognition alone never verifies playback. */
public final class LabVodPolicy {
 private LabVodPolicy(){}
 public static String adapterKind(String url){String host=LabPipeline.host(url).replaceFirst("^www\\.","");if(host.matches("(?:cb01[a-z0-9]*|cineblog[a-z0-9]*)\\.[a-z]{2,24}"))return "cb01";if(host.matches("altadefinizione[a-z0-9]*\\.[a-z]{2,24}"))return "altadefinizione";return "";}
 public static String clean(String name){return name.replaceAll("(?i)\\[(?:hd|sd|4k|ita|sub ita)\\]","").replaceFirst("\\s*\\((?:19|20)[0-9]{2}\\)\\s*$","").trim();}
 public static String year(String name){Matcher m=Pattern.compile("\\((19[0-9]{2}|20[0-9]{2})\\)\\s*$").matcher(name);return m.find()?m.group(1):"";}
 private static String normal(String name){return Normalizer.normalize(name,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]","");}
 public static boolean titleMatches(String wanted,String original,String wantedYear,LabPipeline.Title found){try{String path=LabPipeline.https(found.url).getPath();if(path.matches("(?i).*(?:/serie-tv/|/series/|/tv/|episodi|stagione).*"))return false;}catch(Exception e){return false;}String candidate=normal(clean(found.name)),candidateYear=year(found.name);return (candidate.equals(normal(wanted))||!original.isEmpty()&&candidate.equals(normal(original)))&&(wantedYear.isEmpty()||candidateYear.isEmpty()||wantedYear.equals(candidateYear));}
 public static boolean playable(LabPipeline.Source source){
  try{LabPipeline.https(source.url);}catch(Exception e){return false;}
  if(source.kind==LabPipeline.SourceKind.TRAILER)return false;
  if(source.kind==LabPipeline.SourceKind.MEDIA)return true;
  try{java.net.URI uri=LabPipeline.https(source.url);if("vixsrc.to".equalsIgnoreCase(uri.getHost())&&uri.getPath().matches("/api/movie/(?:tt[0-9]{7,12}|[1-9][0-9]{0,9})"))return true;
   if(source.kind==LabPipeline.SourceKind.INTERMEDIARY)return "stayonline.pro".equalsIgnoreCase(uri.getHost())&&normal(source.label).contains("mixdrop");
   String resolver=new LabPipeline(null,null).resolver(source.url).name();return resolver.equals("MixDrop")||resolver.equals("VidXGo");
  }catch(Exception e){return false;}
 }
}
