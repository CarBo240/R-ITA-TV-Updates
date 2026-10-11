package it.carmine.streamplayer;
import android.content.*;
import org.json.*;
import java.util.*;
import it.carmine.streamplayer.lab211.*;

/** Reuses confirmed LAB adapters/resolvers, but launches the normal R. ITA player. */
final class VodLabSources {
 static String kind(String url){return LabVodPolicy.adapterKind(url);}
 static boolean supported(VodSettings.Source source){return source!=null&&!kind(source.url).isEmpty()&&!source.nativeCatalog()&&!source.vixsrc();}
 static List<VodSettings.Source> providers(Context context){
  List<VodSettings.Source> all=new ArrayList<>(VodSettings.sources(context));Set<String> hosts=new HashSet<>();for(VodSettings.Source s:all)hosts.add(LabPipeline.host(s.url).replaceFirst("^www\\.",""));
  try{JSONArray lab=new JSONArray(context.getSharedPreferences("lab211",0).getString("providers","[]"));for(int i=0;i<lab.length();i++){JSONObject p=lab.getJSONObject(i);String url=p.getString("url"),host=LabPipeline.host(url).replaceFirst("^www\\.","");if(kind(url).isEmpty()||!hosts.add(host))continue;LabPipeline.https(url);all.add(new VodSettings.Source("lab_"+host.replaceAll("[^A-Za-z0-9_-]","_"),p.getString("name"),url,"web"));}}catch(Exception ignored){}
  return all;
 }
 static String year(String name){return LabVodPolicy.year(name);}
 static VodProviders.Match search(VodSettings.Source source,VodSource.Title wanted){
  if(wanted.series())return new VodProviders.Match(source,null,"Film LAB disponibili; stagioni/episodi non ancora integrati");
  try{
   LabPipeline pipeline=new LabPipeline(new LabHttp(),null);LabPipeline.CatalogAdapter adapter=LabPipeline.adapter(kind(source.url));
   JSONObject metadata=TmdbClient.metadata(wanted);String original=metadata==null?wanted.json.optString("original_name"):metadata.optString("original_name");
   List<String> queries=new ArrayList<>();queries.add(wanted.name);if(!original.isEmpty()&&!VodProviders.normal(original).equals(VodProviders.normal(wanted.name)))queries.add(original);
   LinkedHashMap<String,LabPipeline.Title> unique=new LinkedHashMap<>();
   for(String query:queries){List<LabPipeline.Title> found;try{found=pipeline.catalog(adapter,source.url,query);}catch(LabPipeline.Failure error){if(error.code.equals("NO_TITLES"))continue;throw error;}for(LabPipeline.Title candidate:found)if(LabVodPolicy.titleMatches(wanted.name,original,wanted.year,candidate))unique.putIfAbsent(candidate.url,candidate);}
   List<LabPipeline.Title> candidates=new ArrayList<>(unique.values());
   if(candidates.size()!=1)return new VodProviders.Match(source,null,candidates.isEmpty()?"Titolo non trovato":"Più titoli omonimi; associazione non determinata");
   LabPipeline.Title candidate=candidates.get(0);JSONObject json=new JSONObject(wanted.json.toString());if(metadata!=null)json.put("_metadata",metadata);json.put("_lab_page",candidate.url).put("_lab_adapter",kind(source.url)).put("_lab_poster",candidate.poster);
   LabPipeline.SourceReport report=pipeline.sourceReport(adapter,candidate);int count=0;for(LabPipeline.Source host:report.sources)if(playable(host))count++;
   return new VodProviders.Match(source,new VodSource.Title(json,""),count+" fonti interne · "+candidate.name+(year(candidate.name).isEmpty()&&!wanted.year.isEmpty()?" · anno da confermare":""),candidate.url,true);
  }catch(Exception e){return new VodProviders.Match(source,null,e instanceof LabPipeline.Failure?((LabPipeline.Failure)e).code+" · "+e.getMessage():VodNetwork.error(e));}
 }
 static LabPipeline.SourceReport sources(VodSource.Title title)throws Exception{
  String page=title.json.getString("_lab_page");LabPipeline.https(page);String adapter=title.json.getString("_lab_adapter");if(!adapter.equals(kind(page)))throw new Exception("Adattatore della scheda non valido");return new LabPipeline(new LabHttp(),null).sourceReport(LabPipeline.adapter(adapter),new LabPipeline.Title(title.name,title.poster,page));
 }
 static boolean playable(LabPipeline.Source source){return LabVodPolicy.playable(source);}
 static Intent player(Context context,VodProviders.Match match,LabPipeline.Source host,long position)throws Exception{
  if(!playable(host))throw new Exception("Fonte non ancora integrata in R. ITA");JSONObject json=new JSONObject(match.title.json.toString());json.put("_lab_target",host.url).put("_lab_target_label",host.label);
  return new Intent(context,VodPlayerActivity.class).putExtra("source",match.source.json().toString()).putExtra("titleJson",json.toString()).putExtra("episode","").putExtra("label",match.title.name).putExtra("episodes","[]").putExtra("index",-1).putExtra("startPosition",position);
 }
 static final class Resolved {final VodSource.Stream stream;final LabPipeline.Media media;Resolved(VodSource.Stream s,LabPipeline.Media m){stream=s;media=m;}}
 static Resolved resolve(Context context,VodSource.Title title)throws Exception{
  String target=title.json.getString("_lab_target"),label=title.json.optString("_lab_target_label");LabPipeline.SourceReport report=sources(title);LabPipeline.Source selected=null;List<LabPipeline.Source> sameLabel=new ArrayList<>();
  for(LabPipeline.Source source:report.sources){if(source.url.equals(target)){selected=source;break;}if(source.label.equals(label)&&playable(source))sameLabel.add(source);}
  if(selected==null&&sameLabel.size()==1)selected=sameLabel.get(0);
  if(selected==null||!playable(selected))throw new Exception("Fonte cambiata o non più disponibile · scegli Altre fonti");
  if(LabVixBridge.matches(selected.url))return new Resolved(LabVixBridge.resolve(context,selected.url,(stage,code,message)->{}),null);
  LabPipeline.Media media=new LabPipeline(new LabHttp(),null).resolve(selected);
  if(!media.host.equals("MixDrop")&&!media.host.equals("VidXGo")&&selected.kind!=LabPipeline.SourceKind.MEDIA)throw new Exception("La destinazione non è una fonte LAB integrata");
  return new Resolved(new VodSource.Stream(media.url,media.referer,"",media.mime,Collections.emptyMap()),media);
 }
}
