package it.carmine.streamplayer;
import java.net.*;import java.util.regex.*;import java.io.IOException;
/** Reads literal public configuration, including nested objects; never executes remote JS. */
public final class ScPlaylistConfig {
 public static final String USER_AGENT="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36";
 static String object(String html,String name)throws IOException{
  Matcher m=Pattern.compile("(?:window\\.)?"+name+"\\s*=\\s*\\{").matcher(html);if(!m.find())return "";
  int start=m.end()-1,depth=0;char quote=0;boolean escape=false;
  for(int i=start;i<html.length()&&i-start<65536;i++){char c=html.charAt(i);if(quote!=0){if(escape)escape=false;else if(c=='\\')escape=true;else if(c==quote)quote=0;continue;}if(c=='\''||c=='\"')quote=c;else if(c=='{')depth++;else if(c=='}'&&--depth==0)return html.substring(start,i+1);}
  throw new IOException("SC · configurazione incompleta");
 }
 public static String playlist(String html,String embed)throws Exception {
  String block=object(html,"masterPlaylist");if(block.isEmpty())throw new IOException("SC · configurazione playlist non riconosciuta");
  String url=field(block,"url");if(url.isEmpty()){String id=field(object(html,"video"),"id");if(!id.matches("[0-9]+"))throw new IOException("SC · identificativo video assente");URI page=new URI(embed);url=page.getScheme()+"://"+page.getAuthority()+"/playlist/"+id;}
  URI uri=new URI(url);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null)throw new IOException("SC · playlist non valida");
  for(String key:new String[]{"token","expires","asn"}){String value=field(block,key);if(!value.isEmpty()&&!has(url,key))url=append(url,key,value);}
  if(!has(url,"b")&&"1".equals(query(embed,"b")))url=append(url,"b","1");
  // The selected embed explicitly authorizes FHD, rather than a mere name in page scripts.
  if(!has(url,"h")&&"true".equalsIgnoreCase(query(embed,"canPlayFHD")))url=append(url,"h","1");
  return url;
 }
 private static String field(String text,String key){Matcher m=Pattern.compile("(?:^|[,{])\\s*[\"']?"+key+"[\"']?\\s*:\\s*('(?:\\\\.|[^'\\\\])*'|\"(?:\\\\.|[^\"\\\\])*\"|[0-9]+)",Pattern.DOTALL).matcher(text);if(!m.find())return "";String s=m.group(1);return s.startsWith("\"")||s.startsWith("'")?CbMixdropConfig.unquote(s):s;}
 private static boolean has(String url,String key){return Pattern.compile("[?&]"+key+"=").matcher(url).find();}
 private static String query(String url,String key)throws Exception{String q=new URI(url).getRawQuery();if(q!=null)for(String p:q.split("&")){String[] pair=p.split("=",2);if(pair.length==2&&key.equals(pair[0]))return URLDecoder.decode(pair[1],"UTF-8");}return "";}
 private static String append(String url,String key,String value)throws Exception{URI u=new URI(url);String base=url;int hash=base.indexOf('#');String fragment=hash<0?"":base.substring(hash);if(hash>=0)base=base.substring(0,hash);return base+(u.getRawQuery()==null?"?":"&")+key+"="+URLEncoder.encode(value,"UTF-8")+fragment;}
 private ScPlaylistConfig(){}
}
