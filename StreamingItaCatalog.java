package it.carmine.streamplayer.lab211;
import java.util.*;import java.util.regex.*;
/** Reads public DooPlayer buttons and submits only their declared post/server identifiers. */
public final class StreamingItaCatalog implements LabPipeline.CatalogAdapter {
 public String name(){return "StreamingIta · DooPlayer";}
 public String search(String base,String query)throws Exception{return LabPipeline.origin(base)+(query.isEmpty()?"/film/":"/?s="+java.net.URLEncoder.encode(query,"UTF-8"));}
 public List<LabPipeline.Title> titles(LabPipeline.Page page){return new LabPipeline.WordpressCatalog().titles(page);}
 public List<LabPipeline.Source> sources(LabPipeline.Page page){return LabPipeline.declaredSources(page);}
 public List<LabPipeline.Source> sources(LabPipeline.Page page,LabPipeline.Transport transport)throws Exception{
  List<LabPipeline.Source> out=new ArrayList<>(sources(page));Matcher tags=Pattern.compile("(?is)<[a-z]+\\b[^>]*>").matcher(page.text());Set<String> seen=new HashSet<>();int n=0;while(tags.find()&&n<12){Map<String,String> a=LabPipeline.attrs(tags.group());String post=a.getOrDefault("data-post",""),number=a.getOrDefault("data-nume",""),type=a.getOrDefault("data-type","movie");if(!post.matches("[0-9]+")||!number.matches("[0-9]{1,2}")||!type.matches("movie|tv|episode")||!seen.add(post+":"+number))continue;n++;Map<String,String> fields=new LinkedHashMap<>();fields.put("action","doo_player_ajax");fields.put("post",post);fields.put("nume",number);fields.put("type",type);LabPipeline.Page response=transport.postForm(LabPipeline.origin(page.url)+"/wp-admin/admin-ajax.php",fields,page.url);if(response.status!=200)throw new LabPipeline.Failure(LabPipeline.Stage.HOST_SEARCH,"HTTP_"+response.status,"StreamingIta: richiesta player rifiutata");Object parsed=LabPipeline.publicJson(response.text());Object value=parsed instanceof Map?((Map<?,?>)parsed).get("embed_url"):null;if(!(value instanceof String))continue;String url=LabPipeline.absolute(page.url,(String)value);if(!url.isEmpty())out.add(new LabPipeline.Source(url,"Server "+number,page.url));}
  return out;
 }
}
