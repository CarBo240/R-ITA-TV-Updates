package it.carmine.streamplayer;

import org.json.*;import java.util.*;

/** Java adapter for the public request flow documented by streamingcommunity-unofficialapi. */
final class ScuApi {
 static List<VodSource.Title> results(String raw,String home)throws Exception{
  JSONObject root=VodSource.props(raw);Object value=root.opt("data");if(value==null)value=root.opt("results");JSONArray data=value instanceof JSONArray?(JSONArray)value:value instanceof JSONObject?((JSONObject)value).optJSONArray("data"):null;if(data==null)return VodSource.catalog(root,home);JSONObject wrapper=new JSONObject().put("results",data);return VodSource.catalog(wrapper,home);
 }
 static VodProviders.Match search(VodSettings.Source source,VodSource.Title wanted,VodSource.Fetcher fetch){try{
  List<String> queries=new ArrayList<>();queries.add(wanted.name);JSONObject metadata=TmdbClient.metadata(wanted);String original=metadata==null?wanted.json.optString("original_name",""):metadata.optString("original_name","");if(!original.isEmpty()&&!VodProviders.normal(original).equals(VodProviders.normal(wanted.name)))queries.add(original);
  for(String query:queries){List<VodSource.Title> candidates=results(fetch.get(source.url+"/it/search?q="+VodSource.encode(query),source.url+"/"),source.url);int checked=0;for(VodSource.Title item:candidates){if(item.series()!=wanted.series())continue;if(!VodProviders.normal(item.name).equals(VodProviders.normal(wanted.name))&&!VodProviders.normal(item.name).equals(VodProviders.normal(original)))continue;if(++checked>5)break;VodSource.Title full=VodSource.title(VodSource.page(source.url,item.path(),fetch),source.url);if(VodProviders.matches(wanted,full)){JSONObject json=new JSONObject(full.json.toString());if(metadata!=null)json.put("_metadata",metadata);return new VodProviders.Match(source,new VodSource.Title(json,source.url.replace("https://","https://cdn.")),"Disponibile · SC API");}}
  }return new VodProviders.Match(source,null,"Titolo non trovato");
 }catch(Exception error){return new VodProviders.Match(source,null,VodNetwork.error(error));}}
}
