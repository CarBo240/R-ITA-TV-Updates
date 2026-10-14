package it.carmine.streamplayer.lab211;
import java.net.*;import java.util.*;import java.util.regex.*;
/** Uses the provider's own catalogue session and explicitly declared episode IDs. */
public final class AnimeUnityCatalog implements LabPipeline.CatalogAdapter {
 public String name(){return "AnimeUnity";}
 public String search(String base,String query)throws Exception{return LabPipeline.origin(base)+"/archivio";}
 public LabPipeline.Page catalog(LabPipeline.Transport transport,String base,String query)throws Exception {
  String archive=search(base,query);LabPipeline.Page session=transport.get(archive,base,false);if(session.status!=200)throw new LabPipeline.Failure(LabPipeline.Stage.CATALOG,"HTTP_"+session.status,"AnimeUnity: archivio non disponibile");
  Matcher meta=Pattern.compile("(?is)<meta\\b[^>]*>").matcher(session.text());String csrf="";while(meta.find()){Map<String,String> attrs=LabPipeline.attrs(meta.group());if("csrf-token".equals(attrs.get("name")))csrf=attrs.getOrDefault("content","");}
  if(csrf.isEmpty()||csrf.length()>1024)throw new LabPipeline.Failure(LabPipeline.Stage.CATALOG,"UNITY_SESSION","AnimeUnity: sessione di ricerca non disponibile");
  String title=query.isEmpty()?"false":"\""+query.replace("\\","\\\\").replace("\"","\\\"").replaceAll("[\\x00-\\x1f]","")+"\"";
  return transport.postJson(LabPipeline.origin(base)+"/archivio/get-animes","{\"title\":"+title+",\"type\":false,\"year\":false,\"order\":false,\"status\":false,\"genres\":false,\"offset\":0,\"dubbed\":false,\"season\":false}",archive,csrf);
 }
 public List<LabPipeline.Title> titles(LabPipeline.Page page)throws Exception {
  Object value=LabPipeline.publicJson(page.text());Object records=value instanceof Map?((Map<?,?>)value).get("records"):null;List<LabPipeline.Title> out=new ArrayList<>();if(!(records instanceof List))return out;
  for(Object item:(List<?>)records)if(item instanceof Map&&out.size()<100){Map<?,?> r=(Map<?,?>)item;String id=text(r,"id"),slug=text(r,"slug"),title=text(r,"title_eng");if(title.isEmpty())title=text(r,"title");String image=text(r,"imageurl").replace('\\','/');image=image.substring(image.lastIndexOf('/')+1);if(!id.matches("[1-9][0-9]*")||!slug.matches("[A-Za-z0-9_-]+")||title.isEmpty()||image.isEmpty())continue;String domain=LabPipeline.host(page.url).replaceFirst("^www\\.","");out.add(new LabPipeline.Title(title,"https://img."+domain+"/anime/"+image,LabPipeline.origin(page.url)+"/anime/"+id+"-"+slug));}return out;
 }
 static String text(Map<?,?> map,String key){Object value=map.get(key);if(value instanceof Number){double number=((Number)value).doubleValue();if(Double.isFinite(number)&&number==Math.rint(number)&&Math.abs(number)<9e15)return Long.toString(((Number)value).longValue());}return value==null?"":String.valueOf(value);}
 public List<LabPipeline.Source> sources(LabPipeline.Page page)throws Exception{return sources(page,null);}
 public List<LabPipeline.Source> sources(LabPipeline.Page page,LabPipeline.Transport transport)throws Exception {
  Matcher player=Pattern.compile("(?is)<video-player\\b[^>]*>").matcher(page.text());List<LabPipeline.Source> out=new ArrayList<>();if(!player.find())return out;Map<String,String> a=LabPipeline.attrs(player.group());String raw=a.getOrDefault("episodes","");Object episodes=null;if(!raw.isEmpty())try{episodes=LabPipeline.publicJson(raw.trim().startsWith("[")?raw:URLDecoder.decode(raw,"UTF-8"));}catch(Exception ignored){}
  if(!(episodes instanceof List)&&transport!=null){Matcher id=Pattern.compile("/anime/([1-9][0-9]*)-").matcher(URI.create(page.url).getPath());int count=0;try{count=Integer.parseInt(a.getOrDefault("episodes_count","0"));}catch(Exception ignored){}if(id.find()&&count>120){String endpoint=LabPipeline.origin(page.url)+"/info_api/"+id.group(1)+"/1?start_range=0&end_range="+Math.min(count,1000);LabPipeline.Page response=transport.get(endpoint,page.url,false);if(response.status!=200)throw new LabPipeline.Failure(LabPipeline.Stage.HOST_SEARCH,"HTTP_"+response.status,"AnimeUnity: episodi non disponibili");Object json=LabPipeline.publicJson(response.text());if(json instanceof Map)episodes=((Map<?,?>)json).get("episodes");}}
  Set<String> seen=new HashSet<>();if(episodes instanceof List)for(Object item:(List<?>)episodes)if(item instanceof Map&&out.size()<1000){Map<?,?> ep=(Map<?,?>)item;String id=text(ep,"id"),number=text(ep,"number");if(id.matches("[1-9][0-9]*")&&seen.add(id))out.add(new LabPipeline.Source(LabPipeline.origin(page.url)+"/embed-url/"+id,"Episodio "+number,page.url));}
  if(out.isEmpty()){String embed=LabPipeline.absolute(page.url,a.getOrDefault("embed_url",""));if(!embed.isEmpty()&&LabPipeline.knownHost(LabPipeline.host(embed)))out.add(new LabPipeline.Source(embed,"AnimeUnity · VixCloud",page.url));}return out;
 }
}
