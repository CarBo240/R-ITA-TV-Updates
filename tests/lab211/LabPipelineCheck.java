package it.carmine.streamplayer.lab211;
import java.util.*;
import java.nio.charset.StandardCharsets;

/** JVM regression tests with local synthetic, authorized content. No live-host claim. */
public final class LabPipelineCheck {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static LabPipeline.Page page(String url,String html){return new LabPipeline.Page(url,200,"text/html",html.getBytes(StandardCharsets.UTF_8));}
 static void failure(String code,Work work)throws Exception{try{work.run();throw new AssertionError("Expected "+code);}catch(LabPipeline.Failure e){check(e.code.equals(code),e.code+" vs "+code);}}
 interface Work{void run()throws Exception;}
 public static void main(String[] args)throws Exception {
  Map<String,LabPipeline.Page> pages=new HashMap<>();List<String> stages=new ArrayList<>();
  String base="https://catalog.example/",detail=base+"film/test",host="https://mixdrop.example/e/abc",stream="https://cdn.example/movie.mp4?expires=123&token=fixture";
  pages.put(base,page(base,"<article><a href='/film/test'><img data-src='/poster.jpg' alt='Film di prova'></a></article>"));
  pages.put(detail,page(detail,"<a href='"+host+"'>MixDrop</a>"));
  pages.put(host,page(host,"<script>MDCore.wurl='"+stream+"';</script><track kind='subtitles' src='https://cdn.example/it.vtt' srclang='it' label='Italiano'>"));
  pages.put(stream,new LabPipeline.Page(stream,206,"video/mp4",new byte[]{0,0,0,24,102,116,121,112,105,115,111,109}));
  LabPipeline pipeline=new LabPipeline((url,ref,media)->{if(!pages.containsKey(url))throw new AssertionError("Unexpected fetch "+url);return pages.get(url);},(s,c,d)->stages.add(s+":"+c));
  LabPipeline.CatalogAdapter adapter=new LabPipeline.Cb01Catalog();List<LabPipeline.Title> titles=pipeline.catalog(adapter,base,"");check(titles.size()==1,"catalog count");check(titles.get(0).poster.equals(base+"poster.jpg"),"poster URL");
  List<LabPipeline.Source> sources=pipeline.sources(adapter,titles.get(0));check(sources.size()==1,"host source count");LabPipeline.Media media=pipeline.resolve(sources.get(0));check(media.url.equals(stream),"signed query preserved");check(media.mime.equals(LabPipeline.MP4),"validated MP4");check(media.referer.equals(host),"host referer");check(media.subtitles.size()==1&&media.subtitles.get(0).language.equals("it"),"subtitles propagated");
  check(stages.contains("CATALOG:TITLES")&&stages.contains("HOST_SEARCH:SOURCES")&&stages.contains("RESOLVER:ADAPTER")&&stages.contains("SOURCE:VERIFIED"),"complete phases");
  for(String[] example:new String[][]{{"MixDrop","https://mixdrop.si/e/abc","MDCore.wurl='https://cdn.example/a.mp4';"},{"MaxStream","https://maxstream.video/abc","sources:[{file:'https://cdn.example/a.m3u8'}]"},{"VOE","https://voe.sx/e/abc","sources={'hls':'https://cdn.example/a.m3u8'};"},{"StreamTape","https://streamtape.com/e/abc","<div id='robotlink'>//streamtape.com/get_video?id=abc&amp;expires=123</div>"},{"SuperVideo","https://supervideo.tv/embed-abc.html","sources:[{file:'https://cdn.example/a.m3u8'}]"},{"Vidoza","https://vidoza.net/abc","src:'https://cdn.example/a.mp4'"},{"DoodStream","https://dood.to/e/abc","<video src='https://cdn.example/a.mp4'></video>"}}){LabPipeline.HostResolver resolver=pipeline.resolver(example[1]);check(resolver.name().equals(example[0]),"dispatch "+example[0]);check(resolver.parse(page(example[1],example[2]),base).url.startsWith("https://"),"real extraction "+example[0]);}
  check(pipeline.resolver("https://evil-mixdrop.example/page").name().equals("HTML5 pubblico"),"no substring host dispatch");
  failure("NO_PUBLIC_MEDIA",()->pipeline.resolver(host).parse(page(host,"<h1>MixDrop</h1>"),base));
  failure("NO_PUBLIC_MEDIA",()->new LabPipeline.DoodStream().parse(page("https://dood.to/e/x","/pass_md5/abc ?token=secret"),base));
  failure("INVALID_HTTPS",()->LabPipeline.https("http://cdn.example/a.mp4"));failure("INVALID_HTTPS",()->LabPipeline.https("https://user:pass@cdn.example/a.mp4"));
  failure("INVALID_HTTPS",()->LabPipeline.https("https://127.0.0.1/a.mp4"));
  String packed="eval(function(p,a,c,k,e,d){return p;}('0.1=\"2://3/4.5\";',6,6,'MDCore|wurl|https|cdn.example|movie|mp4'.split('|'),0,{}))";
  check(new LabPipeline.MixDrop().parse(page(host,packed),base).url.equals("https://cdn.example/movie.mp4"),"bounded literal packer decode");
  failure("PACKER_FORMAT",()->LabPipeline.unpackPublic("}('x',99,1,'a'.split('|'))"));
  String hls="https://cdn.example/master.m3u8",dash="https://cdn.example/master.mpd";
  pages.put(hls,page(hls,"#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=100\nvideo.m3u8"));pages.put(dash,page(dash,"<?xml version='1.0'?><MPD xmlns='urn:mpeg:dash:schema:mpd:2011'></MPD>"));
  check(pipeline.resolve(new LabPipeline.Source(hls,"HLS",base)).mime.equals(LabPipeline.HLS),"HLS body verification");check(pipeline.resolve(new LabPipeline.Source(dash,"DASH",base)).mime.equals(LabPipeline.DASH),"DASH body verification");
  pages.put(hls,page(hls,"<html>Access denied</html>"));failure("NOT_MEDIA",()->pipeline.resolve(new LabPipeline.Source(hls,"HLS",base)));
  pages.put(host,new LabPipeline.Page(host,403,"text/html",new byte[0]));failure("HTTP_403",()->pipeline.resolve(sources.get(0)));
  pages.put(host,page(host,"<div class='cf-chl-widget'></div>"));failure("ACCESS_CHALLENGE",()->pipeline.resolve(sources.get(0)));
  check(new LabPipeline.AltaCatalog().titles(page(base,"<div class='shortstory'><a href='/film/1'><img src='/poster.jpg' alt='Titolo DLE'></a></div>")).size()==1,"DLE adapter");
  check(adapter.search(base,"film prova").equals(base+"?s=film+prova"),"catalog search endpoint");
  failure("UNSUPPORTED_CATALOG",()->LabPipeline.adapter("unknown"));
  check(LabPipeline.declaredSources(page(base,"<a href='https://cdn.example/file.mp4?token=x'>video</a><img src='/poster.jpg'><a href='/menu'>menu</a>")).size()==1,"only sources, query retained");
  String encoded=Base64.getEncoder().encodeToString("https://cdn.example/base.m3u8".getBytes(StandardCharsets.UTF_8));
  check(new LabPipeline.Voe().parse(page("https://voe.sx/e/x","hls: atob('"+encoded+"')"),base).url.equals("https://cdn.example/base.m3u8"),"public VOE base64 declaration");
  check(LabPipeline.absolute(base,"").isEmpty(),"empty source rejected");
  check(LabPipeline.knownHost("doodstream.com"),"DoodStream alias sources");
  failure("NO_PUBLIC_MEDIA",()->new LabPipeline.Html5().parse(page(base,"<video poster='x.jpg'></video>"),base));
  String upperPacked="}('A:\"https://cdn.example/a.m3u8\"',62,37,'"+String.join("|",Collections.nCopies(36,""))+"|file'.split('|'))";
  check(new LabPipeline.SuperVideo().parse(page("https://supervideo.tv/embed-x.html",upperPacked),base).url.equals("https://cdn.example/a.m3u8"),"radix62 uppercase token");
  pages.put(host,new LabPipeline.Page(stream,206,"video/mp4",pages.get(stream).bytes));
  check(pipeline.resolve(sources.get(0)).url.equals(stream),"host redirects directly to media");
  check(new LabPipeline.WordpressCatalog().titles(pages.get(base)).size()==1,"WordPress adapter");
  check(new LabPipeline.DleCatalog().search(base,"film prova").contains("story=film+prova"),"DLE remote search");
  System.out.println("PASS: "+checks+" pipeline checks; catalogue -> host -> resolver -> validated Media3 input; synthetic fixtures only.");
 }
}
