package it.carmine.streamplayer;

import org.json.JSONObject;import java.io.IOException;import java.net.URI;

/** Official VixSrc embed URL builder. The catalogue still comes from Streaming Community. */
final class VixSrc {
 static long tmdbId(VodSource.Title title){JSONObject metadata=TmdbClient.metadata(title);return metadata==null?title.json.optLong("tmdb_id"):metadata.optLong("tmdb_id");}
 static String movie(long id,long startMs){return url("/movie/"+id,startMs);}
 static String episode(long id,int season,int episode,long startMs){return url("/tv/"+id+"/"+season+"/"+episode,startMs);}
 private static String url(String path,long startMs){String result=VodSettings.VIXSRC+path+"?lang=it&autoplay=true&primaryColor=D3A84C&secondaryColor=3A3020";if(startMs>0)result+="&startAt="+(startMs/1000);return result;}
 static VodProviders.Match match(VodSettings.Source source,VodSource.Title title,VodSource.Fetcher fetch){long id=tmdbId(title);if(id<=0)return new VodProviders.Match(source,null,"ID TMDB non disponibile");try{if(!title.series()){JSONObject api=new JSONObject(fetch.get(VodSource.base(source.url)+"/api/movie/"+id+"?lang=it&autoplay=true",source.url+"/"));if(api.optString("src").isEmpty())return new VodProviders.Match(source,null,"Non disponibile su VixSrc");}return new VodProviders.Match(source,title,title.series()?"Disponibile · verifica episodio":"Disponibile · audio italiano preferito",title.series()?"":movie(id,0),false);}catch(Exception error){return new VodProviders.Match(source,null,VodNetwork.error(error));}}
 static VodSource.Stream resolve(VodSettings.Source source,VodSource.Title title,String episode,VodSource.Fetcher fetch)throws Exception{
  long id=tmdbId(title);if(id<=0)throw new IOException("ID TMDB non disponibile");String base=VodSource.base(source.url),path;
  if(title.series()){int season=title.json.optInt("_seasonNumber");int number=title.json.optInt("_episodeNumber");if(number<=0&&episode.matches("[0-9]+"))number=Integer.parseInt(episode);if(season<=0||number<=0)throw new IOException("Episodio non valido");path="/api/tv/"+id+"/"+season+"/"+number;}else path="/api/movie/"+id;
  JSONObject api=new JSONObject(fetch.get(base+path+"?lang=it&autoplay=true",base+"/"));String relative=api.optString("src");if(relative.isEmpty())throw new IOException("Titolo non disponibile su VixSrc");String embed=new URI(base+"/").resolve(relative).toString();String playlist=VodSource.playlist(fetch.get(embed,base+"/"));playlist+=(playlist.contains("?")?"&":"?")+"h=1&lang=it";String manifest=fetch.get(playlist,embed);if(!manifest.trim().startsWith("#EXTM3U"))throw new IOException("Playlist VixSrc non disponibile");String upper=manifest.toUpperCase(java.util.Locale.ROOT);if(upper.contains("#EXT-X-MEDIA:TYPE=AUDIO")&&!upper.matches("(?s).*(LANGUAGE=\\\"?(IT|ITA)\\\"?|NAME=\\\"?ITALIAN[OA]?).*"))throw new IOException("Audio italiano non disponibile su VixSrc");return new VodSource.Stream(playlist,base+"/");
 }
}
