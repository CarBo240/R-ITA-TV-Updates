package it.carmine.streamplayer;
import okhttp3.*;
final class CbMixdropResolver {
 static final String USER_AGENT="Mozilla/5.0 (Linux; Android 15; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/153.0.0.0 Mobile Safari/537.36";
 static final class Result {final String url,page;Result(String url,String page){this.url=url;this.page=page;}}
 static Result resolve(String link,String parent,String ua)throws Exception{
  String embed=CbLinkTargets.embed(link);if(!CbLinkTargets.mixdrop(embed)||CbLinkTargets.fileId(embed).isEmpty())throw new Exception("Link Mixdrop non riconosciuto");
  try(Response r=CbTransport.HTTP.newCall(new Request.Builder().url(embed).header("User-Agent",USER_AGENT).header("Referer",parent).build()).execute()){
   if(!r.isSuccessful()||r.body()==null)throw new Exception("Mixdrop HTTP "+r.code());String page=r.request().url().toString();if(!CbLinkTargets.navigation(embed,page))throw new Exception("Mixdrop: destinazione estranea al video");String html=r.body().string();if(html.length()>2000000)throw new Exception("Pagina del player troppo grande");String media=CbMixdropConfig.extract(html,page);if(media.isEmpty())throw new Exception("Configurazione video assente nella risposta Mixdrop");return new Result(media,page);
  }
 }
}
