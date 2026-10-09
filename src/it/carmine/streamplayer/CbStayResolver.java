package it.carmine.streamplayer;
import okhttp3.*;import org.json.*;import java.net.URI;
/** Same public request made by StayOnline's userViewLink() after Continue. */
final class CbStayResolver {
 static String resolve(String url,String parent,String ua)throws Exception{
  if(!CbLinkTargets.stay(url))throw new Exception("Link intermedio non riconosciuto");
  String html;try(Response r=CbTransport.HTTP.newCall(new Request.Builder().url(url).header("User-Agent",ua).header("Referer",parent).build()).execute()){
   if(!r.isSuccessful()||r.body()==null)throw new Exception("StayOnline HTTP "+r.code());html=r.body().string();
  }
  if(!html.contains("/ajax/linkView.php")||!html.contains("btnClickToContinueLink"))throw new Exception("Comando Continua non disponibile");
  String id=new URI(url).getPath().split("/")[2];
  Request req=new Request.Builder().url("https://stayonline.pro/ajax/linkView.php").header("User-Agent",ua).header("Referer",url).header("Origin","https://stayonline.pro").header("X-Requested-With","XMLHttpRequest").header("Accept","application/json, text/javascript, */*; q=0.01").post(new FormBody.Builder().add("id",id).add("ref",parent).build()).build();
  try(Response r=CbTransport.HTTP.newCall(req).execute()){
   if(!r.isSuccessful()||r.body()==null)throw new Exception("StayOnline HTTP "+r.code());JSONObject data=new JSONObject(r.body().string());
   if(!"success".equals(data.optString("status"))){String message=data.optString("message","Fonte non disponibile");if(data.optInt("code")==429)message="La fonte richiede una verifica nel sito";throw new Exception(message);}
   String next=data.getJSONObject("data").getString("value");URI u=new URI(next);if(!"https".equals(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null)throw new Exception("Destinazione non valida");return CbLinkTargets.embed(next);
  }
 }
}
