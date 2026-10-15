package it.carmine.streamplayer;
import java.util.*;import java.nio.charset.StandardCharsets;import it.carmine.streamplayer.lab211.*;
public final class Resolver236Check {
 static int count;static void check(boolean ok){count++;if(!ok)throw new AssertionError("236 check "+count);}
 static final class Transport implements LabPipeline.Transport {
  String pageUrl;Map<String,String> pageHeaders;
  public LabPipeline.Page get(String u,String r,boolean media){throw new AssertionError("Host must use shared request context");}
  public LabPipeline.Page getWithHeaders(String u,String r,boolean media,Map<String,String> headers){if(media){check(!headers.containsKey("Referer"));byte[] bytes={0,0,0,16,'f','t','y','p','i','s','o','m',0,0,0,0};return new LabPipeline.Page(u,206,"video/mp4",bytes);}pageUrl=u;pageHeaders=headers;return new LabPipeline.Page(u,200,"text/html","MDCore.wurl='https://cdn.example/test.mp4';".getBytes(StandardCharsets.UTF_8));}
 }
 public static void main(String[] args)throws Exception {
  String selected="https://mxdrop.click/f/abc123?ref=provider";check(CbLinkTargets.embed(selected).equals("https://mxdrop.click/e/abc123"));check(CbLinkTargets.embed("https://mixdrop.club/f/abc123").equals("https://mixdrop.ag/e/abc123"));
  Transport t=new Transport();new LabPipeline(t,null).resolve(new LabPipeline.Source(selected,"MixDrop","https://cb01uno.wiki/title"));check(t.pageUrl.endsWith("/e/abc123"));check(t.pageHeaders.get("Referer").equals("https://mixdrop.co/"));check(t.pageHeaders.get("X-Requested-With").equals("XMLHttpRequest"));
  String finalPage="https://vixcloud.co/embed/123",config="window.video={id:123};window.masterPlaylist={params:{token:'fresh',expires:1999999999},url:'/playlist/'+window.video.id};";check(ScEmbedRoute.next(finalPage,config).equals(finalPage));check(ScPlaylistConfig.playlist(config,finalPage).startsWith("https://vixcloud.co/playlist/123?token=fresh"));
  check(ScEmbedRoute.next("https://sc.example/it/iframe/10","<iframe data-src='https://vixcloud.co/embed/123?b=1&amp;canPlayFHD=true'></iframe>").contains("b=1&canPlayFHD=true"));
  boolean missing=false;try{ScEmbedRoute.next(finalPage,"<h1>empty</h1>");}catch(Exception e){missing=true;}check(missing);
  System.out.println("Resolver 2.1.36: "+count+" PASS");
 }
}
