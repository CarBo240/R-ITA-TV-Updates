package it.carmine.streamplayer;
import java.net.*;import java.util.regex.*;import java.io.IOException;
/** Parses declared player data, preserving signed query parameters. */
final class ScPlaylistConfig {
 static String playlist(String html,String embed)throws Exception {
  Matcher object=Pattern.compile("(?:window\\.)?masterPlaylist\\s*=\\s*\\{([^}]{1,32768})\\}",Pattern.DOTALL).matcher(html);
  if(!object.find())throw new IOException("SC · configurazione playlist non riconosciuta");
  String block=object.group(1),url=field(block,"url");
  URI uri=new URI(url);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null)throw new IOException("SC · playlist non valida");
  for(String key:new String[]{"token","expires","asn"}){String value=field(block,key);if(!value.isEmpty()&&!has(url,key))url=append(url,key,value);}
  if(!has(url,"b")&&"1".equals(query(embed,"b")))url=append(url,"b","1");
  if(!has(url,"h")&&Pattern.compile("[\"']?canPlayFHD[\"']?\\s*[:=]\\s*true\\b",Pattern.CASE_INSENSITIVE).matcher(html).find())url=append(url,"h","1");
  return url;
 }
 private static String field(String text,String key){Matcher m=Pattern.compile("(?:^|,)\\s*[\"']?"+key+"[\"']?\\s*:\\s*('(?:\\\\.|[^'\\\\])*'|\"(?:\\\\.|[^\"\\\\])*\"|[0-9]+)",Pattern.DOTALL).matcher(text);if(!m.find())return "";String s=m.group(1);return s.startsWith("\"")||s.startsWith("'")?CbMixdropConfig.unquote(s):s;}
 private static boolean has(String url,String key){return Pattern.compile("[?&]"+key+"=").matcher(url).find();}
 private static String query(String url,String key)throws Exception{String q=new URI(url).getRawQuery();if(q!=null)for(String p:q.split("&")){String[] pair=p.split("=",2);if(pair.length==2&&key.equals(pair[0]))return URLDecoder.decode(pair[1],"UTF-8");}return "";}
 private static String append(String url,String key,String value)throws Exception{URI u=new URI(url);String base=url;int hash=base.indexOf('#');String fragment=hash<0?"":base.substring(hash);if(hash>=0)base=base.substring(0,hash);return base+(u.getRawQuery()==null?"?":"&")+key+"="+URLEncoder.encode(value,"UTF-8")+fragment;}
 private ScPlaylistConfig(){}
}
