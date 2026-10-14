package it.carmine.streamplayer;
import org.json.JSONObject;import java.net.URI;
/** Fresh iframe resolution and origin referer shared by R. ITA and the new LAB proxy. */
final class LabScResolver {
 static VodSource.Stream resolve(String home,String id,String episode)throws Exception {
  if(!id.matches("[0-9]+")||(!episode.isEmpty()&&!episode.matches("[0-9]+")))throw new java.io.IOException("Titolo non valido");
  String outer=home+"/it/iframe/"+id+"?language=it"+(episode.isEmpty()?"":"&episode_id="+episode+"&next_episode=1");String wrapper;
  try{wrapper=VodNetwork.fetchWithAgent(outer,home+"/",ScPlaylistConfig.USER_AGENT);}catch(VodNetwork.HttpError e){if(e.status!=404)throw e;String watch=home+"/it/watch/"+id+(episode.isEmpty()?"":"?e="+episode);JSONObject props=VodSource.props(VodNetwork.fetchWithAgent(watch,home+"/",ScPlaylistConfig.USER_AGENT));if(props.optBoolean("redirectToLogin"))throw new java.io.IOException("SC · accesso richiesto");outer=new URI(watch).resolve(props.getString("embedUrl")).toString();wrapper=VodNetwork.fetchWithAgent(outer,watch,ScPlaylistConfig.USER_AGENT);}
  String embed=VodSource.iframe(wrapper,outer),html=VodNetwork.fetchWithAgent(embed,outer,ScPlaylistConfig.USER_AGENT);URI uri=new URI(embed);
  return new VodSource.Stream(ScPlaylistConfig.playlist(html,embed),uri.getScheme()+"://"+uri.getAuthority()+"/");
 }
}
