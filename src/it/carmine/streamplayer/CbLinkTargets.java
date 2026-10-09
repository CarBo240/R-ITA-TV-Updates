package it.carmine.streamplayer;
import java.net.URI;import java.util.Locale;
final class CbLinkTargets {
 static URI uri(String url){try{URI u=new URI(url);return "https".equals(u.getScheme())&&u.getHost()!=null&&u.getUserInfo()==null?u:null;}catch(Exception e){return null;}}
 static boolean stay(String url){URI u=uri(url);return u!=null&&"stayonline.pro".equalsIgnoreCase(u.getHost())&&u.getPath().matches("/l/[A-Za-z0-9]+/?");}
 static boolean mixdrop(String url){URI u=uri(url);return u!=null&&u.getHost().toLowerCase(Locale.ROOT).matches("(?:[a-z0-9-]+\\.)*(?:mixdrop|m1xdrop|mxdrop|mixdroop|mixdrp)\\.[a-z]{2,24}");}
 static String fileId(String url){URI u=uri(url);if(u==null||!mixdrop(url)||!u.getPath().matches("/[ef]/[A-Za-z0-9]+/?"))return "";return u.getPath().split("/")[2];}
 static String embed(String url){URI u=uri(url);try{if(u!=null&&mixdrop(url)&&u.getPath().matches("/f/[A-Za-z0-9]+/?"))return new URI(u.getScheme(),u.getAuthority(),u.getPath().replaceFirst("^/f/","/e/"),u.getQuery(),u.getFragment()).toString();}catch(Exception ignored){}return url;}
 static boolean provider(String url){URI u=uri(url);if(u==null)return false;return mixdrop(url)||u.getHost().toLowerCase(Locale.ROOT).matches("(?:[a-z0-9-]+\\.)*(?:stayonline|uprot|vcrypt|maxstream|supervideo|dropload|streamtape|vidxgo)\\.[a-z]{2,24}");}
 static boolean navigation(String selected,String target){URI a=uri(selected),b=uri(target);if(a==null||b==null)return false;if(mixdrop(selected))return mixdrop(target)&&!fileId(selected).isEmpty()&&fileId(selected).equals(fileId(target));if(a.getHost().equalsIgnoreCase(b.getHost()))return true;
  return mixdrop(target)||b.getHost().toLowerCase(Locale.ROOT).matches("(?:[a-z0-9-]+\\.)*(?:maxstream|supervideo|dropload|streamtape|vidxgo)[a-z0-9-]*\\.[a-z]{2,24}");
 }
 static boolean ownsMedia(String selected,String media){if(!mixdrop(selected))return true;URI u=uri(media);String id=fileId(selected);if(u==null||id.isEmpty())return false;String h=u.getHost().toLowerCase(Locale.ROOT),path=u.getPath();return (h.equals("mxcontent.net")||h.endsWith(".mxcontent.net")||mixdrop(media))&&(path.contains("/"+id+".")||path.contains("/"+id+"/")||path.endsWith("/"+id));}
}
