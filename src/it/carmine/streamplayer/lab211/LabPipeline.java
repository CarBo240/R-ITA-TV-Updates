package it.carmine.streamplayer.lab211;

import java.net.URI;
import java.util.*;
import java.util.regex.*;
import java.nio.charset.StandardCharsets;

/** Independently written LAB pipeline. No production preferences, scripts or credentials. */
public final class LabPipeline {
 public enum Stage { CATALOG, HOST_SEARCH, RESOLVER, SOURCE, PLAYBACK }
 public interface Observer { void event(Stage stage,String code,String detail); }
 public interface Transport { Page get(String url,String referer,boolean media)throws Exception; }
 public static final class Failure extends Exception {
  public final Stage stage; public final String code;
  public Failure(Stage s,String c,String message){super(message);stage=s;code=c;}
 }
 public static final class Page {
  public final String url,type;public final int status;public final byte[] bytes;
  public Page(String u,int s,String t,byte[] b){url=u;status=s;type=t==null?"":t;bytes=b;}
  public String text(){return new String(bytes,StandardCharsets.UTF_8);}
 }
 public static final class Title {
  public final String name,poster,url;
  public Title(String n,String p,String u){name=n;poster=p;url=u;}
 }
 public enum SourceKind { MEDIA, HOST, PAGE, INTERMEDIARY, TRAILER }
 public static final class Source {
  public final String url,label,referer;public final SourceKind kind;
  public Source(String u,String l,String r){url=u;label=l;referer=r;kind=classify(u,l);}
 }
 public static final class SourceReport {
  public final List<Source> sources,trailers;
  public SourceReport(List<Source> all){List<Source> main=new ArrayList<>(),preview=new ArrayList<>();for(Source source:all)(source.kind==SourceKind.TRAILER?preview:main).add(source);sources=Collections.unmodifiableList(main);trailers=Collections.unmodifiableList(preview);}
 }

 public static final class Subtitle {
  public final String url,language,label,mime;
  public Subtitle(String u,String l,String n,String m){url=u;language=l;label=n;mime=m;}
 }
 public static final class Media {
  public final String url,mime,referer,host;
  public final List<Subtitle> subtitles;
  public Media(String u,String m,String r,String h,List<Subtitle> s){url=u;mime=m;referer=r;host=h;subtitles=Collections.unmodifiableList(new ArrayList<>(s));}
 }
 public interface CatalogAdapter {
  String name(); String search(String base,String query)throws Exception;
  List<Title> titles(Page page)throws Exception;
  List<Source> sources(Page page)throws Exception;
 }
 public interface HostResolver {
  String name();boolean matches(URI uri);Media parse(Page page,String referer)throws Exception;
 }
 public static final String HLS="application/x-mpegURL",DASH="application/dash+xml",MP4="video/mp4";
 private final Transport transport;private final Observer observer;
 private final List<HostResolver> resolvers;
 public LabPipeline(Transport t,Observer o){transport=t;observer=o;resolvers=Arrays.asList(new MixDrop(),new MaxStream(),new Voe(),new StreamTape(),new DoodStream(),new SuperVideo(),new Vidoza(),new Html5());}
 private void emit(Stage s,String c,String d){if(observer!=null)observer.event(s,c,d);}
 public static URI https(String raw)throws Failure {
  try{URI u=new URI(raw);String h=u.getHost();if(!"https".equalsIgnoreCase(u.getScheme())||h==null||u.getUserInfo()!=null||u.getFragment()!=null||(u.getPort()!=-1&&u.getPort()!=443)||h.equalsIgnoreCase("localhost")||h.endsWith(".local")||h.contains(":")||h.matches("[0-9.]+"))throw new Exception();return u;}
  catch(Exception e){throw new Failure(Stage.SOURCE,"INVALID_HTTPS","Indirizzo HTTPS non valido");}
 }
 public static String absolute(String base,String raw){if(raw==null||raw.trim().isEmpty())return "";try{return https(new URI(base).resolve(decode(raw)).toString()).toString();}catch(Exception e){return "";}}
 public static String decode(String s){return s.replace("\\/","/").replace("\\u0026","&").replace("&amp;","&").replace("&#038;","&").replace("&quot;","\"").replace("&#39;","'").trim();}
 public static String origin(String url)throws Exception{URI u=https(url);return u.getScheme()+"://"+u.getRawAuthority();}
 public static String host(String url){try{return https(url).getHost();}catch(Exception e){return "HTTPS non valido";}}
 public static String mime(String url){try{String p=new URI(url).getPath().toLowerCase(Locale.ROOT);return p.endsWith(".m3u8")?HLS:p.endsWith(".mpd")?DASH:p.endsWith(".mp4")?MP4:"";}catch(Exception e){return "";}}
 private Page fetch(String url,String referer,boolean media,Stage stage)throws Exception{
  https(url);Page page;
  try{page=transport.get(url,referer,media);}catch(Failure e){emit(stage,e.code,e.getMessage());throw new Failure(stage,e.code,e.getMessage());}catch(Exception e){emit(stage,"NETWORK",e.getClass().getSimpleName());throw new Failure(stage,"NETWORK",e.getClass().getSimpleName());}
  https(page.url);emit(stage,"HTTP_"+page.status,host(page.url));
  if(page.status!=200&&page.status!=206)throw new Failure(stage,"HTTP_"+page.status,page.status==403?"Accesso rifiutato (HTTP 403) da "+host(page.url)+"; nessuna sorgente letta, causa non determinata":"Risposta HTTP "+page.status+" da "+host(page.url));
  if(!media&&(page.text().contains("cf-chl-")||page.text().contains("g-recaptcha")||page.text().contains("h-captcha")))throw new Failure(stage,"ACCESS_CHALLENGE","La pagina richiede una verifica interattiva; nessun aggiramento");
  return page;
 }
 public List<Title> catalog(CatalogAdapter adapter,String base,String query)throws Exception {
  emit(Stage.CATALOG,"ADAPTER",adapter.name());Page page=fetch(query.isEmpty()?base:adapter.search(base,query),base,false,Stage.CATALOG);
  List<Title> titles=adapter.titles(page);emit(Stage.CATALOG,"TITLES",titles.size()+" locandine");
  if(titles.isEmpty())throw new Failure(Stage.CATALOG,"NO_TITLES","Nessun titolo compatibile con "+adapter.name()+"; layout o contenuto non supportato");return titles;
 }
 public SourceReport sourceReport(CatalogAdapter adapter,Title title)throws Exception {
  Page page=fetch(title.url,title.url,false,Stage.HOST_SEARCH);SourceReport report=new SourceReport(adapter.sources(page));
  emit(Stage.HOST_SEARCH,"SOURCES",report.sources.size()+" collegamenti candidati · "+report.trailers.size()+" trailer/YouTube separati");
  if(report.sources.isEmpty())emit(Stage.HOST_SEARCH,report.trailers.isEmpty()?"NO_SOURCES":"TRAILER_ONLY",report.trailers.isEmpty()?"Nessun collegamento video individuato nel markup pubblico":"Trovati soltanto trailer/YouTube; nessuna fonte del titolo individuata");return report;
 }
 public List<Source> sources(CatalogAdapter adapter,Title title)throws Exception {
  SourceReport report=sourceReport(adapter,title);if(report.sources.isEmpty())throw new Failure(Stage.HOST_SEARCH,report.trailers.isEmpty()?"NO_SOURCES":"TRAILER_ONLY",report.trailers.isEmpty()?"Nessun collegamento video individuato; layout, script o intermediari non supportati":"La pagina contiene soltanto trailer/YouTube riconosciuti; nessuna fonte del titolo individuata");return report.sources;
 }
 public HostResolver resolver(String url)throws Exception{URI uri=https(url);for(HostResolver r:resolvers)if(r.matches(uri))return r;throw new Failure(Stage.RESOLVER,"UNSUPPORTED_HOST","Host senza adattatore: "+uri.getHost());}
 public Media resolve(Source source)throws Exception {
  if(source.kind==SourceKind.TRAILER)throw new Failure(Stage.RESOLVER,"TRAILER_EXCLUDED","Trailer/anteprima escluso dal player del titolo");
  if(source.kind==SourceKind.INTERMEDIARY)throw new Failure(Stage.RESOLVER,"INTERMEDIARY_UNSUPPORTED","Intermediario "+host(source.url)+": collegamento rilevato, estrazione non implementata; nessun flusso recuperato");
  String direct=mime(source.url);Media result;
  if(!direct.isEmpty()){emit(Stage.RESOLVER,"DIRECT",direct);result=new Media(source.url,direct,source.referer,"Diretto",Collections.emptyList());}
  else{HostResolver r=resolver(source.url);emit(Stage.RESOLVER,"ADAPTER",r.name());Page page=fetch(source.url,source.referer,false,Stage.RESOLVER);
   // HTTP redirects may legitimately change the host. Dispatch again on the final URL.
   String redirectedMime=detectedMime(page);
   if(!redirectedMime.isEmpty())result=new Media(page.url,redirectedMime,source.referer,r.name(),Collections.emptyList());
   else {HostResolver finalResolver=resolver(page.url);if(!finalResolver.name().equals(r.name()))emit(Stage.RESOLVER,"REDISPATCH",finalResolver.name());
    try{result=finalResolver.parse(page,source.referer);}catch(Failure e){emit(e.stage,e.code,e.getMessage());throw e;}}
  }
  emit(Stage.SOURCE,"DECLARED",result.host+" · "+result.mime);Page media=fetch(result.url,result.referer,true,Stage.SOURCE);
  String detected=detectedMime(media);
  if(detected.isEmpty())throw new Failure(Stage.SOURCE,"NOT_MEDIA","La risposta non contiene un manifest HLS/DASH o un file MP4 riconoscibile");
  emit(Stage.SOURCE,"VERIFIED",detected+" · "+host(media.url));return new Media(media.url,detected,result.referer,result.host,result.subtitles);
 }
 public static String detectedMime(Page page){String text=page.text().trim();if(text.startsWith("\ufeff"))text=text.substring(1).trim();if(text.startsWith("#EXTM3U"))return HLS;if(text.matches("(?s)(?:<\\?xml[^>]*>\\s*)?<MPD(?:\\s|>).*"))return DASH;if(page.bytes.length>=12&&new String(page.bytes,4,4,StandardCharsets.US_ASCII).equals("ftyp"))return MP4;return "";}
 public static Map<String,String> attrs(String tag){Map<String,String> out=new HashMap<>();Matcher m=Pattern.compile("([\\w:-]+)\\s*=\\s*([\"'])(.*?)\\2",Pattern.DOTALL).matcher(tag);while(m.find())out.put(m.group(1).toLowerCase(Locale.ROOT),decode(m.group(3)));return out;}
 public static SourceKind classify(String url,String label){
  String h=host(url).toLowerCase(Locale.ROOT);String context=(label==null?"":label).toLowerCase(Locale.ROOT);
  if(trailerPlatform(h)||previewLabel(context)||url.toLowerCase(Locale.ROOT).matches(".*[?&](?:content|type|mode)=(?:trailer|teaser|preview)(?:[&#].*|$)"))return SourceKind.TRAILER;
  if(!mime(url).isEmpty())return SourceKind.MEDIA;if(knownHost(h))return SourceKind.HOST;
  if(intermediary(h))return SourceKind.INTERMEDIARY;return SourceKind.PAGE;
 }
 private static boolean previewLabel(String label){return label.matches("(?i)(?:.*\\s[-·:]\\s*)?(?:trailer|teaser|preview|anteprima)(?:\\s+(?:ita|it|en|ufficiale|official|hd|4k))*");}
 private static boolean trailerPlatform(String h){return h.equals("youtu.be")||h.equals("youtube.com")||h.endsWith(".youtube.com")||h.equals("youtube-nocookie.com")||h.endsWith(".youtube-nocookie.com");}
 public static boolean intermediary(String h){for(String stem:new String[]{"stayonline","staycheck","uprot","rapidcrypt","linkhub","protectlinker","safevideo"})if(domain(h,stem))return true;return false;}
 private static String first(Map<String,String> attrs,String... keys){for(String key:keys){String value=attrs.get(key);if(value!=null&&!value.trim().isEmpty())return value;}return "";}
 public static List<Source> declaredSources(Page page){LinkedHashMap<String,Source> all=new LinkedHashMap<>();String html=page.text();
  Matcher tags=Pattern.compile("(?is)<(a|iframe|source|video|button|option|div)\\b[^>]{0,8192}>").matcher(html);
  while(tags.find()&&all.size()<100){Map<String,String> a=attrs(tags.group());String tag=tags.group(1).toLowerCase(Locale.ROOT);String raw=first(a,"data-src","data-url","data-link","href","src", "value");String u=absolute(page.url,raw);if(u.isEmpty())continue;
   String label=first(a,"title","aria-label","data-title");if(label.isEmpty()&&(tag.equals("a")||tag.equals("button")||tag.equals("option"))){int closing=html.indexOf("</"+tag,tags.end());if(closing>=tags.end()&&closing-tags.end()<500)label=decode(html.substring(tags.end(),closing).replaceAll("<[^>]*>"," ").replaceAll("\\s+"," ").trim());}
   if(label.isEmpty())label=host(u);Source source=new Source(u,label,page.url);
   if(!mime(u).isEmpty()||knownHost(host(u))||intermediary(host(u))||tag.equals("iframe")||source.kind==SourceKind.TRAILER||(tag.equals("a")&&label.matches("(?is).*\\s[-·:]\\s*player"))){
    Source previous=all.get(u);if(previous==null||(!previous.label.equals(label)&&previous.label.equals(host(u))))all.put(u,source);
   }
  }return new ArrayList<>(all.values());
 }
 public static String sourceStatus(Source source){switch(source.kind){case MEDIA:return "Media dichiarato · da verificare";case HOST:return "Host riconosciuto · sorgente da risolvere";case INTERMEDIARY:return "Intermediario · estrazione non supportata";case TRAILER:return "Trailer / anteprima · escluso";default:return "Pagina esterna · compatibilità da verificare";}}
 public static boolean knownHost(String h){for(String name:new String[]{"mixdrop","maxstream","voe","streamtape","dood","doodstream","supervideo","vidoza"})if(domain(h,name))return true;return false;}
 private static boolean domain(String h,String stem){String[] labels=h.toLowerCase(Locale.ROOT).split("\\.");return labels.length>=2&&labels[labels.length-2].equals(stem);}
 private abstract static class PublicResolver implements HostResolver {
  final String stem,label;
  PublicResolver(String s,String l){stem=s;label=l;}
  public String name(){return label;}public boolean matches(URI u){return domain(u.getHost(),stem);}
  String prepare(String html)throws Failure{return html;}
  abstract String[] fields();
  public Media parse(Page page,String referer)throws Exception {
   String html=prepare(page.text());List<Subtitle> tracks=tracks(page);
   // Always prefer explicit HTML5 declarations, even for a named host.
   Matcher tags=Pattern.compile("(?is)<(?:video|source)\\b[^>]{0,8192}>").matcher(html);
   while(tags.find()){Map<String,String> a=attrs(tags.group());String u=absolute(page.url,a.getOrDefault("src",""));String m=mime(u);if(m.isEmpty())m=normalMime(a.getOrDefault("type",""));if(!u.isEmpty()&&!m.isEmpty())return new Media(u,m,page.url,label,tracks);}
   for(String field:fields()){Matcher m=Pattern.compile(field,Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(html);while(m.find()){String raw=m.group(1);String u=absolute(page.url,raw);if(!u.isEmpty()&&(raw.startsWith("https:")||raw.startsWith("//")||!mime(u).isEmpty()))return new Media(u,mime(u),page.url,label,tracks);}}
   throw new Failure(Stage.RESOLVER,"NO_PUBLIC_MEDIA",label+": nessuna sorgente pubblica supportata; script, API riservate o token non vengono eseguiti");
  }
 }
 private static String normalMime(String m){String x=m.toLowerCase(Locale.ROOT);return x.contains("mpegurl")?HLS:x.contains("dash+xml")?DASH:x.equals(MP4)?MP4:"";}
 private static String key(String k){return "[\"']?"+k+"[\"']?\\s*[:=]\\s*[\"']([^\"'\\r\\n]+)[\"']";}
 public static final class MixDrop extends PublicResolver {public MixDrop(){super("mixdrop","MixDrop");}String prepare(String h)throws Failure{return unpackPublic(h);}String[] fields(){return new String[]{"MDCore\\.(?:wurl|vurl)\\s*=\\s*[\"']([^\"']+)[\"']"};}}
 public static final class MaxStream extends PublicResolver {public MaxStream(){super("maxstream","MaxStream");}String prepare(String h)throws Failure{return unpackPublic(h);}String[] fields(){return new String[]{key("file"),key("src")};}}
 public static final class Voe extends PublicResolver {public Voe(){super("voe","VOE");}String prepare(String h){
   Matcher m=Pattern.compile("[\"']?(hls|mp4)[\"']?\\s*:\\s*(?:atob\\(\\s*)?[\"']([A-Za-z0-9+/=]{20,})[\"']\\s*\\)?").matcher(h);StringBuffer out=new StringBuffer();while(m.find()){try{String decoded=new String(Base64.getDecoder().decode(m.group(2)),StandardCharsets.UTF_8);https(decoded);m.appendReplacement(out,Matcher.quoteReplacement(m.group(1)+":\""+decoded+"\""));}catch(Exception e){m.appendReplacement(out,Matcher.quoteReplacement(m.group()));}}m.appendTail(out);return out.toString();
  }String[] fields(){return new String[]{key("hls"),key("mp4")};}}
 public static final class StreamTape extends PublicResolver {public StreamTape(){super("streamtape","StreamTape");}String[] fields(){return new String[]{"id=[\"'](?:robotlink|videolink)[\"'][^>]*>\\s*(https?://[^<]+|//[^<]+)<","(?:getElementById\\([\"'](?:robotlink|videolink)[\"']\\)\\.innerHTML)\\s*=\\s*[\"'](https?://[^\"']+|//[^\"']+)[\"']\\s*;"};}}
 public static final class DoodStream extends PublicResolver {public DoodStream(){super("dood","DoodStream");}public boolean matches(URI u){return super.matches(u)||domain(u.getHost(),"doodstream");}String[] fields(){return new String[0];}}
 public static final class SuperVideo extends PublicResolver {public SuperVideo(){super("supervideo","SuperVideo");}String prepare(String h)throws Failure{return unpackPublic(h);}String[] fields(){return new String[]{key("file"),key("src")};}}
 public static final class Vidoza extends PublicResolver {public Vidoza(){super("vidoza","Vidoza");}String[] fields(){return new String[]{key("src")};}}
 public static final class Html5 extends PublicResolver {public Html5(){super("","HTML5 pubblico");}public boolean matches(URI u){return true;}String[] fields(){return new String[0];}}
 public static List<Subtitle> tracks(Page page){List<Subtitle> out=new ArrayList<>();Matcher m=Pattern.compile("(?is)<track\\b[^>]{0,8192}>").matcher(page.text());while(m.find()&&out.size()<30){Map<String,String> a=attrs(m.group());String kind=a.getOrDefault("kind","");if(!kind.equals("subtitles")&&!kind.equals("captions"))continue;String u=absolute(page.url,a.getOrDefault("src",""));if(u.isEmpty())continue;String path=URI.create(u).getPath().toLowerCase(Locale.ROOT);String mime=path.endsWith(".vtt")?"text/vtt":path.endsWith(".srt")?"application/x-subrip":path.endsWith(".ttml")?"application/ttml+xml":"";if(!mime.isEmpty())out.add(new Subtitle(u,a.getOrDefault("srclang","und"),a.getOrDefault("label","Sottotitoli"),mime));}return out;}
 /** Decode a bounded, literal public packer payload. Never evaluate remote code. */
 public static String unpackPublic(String html)throws Failure {
  Pattern p=Pattern.compile("\\}\\s*\\(\\s*'((?:\\\\.|[^'\\\\])*)'\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*'((?:\\\\.|[^'\\\\])*)'\\.split\\(\\s*'\\|'\\s*\\)",Pattern.DOTALL);
  Matcher m=p.matcher(html);if(!m.find())return html;
  try{int radix=Integer.parseInt(m.group(2)),count=Integer.parseInt(m.group(3));if(radix<2||radix>62||count>10000)throw new Exception();String payload=literal(m.group(1));String[] keys=literal(m.group(4)).split("\\|",-1);if(count>keys.length||payload.length()>1000000)throw new Exception();Map<String,String> dict=new HashMap<>();for(int i=0;i<count;i++)if(!keys[i].isEmpty())dict.put(baseCode(i,radix),keys[i]);Matcher word=Pattern.compile("\\b[0-9a-zA-Z]+\\b").matcher(payload);StringBuffer out=new StringBuffer();while(word.find())word.appendReplacement(out,Matcher.quoteReplacement(dict.getOrDefault(word.group(),word.group())));word.appendTail(out);return html+"\n"+out;}
  catch(Exception e){throw new Failure(Stage.RESOLVER,"PACKER_FORMAT","Formato packer pubblico non supportato (nessuna esecuzione)");}
 }
 private static String baseCode(int value,int radix){String digits="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";if(value==0)return "0";StringBuilder out=new StringBuilder();while(value>0){out.append(digits.charAt(value%radix));value/=radix;}return out.reverse().toString();}
 private static String literal(String s){return s.replace("\\'","'").replace("\\\"","\"").replace("\\/","/").replace("\\\\","\\");}
 public static CatalogAdapter adapter(String kind)throws Failure {
  switch(kind){case "cb01":return new Cb01Catalog();case "altadefinizione":return new AltaCatalog();case "wordpress":return new WordpressCatalog();case "dle":return new DleCatalog();default:throw new Failure(Stage.CATALOG,"UNSUPPORTED_CATALOG","Selezionare un adattatore LAB: CB01, AltaDefinizione, WordPress o DLE");}
 }
 private abstract static class HtmlCatalog implements CatalogAdapter {
  abstract Pattern blocks();
  public List<Title> titles(Page page){LinkedHashMap<String,Title> out=new LinkedHashMap<>();Matcher blocks=blocks().matcher(page.text());while(blocks.find()&&out.size()<100){String body=blocks.group();Matcher anchors=Pattern.compile("(?is)<a\\b[^>]*>").matcher(body);Matcher img=Pattern.compile("(?is)<img\\b[^>]*>").matcher(body);if(!img.find())continue;Map<String,String> a=attrs(img.group());String poster=absolute(page.url,a.getOrDefault("data-src",a.getOrDefault("data-original",a.getOrDefault("src",""))));String title=a.getOrDefault("alt",a.getOrDefault("title",""));String url="";while(anchors.find()){Map<String,String> link=attrs(anchors.group());String candidate=absolute(page.url,link.getOrDefault("href",""));if(!candidate.isEmpty()&&host(candidate).equals(host(page.url))&&!candidate.equals(page.url)&&!URI.create(candidate).getPath().matches(".*/(?:category|tag|author)/.*")){url=candidate;if(title.isEmpty())title=link.getOrDefault("title","");break;}}
   if(title.isEmpty()){Matcher h=Pattern.compile("(?is)<h[1-4]\\b[^>]*>(.*?)</h[1-4]>").matcher(body);if(h.find())title=h.group(1).replaceAll("<[^>]*>"," ").trim();}
   if(!url.isEmpty()&&!poster.isEmpty()&&title.length()>1)out.putIfAbsent(url,new Title(decode(title),poster,url));}return new ArrayList<>(out.values());}
  public List<Source> sources(Page page){return declaredSources(page);}
 }
 public static final class Cb01Catalog extends HtmlCatalog {public String name(){return "CB01 · articoli";}Pattern blocks(){return Pattern.compile("(?is)<article\\b[^>]*>.*?</article>|<(?:div|section)\\b[^>]*class=[\"'][^\"']*\\bcard(?:-body)?\\b[^\"']*[\"'][^>]*>.*?(?=<(?:div|section)\\b[^>]*class=[\"'][^\"']*\\bcard\\b|$)");}public String search(String b,String q)throws Exception{return origin(b)+"/?s="+java.net.URLEncoder.encode(q,"UTF-8");}}
 public static final class AltaCatalog extends HtmlCatalog {public String name(){return "AltaDefinizione · schede DLE";}Pattern blocks(){return Pattern.compile("(?is)<article\\b[^>]*>.*?</article>|<div\\b[^>]*class=[\"'][^\"']*(?:shortstory|short-item|movie-item|box-film)[^\"']*[\"'][^>]*>.*?(?=<div\\b[^>]*class=[\"'][^\"']*(?:shortstory|short-item|movie-item|box-film)|$)");}public String search(String b,String q)throws Exception{return origin(b)+"/index.php?do=search&subaction=search&story="+java.net.URLEncoder.encode(q,"UTF-8");}}
 public static final class WordpressCatalog extends HtmlCatalog {public String name(){return "WordPress · articoli";}Pattern blocks(){return Pattern.compile("(?is)<article\\b[^>]*>.*?</article>");}public String search(String b,String q)throws Exception{return origin(b)+"/?s="+java.net.URLEncoder.encode(q,"UTF-8");}}
 public static final class DleCatalog extends HtmlCatalog {public String name(){return "DLE · shortstory";}Pattern blocks(){return new AltaCatalog().blocks();}public String search(String b,String q)throws Exception{return new AltaCatalog().search(b,q);}}
}
