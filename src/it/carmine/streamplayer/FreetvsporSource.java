package it.carmine.streamplayer;

import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

/** Resolves the FreeTVSpor slot declared by Daddy Live without fixed channel/player URLs. */
final class FreetvsporSource {
 static final String HOME="https://freetvspor.cfd";
 static final class Channel {final String id,name;Channel(String id,String name){this.id=id;this.name=name;}}
 static final class Page {final String url,body;Page(String url,String body){this.url=url;this.body=body;}}
 static final class Resolved {final String url,referer;Resolved(String url,String referer){this.url=url;this.referer=referer;}}
 interface Transport {Page get(String url,String referer)throws IOException;}

 static List<Channel> catalog(String daddyHome,Transport http)throws Exception {
  Page page=http.get(daddyHome+"/cache/channels.json",daddyHome+"/");List<Channel> out=new ArrayList<>();Set<String> ids=new HashSet<>();
  for(DaddyLiveSource.Item item:DaddyLiveSource.parseChannels(page.body)){
   if(!item.italian()||item.links.isEmpty())continue;String id=item.links.get(0).id;if(!id.matches("[0-9]{1,8}")||!ids.add(id))continue;out.add(new Channel(id,item.title));
  }
  if(out.isEmpty())throw new IOException("Il catalogo non contiene canali italiani");return out;
 }

 static String resolve(Channel channel,String daddyHome,String configuredHome,Transport http)throws Exception {
  return resolveWithContext(channel,daddyHome,configuredHome,http).url;
 }

 static Resolved resolveWithContext(Channel channel,String daddyHome,String configuredHome,Transport http)throws Exception {
  if(!channel.id.matches("[0-9]{1,8}"))throw new IOException("Canale non valido");
  String parent=daddyHome+"/player/embed.php?id="+URLEncoder.encode(channel.id,"UTF-8");Page selector=http.get(parent,daddyHome+"/");String selected=selectPlayer(selector.body,selector.url,configuredHome);
  ArrayDeque<String[]> pending=new ArrayDeque<>();pending.add(new String[]{selected,selector.url,"0"});Set<String> seen=new HashSet<>();IOException last=null;
  while(!pending.isEmpty()&&seen.size()<12){String[] item=pending.removeFirst();String url=item[0],referer=item[1];int depth=Integer.parseInt(item[2]);if(!seen.add(url))continue;
   try{Page page=http.get(url,referer);String media=media(page);if(media!=null)return new Resolved(media,page.url);if(depth>=5)continue;for(String frame:frames(page))pending.addLast(new String[]{frame,page.url,String.valueOf(depth+1)});}catch(IOException e){last=e;}
  }
  throw new IOException("FreeTVSpor non ha esposto una playlist HLS pubblica",last);
 }

 static String selectPlayer(String html,String parent,String configuredHome)throws Exception {
  Matcher m=Pattern.compile("(?:const|let|var)\\s+PLAYERS\\s*=\\s*(\\[.*?\\])\\s*;",Pattern.DOTALL).matcher(html);if(!m.find())throw new IOException("Elenco player del canale non riconosciuto");
  JSONArray players=new JSONArray(m.group(1));String configured=host(configuredHome);String fallback=null;
  for(int i=0;i<players.length();i++){JSONObject p=players.optJSONObject(i);if(p==null)continue;String raw=decode(p.optString("src",""));if(raw.isEmpty())continue;String absolute=absolute(parent,raw),candidate=host(absolute);if(candidate.equals(configured)||candidate.contains("freetvspor"))return absolute;if(p.optString("name","").toLowerCase(Locale.ROOT).contains("freetvspor"))fallback=absolute;}
  if(fallback!=null)return fallback;throw new IOException("FreeTVSpor non è presente tra i player di questo canale");
 }

 static String media(Page page)throws IOException {
  String html=decode(page.body);List<Pattern> rules=Arrays.asList(
   Pattern.compile("(?:const|let|var)\\s+(?:SRC|source|file)\\s*=\\s*([\"'])([^\"'\\r\\n]+)\\1",Pattern.CASE_INSENSITIVE),
   Pattern.compile("(?:file|src)\\s*:\\s*([\"'])([^\"'\\r\\n]+\\.m3u8(?:\\?[^\"'\\r\\n]*)?)\\1",Pattern.CASE_INSENSITIVE),
   Pattern.compile("(https?:\\/\\/[^\\s\"'<>]+\\.m3u8(?:\\?[^\\s\"'<>]*)?)",Pattern.CASE_INSENSITIVE));
  for(Pattern rule:rules){Matcher m=rule.matcher(html);while(m.find()){String value=m.groupCount()>1?m.group(2):m.group(1);try{String candidate=absolute(page.url,decode(value));if(isHls(candidate))return candidate;}catch(Exception ignored){}}}
  return null;
 }

 static List<String> frames(Page page){List<String> out=new ArrayList<>();Matcher m=Pattern.compile("<iframe\\b[^>]*\\bsrc\\s*=\\s*([\"'])(.*?)\\1",Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(page.body);while(m.find()&&out.size()<8)try{String u=absolute(page.url,decode(m.group(2)));String h=host(u);if(!h.contains("chatango")&&!h.contains("histats")&&!out.contains(u))out.add(u);}catch(Exception ignored){}return out;}
 static boolean isHls(String value){try{URI u=new URI(value);return "https".equalsIgnoreCase(u.getScheme())&&u.getHost()!=null&&u.getUserInfo()==null&&u.getPath()!=null&&u.getPath().toLowerCase(Locale.ROOT).endsWith(".m3u8");}catch(Exception e){return false;}}
 static String absolute(String parent,String value)throws IOException {try{URI u=URI.create(parent).resolve(value.trim());if("http".equalsIgnoreCase(u.getScheme()))u=new URI("https",u.getUserInfo(),u.getHost(),u.getPort(),u.getPath(),u.getQuery(),u.getFragment());if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null)throw new IOException("Indirizzo non HTTPS");return u.toASCIIString();}catch(IllegalArgumentException|URISyntaxException e){throw new IOException("Indirizzo player non valido",e);}}
 static String host(String value){try{String h=new URI(value).getHost();return h==null?"":h.toLowerCase(Locale.ROOT);}catch(Exception e){return "";}}
 static String decode(String value){return value.replace("\\/","/").replace("&amp;","&").replace("&#x2F;","/").replace("&#47;","/").trim();}
}
