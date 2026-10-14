package it.carmine.streamplayer;
import okhttp3.*;
final class CbMixdropResolver {
 static final String USER_AGENT="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
 static final class Result {final String url,page;final boolean extraBuffering;Result(String url,String page,boolean extra){this.url=url;this.page=page;extraBuffering=extra;}}
 static Result resolve(String link,String parent,String ua)throws Exception{
  String embed=CbLinkTargets.embed(link);if(!CbLinkTargets.mixdrop(embed)||CbLinkTargets.fileId(embed).isEmpty())throw new Exception("Link Mixdrop non riconosciuto");
  it.carmine.streamplayer.lab211.LabPipeline.Media media=StreamflixBridge.resolve(embed);return new Result(media.url,embed,media.extraBuffering);
 }
}
