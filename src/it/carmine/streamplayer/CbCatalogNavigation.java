package it.carmine.streamplayer;
import java.net.URI;import java.util.*;
/** Preserve the site's own series route and form, as in VOD Universal Test. */
final class CbCatalogNavigation {
 static final String LINKS_JS="""
 JSON.stringify(Array.from(document.querySelectorAll('a[href]')).filter(a=>/^(?:serie[ -]?tv|series|film|movies)$/i.test(a.textContent.trim())).map(a=>a.href))
 """;
 static boolean allowed(String url){try{URI u=new URI(url);return "https".equalsIgnoreCase(u.getScheme())&&u.getHost()!=null&&u.getUserInfo()==null&&!blocked(url);}catch(Exception e){return false;}}
 static boolean blocked(String url){return url.toLowerCase(Locale.ROOT).matches(".*(?:doubleclick|googlesyndication|popads|popcash|adsco\\.|/ads?[/.]|preroll|trailer|youtube|youtu\\.be).* ".trim());}
 static boolean seriesPage(String url){try{URI u=new URI(url);String value=(u.getHost()+u.getPath()).toLowerCase(Locale.ROOT);return value.matches(".*(?:serie-tv|serie_tv|serietv|serial|(?:^|[./-])series(?:[./-]|$)).*");}catch(Exception e){return false;}}
 static void enqueue(ArrayDeque<String> pages,Set<String> visited,String url,int mode){if(!allowed(url)||visited.contains(url)||pages.contains(url)||pages.size()>=3)return;if(mode==2&&seriesPage(url))pages.addFirst(url);else pages.addLast(url);}
}
