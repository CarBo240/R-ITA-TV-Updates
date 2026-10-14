package it.carmine.streamplayer;
import org.json.JSONObject;import java.net.URI;
/** Fresh iframe resolution and origin referer shared by R. ITA and the new LAB proxy. */
final class LabScResolver {
 static VodSource.Stream resolve(String home,String id,String episode)throws Exception {
  if(!id.matches("[0-9]+")||(!episode.isEmpty()&&!episode.matches("[0-9]+")))throw new java.io.IOException("Titolo non valido");
  String outer=home+"/it/iframe/"+id+"?language=it"+(episode.isEmpty()?"":"&episode_id="+episode+"&next_episode=1");VodNetwork.PageResponse page;
  try{page=VodNetwork.fetchPageWithAgent(outer,home+"/",ScPlaylistConfig.USER_AGENT);}catch(VodNetwork.HttpError e){if(e.status!=404)throw e;String watch=home+"/it/watch/"+id+(episode.isEmpty()?"":"?e="+episode);JSONObject props=VodSource.props(VodNetwork.fetchWithAgent(watch,home+"/",ScPlaylistConfig.USER_AGENT));if(props.optBoolean("redirectToLogin"))throw new java.io.IOException("SC · accesso richiesto");outer=new URI(watch).resolve(props.getString("embedUrl")).toString();page=VodNetwork.fetchPageWithAgent(outer,watch,ScPlaylistConfig.USER_AGENT);}
  java.util.Set<String> visited=new java.util.HashSet<>();
  for(int step=0;step<4;step++){if(!visited.add(page.url))throw new java.io.IOException("SC · collegamento circolare");String next=ScEmbedRoute.next(page.url,page.html);if(next.equals(page.url)){URI uri=new URI(page.url);it.carmine.streamplayer.lab211.LabPipeline.Media imported=StreamflixBridge.resolve(page.url);return new VodSource.Stream(imported.url,imported.referer);}page=VodNetwork.fetchPageWithAgent(next,page.url,ScPlaylistConfig.USER_AGENT);}
  throw new java.io.IOException("SC · troppi passaggi verso il player");
 }
}
