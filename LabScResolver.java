package it.carmine.streamplayer;

import org.json.JSONObject;

/** Fresh SC resolution without pre-consuming the protected manifest. */
final class LabScResolver {
 static VodSource.Stream resolve(String home,String id,String episode)throws Exception{
  if(!id.matches("[0-9]+")||(!episode.isEmpty()&&!episode.matches("[0-9]+")))throw new java.io.IOException("Titolo non valido");
  String watch=home+"/it/watch/"+id+(episode.isEmpty()?"":"?e="+episode);JSONObject p=VodSource.props(VodSource.fetch(watch,home+"/"));
  if(p.optBoolean("redirectToLogin"))throw new java.io.IOException("Questa fonte richiede accesso sul sito");String outer=p.optString("embedUrl","");if(outer.isEmpty())throw new java.io.IOException("Video non disponibile");
  String embed=VodSource.iframe(VodSource.fetch(outer,watch),outer),html=VodSource.fetch(embed,outer),url=ScPlaylistConfig.playlist(html,embed);
  return new VodSource.Stream(url,embed);
 }
}
