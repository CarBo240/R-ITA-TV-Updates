package it.carmine.streamplayer;
import it.carmine.streamplayer.lab211.LabPipeline;import java.nio.charset.StandardCharsets;
public final class Resolver234Check {
 static int checks;static void check(boolean ok){checks++;if(!ok)throw new AssertionError("Check "+checks);}
 public static void main(String[] args)throws Exception {
  String config="window.masterPlaylist={url:'https://vixcloud.co/playlist/123',token:'example',expires:1999999999};";
  String off=ScPlaylistConfig.playlist(config+"canPlayFHD:false;","https://vixcloud.co/embed/123?b=1");check(off.contains("b=1"));check(!off.contains("h=1"));check(off.contains("expires=1999999999"));
  check(ScPlaylistConfig.playlist(config+"canPlayFHD:true;","https://vixcloud.co/embed/123?canPlayFHD=true").contains("h=1"));
  check(!ScPlaylistConfig.playlist(config,"https://vixcloud.co/embed/123?ab=1").contains("b=1"));
  String signed="window.masterPlaylist={url:'https://vixcloud.co/playlist/123?token=original&h=0',token:'other'};canPlayFHD:true;";
  String kept=ScPlaylistConfig.playlist(signed,"https://vixcloud.co/embed/123");check(kept.contains("token=original"));check(!kept.contains("other"));check(kept.contains("h=0"));check(!kept.contains("h=1"));
  String page="https://mxdrop.click/e/mkdm7x3wt08l7n",media="https://test.mxcontent.net/v2/mkdm7x3wt08l7n.mp4?s=example&e=1999999999";
  check(LabPipeline.knownHost("mxdrop.click"));check(!LabPipeline.knownHost("mxdrop.click.evil.example"));
  check(new LabPipeline(null,null).resolver(page).name().equals("MixDrop"));
  for(String prop:new String[]{"MDCore.wurl","MDCore.vurl","MDCore['wurl']","MDCore[\"vurl\"]"})check(CbMixdropConfig.extract(prop+"='"+media+"';",page).equals(media));
  check(CbMixdropConfig.extract("MDCore.wurl='https://evil.example/video.mp4';",page).isEmpty());
  LabPipeline.Page fixture=new LabPipeline.Page(page,200,"text/html",("MDCore['vurl']='"+media+"';").getBytes(StandardCharsets.UTF_8));
  check(new LabPipeline(null,null).resolver(page).parse(fixture,page).url.equals(media));
  System.out.println("Resolver 2.1.34: "+checks+" PASS");
 }
}
