package it.carmine.streamplayer;
import java.net.URI;import java.util.*;import java.util.regex.*;
final class CbStreamChoices {
 static final class Choice {final String url,label;final boolean direct;Choice(String u,String l,boolean d){url=u;label=l;direct=d;}}
 static List<Choice> parse(String html,String page){LinkedHashMap<String,Choice> out=new LinkedHashMap<>();
  Matcher tags=Pattern.compile("(?is)<iframe\\b[^>]*>|<source\\b[^>]*>|<video\\b[^>]*>|<a\\b[^>]*>.*?</a>|<(?:button|li|div)\\b[^>]*(?:data-src|data-url|data-link|data-embed)[^>]*>").matcher(html);
  while(tags.find()&&out.size()<30){String tag=tags.group();if(WebMovieLinks.trailer(tag))continue;boolean anchor=tag.regionMatches(true,1,"a",0,1)&&Character.isWhitespace(tag.charAt(2));String raw=WebMovieLinks.attr(tag,anchor?"href":"src");for(String key:new String[]{"data-src","data-url","data-link","data-embed"})if(raw.isEmpty())raw=WebMovieLinks.attr(tag,key);
   try{URI u=new URI(page).resolve(raw);String host=u.getHost();if(raw.isEmpty()||!"https".equals(u.getScheme())||host==null||u.getUserInfo()!=null||u.toString().split("#")[0].equals(page.split("#")[0]))continue;
    String lower=tag.toLowerCase(Locale.ROOT);if(lower.matches("(?s).*(?:doubleclick|googlesyndication|popads|popcash|adsco\\.re|download|scarica|torrent).*"))continue;
    boolean direct=WebMovieLinks.media(u.toString());if(!direct&&!CbLinkTargets.provider(u.toString()))continue;if(anchor&&!direct&&host.equalsIgnoreCase(new URI(page).getHost())&&!u.getPath().matches("(?i).*/(?:go|out|redirect|embed|player)(?:[./].*)?"))continue;if(anchor&&!direct&&!lower.matches("(?s).*(?:streaming|guarda|riproduci|player|watch|supervideo|dropload|mixdrop|streamtape|vidxgo|vcrypt|stayonline|uprot|maxstream).*"))continue;
    String label=anchor?tag.replaceAll("(?s)<[^>]+>"," ").replaceAll("\\s+"," ").trim():WebMovieLinks.attr(tag,"title");if(anchor&&label.length()<2){label=WebMovieLinks.attr(tag,"alt");if(label.isEmpty())label=WebMovieLinks.attr(tag,"title");}if(label.length()<2||label.length()>90)label=host;boolean hd=tag.matches("(?is).*\\b(?:HD|1080p|720p|2160p|4K)\\b.*");if(hd&&!label.matches("(?is).*\\b(?:HD|1080p|720p|2160p|4K)\\b.*"))label+=" · HD";if(!out.containsKey(u.toString()))out.put(u.toString(),new Choice(u.toString(),label,direct));
   }catch(Exception ignored){}
  }
  boolean named=out.values().stream().anyMatch(c->c.label.toLowerCase(Locale.ROOT).matches("(?s).*(?:mixdrop|maxstream|supervideo|dropload|streamtape).*"));
  if(named)out.values().removeIf(c->c.label.matches("(?i)(?:.*\\.)?(?:stayonline\\.pro|uprot\\.net|vcrypt\\.[a-z]+)"));
  return new ArrayList<>(out.values());
 }
}
