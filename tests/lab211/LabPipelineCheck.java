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
  // Regression: screenshots 1513/1514 from the user; synthetic URLs and markup.
  String candidatePage="<iframe src='https://www.youtube.com/embed/preview'></iframe>";
  List<LabPipeline.Source> previewOnly=LabPipeline.declaredSources(page(base,candidatePage));
  LabPipeline.SourceReport previewReport=new LabPipeline.SourceReport(previewOnly);
  check(previewReport.sources.isEmpty()&&previewReport.trailers.size()==1,"YouTube not a feature-video source");
  failure("TRAILER_EXCLUDED",()->pipeline.resolve(previewOnly.get(0)));
  String neagley="<iframe title='Serie - Trailer' src='https://v.vidxgo.co/x?content=trailer&amp;player=false'></iframe><iframe title='Serie - Player' src='https://v.vidxgo.co/x/1/1?se=0'></iframe>";
  LabPipeline.SourceReport split=new LabPipeline.SourceReport(LabPipeline.declaredSources(page(base,neagley)));
  check(split.trailers.size()==1&&split.sources.size()==1,"separate trailer and player");
  check(split.sources.get(0).kind==LabPipeline.SourceKind.PAGE,"unknown page is unverified, not supported HTML5");
  check(LabPipeline.sourceStatus(split.sources.get(0)).contains("da verificare"),"honest source status");
  String candidate=split.sources.get(0).url;pages.put(candidate,new LabPipeline.Page(candidate,403,"text/html",new byte[0]));
  failure("HTTP_403",()->pipeline.resolve(split.sources.get(0)));
  String wrappers="<a href='https://stayonline.example/l/x'>MixDrop HD</a><a href='https://uprot.net/msf/id'>MaxStream</a>";
  List<LabPipeline.Source> links=LabPipeline.declaredSources(page(base,wrappers));
  check(links.size()==2,"public intermediary links not discarded");check(links.get(0).kind==LabPipeline.SourceKind.INTERMEDIARY,"intermediary status");
  check(links.get(0).label.equals("MixDrop HD"),"anchor label preserved");pages.put(links.get(0).url,page(links.get(0).url,"<h1>Continua</h1>"));failure("INTERMEDIARY_NO_PUBLIC_LINK",()->pipeline.resolve(links.get(0)));
  check(LabPipeline.declaredSources(page(base,"<button data-url='https://mixdrop.si/e/x'>MixDrop</button>")).size()==1,"data-url source");
  check(LabPipeline.declaredSources(page(base,"<iframe src='about:blank' data-src='https://supervideo.tv/embed-x.html'></iframe>")).get(0).kind==LabPipeline.SourceKind.HOST,"lazy iframe source");
  check(LabPipeline.declaredSources(page(base,"<iframe data-src='' src='https://vidoza.net/x'></iframe>")).size()==1,"empty lazy src falls back");
  check(new LabPipeline.Source("https://maxstream.video/x","Trailer Park Boys",base).kind==LabPipeline.SourceKind.HOST,"film name not misclassified as trailer");
  check(new LabPipeline.Source("https://maxstream.video/x","Film - Trailer",base).kind==LabPipeline.SourceKind.TRAILER,"explicit trailer suffix");
  check(new LabPipeline.Source("https://youtube.com.evil.example/x","Player",base).kind==LabPipeline.SourceKind.PAGE,"platform matching uses exact domain suffix");
  pages.put(detail,page(detail,candidatePage));failure("TRAILER_ONLY",()->pipeline.sources(adapter,titles.get(0)));
  check(LabPipeline.declaredSources(page(base,"<a title='Film - Player' href='https://external.example/page'>Apri</a>")).size()==1,"explicit player page link");
  // Public wrapper traversal must reach the host resolver and verify actual media bytes.
  String wrapper=links.get(0).url;
  pages.put(wrapper,page(wrapper,"<a href='"+host+"'>MixDrop</a><iframe src='https://ads.example/frame'></iframe>"));
  pages.put(host,page(host,"MDCore.wurl='"+stream+"';"));
  check(pipeline.resolve(links.get(0)).url.equals(stream),"wrapper -> MixDrop -> MP4 body verified");
  check(stages.contains("HOST_SEARCH:PUBLIC_LINK"),"public intermediate phase logged");
  pages.put(wrapper,page(wrapper,"<meta http-equiv='refresh' content='0; URL="+host+"'>"));
  check(pipeline.resolve(links.get(0)).url.equals(stream),"public HTML refresh traversal");
  String max="https://maxstream.video/e/test";pages.put(max,page(max,"file:'"+stream+"'"));
  pages.put(links.get(1).url,page(links.get(1).url,"<iframe src='"+max+"'></iframe>"));
  check(pipeline.resolve(links.get(1)).host.equals("MaxStream"),"uprot -> MaxStream -> verified media");
  pages.put(wrapper,page(wrapper,"<a href='"+max+"'>MaxStream</a><a href='"+host+"'>MixDrop</a>"));
  check(pipeline.resolve(links.get(0)).host.equals("MixDrop"),"selected server disambiguates public links");
  failure("INTERMEDIARY_AMBIGUOUS",()->pipeline.resolve(new LabPipeline.Source(wrapper,"Server",base)));
  pages.put(wrapper,page(wrapper,"<a href='"+wrapper+"'>continua</a>"));failure("CHAIN_CYCLE",()->pipeline.resolve(links.get(0)));
  for(int i=0;i<7;i++)pages.put("https://uprot.net/x/"+i,page("https://uprot.net/x/"+i,"<a href='https://uprot.net/x/"+(i+1)+"'>Next</a>"));
  failure("CHAIN_LIMIT",()->pipeline.resolve(new LabPipeline.Source("https://uprot.net/x/0","MaxStream",base)));
  pages.put(wrapper,page(wrapper,"<div class='h-captcha'></div><a href='"+host+"'>MixDrop</a>"));failure("ACCESS_CHALLENGE",()->pipeline.resolve(links.get(0)));
  pages.put(wrapper,page(wrapper,"<a href='http://mixdrop.example/e/x'>MixDrop</a>"));failure("INTERMEDIARY_NO_PUBLIC_LINK",()->pipeline.resolve(links.get(0)));
  pages.put(wrapper,new LabPipeline.Page(max,200,"text/html",("file:'"+stream+"'").getBytes(StandardCharsets.UTF_8)));
  check(pipeline.resolve(links.get(0)).host.equals("MaxStream"),"HTTP redirected wrapper redispatches actual host");
  // Shape observed in public AltaDefinizione DOM; fixtures contain no copied scripts.
  String modern="<article><a href='/horror/33473-insidious-streaming.html'><img src='/logos/insidious.png' alt='Insidious'></a></article>"
   +"<div class='movie-poster'><a data-title='Milonga' href='/drammatico/45172-milonga-streaming.html'><img loading='lazy' src='https://img.catalog.example/t/p/w300/poster.jpg' alt='Milonga'></a></div>"
   +"<a href='/drammatico/45172-milonga-streaming.html'><img src='https://img.catalog.example/t/p/w780/backdrop.jpg' alt='Milonga'></a>"
   +"<a data-title='Insidious' href='/horror/33473-insidious-streaming.html'><img data-src='/poster-insidious.jpg' src='/logos/insidious.png' alt='Insidious'></a>"
   +"<a href='/menu'><img src='/icon.jpg' alt='Menu'></a>";
  LabPipeline.AltaCatalog alta=new LabPipeline.AltaCatalog();List<LabPipeline.Title> modernTitles=alta.titles(page(base,modern));
  check(modernTitles.size()==2,"modern Alta cards instead of six hero articles");
  check(modernTitles.get(0).name.equals("Milonga")&&modernTitles.get(0).poster.contains("/w300/"),"poster anchor title and actual vertical image");
  check(modernTitles.get(1).poster.equals(base+"poster-insidious.jpg"),"lazy poster takes precedence over logo");
  check(alta.search(base,"film prova").equals(base+"archivio?search=film+prova"),"observed modern Alta search path");
  String searchUrl=alta.search(base,"Milonga");pages.put(searchUrl,page(searchUrl,modern));
  check(pipeline.catalog(alta,base,"Milonga").size()==1,"search excludes unrelated homepage cards");
  // Exact alias and /f/ -> /e/ shape observed on the user-provided public MixDrop link.
  String share="https://mxdrop.top/f/test123456",embed="https://mxdrop.top/e/test123456";
  pages.put(share,page(share,"<iframe src='/e/test123456'></iframe><iframe src='https://ads.example/e/advert'></iframe>"));
  pages.put(embed,page(embed,"}('2.3=\"//4/5.6?7=8\";',10,9,'||MDCore|wurl|cdn.example|movie|mp4|token|fixture'.split('|'))"));
  String aliasedMedia="https://cdn.example/movie.mp4?token=fixture";pages.put(aliasedMedia,new LabPipeline.Page(aliasedMedia,206,"video/mp4",pages.get(stream).bytes));
  LabPipeline.Media mixResult=pipeline.resolve(new LabPipeline.Source(share,"MixDrop",base));
  check(mixResult.host.equals("MixDrop")&&mixResult.url.equals(aliasedMedia),"mxdrop alias -> actual declared iframe -> radix10 wurl -> verified media");
  check(mixResult.referer.equals(embed),"embed referer propagated to media");
  check(stages.contains("HOST_SEARCH:HOST_EMBED"),"host embed stage logged");
  check(pipeline.resolver("https://mxdrop.top.evil.example/e/x").name().equals("HTML5 pubblico"),"no alias suffix forgery");
  String sun="https://maxsun623.site/freeswatcee/test",continueUrl="https://maxsun623.site/player/test";
  pages.put(sun,page(sun,"<a href='/player/test'><span>C0NTINUE</span></a>"));pages.put(continueUrl,page(continueUrl,"file:'"+stream+"'"));
  check(pipeline.resolve(new LabPipeline.Source(sun,"MaxStream",base)).host.equals("MaxStream"),"explicit MaxStream alias and ordinary same-site Continue");
  pages.put(sun,page(sun,"<a href='https://ads.example/x'>Continue</a>"));failure("NO_PUBLIC_MEDIA",()->pipeline.resolve(new LabPipeline.Source(sun,"MaxStream",base)));
  // Public AJAX integration: first read the link page, then POST its public ID.
  String stay="https://stayonline.pro/l/test123456/",api="https://stayonline.pro/ajax/linkView.php";
  Map<String,LabPipeline.Page> apiResponses=new HashMap<>();
  pages.put(stay,page(stay,"<script>var linkId = 'test123456';</script><a href='https://uprot.net/x/0'>Other link</a><a href='https://uprot.net/x/1'>Other link</a>"));
  apiResponses.put(api,page(api,"{\"status\":\"success\",\"data\":{\"value\":\"https:\\/\\/mxdrop.top\\/f\\/test123456\"}}"));
  List<Map<String,String>> posted=new ArrayList<>();
  String[] expectedStayReferer={stay};
  LabPipeline ajaxPipeline=new LabPipeline(new LabPipeline.Transport(){
   public LabPipeline.Page get(String url,String ref,boolean media){if(!pages.containsKey(url))throw new AssertionError(url);return pages.get(url);}
   public LabPipeline.Page postForm(String url,Map<String,String> fields,String ref){check(url.equals(api)&&ref.equals(expectedStayReferer[0]),"POST fixed same-origin endpoint and page referer");posted.add(new LinkedHashMap<>(fields));return apiResponses.get(url);}
  },(s,c,d)->stages.add(s+":"+c));
  check(ajaxPipeline.resolve(new LabPipeline.Source(stay,"MixDrop",base)).url.equals(aliasedMedia),"catalog wrapper -> StayOnline POST -> mxdrop /f/ -> /e/ -> verified MP4");
  check(posted.get(0).size()==2&&posted.get(0).get("id").equals("test123456")&&posted.get(0).get("ref").isEmpty(),"public ID only; no private key or generated token");
  check(stages.contains("HOST_SEARCH:PUBLIC_API_LINK"),"AJAX destination phase logged");
  apiResponses.put(api,page(api,"{\"message\":\"ready\\nnow\",\"data\":{\"id\":123,\"value\":\"https://mxdrop.top/f/test123456\",\"flags\":[true,null,false]},\"status\":\"success\"}"));
  check(ajaxPipeline.resolve(new LabPipeline.Source(stay,"MixDrop",base)).url.equals(aliasedMedia),"JSON data.value independent of field order and extra fields");
  apiResponses.put(api,page(api,"{\"data\":{\"value\":\"https://mxdrop.top/e/a\",\"value\":\"https://mxdrop.top/e/b\"}}"));failure("STAYONLINE_JSON",()->ajaxPipeline.resolve(new LabPipeline.Source(stay,"MixDrop",base)));
  apiResponses.put(api,page(api,"{\"data\":{\"value\":\"http://mxdrop.top/e/test123456\"}}"));failure("INVALID_HTTPS",()->ajaxPipeline.resolve(new LabPipeline.Source(stay,"MixDrop",base)));
  apiResponses.put(api,page(api,"{\"data\":{\"value\":\"https://ads.example/player\"}}"));failure("STAYONLINE_UNSUPPORTED_TARGET",()->ajaxPipeline.resolve(new LabPipeline.Source(stay,"MixDrop",base)));
  apiResponses.put(api,page(api,"{\"status\":\"error\"}"));failure("STAYONLINE_NO_DESTINATION",()->ajaxPipeline.resolve(new LabPipeline.Source(stay,"MixDrop",base)));
  apiResponses.put(api,new LabPipeline.Page(api,403,"application/json",new byte[0]));failure("HTTP_403",()->ajaxPipeline.resolve(new LabPipeline.Source(stay,"MixDrop",base)));
  int postsBefore=posted.size();pages.put(stay,page(stay,"<div class='g-recaptcha'></div><script>linkId='test123456'</script>"));failure("ACCESS_CHALLENGE",()->ajaxPipeline.resolve(new LabPipeline.Source(stay,"MixDrop",base)));check(posted.size()==postsBefore,"no API request past access challenge");
  pages.put(stay,new LabPipeline.Page(stay,403,"text/html",new byte[0]));failure("HTTP_403",()->ajaxPipeline.resolve(new LabPipeline.Source(stay,"MixDrop",base)));check(posted.size()==postsBefore,"no API request after refused page");
  pages.put(sun,new LabPipeline.Page(sun,403,"text/html","Cloudflare Error 1005: The owner has banned the autonomous system number (ASN).".getBytes(StandardCharsets.UTF_8)));failure("ACCESS_ASN_BLOCK",()->pipeline.resolve(new LabPipeline.Source(sun,"MaxStream",base)));
  apiResponses.put(api,page(api,"{\"data\":{\"value\":\"https://mxdrop.top/f/test123456\"}}"));
  expectedStayReferer[0]="https://stayonline.pro/";
  pages.put(stay,page(expectedStayReferer[0],"<p>Pagina dopo redirect, senza linkId</p>"));
  check(ajaxPipeline.resolve(new LabPipeline.Source(stay,"MixDrop",base)).url.equals(aliasedMedia),"selected StayOnline ID survives redirect to root and reaches verified media");
  check(posted.get(posted.size()-1).get("id").equals("test123456"),"original opaque ID preserved in AJAX form");
  check(stages.contains("RESOLVER:STAYONLINE_ID_SOURCE"),"ID origin diagnostic emitted");
  String shortStay="https://stayonline.pro/l/7/extra?view=1";pages.put(shortStay,page(expectedStayReferer[0],"<p>Public page</p>"));
  check(ajaxPipeline.resolve(new LabPipeline.Source(shortStay,"MixDrop",base)).url.equals(aliasedMedia),"short opaque ID and trailing path supported through verified media");
  check(posted.get(posted.size()-1).get("id").equals("7"),"no undocumented minimum ID length");
  int beforeAmbiguous=posted.size();pages.put(shortStay,page(expectedStayReferer[0],"<script>linkId='other';</script>"));
  failure("STAYONLINE_ID_AMBIGUOUS",()->ajaxPipeline.resolve(new LabPipeline.Source(shortStay,"MixDrop",base)));check(posted.size()==beforeAmbiguous,"conflicting ID stops before POST");
  String bareStay="https://stayonline.pro/";pages.put(bareStay,page(bareStay,"<script>linkId='42';</script>"));
  check(ajaxPipeline.resolve(new LabPipeline.Source(bareStay,"MixDrop",base)).url.equals(aliasedMedia),"short declared linkId supported");
  pages.put(bareStay,page(bareStay,"No ID"));failure("STAYONLINE_NO_ID",()->ajaxPipeline.resolve(new LabPipeline.Source(bareStay,"MixDrop",base)));
  String refreshWrapper="https://uprot.net/refresh/test";
  pages.put(refreshWrapper,new LabPipeline.Page(refreshWrapper,200,"text/html",new byte[0],"0; URL='https://mxdrop.top/f/test123456'"));
  check(pipeline.resolve(new LabPipeline.Source(refreshWrapper,"MixDrop",base)).url.equals(aliasedMedia),"public HTTP Refresh -> host embed -> verified MP4");
  pages.put(refreshWrapper,new LabPipeline.Page(refreshWrapper,200,"text/html",new byte[0],"0; URL=http://mxdrop.top/f/test123456"));failure("INTERMEDIARY_NO_PUBLIC_LINK",()->pipeline.resolve(new LabPipeline.Source(refreshWrapper,"MixDrop",base)));
  pages.put(refreshWrapper,new LabPipeline.Page(refreshWrapper,403,"text/html",new byte[0],"0; URL=https://mxdrop.top/f/test123456"));failure("HTTP_403",()->pipeline.resolve(new LabPipeline.Source(refreshWrapper,"MixDrop",base)));
  pages.put(sun,page(sun,"<script id='cccc'>"+aliasedMedia+"</script>"));check(pipeline.resolve(new LabPipeline.Source(sun,"MaxStream",base)).url.equals(aliasedMedia),"MaxStream public cccc declaration preserves exact URL and verifies bytes");
  pages.put(sun,page(sun,"<script id='cccc'>https://media.example/not-video.mp4</script>"));pages.put("https://media.example/not-video.mp4",page("https://media.example/not-video.mp4","<html>Error</html>"));failure("NOT_MEDIA",()->pipeline.resolve(new LabPipeline.Source(sun,"MaxStream",base)));
  String realStayShape="https://stayonline.pro/l/XNnym/",m1="https://m1xdrop.net/f/test123456";
  expectedStayReferer[0]=realStayShape;pages.put(realStayShape,page(realStayShape,"<script>var linkId = \"XNnym\";</script>"));
  apiResponses.put(api,page(api,"{\"data\":{\"value\":\""+m1+"\"}}"));
  pages.put(m1,page("https://mxdrop.top/f/test123456","<iframe src='/e/test123456'></iframe>"));
  check(ajaxPipeline.resolve(new LabPipeline.Source(realStayShape,"MixDrop",base)).url.equals(aliasedMedia),"five-character StayOnline ID -> m1xdrop -> redirect mxdrop -> embed -> verified MP4");
  check(posted.get(posted.size()-1).get("id").equals("XNnym"),"provided StayOnline ID reaches public POST unchanged");
  check(pipeline.resolver(m1).name().equals("MixDrop")&&LabPipeline.classify(m1,"MixDrop")==LabPipeline.SourceKind.HOST,"m1 alias uses actual MixDrop resolver and host classification");
  check(pipeline.resolver("https://www.m1xdrop.net/e/test123456").name().equals("MixDrop"),"www alias dispatch");
  check(!LabPipeline.knownHost("m1xdrop.net.evil.example")&&!LabPipeline.knownHost("evil-m1xdrop.net"),"exact m1 alias rejects forged domains");
  pages.put(m1,page(m1,"MDCore.wurl='"+aliasedMedia+"';"));check(ajaxPipeline.resolve(new LabPipeline.Source(realStayShape,"MixDrop",base)).host.equals("MixDrop"),"m1 page without redirect extracts actual declaration and verifies media");
  pages.put(m1,page(m1,"No media"));failure("NO_PUBLIC_MEDIA",()->ajaxPipeline.resolve(new LabPipeline.Source(realStayShape,"MixDrop",base)));
  pages.put(m1,new LabPipeline.Page(m1,403,"text/html",new byte[0]));failure("HTTP_403",()->ajaxPipeline.resolve(new LabPipeline.Source(realStayShape,"MixDrop",base)));
  String uprotHead="https://uprot.net/msf/test123",maxFinal="https://maxstream.video/video/test123";
  List<String> headCalls=new ArrayList<>(),getCalls=new ArrayList<>();Map<String,LabPipeline.Page> heads=new HashMap<>();
  heads.put(uprotHead,new LabPipeline.Page(maxFinal,200,"text/html",new byte[0]));
  pages.put(maxFinal,page(maxFinal,"<video src='"+aliasedMedia+"'></video>"));
  LabPipeline headPipeline=new LabPipeline(new LabPipeline.Transport(){
   public LabPipeline.Page head(String url,String ref){headCalls.add(url);return heads.get(url);}
   public LabPipeline.Page get(String url,String ref,boolean media){getCalls.add(url);if(!pages.containsKey(url))throw new AssertionError(url);return pages.get(url);}
  },(s,c,d)->stages.add(s+":"+c));
  check(headPipeline.resolve(new LabPipeline.Source(uprotHead,"MaxStream",base)).url.equals(aliasedMedia),"Uprot HEAD final URL -> actual MaxStream GET -> verified MP4");
  check(headCalls.contains(uprotHead)&&!getCalls.contains(uprotHead)&&getCalls.contains(maxFinal),"HEAD dispatch preserves public final URL without a guessed conversion endpoint");
  check(stages.contains("HOST_SEARCH:UPROT_HEAD_DESTINATION"),"HEAD destination diagnostics");
  heads.put(uprotHead,new LabPipeline.Page(uprotHead,403,"text/html",new byte[0]));int beforeHeadRefusal=getCalls.size();failure("HTTP_403",()->headPipeline.resolve(new LabPipeline.Source(uprotHead,"MaxStream",base)));check(getCalls.size()==beforeHeadRefusal,"no GET fallback after HEAD access refusal");
  heads.put(uprotHead,null);pages.put(uprotHead,page(uprotHead,"<a href='"+maxFinal+"'>MaxStream</a>"));check(headPipeline.resolve(new LabPipeline.Source(uprotHead,"MaxStream",base)).url.equals(aliasedMedia),"transport without HEAD falls back to ordinary public GET links");
  String adPacker="<script>eval(function(p,a,c,k,e,d){return p;}('0.1=\"2://3/banner.js\";',4,4,'ad|src|https|ads.example'.split('|'),0,{}))</script>";
  String videoPacker="<script>eval(function(p,a,c,k,e,d){return p;}('0:[{1:\"2://3/movie.4?token=fixture\"}];',5,5,'sources|src|https|cdn.example|mp4'.split('|'),0,{}))</script>";
  pages.put(maxFinal,page(maxFinal,adPacker+videoPacker));check(pipeline.resolve(new LabPipeline.Source(maxFinal,"MaxStream",base)).url.equals(aliasedMedia),"MaxStream second packed script resolves media instead of advertising script URL");
  String altaDetail="https://altadefinizionex.surf/thriller/123-title.html",vidPublic="https://v.vidxgo.co/test";
  pages.put(altaDetail,page(altaDetail,"<iframe src='"+vidPublic+"' referrerpolicy='origin' title='Title - Player'></iframe><iframe src='"+vidPublic+"?content=trailer' referrerpolicy='origin' title='Title - Trailer'></iframe>"));
  LabPipeline.SourceReport policyReport=pipeline.sourceReport(new LabPipeline.AltaCatalog(),new LabPipeline.Title("Title","",altaDetail));
  check(policyReport.sources.size()==1&&policyReport.trailers.size()==1,"Alta origin policy retains movie/trailer separation");
  check(policyReport.sources.get(0).referer.equals("https://altadefinizionex.surf/"),"origin referrer matches actual iframe policy");
  List<LabPipeline.Source> fullPolicy=new LabPipeline.AltaCatalog().sources(page(altaDetail,"<iframe src='"+vidPublic+"'></iframe>"));check(fullPolicy.get(0).referer.equals(altaDetail),"existing full referrer unchanged when origin policy absent");
  pages.put(vidPublic,page(vidPublic,"<video src='"+aliasedMedia+"'></video>"));check(pipeline.resolve(policyReport.sources.get(0)).url.equals(aliasedMedia),"Alta source -> public HTML5 -> verified media input");
  pages.put(vidPublic,new LabPipeline.Page(vidPublic,403,"text/html","Access Denied - Stiamo lavorando, torniamo presto".getBytes(StandardCharsets.UTF_8)));failure("HTTP_403",()->pipeline.resolve(policyReport.sources.get(0)));
  System.out.println("PASS: "+checks+" pipeline checks; catalogue -> host -> resolver -> validated Media3 input; synthetic fixtures, including observed public DOM shape. No live playback claim.");
 }
}
