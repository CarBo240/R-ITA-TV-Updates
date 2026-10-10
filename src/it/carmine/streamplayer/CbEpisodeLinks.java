package it.carmine.streamplayer;
import java.net.URI;import java.util.*;import java.util.regex.*;
/** Same-site episode links only; the selected series is never searched again by name. */
final class CbEpisodeLinks {
 static Map<String,String> parse(String html,String page){Map<String,String> out=new LinkedHashMap<>();Matcher m=Pattern.compile("(?is)<a\\b[^>]*>.*?</a>").matcher(html);while(m.find()){String tag=m.group(),label=tag.replaceAll("(?s)<[^>]+>"," ").replaceAll("\\s+"," ").trim();if(!label.matches("(?is).*(?:episod|puntat|\\b\\d{1,2}[x×]\\d{1,3}\\b|s\\d+e\\d+).*"))continue;try{URI p=URI.create(page),u=p.resolve(WebMovieLinks.attr(tag,"href"));if("https".equals(u.getScheme())&&Objects.equals(p.getHost(),u.getHost())&&!u.equals(p)&&!u.getPath().matches("(?i).*/(?:tag|category|page)/.*")&&out.size()<200)out.putIfAbsent(u.toString(),label);}catch(Exception ignored){}}return out;}
}
