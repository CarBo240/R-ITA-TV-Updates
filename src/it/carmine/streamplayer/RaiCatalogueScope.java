package it.carmine.streamplayer;
import java.net.URI;
/** Live channel URLs belong to TV, never to the on-demand catalogue or its search. */
final class RaiCatalogueScope {
 static boolean live(String url){try{URI u=new URI(url);return "https".equalsIgnoreCase(u.getScheme())&&u.getUserInfo()==null&&("www.raiplay.it".equalsIgnoreCase(u.getHost())||"raiplay.it".equalsIgnoreCase(u.getHost()))&&u.getPath()!=null&&u.getPath().matches("/dirette/[^/]+(?:\\.html)?");}catch(Exception e){return false;}}
 static boolean onDemand(String url){try{URI u=new URI(url);String h=u.getHost(),p=u.getPath();return "https".equalsIgnoreCase(u.getScheme())&&u.getUserInfo()==null&&h!=null&&(h.equalsIgnoreCase("raiplay.it")||h.toLowerCase(java.util.Locale.ROOT).endsWith(".raiplay.it"))&&p!=null&&(p.startsWith("/video/")||p.startsWith("/programmi/"));}catch(Exception e){return false;}}
}
