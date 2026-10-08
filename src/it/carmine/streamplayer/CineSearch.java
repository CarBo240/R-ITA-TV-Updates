package it.carmine.streamplayer;

import org.json.*;import java.io.IOException;import java.util.*;

/** Catalogue-only client for a user-hosted CineSearch backend. */
final class CineSearch {
 static List<VodSource.Title> parse(String raw,String forcedType)throws Exception{JSONArray data=new JSONArray(raw);List<VodSource.Title> out=new ArrayList<>();for(int i=0;i<data.length();i++){JSONObject item=data.optJSONObject(i);if(item==null)continue;VodSource.Title title=TmdbClient.convert(item,item.optString("media_type",forcedType));if(title!=null)out.add(title);}return out;}
 static List<VodSource.Title> catalog(VodSettings.Source source,int mode,String query,VodSource.Fetcher fetch)throws Exception{
  if(source.url==null||source.url.isEmpty())throw new IOException("Configura l’URL del server CineSearch");List<VodSource.Title> out=new ArrayList<>();if(mode==1||mode==2)return one(source,mode==2?"tv":"movie",query,fetch);List<VodSource.Title> movies=one(source,"movie",query,fetch),shows=one(source,"tv",query,fetch);for(int i=0;i<Math.max(movies.size(),shows.size());i++){if(i<movies.size())out.add(movies.get(i));if(i<shows.size())out.add(shows.get(i));}return out;
 }
 private static List<VodSource.Title> one(VodSettings.Source source,String type,String query,VodSource.Fetcher fetch)throws Exception{String path=query.isEmpty()?"/api/discover?media_type="+type+"&sort_by=popularity.desc":"/api/search?query="+VodSource.encode(query)+"&media_type="+type+"&sort_by=popularity.desc";return parse(fetch.get(source.url+path,source.url+"/"),type);}
 static VodSource.Title details(VodSettings.Source source,VodSource.Title title,VodSource.Fetcher fetch)throws Exception{long id=title.json.optLong("tmdb_id");if(id<=0)throw new IOException("ID TMDB non disponibile");JSONObject raw=new JSONObject(fetch.get(source.url+"/api/"+(title.series()?"tv":"movie")+"/id/"+id,source.url+"/"));return TmdbClient.convert(raw,title.series()?"tv":"movie");}
}
