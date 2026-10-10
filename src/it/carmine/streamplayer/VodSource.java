package it.carmine.streamplayer;

import android.text.Html;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/** Adapter for the public Italian catalogue and ordinary HLS playback pages. */
final class VodSource {
 static final String USER_AGENT="Mozilla/5.0 (Linux; Android 12; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Mobile Safari/537.36";
 interface Fetcher { String get(String url,String referer)throws Exception; }
 static final Fetcher HTTP=VodSource::fetch;
 static String fetch(String url,String referer)throws Exception{return VodNetwork.fetch(url,referer);}
 static String base(String raw)throws Exception{URI u=new URI(raw.trim().contains("://")?raw.trim():"https://"+raw.trim());if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||!u.getHost().contains(".")||u.getUserInfo()!=null||u.getPort()!=-1)throw new IOException("Inserisci un dominio HTTPS valido");return "https://"+u.getHost().toLowerCase(Locale.ROOT);}
 static String encode(String s){try{return URLEncoder.encode(s,"UTF-8");}catch(UnsupportedEncodingException impossible){throw new AssertionError(impossible);}}
 /** Avoid unknown cache-busting query parameters: some catalogue frontends reject them with HTTP 403. */
 static String fresh(String url){return url;}
 static JSONObject props(String html)throws Exception{
  if(html.trim().startsWith("{")){JSONObject j=new JSONObject(html);return j.optJSONObject("props")!=null?j.getJSONObject("props"):j;}
  Matcher m=Pattern.compile("\\bdata-page\\s*=\\s*([\"'])(.*?)\\1",Pattern.DOTALL).matcher(html);if(!m.find())throw new IOException("Catalogo non riconosciuto o pagina di accesso");
  return new JSONObject(Html.fromHtml(m.group(2),Html.FROM_HTML_MODE_LEGACY).toString()).getJSONObject("props");
 }
 static final class Title {
  final JSONObject json;final String id,slug,name,type,plot,poster,year;
  Title(JSONObject j,String cdn){json=j;id=j.optString("id");slug=j.optString("slug");name=j.optString("name","Senza titolo");type=j.optString("type","movie");plot=j.optString("plot","");String date=j.optString("release_date",j.optString("last_air_date",""));year=date.length()>=4?date.substring(0,4):"";String image="";JSONArray images=j.optJSONArray("images");if(images!=null)for(int i=0;i<images.length();i++){JSONObject x=images.optJSONObject(i);if(x!=null&&"poster".equals(x.optString("type"))){image=cdn+"/images/"+x.optString("filename");break;}}poster=j.optString("_poster_url",image);}
  boolean series(){return "tv".equalsIgnoreCase(type)||"series".equalsIgnoreCase(type);}
  String path(){return "/it/titles/"+id+"-"+slug;}
 }
 static List<Title> catalog(JSONObject p,String home)throws Exception{
  String cdn=p.optString("cdn_url",home.replace("https://","https://cdn."));LinkedHashMap<String,Title> items=new LinkedHashMap<>();JSONArray sliders=p.optJSONArray("sliders");
  if(sliders!=null)for(int i=0;i<sliders.length();i++)append(items,sliders.getJSONObject(i).optJSONArray("titles"),cdn);
  Object titles=p.opt("titles");if(titles instanceof JSONArray)append(items,(JSONArray)titles,cdn);else if(titles instanceof JSONObject)append(items,((JSONObject)titles).optJSONArray("data"),cdn);
  Object results=p.opt("results");if(results instanceof JSONArray)append(items,(JSONArray)results,cdn);
  return new ArrayList<>(items.values());
 }
 private static void append(Map<String,Title> out,JSONArray list,String cdn){if(list==null)return;for(int i=0;i<list.length();i++){JSONObject j=list.optJSONObject(i);if(j==null)continue;Title t=new Title(j,cdn);if(t.id.matches("[0-9]+")&&!t.slug.isEmpty())out.put(t.id,t);}}
 static JSONObject page(String home,String path,Fetcher f)throws Exception{return props(f.get(fresh(home+path),home+"/"));}
 static Title title(JSONObject p,String home)throws Exception{return new Title(p.getJSONObject("title"),p.optString("cdn_url",home.replace("https://","https://cdn.")));}
 static final class Stream {final String url,referer,license,mime;final Map<String,String> licenseHeaders;Stream(String u,String r){this(u,r,"","",Collections.emptyMap());}Stream(String u,String r,String l,String m,Map<String,String> h){url=u;referer=r;license=l;mime=m;licenseHeaders=h;}}
 static String iframe(String html,String parent)throws Exception{Matcher m=Pattern.compile("<iframe\\b[^>]*\\bsrc\\s*=\\s*([\"'])(.*?)\\1",Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(html);if(!m.find())throw new IOException("Player non disponibile");return new URI(parent).resolve(Html.fromHtml(m.group(2),0).toString()).toString();}
 static String playlist(String html)throws Exception{
  Matcher master=Pattern.compile("window\\.masterPlaylist\\s*=\\s*\\{(.*?)\\n\\s*\\}\\s*(?:;|\\n)",Pattern.DOTALL).matcher(html);if(!master.find())throw new IOException("Il server video non espone una playlist compatibile");String block=master.group(1);
  String url=field(block,"url"),token=field(block,"token"),expires=field(block,"expires"),asn=optionalField(block,"asn");
  if(!url.startsWith("https://"))throw new IOException("Playlist non valida");return url+(url.contains("?")?"&":"?")+"token="+encode(token)+"&expires="+encode(expires)+(asn.isEmpty()?"":"&asn="+encode(asn));
 }
 private static String optionalField(String block,String key){Matcher m=Pattern.compile("['\"]?"+key+"['\"]?\\s*:\\s*['\"]([^'\"]*)['\"]").matcher(block);return m.find()?m.group(1):"";}
 private static String field(String block,String key)throws Exception{String v=optionalField(block,key);if(v.isEmpty())throw new IOException("Parametro video assente: "+key);return v;}
 static Stream resolve(String home,String id,String episode,Fetcher f)throws Exception{
  if(!id.matches("[0-9]+")||(!episode.isEmpty()&&!episode.matches("[0-9]+")))throw new IOException("Titolo non valido");String watch=home+"/it/watch/"+id+(episode.isEmpty()?"":"?e="+episode);
  JSONObject p=props(f.get(fresh(watch),home+"/"));if(p.optBoolean("redirectToLogin"))throw new IOException("Questa fonte richiede accesso sul sito");String outer=p.optString("embedUrl","");if(outer.isEmpty())throw new IOException("Video non disponibile");String embed=iframe(f.get(fresh(outer),watch),outer);String html=f.get(embed,outer);String url=playlist(html);
  String manifest=f.get(url,embed);if(!manifest.trim().startsWith("#EXTM3U"))throw new IOException("Playlist non disponibile");return new Stream(url,embed);
 }
}
