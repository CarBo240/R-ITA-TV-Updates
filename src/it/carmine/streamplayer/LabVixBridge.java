package it.carmine.streamplayer;

import android.content.Context;
import java.net.URI;
import org.json.*;
import it.carmine.streamplayer.lab211.LabPipeline;

/** Maps exact IMDb identity to TMDB, then delegates to R. ITA's unchanged VixSrc resolver. */
final class LabVixBridge {
 static boolean matches(String raw){try{URI u=LabPipeline.https(raw);return "vixsrc.to".equals(u.getHost())&&u.getPath().matches("/api/movie/tt[0-9]{7,12}");}catch(Exception e){return false;}}
 static VodSource.Stream resolve(Context context,String raw,LabPipeline.Observer observer)throws Exception {
  if(!matches(raw))throw new LabPipeline.Failure(LabPipeline.Stage.RESOLVER,"VIX_ID_INVALID","Identità VixSrc non valida");
  String key=TmdbClient.key(context);if(key.isEmpty())throw new LabPipeline.Failure(LabPipeline.Stage.RESOLVER,"TMDB_KEY_MISSING","La configurazione TMDB di R. ITA non è disponibile");
  String imdb=LabPipeline.https(raw).getPath().substring("/api/movie/".length());
  observer.event(LabPipeline.Stage.RESOLVER,"VIX_IMDB","Associazione esatta IMDb → TMDB");
  JSONObject found;try{found=TmdbClient.request("/find/"+imdb+"?external_source=imdb_id",key,VodSource.HTTP);}catch(Exception e){throw new LabPipeline.Failure(LabPipeline.Stage.RESOLVER,"VIX_LOOKUP_FAILED",VodNetwork.error(e));}JSONArray movies=found.optJSONArray("movie_results");
  if(movies==null||movies.length()!=1)throw new LabPipeline.Failure(LabPipeline.Stage.RESOLVER,"VIX_ID_UNRESOLVED","IMDb senza un unico film TMDB; nessuna ricerca approssimativa");
  VodSource.Title title=TmdbClient.convert(movies.getJSONObject(0),"movie");
  observer.event(LabPipeline.Stage.RESOLVER,"VIX_EXISTING","Resolver e configurazione HTTPS originali R. ITA");
  VodSource.Fetcher monitored=(url,ref)->{LabPipeline.https(url);LabPipeline.Stage stage=LabPipeline.https(url).getPath().startsWith("/playlist/")||!LabPipeline.mime(url).isEmpty()?LabPipeline.Stage.SOURCE:LabPipeline.Stage.RESOLVER;observer.event(stage,"VIX_FETCH",LabPipeline.host(url));try{return VodSource.HTTP.get(url,ref);}catch(Exception e){throw new LabPipeline.Failure(stage,e instanceof VodNetwork.HttpError?"HTTP_"+((VodNetwork.HttpError)e).status:"VIX_NETWORK",VodNetwork.error(e));}};
  VodSource.Stream stream;try{stream=VixSrc.resolve(new VodSettings.Source("vixsrc","VixSrc",VodSettings.VIXSRC,"vixsrc"),title,"",monitored);}catch(LabPipeline.Failure e){throw e;}catch(Exception e){String message=e.getMessage(),code="VIX_SOURCE_FAILED";if("Audio italiano non disponibile su VixSrc".equals(message))code="VIX_AUDIO_IT_UNAVAILABLE";else if("Titolo non disponibile su VixSrc".equals(message))code="VIX_TITLE_UNAVAILABLE";else if("Playlist VixSrc non disponibile".equals(message))code="VIX_HLS_UNAVAILABLE";throw new LabPipeline.Failure(LabPipeline.Stage.SOURCE,code,code.equals("VIX_SOURCE_FAILED")?VodNetwork.error(e):message);}
  observer.event(LabPipeline.Stage.SOURCE,"VIX_VERIFIED","Manifest HLS verificato dal resolver esistente");return stream;
 }
}
