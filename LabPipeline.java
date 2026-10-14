package it.carmine.streamplayer.lab211;

import java.net.URI;
import java.util.*;
import java.util.regex.*;
import java.nio.charset.StandardCharsets;

/** Independently written LAB pipeline. No production preferences, scripts or credentials. */
public final class LabPipeline {
 public enum Stage { CATALOG, HOST_SEARCH, RESOLVER, SOURCE, PLAYBACK }
 public interface Observer { void event(Stage stage,String code,String detail); }
 public interface Transport {
  Page get(String url,String referer,boolean media)throws Exception;
  default Map<String,String> playbackHeaders(String url){return Collections.emptyMap();}
  default Page getWithHeaders(String url,String referer,boolean media,Map<String,String> headers)throws Exception{return get(url,referer,media);}
  default Page postJson(String url,String body,String referer,String csrf)throws Exception {throw new Failure(Stage.CATALOG,"PUBLIC_API_UNAVAILABLE","Trasporto JSON pubblico non disponibile");}
  default Page head(String url,String referer)throws Exception{return null;}
  default Page postForm(String url,Map<String,String> fields,String referer)throws Exception {throw new Failure(Stage.RESOLVER,"PUBLIC_API_UNAVAILABLE","Trasporto POST pubblico non disponibile");}
 }
 public static final class Failure extends Exception {
  public final Stage stage; public final String code;
  public Failure(Stage s,String c,String message){super(message);stage=s;code=c;}
 }
 public static final class Page {
  public final String url,type,refresh;public final int status;public final byte[] bytes;
  public Page(String u,int s,String t,byte[] b){this(u,s,t,b,"");}
  public Page(String u,int s,String t,byte[] b,String r){url=u;status=s;type=t==null?"":t;bytes=b;refresh=r==null?"":r;}
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
  public final String url,mime,referer,host,refreshId;public final Map<String,String> headers;
  public final List<Subtitle> subtitles;
  public Media(String u,String m,String r,String h,List<Subtitle> s){this(u,m,r,h,s,"");}
  public Media(String u,String m,String r,String h,List<Subtitle> s,String id){this(u,m,r,h,s,id,Collections.emptyMap());}
  public Media(String u,String m,String r,String h,List<Subtitle> s,String id,Map<String,String> extra){url=u;mime=m;referer=r;host=h;refreshId=id;Map<String,String> context=new LinkedHashMap<>(PlaybackHeaders.forHost(h,r));context.putAll(extra);headers=Collections.unmodifiableMap(context);subtitles=Collections.unmodifiableList(new ArrayList<>(s));}
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
 public LabPipeline(Transport t,Observer o){transport=t;observer=o;resolvers=Arrays.asList(new MixDrop(),new MaxStream(),new VidXGo(),new Voe(),new StreamTape(),new DoodStream(),new SuperVideo(),new Dropload(),new StreamWish(),new AnimeWorld(),new VixCloud(),new Vidoza(),new Html5());}
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
  return checked(page,media,stage);
 }
 private Page checked(Page page,boolean media,Stage stage)throws Exception {
  https(page.url);emit(stage,"HTTP_"+page.status,host(page.url));
  String responseText=page.text().toLowerCase(Locale.ROOT);
  if(!media&&responseText.contains("cloudflare")&&responseText.contains("1005")&&(responseText.contains("autonomous system")||responseText.contains("asn")))throw new Failure(stage,"ACCESS_ASN_BLOCK","Cloudflare 1005 da "+host(page.url)+": rete ASN bloccata dal sito; nessun tentativo di aggiramento");
  if(page.status!=200&&page.status!=206)throw new Failure(stage,"HTTP_"+page.status,page.status==403?"Accesso rifiutato (HTTP 403) da "+host(page.url)+"; nessuna sorgente letta, causa non determinata":"Risposta HTTP "+page.status+" da "+host(page.url));
  if(!media&&(page.text().contains("cf-chl-")||page.text().contains("g-recaptcha")||page.text().contains("h-captcha")))throw new Failure(stage,"ACCESS_CHALLENGE","La pagina richiede una verifica interattiva; nessun aggiramento");
  return page;
 }
 public List<Title> catalog(CatalogAdapter adapter,String base,String query)throws Exception {
  if(host(base).equals("k9vo.com")||host(base).endsWith(".k9vo.com"))throw new Failure(Stage.CATALOG,"K9VO_SIGNED_API_REQUIRED","K9vo/PPCine: catalogo, identità client e flussi richiedono API e firme specifiche; integrazione non disponibile senza credenziali proprie autorizzate dal servizio");
  emit(Stage.CATALOG,"ADAPTER",adapter.name());String route=query.isEmpty()?base:adapter.search(base,query);
  if(adapter instanceof EuroCatalog&&query.isEmpty()&&(https(base).getPath().isEmpty()||https(base).getPath().equals("/")))route=origin(base)+"/archive?type=movie";
  if(adapter instanceof AltaCatalog&&query.isEmpty()&&(https(base).getPath().isEmpty()||https(base).getPath().equals("/")))route=origin(base)+"/film";
  Page page;try{page=adapter instanceof AnimeUnityCatalog?checked(((AnimeUnityCatalog)adapter).catalog(transport,base,query),false,Stage.CATALOG):fetch(route,base,false,Stage.CATALOG);}catch(Failure e){if(!(adapter instanceof AltaCatalog)||!e.code.equals("HTTP_404")||route.equals(base))throw e;emit(Stage.CATALOG,"LAYOUT_ROUTE_FALLBACK","Percorso aggiornato assente (404); percorso precedente");page=fetch(query.isEmpty()?base:origin(base)+"/archivio?search="+java.net.URLEncoder.encode(query,"UTF-8"),base,false,Stage.CATALOG);}
  List<Title> titles=adapter.titles(page);
  if(adapter instanceof AltaCatalog&&!query.isEmpty()){List<Title> matching=new ArrayList<>();for(Title title:titles)if(title.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))matching.add(title);titles=matching;}
  emit(Stage.CATALOG,"TITLES",titles.size()+" locandine");
  if(titles.isEmpty())throw new Failure(Stage.CATALOG,"NO_TITLES","Nessun titolo compatibile con "+adapter.name()+"; layout o contenuto non supportato");return titles;
 }
 public SourceReport sourceReport(CatalogAdapter adapter,Title title)throws Exception {
  Page page=fetch(title.url,title.url,false,Stage.HOST_SEARCH);SourceReport report=new SourceReport(adapter instanceof StreamingItaCatalog?((StreamingItaCatalog)adapter).sources(page,transport):adapter instanceof AnimeUnityCatalog?((AnimeUnityCatalog)adapter).sources(page,transport):adapter.sources(page));
  emit(Stage.HOST_SEARCH,"SOURCES",report.sources.size()+" collegamenti candidati · "+report.trailers.size()+" trailer/YouTube separati");
  if(report.sources.isEmpty())emit(Stage.HOST_SEARCH,report.trailers.isEmpty()?"NO_SOURCES":"TRAILER_ONLY",report.trailers.isEmpty()?"Nessun collegamento video individuato nel markup pubblico":"Trovati soltanto trailer/YouTube; nessuna fonte del titolo individuata");return report;
 }
 public List<Source> sources(CatalogAdapter adapter,Title title)throws Exception {
  SourceReport report=sourceReport(adapter,title);if(report.sources.isEmpty())throw new Failure(Stage.HOST_SEARCH,report.trailers.isEmpty()?"NO_SOURCES":"TRAILER_ONLY",report.trailers.isEmpty()?"Nessun collegamento video individuato; layout, script o intermediari non supportati":"La pagina contiene soltanto trailer/YouTube riconosciuti; nessuna fonte del titolo individuata");return report.sources;
 }
 public HostResolver resolver(String url)throws Exception{URI uri=https(url);for(HostResolver r:resolvers)if(r.matches(uri))return r;throw new Failure(Stage.RESOLVER,"UNSUPPORTED_HOST","Host senza adattatore: "+uri.getHost());}
 public Media resolve(Source source)throws Exception {
  if(source.kind==SourceKind.TRAILER)throw new Failure(Stage.RESOLVER,"TRAILER_EXCLUDED","Trailer/anteprima escluso dal player del titolo");
  Media result=resolvePublic(source,new LinkedHashSet<>(),0);
  emit(Stage.SOURCE,"DECLARED",result.host+" · "+result.mime);Page media=transport.getWithHeaders(result.url,result.referer,true,result.headers);checked(media,true,Stage.SOURCE);
  String detected=detectedMime(media);
  if(detected.isEmpty())throw new Failure(Stage.SOURCE,"NOT_MEDIA","La risposta non contiene un manifest HLS/DASH o un file MP4 riconoscibile");
  emit(Stage.SOURCE,"VERIFIED",detected+" · "+host(media.url));Map<String,String> context=new LinkedHashMap<>(result.headers);context.putAll(transport.playbackHeaders(media.url));return new Media(media.url,detected,result.referer,result.host,result.subtitles,result.refreshId,context);
 }
 private Media resolvePublic(Source source,Set<String> visited,int depth)throws Exception {
  if(depth>5)throw new Failure(Stage.RESOLVER,"CHAIN_LIMIT","Superati cinque passaggi pubblici verso il video");
  if(!visited.add(source.url))throw new Failure(Stage.RESOLVER,"CHAIN_CYCLE","Collegamento circolare: "+host(source.url));
  if(isUprotVideo(source.url)){
   emit(Stage.RESOLVER,"UPROT_PUBLIC_HEAD","Uprot · verifica redirect HTTPS con HEAD, senza conversione API");
   Page destination;try{destination=transport.head(source.url,source.referer);}catch(Failure e){throw new Failure(Stage.RESOLVER,e.code,e.getMessage());}catch(Exception e){throw new Failure(Stage.RESOLVER,"NETWORK","HEAD Uprot: "+e.getClass().getSimpleName());}
   if(destination!=null){checked(destination,false,Stage.RESOLVER);
    if(!destination.url.equals(source.url)&&(knownHost(host(destination.url))||!mime(destination.url).isEmpty())){
     emit(Stage.HOST_SEARCH,"UPROT_HEAD_DESTINATION",host(source.url)+" → "+host(destination.url));return resolvePublic(new Source(destination.url,source.label,source.url),visited,depth+1);
    }
   }
  }
  String direct=mime(source.url);Media result;
  if(!direct.isEmpty()){emit(Stage.RESOLVER,"DIRECT",direct);result=new Media(source.url,direct,source.referer,"Diretto",Collections.emptyList());}
  else{HostResolver r=resolver(source.url);emit(Stage.RESOLVER,"ADAPTER",source.kind==SourceKind.INTERMEDIARY?"Collegamenti pubblici · "+host(source.url):r.name());Page page=fetch(source.url,source.referer,false,Stage.RESOLVER);
   // HTTP redirects may legitimately change the host. Dispatch again on the final URL.
   String redirectedMime=detectedMime(page);
   if(!redirectedMime.isEmpty())result=new Media(page.url,redirectedMime,source.referer,r.name(),Collections.emptyList());
   else if(intermediary(host(page.url))){
    emit(Stage.HOST_SEARCH,"INTERMEDIARY",host(page.url)+" · ricerca collegamenti pubblici");
    if(host(page.url).equals("stayonline.pro")||host(page.url).equals("www.stayonline.pro")){
     Source destination=stayOnline(page,source.url);emit(Stage.HOST_SEARCH,"PUBLIC_API_LINK",host(page.url)+" → "+host(destination.url));return resolvePublic(destination,visited,depth+1);
    }
    List<Source> candidates=new ArrayList<>();
    for(Source next:declaredSources(page))if(next.kind==SourceKind.HOST||next.kind==SourceKind.MEDIA||next.kind==SourceKind.INTERMEDIARY)candidates.add(next);
    Source headerTarget=refreshTarget(page.url,page.refresh,source.label);if(headerTarget!=null)candidates.add(headerTarget);
    // Ordinary HTML refresh only. No remote code, hidden API, gate or token generation.
    Matcher metas=Pattern.compile("(?is)<meta\\b[^>]*>").matcher(page.text());
    while(metas.find()){Map<String,String> a=attrs(metas.group());if(!"refresh".equalsIgnoreCase(a.getOrDefault("http-equiv","")))continue;
     Matcher target=Pattern.compile("(?is)(?:^|;)\\s*url\\s*=\\s*(.*?)\\s*$").matcher(a.getOrDefault("content",""));
     if(target.find()){String raw=target.group(1).replaceAll("^['\"]|['\"]$","");String u=absolute(page.url,raw);if(!u.isEmpty()){Source next=new Source(u,source.label,page.url);if(next.kind==SourceKind.HOST||next.kind==SourceKind.MEDIA||next.kind==SourceKind.INTERMEDIARY)candidates.add(next);}}
    }
    LinkedHashMap<String,Source> unique=new LinkedHashMap<>();for(Source next:candidates)unique.putIfAbsent(next.url,next);candidates=new ArrayList<>(unique.values());
    if(candidates.size()>1){List<Source> matching=new ArrayList<>();String label=source.label.toLowerCase(Locale.ROOT);for(Source next:candidates)if(next.kind==SourceKind.HOST&&label.contains(resolver(next.url).name().toLowerCase(Locale.ROOT)))matching.add(next);if(matching.size()==1)candidates=matching;}
    if(candidates.isEmpty())throw new Failure(Stage.RESOLVER,"INTERMEDIARY_NO_PUBLIC_LINK",host(page.url)+": nessun link HTTPS pubblico a host/media; eventuali script, API o verifiche interattive non gestiti");
    if(candidates.size()!=1)throw new Failure(Stage.RESOLVER,"INTERMEDIARY_AMBIGUOUS",host(page.url)+": "+candidates.size()+" destinazioni pubbliche; impossibile associare con certezza il server selezionato");
    Source next=candidates.get(0);emit(Stage.HOST_SEARCH,"PUBLIC_LINK",host(page.url)+" → "+host(next.url));return resolvePublic(next,visited,depth+1);
   }
   else {HostResolver finalResolver=resolver(page.url);
    String animeKind=LabVodPolicy.adapterKind(page.url);if(animeKind.isEmpty()&&source.label.startsWith("AnimeSaturn")&&LabVodPolicy.adapterKind(source.referer).equals("animesaturn")&&https(page.url).getPath().matches("/embed/[0-9]+"))animeKind="animesaturn";if(animeKind.equals("animesaturn")||animeKind.equals("animeworld")&&!https(page.url).getPath().equals("/api/episode/info")){if(https(page.url).getPath().matches("/embed/[0-9]+")){
     Matcher declared=Pattern.compile("window\\.__E\\s*=\\s*\\{([^}]+)\\}").matcher(page.text());if(!declared.find())throw new Failure(Stage.RESOLVER,"SATURN_CONFIG","AnimeSaturn: configurazione embed assente");String config=declared.group(1);Matcher id=Pattern.compile("\\bi\\s*:\\s*([0-9]+)").matcher(config),key=Pattern.compile("\\bk\\s*:\\s*['\"]([^'\"]+)['\"]").matcher(config),expiry=Pattern.compile("\\be\\s*:\\s*([0-9]+)").matcher(config);if(!id.find()||!key.find()||!expiry.find())throw new Failure(Stage.RESOLVER,"SATURN_CONFIG","AnimeSaturn: parametri mancanti");String token=key.group(1);String endpoint=origin(page.url)+"/embed/"+id.group(1)+"/playlist?token="+java.net.URLEncoder.encode(token,"UTF-8")+"&expires="+expiry.group(1);Page payload=fetch(endpoint,page.url,false,Stage.RESOLVER);Object obj=publicJson(payload.text());Object data=obj instanceof Map?((Map<?,?>)obj).get("d"):null;if(!(data instanceof String)||token.isEmpty()||token.length()>512)throw new Failure(Stage.RESOLVER,"SATURN_PAYLOAD","AnimeSaturn: risposta non valida");byte[] bytes=Base64.getDecoder().decode((String)data);if(bytes.length>65536)throw new Failure(Stage.RESOLVER,"SATURN_PAYLOAD","AnimeSaturn: risposta troppo grande");for(int i=0;i<bytes.length;i++)bytes[i]^=(byte)token.charAt(i%token.length());String url=absolute(page.url,new String(bytes,StandardCharsets.ISO_8859_1));if(url.isEmpty())throw new Failure(Stage.RESOLVER,"SATURN_MEDIA","AnimeSaturn: sorgente assente");return new Media(url,mime(url),page.url,"AnimeSaturn",Collections.emptyList());
    }List<Source> episodes=new AnimeCatalog(animeKind.equals("animesaturn")).sources(page);if(episodes.isEmpty())throw new Failure(Stage.RESOLVER,"ANIME_NO_SERVERS","Nessun server dichiarato nell'episodio");return resolvePublic(episodes.get(0),visited,depth+1);}

    if(LabVodPolicy.adapterKind(page.url).equals("animeunity")){if(https(page.url).getPath().startsWith("/embed-url/")){String target=absolute(page.url,page.text().replaceAll("<[^>]*>","").trim());if(!target.isEmpty()&&knownHost(host(target)))return resolvePublic(new Source(target,source.label,source.referer),visited,depth+1);throw new Failure(Stage.RESOLVER,"UNITY_EMBED","AnimeUnity: destinazione embed assente");}List<Source> episodes=new AnimeUnityCatalog().sources(page,transport);if(episodes.isEmpty())throw new Failure(Stage.RESOLVER,"UNITY_EPISODES","AnimeUnity: episodi assenti");return resolvePublic(episodes.get(0),visited,depth+1);}
    if(finalResolver instanceof AnimeWorld&&https(page.url).getPath().equals("/api/episode/info")){Object value=publicJson(page.text());Object grabber=value instanceof Map?((Map<?,?>)value).get("grabber"):null;if(grabber instanceof String){String target=absolute(page.url,(String)grabber);if(!target.isEmpty()&&knownHost(host(target))&&!domain(host(target),"animeworld"))return resolvePublic(new Source(target,source.label,source.referer),visited,depth+1);}}
    if(finalResolver instanceof Voe){Matcher redirect=Pattern.compile("(?:window\\.)?location(?:\\.href)?\\s*=\\s*['\"]([^'\"]+)['\"]").matcher(page.text());if(redirect.find()){String next=absolute(page.url,redirect.group(1));if(!next.isEmpty()&&!next.equals(page.url)&&finalResolver.matches(https(next)))return resolvePublic(new Source(next,source.label,page.url),visited,depth+1);}}
    if(finalResolver instanceof DoodStream){Matcher pass=Pattern.compile("/pass_md5/[^'\"\\s<>]+").matcher(page.text());if(pass.find()){String endpoint=origin(page.url)+pass.group();Page ticket=fetch(endpoint,page.url,false,Stage.RESOLVER);String prefix=ticket.text().trim();if(!prefix.startsWith("https://"))throw new Failure(Stage.RESOLVER,"DOOD_PREFIX","Dood: risposta sessione non valida");Matcher token=Pattern.compile("[?&]token=([A-Za-z0-9_-]+)").matcher(page.text());String value=token.find()?token.group(1):https(endpoint).getPath().substring(https(endpoint).getPath().lastIndexOf('/')+1);String tail=UUID.randomUUID().toString().replace("-","").substring(0,10);return new Media(prefix+tail+"?token="+java.net.URLEncoder.encode(value,"UTF-8")+"&expiry="+System.currentTimeMillis(),MP4,page.url,"DoodStream",tracks(page),"",transport.playbackHeaders(page.url));}}
if(!finalResolver.name().equals(r.name()))emit(Stage.RESOLVER,"REDISPATCH",finalResolver.name());
    try{result=finalResolver.parse(page,source.referer);}catch(Failure e){
     if(e.code.equals("NO_PUBLIC_MEDIA")&&(finalResolver instanceof MixDrop||finalResolver instanceof MaxStream||finalResolver instanceof Html5)){
      Source next=hostEmbed(page,finalResolver);
      if(next!=null){emit(Stage.HOST_SEARCH,"HOST_EMBED",host(page.url)+" · "+finalResolver.name());return resolvePublic(next,visited,depth+1);}
     }
     emit(e.stage,e.code,e.getMessage());throw e;}}
  }
  return result;
 }
 private static boolean isUprotVideo(String url)throws Exception {
  String h=host(url);return (h.equals("uprot.net")||h.equals("www.uprot.net"))&&https(url).getPath().matches("/(?:msf|msfi)/[A-Za-z0-9_-]{1,128}/?");
 }
 private Source stayOnline(Page page,String selected)throws Exception {
  // Preserve the selected link before transport redirects, as in the observed
  // Veezie flow. IDs are opaque public strings, not necessarily six characters.
  LinkedHashSet<String> ids=new LinkedHashSet<>();String original=stayId(selected),current=stayId(page.url);
  if(!original.isEmpty())ids.add(original);if(!current.isEmpty())ids.add(current);
  Matcher declared=Pattern.compile("\\blinkId\\s*=\\s*['\"]([A-Za-z0-9_-]{1,128})['\"]").matcher(page.text());while(declared.find())ids.add(declared.group(1));
  if(ids.isEmpty())throw new Failure(Stage.RESOLVER,"STAYONLINE_NO_ID","StayOnline: ID non trovato nel collegamento selezionato, nell'URL finale o in linkId; inviare il collegamento StayOnline completo");
  if(ids.size()!=1)throw new Failure(Stage.RESOLVER,"STAYONLINE_ID_AMBIGUOUS","StayOnline: identificativi pubblici discordanti; nessuna destinazione selezionata");
  String id=ids.iterator().next();emit(Stage.RESOLVER,"STAYONLINE_ID_SOURCE",!original.isEmpty()?"Collegamento originale · ID conservato prima dei redirect":!current.isEmpty()?"URL finale":"Dichiarazione linkId pubblica");
  Map<String,String> form=new LinkedHashMap<>();form.put("id",id);form.put("ref","");
  String endpoint=origin(page.url)+"/ajax/linkEmbedView.php";emit(Stage.RESOLVER,"STAYONLINE_PUBLIC_POST","StayOnline · richiesta destinazione senza credenziali");Page response;
  try{response=transport.postForm(endpoint,form,page.url);}catch(Failure e){throw e;}catch(Exception e){throw new Failure(Stage.RESOLVER,"NETWORK",e.getClass().getSimpleName());}
  checked(response,false,Stage.RESOLVER);
  // Read the data.value envelope, allowing ordinary extra JSON fields.
  // No evaluation, token generation or ID conversion.
  Object decoded=new PublicJson(response.text()).parse();Object data=decoded instanceof Map?((Map<?,?>)decoded).get("data"):null;
  Object value=data instanceof Map?((Map<?,?>)data).get("value"):null;
  if(!(value instanceof String))throw new Failure(Stage.RESOLVER,"STAYONLINE_NO_DESTINATION","StayOnline: risposta senza data.value pubblico supportato");
  String url=(String)value;
  https(url);Source next=new Source(url,host(url),page.url);
  if(next.kind!=SourceKind.HOST&&next.kind!=SourceKind.MEDIA&&next.kind!=SourceKind.INTERMEDIARY)throw new Failure(Stage.RESOLVER,"STAYONLINE_UNSUPPORTED_TARGET","StayOnline: dominio destinazione senza adattatore compatibile: "+host(url));
  return next;
 }
 private static String stayId(String url)throws Exception {
  String h=host(url);if(!h.equals("stayonline.pro")&&!h.equals("www.stayonline.pro"))return "";
  Matcher path=Pattern.compile("/(?:[a-z]?l)/([A-Za-z0-9_-]{1,128})(?:/.*)?$").matcher(https(url).getPath());return path.find()?path.group(1):"";
 }
 private static Source refreshTarget(String base,String value,String label){
  Matcher m=Pattern.compile("(?is)^\\s*[0-9]+(?:\\.[0-9]+)?\\s*;\\s*url\\s*=\\s*(['\"]?)(.*?)\\1\\s*$").matcher(value);
  if(!m.matches())return null;String url=absolute(base,m.group(2));if(url.isEmpty())return null;
  Source target=new Source(url,label,base);return target.kind==SourceKind.HOST||target.kind==SourceKind.MEDIA||target.kind==SourceKind.INTERMEDIARY?target:null;
 }
 private static String jsonString(String raw)throws Failure {
  StringBuilder out=new StringBuilder();try{for(int i=0;i<raw.length();i++){char c=raw.charAt(i);if(c!='\\'){if(c<32)throw new Exception();out.append(c);continue;}char escape=raw.charAt(++i);switch(escape){case '/':case '\\':case '"':out.append(escape);break;case 'b':out.append('\b');break;case 'f':out.append('\f');break;case 'n':out.append('\n');break;case 'r':out.append('\r');break;case 't':out.append('\t');break;case 'u':out.append((char)Integer.parseInt(raw.substring(i+1,i+5),16));i+=4;break;default:throw new Exception();}}return out.toString();}
  catch(Exception e){throw new Failure(Stage.RESOLVER,"STAYONLINE_JSON","Escape JSON non supportato nella destinazione");}
 }
 /** Small bounded JSON reader for a public response; independent of Android. */
 public static Object publicJson(String json)throws Failure{return new PublicJson(json).parse();}
 private static final class PublicJson {
  final String text;int pos,items;
  PublicJson(String s){text=s;}
  Failure bad(){return new Failure(Stage.RESOLVER,"STAYONLINE_JSON","StayOnline: risposta JSON non valida o oltre i limiti");}
  void space(){while(pos<text.length()&&Character.isWhitespace(text.charAt(pos)))pos++;}
  boolean take(char c){space();if(pos<text.length()&&text.charAt(pos)==c){pos++;return true;}return false;}
  Object parse()throws Failure{if(text.length()>2*1024*1024)throw bad();Object value=value(0);space();if(pos!=text.length())throw bad();return value;}
  String string()throws Failure{if(!take('"'))throw bad();int start=pos;while(pos<text.length()){char c=text.charAt(pos++);if(c=='"')return jsonString(text.substring(start,pos-1));if(c=='\\'){if(pos>=text.length())throw bad();pos++;}}throw bad();}
  Object value(int depth)throws Failure{
   if(depth>12||++items>10000)throw bad();space();if(pos>=text.length())throw bad();char c=text.charAt(pos);
   if(c=='"')return string();
   if(take('{')){Map<String,Object> result=new LinkedHashMap<>();if(take('}'))return result;do{String key=string();if(!take(':')||result.containsKey(key))throw bad();result.put(key,value(depth+1));if(take('}'))return result;}while(take(','));throw bad();}
   if(take('[')){List<Object> result=new ArrayList<>();if(take(']'))return result;do{result.add(value(depth+1));if(take(']'))return result;}while(take(','));throw bad();}
   int start=pos;while(pos<text.length()&&!Character.isWhitespace(text.charAt(pos))&&",]}".indexOf(text.charAt(pos))<0)pos++;String atom=text.substring(start,pos);
   if(atom.equals("null"))return null;if(atom.equals("true"))return Boolean.TRUE;if(atom.equals("false"))return Boolean.FALSE;
   if(atom.matches("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")){try{return Double.valueOf(atom);}catch(NumberFormatException e){throw bad();}}throw bad();
  }
 }
 private static Source hostEmbed(Page page,HostResolver resolver)throws Exception {
  LinkedHashMap<String,Source> candidates=new LinkedHashMap<>();Matcher frames=Pattern.compile("(?is)<iframe\\b[^>]*>").matcher(page.text());
  while(frames.find()){Map<String,String> a=attrs(frames.group());String url=absolute(page.url,first(a,"data-src","src"));if(url.isEmpty())continue;if(resolver instanceof Html5){if(knownHost(host(url))&&!maxDestination(host(url)))candidates.put(url,new Source(url,host(url),page.url));continue;}if(!resolver.matches(https(url)))continue;
   boolean same=host(url).equals(host(page.url)),maxHop=resolver instanceof MaxStream&&maxAlias(host(page.url))&&maxDestination(host(url));if(!same&&!maxHop)continue;
   // Only the actual same-site player embed, not download buttons or advertising frames.
   if(https(url).getPath().startsWith("/e/")||https(url).getPath().startsWith("/embed-")||maxHop&&https(url).getPath().matches("/(?:cast2|castv|video)/.*"))candidates.put(url,new Source(url,resolver.name(),page.url));
  }
  if(resolver instanceof MaxStream){Matcher links=Pattern.compile("(?is)<a\\b([^>]*)>(.*?)</a\\s*>").matcher(page.text());while(links.find()){
   String label=decode(links.group(2).replaceAll("<[^>]*>"," ").replaceAll("\\s+"," "));if(!label.matches("(?i)c(?:o|0)ntinue"))continue;
   String url=absolute(page.url,attrs(links.group(1)).getOrDefault("href",""));if(!url.isEmpty()&&(host(url).equals(host(page.url))||maxAlias(host(page.url))&&maxDestination(host(url))))candidates.put(url,new Source(url,resolver.name(),page.url));
  }}
  if(candidates.size()>1)throw new Failure(Stage.RESOLVER,"HOST_EMBED_AMBIGUOUS","Più embed video sullo stesso host");return candidates.isEmpty()?null:candidates.values().iterator().next();
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
 public static String sourceStatus(Source source){switch(source.kind){case MEDIA:return "Media dichiarato · da verificare";case HOST:return "Host riconosciuto · sorgente da risolvere";case INTERMEDIARY:return "Intermediario · ricerca destinazione pubblica";case TRAILER:return "Trailer / anteprima · escluso";default:return "Pagina esterna · compatibilità da verificare";}}
 public static boolean knownHost(String h){if(mixAlias(h)||wishAlias(h)||voeAlias(h)||doodAlias(h)||maxAlias(h)||h.equalsIgnoreCase("v.vidxgo.co"))return true;for(String name:new String[]{"mixdrop","maxstream","voe","streamtape","dood","doodstream","supervideo","dropload","dr0pstream","vidoza","streamta","animeworld","animesaturn","animeunity","vixcloud"})if(domain(h,name))return true;return false;}
 private static boolean mixAlias(String h){return h!=null&&h.toLowerCase(Locale.ROOT).matches("(?:[a-z0-9-]+\\.)*(?:mixdrop|m1xdrop|mxdrop|mixdroop|mixdrp|miixdrop|miiixdrop|md[3bfyz][a-z0-9]*)\\.[a-z]{2,24}");}
 private static boolean wishAlias(String h){return Arrays.asList("streamwish.com","streamwish.to","ajmidyad.sbs","khadhnayad.sbs","yadmalik.sbs","hayaatieadhab.sbs","kharabnahs.sbs","atabkhha.sbs","atabknha.sbs","atabknhk.sbs","atabknhs.sbs","abkrzkr.sbs","abkrzkz.sbs","wishembed.pro","mwish.pro","strmwis.xyz","awish.pro","dwish.pro","vidmoviesb.xyz","embedwish.com","cilootv.store","uqloads.xyz","tuktukcinema.store","doodporn.xyz","ankrzkz.sbs","volvovideo.top","streamwish.site","wishfast.top","ankrznm.sbs","sfastwish.com","eghjrutf.sbs","eghzrutw.sbs","playembed.online","egsyxurh.sbs","egtpgrvh.sbs","flaswish.com","obeywish.com","cdnwish.com","javsw.me","cinemathek.online","trgsfjll.sbs","fsdcmo.sbs","anime4low.sbs","mohahhda.site","ma2d.store","dancima.shop","swhoi.com","gsfqzmqu.sbs","jodwish.com","swdyu.com","strwish.com","asnwish.com","wishonly.site","playerwish.com","katomen.store","streamwish.fun","swishsrv.com","iplayerhls.com","hlsflast.com","4yftwvrdz7.sbs","ghbrisk.com","eb8gfmjn71.sbs","cybervynx.com","edbrdl7pab.sbs","stbhg.click","dhcplay.com","gradehgplus.com","ultpreplayer.com","hglink.to","haxloppd.com","streamwish.club","streamwish.cc","streamwish.biz","swish.site","wishon.site","vidwish.site","awish.top","dwish.top","mwish.top","streamwish.info","streamwish.net","streamwish.org","streamwish.live","streamwish.me","hanerix.com").contains(h.toLowerCase(Locale.ROOT).replaceFirst("^www\\.",""));}
 private static boolean voeAlias(String h){return Arrays.asList("jilliandescribecompany.com","christopheruntilpoint.com","walterprettytheir.com","crystaltreatmenteast.com","lauradaydo.com","lancewhosedifficult.com","dianaavoidthey.com","jefferycontrolmodel.com","charlestoughrace.com","richardquestionbuilding.com","jessicayeahcatch.com","juliewomanwish.com","rebeccapracticeloss.com","johnbeyondnation.com","jamesbornmain.com").contains(h.toLowerCase(Locale.ROOT).replaceFirst("^www\\.",""));}
 private static boolean doodAlias(String h){return Arrays.asList("dsvplay.com","myvidplay.com","playmogo.com","do7go.com","d000d.com","vide0.net").contains(h.toLowerCase(Locale.ROOT).replaceFirst("^www\\.",""));}
 private static boolean maxAlias(String h){return h.toLowerCase(Locale.ROOT).matches("(?:www\\.)?max(?:sun|mon)[0-9]{1,6}\\.site");}
 private static boolean maxDestination(String h){return domain(h,"maxstream")||maxAlias(h);}
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
 public static final class MixDrop extends PublicResolver {public MixDrop(){super("mixdrop","MixDrop");}public boolean matches(URI u){return super.matches(u)||mixAlias(u.getHost());}String prepare(String h)throws Failure{return unpackPublic(h);}String[] fields(){return new String[]{"MDCore\\.(?:wurl|vurl)\\s*=\\s*[\"']([^\"']+)[\"']"};}public Media parse(Page page,String referer)throws Exception{String url=it.carmine.streamplayer.CbMixdropConfig.extract(page.text(),page.url);if(!url.isEmpty())return new Media(url,mime(url),"",name(),tracks(page));return super.parse(page,referer);}}
 public static final class MaxStream extends PublicResolver {public MaxStream(){super("maxstream","MaxStream");}public boolean matches(URI u){return super.matches(u)||maxAlias(u.getHost());}
  String prepare(String h)throws Failure {
   Matcher scripts=Pattern.compile("(?is)<script\\b[^>]*>(.*?)</script\\s*>").matcher(h);StringBuilder out=new StringBuilder(h);int count=0;
   while(scripts.find()){String script=scripts.group(1);if(!script.contains(".split("))continue;if(++count>30)throw new Failure(Stage.RESOLVER,"PACKER_LIMIT","MaxStream: troppi script compattati");String decoded=unpackPublic(script);if(!decoded.equals(script))out.append('\n').append(decoded);}
   return count==0?unpackPublic(h):out.toString();
  }
  String[] fields(){return new String[]{"[\"']?(?:file|src)[\"']?\\s*[:=]\\s*[\"']([^\"'\\r\\n]+\\.(?:m3u8|mpd|mp4)(?:\\?[^\"'\\r\\n]*)?)[\"']"};}
  public Media parse(Page page,String referer)throws Exception {
   Matcher hls=Pattern.compile("(?is)<script\\b([^>]*)>(.*?)</script\\s*>").matcher(page.text());
   while(hls.find())if("m3u8".equals(attrs(hls.group(1)).get("id"))){Matcher url=Pattern.compile("https://[^\\s\"'<>#]+\\.m3u8(?:\\?[^\\s\"'<>#]*)?",Pattern.CASE_INSENSITIVE).matcher(hls.group(2));if(url.find())return new Media(https(decode(url.group())).toString(),HLS,page.url,name(),tracks(page));}
   Matcher publicHls=Pattern.compile("https://[^\\s\"'<>#]+\\.m3u8(?:\\?[^\\s\"'<>#]*)?",Pattern.CASE_INSENSITIVE).matcher(page.text());
   LinkedHashSet<String> candidates=new LinkedHashSet<>();while(publicHls.find())candidates.add(https(decode(publicHls.group())).toString());
   if(candidates.size()==1)return new Media(candidates.iterator().next(),HLS,page.url,name(),tracks(page));
   if(candidates.size()>1)throw new Failure(Stage.RESOLVER,"MAXSTREAM_HLS_AMBIGUOUS","MaxStream: più manifest pubblici fuori dal blocco player; impossibile scegliere con certezza");
   Matcher scripts=Pattern.compile("(?is)<script\\b([^>]*)>\\s*(https://[^<\\s]{1,8192})\\s*</script\\s*>").matcher(page.text());
   while(scripts.find())if("cccc".equals(attrs(scripts.group(1)).get("id"))){String url=absolute(page.url,scripts.group(2));if(!url.isEmpty())return new Media(url,mime(url),page.url,name(),tracks(page));}
   return super.parse(page,referer);
  }
 }
 /** Reads only literal XOR/base64 declarations already delivered by the public embed. No eval. */
 public static final class VidXGo extends PublicResolver {
  public VidXGo(){super("vidxgo","VidXGo");}public boolean matches(URI u){return "v.vidxgo.co".equalsIgnoreCase(u.getHost());}String[] fields(){return new String[0];}
  public Media parse(Page page,String referer)throws Exception {
   Matcher wrappers=Pattern.compile("var\\s+[A-Za-z_$][\\w$]*\\s*=\\s*(['\"])([^'\"]{1,128})\\1\\s*,\\s*d\\s*=\\s*atob\\(\\s*(['\"])([A-Za-z0-9+/=]{1,2800000})\\3\\s*\\)").matcher(page.text());
   Set<String> urls=new LinkedHashSet<>();int count=0;
   while(wrappers.find()){
    if(++count>30)throw new Failure(Stage.RESOLVER,"VIDXGO_SCRIPT_LIMIT","VidXGo: troppi script dichiarati");
    byte[] bytes;try{bytes=Base64.getDecoder().decode(wrappers.group(4));}catch(Exception e){throw new Failure(Stage.RESOLVER,"VIDXGO_SCRIPT_FORMAT","VidXGo: dichiarazione base64 non valida");}
    if(bytes.length>2*1024*1024)throw new Failure(Stage.RESOLVER,"VIDXGO_SCRIPT_LIMIT","VidXGo: script oltre il limite");String key=wrappers.group(2);
    for(int i=0;i<bytes.length;i++)bytes[i]=(byte)((bytes[i]&255)^key.charAt(i%key.length()));
    String decoded=new String(bytes,StandardCharsets.UTF_8);Matcher media=Pattern.compile("(['\"])(https:(?:\\\\/|/){2}[^'\"\\s<>]+?\\.m3u8(?:\\?[^'\"\\s<>]*)?)\\1",Pattern.CASE_INSENSITIVE).matcher(decoded);
    while(media.find()){String url=absolute(page.url,media.group(2));if(!url.isEmpty())urls.add(url);}
   }
   if(urls.size()>1)throw new Failure(Stage.RESOLVER,"VIDXGO_MEDIA_AMBIGUOUS","VidXGo: più playlist dichiarate; selezione non determinata");
   if(urls.isEmpty()){if(count==0)return super.parse(page,referer);throw new Failure(Stage.RESOLVER,"VIDXGO_NO_HLS","VidXGo: nessuna playlist HLS nei dichiarati script pubblici");}
   String id=https(page.url).getPath().replaceFirst("^/","").replaceFirst("/$","");if(!id.matches("(?:tt)?[0-9]{1,12}(?:/[0-9]{1,3}/[0-9]{1,4})?"))throw new Failure(Stage.RESOLVER,"VIDXGO_REFRESH_ID","VidXGo: identificativo per rinnovo non valido");
   return new Media(urls.iterator().next(),HLS,"https://v.vidxgo.co",name(),tracks(page),id);
  }
 }
 /** Restricts token replacement to the original CDN directory; keeps unrelated query parameters. */
 public static final class VidXTokens {
  private final URI initial;private final String directory;private final Map<String,String> tokens=new LinkedHashMap<>();public long due;
  public VidXTokens(String url,long now)throws Exception {initial=https(url);String path=initial.getRawPath();directory=path.substring(0,path.lastIndexOf('/')+1);Map<String,String> query=query(initial);for(String key:Arrays.asList("t","e","b"))if(query.containsKey(key))tokens.put(key,query.get(key));due=next(query.get("e"),now);}
  private static Map<String,String> query(URI uri)throws Exception {Map<String,String> out=new LinkedHashMap<>();if(uri.getRawQuery()!=null)for(String pair:uri.getRawQuery().split("&")){String[] kv=pair.split("=",2);String key=java.net.URLDecoder.decode(kv[0],"UTF-8"),value=kv.length==2?java.net.URLDecoder.decode(kv[1],"UTF-8"):"";if(out.put(key,value)!=null)throw new Exception("VIDXGO_QUERY_DUPLICATE");}return out;}
  private static long expiry(String raw)throws Exception{long value=Long.parseLong(raw);if(value<100000000000L)value=Math.multiplyExact(value,1000);return value;}
  private static long next(String raw,long now){try{return Math.max(now,expiry(raw)-30000);}catch(Exception e){return now+10000;}}
  public boolean scoped(String url){try{URI uri=https(url);return initial.getHost().equalsIgnoreCase(uri.getHost())&&uri.getRawPath().startsWith(directory)&&!uri.getPath().matches("(?s).*(?:^|/)\\.\\.?(?:/|$).*");}catch(Exception e){return false;}}
  public void update(String url,long now)throws Exception {
   URI uri=https(url);if(!initial.getHost().equalsIgnoreCase(uri.getHost())||!initial.getRawPath().equals(uri.getRawPath()))throw new Exception("VIDXGO_REFRESH_SCOPE_CHANGED");Map<String,String> q=query(uri);
   if(!q.containsKey("t")||q.get("t").isEmpty()||!q.containsKey("e"))throw new Exception("VIDXGO_REFRESH_TOKEN_MISSING");if(expiry(q.get("e"))<=now)throw new Exception("VIDXGO_REFRESH_EXPIRED");
   tokens.clear();for(String key:Arrays.asList("t","e","b"))if(q.containsKey(key))tokens.put(key,q.get(key));due=next(q.get("e"),now);
  }
  public String rewrite(String url)throws Exception {
   if(!scoped(url))return url;URI uri=https(url);query(uri);StringJoiner out=new StringJoiner("&");
   if(uri.getRawQuery()!=null&&!uri.getRawQuery().isEmpty())for(String pair:uri.getRawQuery().split("&",-1)){String key=java.net.URLDecoder.decode(pair.split("=",2)[0],"UTF-8");if(!Arrays.asList("t","e","b").contains(key))out.add(pair);}
   for(Map.Entry<String,String> entry:tokens.entrySet())out.add(java.net.URLEncoder.encode(entry.getKey(),"UTF-8")+"="+java.net.URLEncoder.encode(entry.getValue(),"UTF-8"));String query=out.toString();return uri.getScheme()+"://"+uri.getRawAuthority()+uri.getRawPath()+(query.isEmpty()?"":"?"+query);
  }
 }
 public static final class Voe extends PublicResolver {public Voe(){super("voe","VOE");}public boolean matches(URI u){return super.matches(u)||voeAlias(u.getHost());}String prepare(String h){
   Matcher m=Pattern.compile("[\"']?(hls|mp4)[\"']?\\s*:\\s*(?:atob\\(\\s*)?[\"']([A-Za-z0-9+/=]{20,})[\"']\\s*\\)?").matcher(h);StringBuffer out=new StringBuffer();while(m.find()){try{String decoded=new String(Base64.getDecoder().decode(m.group(2)),StandardCharsets.UTF_8);https(decoded);m.appendReplacement(out,Matcher.quoteReplacement(m.group(1)+":\""+decoded+"\""));}catch(Exception e){m.appendReplacement(out,Matcher.quoteReplacement(m.group()));}}m.appendTail(out);return out.toString();
  }String[] fields(){return new String[]{key("hls"),key("mp4")};}
  public Media parse(Page page,String referer)throws Exception{
   Matcher json=Pattern.compile("(?is)<script\\b[^>]*type=['\"]application/json['\"][^>]*>(.*?)</script>").matcher(page.text());
   while(json.find())try{String encoded=json.group(1).trim();if(encoded.length()>2000000)continue;if(encoded.startsWith("\"")){Object value=new PublicJson(encoded).parse();if(!(value instanceof String))continue;encoded=(String)value;}StringBuilder rot=new StringBuilder();for(char c:encoded.toCharArray())rot.append(c>='a'&&c<='z'?(char)('a'+(c-'a'+13)%26):c>='A'&&c<='Z'?(char)('A'+(c-'A'+13)%26):c);String cleaned=rot.toString();for(String marker:new String[]{"@$","^^","~@","%?","*~","!!","#&"})cleaned=cleaned.replace(marker,"");cleaned=cleaned.replace("_","");String first=new String(Base64.getDecoder().decode(cleaned),StandardCharsets.UTF_8);StringBuilder shifted=new StringBuilder();for(char c:first.toCharArray())shifted.append((char)(c-3));String decoded=new String(Base64.getDecoder().decode(shifted.reverse().toString()),StandardCharsets.UTF_8);Object data=new PublicJson(decoded).parse();if(!(data instanceof Map))continue;Object value=((Map<?,?>)data).get("source");if(value instanceof String){String url=absolute(page.url,(String)value);if(!url.isEmpty())return new Media(url,mime(url),page.url,name(),tracks(page));}}catch(Exception ignored){}
   return super.parse(page,referer);
  }
 }

 public static final class StreamTape extends PublicResolver {
  public StreamTape(){super("streamtape","StreamTape");}public boolean matches(URI u){return super.matches(u)||domain(u.getHost(),"streamta");}
  String[] fields(){return new String[]{"id=[\"'](?:robotlink|videolink|botlink)[\"'][^>]*>\\s*(https?://[^<]+|//[^<]+)<"};}
  public Media parse(Page page,String referer)throws Exception{
   Matcher m=Pattern.compile("getElementById\\(['\"](?:botlink|robotlink|videolink)['\"]\\)\\.innerHTML\\s*=\\s*['\"]([^'\"]+)['\"]\\s*\\+\\s*\\(?['\"]([^'\"]+)['\"]\\)?\\.substring\\((\\d{1,3})\\)").matcher(page.text());
   if(m.find()){int from=Integer.parseInt(m.group(3));if(from>m.group(2).length())throw new Failure(Stage.RESOLVER,"STREAMTAPE_CONFIG","Substring non valido");String url=absolute(page.url,m.group(1)+m.group(2).substring(from));if(!url.isEmpty()&&domain(host(url),"streamtape")&&https(url).getPath().equals("/get_video"))return new Media(url,MP4,page.url,name(),tracks(page));}
   return super.parse(page,referer);
  }
 }

 public static final class DoodStream extends PublicResolver {public DoodStream(){super("dood","DoodStream");}public boolean matches(URI u){return super.matches(u)||domain(u.getHost(),"doodstream")||doodAlias(u.getHost());}String[] fields(){return new String[0];}}
 public static final class SuperVideo extends PublicResolver {public SuperVideo(){super("supervideo","SuperVideo");}String prepare(String h)throws Failure{return unpackPublic(h);}String[] fields(){return new String[]{key("file"),key("src")};}}
 public static final class Dropload extends PublicResolver {public Dropload(){super("dropload","Dropload");}public boolean matches(URI u){return super.matches(u)||domain(u.getHost(),"dr0pstream");}String prepare(String h)throws Failure{return unpackPublic(h);}String[] fields(){return new String[]{key("file"),key("src")};}public Media parse(Page page,String referer)throws Exception{Media m=super.parse(page,referer);if(https(m.url).getPath().endsWith("/small3.mp4"))throw new Failure(Stage.RESOLVER,"PLACEHOLDER_VIDEO","Dropload: video segnaposto escluso");return new Media(m.url,m.mime,origin(page.url)+"/",m.host,m.subtitles);}}
 public static final class AnimeWorld implements HostResolver {public String name(){return "AnimeWorld";}public boolean matches(URI uri){return domain(uri.getHost(),"animeworld");}public Media parse(Page page,String referer)throws Exception{Object json=publicJson(page.text());Object value=json instanceof Map?((Map<?,?>)json).get("grabber"):null;if(!(value instanceof String))throw new Failure(Stage.RESOLVER,"ANIMEWORLD_MEDIA","AnimeWorld: risposta senza grabber");String url=absolute(page.url,(String)value);if(url.isEmpty())throw new Failure(Stage.RESOLVER,"ANIMEWORLD_MEDIA","AnimeWorld: URL non valido");return new Media(url,mime(url),referer,name(),Collections.emptyList());}}
 public static final class StreamWish extends PublicResolver {public StreamWish(){super("streamwish","StreamWish");}public boolean matches(URI uri){return super.matches(uri)||wishAlias(uri.getHost());}String prepare(String html)throws Failure{return unpackPublic(html);}String[] fields(){return new String[]{key("hls2"),key("file")};}public Media parse(Page page,String referer)throws Exception{Media media=super.parse(page,referer);return new Media(media.url,media.mime,origin(page.url)+"/",name(),media.subtitles);}}
 public static final class VixCloud implements HostResolver {public String name(){return "VixCloud";}public boolean matches(URI uri){return domain(uri.getHost(),"vixcloud");}public Media parse(Page page,String referer)throws Exception{return new Media(it.carmine.streamplayer.ScPlaylistConfig.playlist(page.text(),page.url),HLS,origin(page.url)+"/",name(),tracks(page));}}
 public static final class Vidoza extends PublicResolver {public Vidoza(){super("vidoza","Vidoza");}String[] fields(){return new String[]{key("src")};}}
 public static final class Html5 extends PublicResolver {public Html5(){super("","HTML5 pubblico");}public boolean matches(URI u){return true;}String[] fields(){return new String[0];}}
 public static List<Subtitle> tracks(Page page){List<Subtitle> out=new ArrayList<>();Matcher m=Pattern.compile("(?is)<track\\b[^>]{0,8192}>").matcher(page.text());while(m.find()&&out.size()<30){Map<String,String> a=attrs(m.group());String kind=a.getOrDefault("kind","");if(!kind.equals("subtitles")&&!kind.equals("captions"))continue;String u=absolute(page.url,a.getOrDefault("src",""));if(u.isEmpty())continue;String path=URI.create(u).getPath().toLowerCase(Locale.ROOT);String mime=path.endsWith(".vtt")?"text/vtt":path.endsWith(".srt")?"application/x-subrip":path.endsWith(".ttml")?"application/ttml+xml":"";if(!mime.isEmpty())out.add(new Subtitle(u,a.getOrDefault("srclang","und"),a.getOrDefault("label","Sottotitoli"),mime));}String html=page.text();try{html=unpackPublic(html);}catch(Exception ignored){}Matcher tracks=Pattern.compile("(?is)tracks\\s*:\\s*\\[(.{0,32768}?)\\]").matcher(html);while(tracks.find()){Matcher obj=Pattern.compile("\\{([^{}]{1,4096})\\}").matcher(tracks.group(1));while(obj.find()&&out.size()<30){String block=obj.group(1);Matcher kind=Pattern.compile(key("kind")).matcher(block);if(!kind.find()||!Arrays.asList("captions","subtitles").contains(kind.group(1)))continue;Matcher file=Pattern.compile(key("file")).matcher(block);if(!file.find())continue;String url=absolute(page.url,file.group(1));String type=url.contains(".vtt")?"text/vtt":url.contains(".srt")?"application/x-subrip":"";if(url.isEmpty()||type.isEmpty())continue;Matcher label=Pattern.compile(key("label")).matcher(block);out.add(new Subtitle(url,"und",label.find()?label.group(1):"Sottotitoli",type));}}return out;}
 /** Decode a bounded, literal public packer payload. Never evaluate remote code. */
 public static String unpackPublic(String html)throws Failure {
  String lit="('(?:\\\\.|[^'\\\\])*'|\"(?:\\\\.|[^\"\\\\])*\")";Matcher m=Pattern.compile("\\}\\s*\\(\\s*"+lit+"\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*"+lit+"\\s*\\.split\\(\\s*['\"]\\|['\"]\\s*\\)",Pattern.DOTALL).matcher(html);
  StringBuilder result=new StringBuilder(html);int count=0;
  while(m.find()){if(++count>12)throw new Failure(Stage.RESOLVER,"PACKER_LIMIT","Troppi script compattati");try{String payload=it.carmine.streamplayer.CbMixdropConfig.unquote(m.group(1));if(payload.length()>1000000)throw new Exception();result.append('\n').append(it.carmine.streamplayer.CbMixdropConfig.unpack(payload,Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)),it.carmine.streamplayer.CbMixdropConfig.unquote(m.group(4))));}catch(Exception e){throw new Failure(Stage.RESOLVER,"PACKER_FORMAT","Formato packer pubblico non supportato");}}
  return result.toString();
 }
 private static String baseCode(int value,int radix){String digits="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";if(value==0)return "0";StringBuilder out=new StringBuilder();while(value>0){out.append(digits.charAt(value%radix));value/=radix;}return out.reverse().toString();}
 private static String literal(String s){return s.replace("\\'","'").replace("\\\"","\"").replace("\\/","/").replace("\\\\","\\");}
 public static CatalogAdapter adapter(String kind)throws Failure {
  switch(kind){case "animeunity":return new AnimeUnityCatalog();case "streamingita":return new StreamingItaCatalog();case "animeworld":return new AnimeCatalog(false);case "animesaturn":return new AnimeCatalog(true);case "guardaflix":return new ItalianCatalog(false);case "guardaserie":return new ItalianCatalog(true);case "eurostreaming":return new EuroCatalog();case "cb01":return new Cb01Catalog();case "altadefinizione":return new AltaCatalog();case "wordpress":return new WordpressCatalog();case "dle":return new DleCatalog();default:throw new Failure(Stage.CATALOG,"UNSUPPORTED_CATALOG","Selezionare un adattatore LAB: CB01, AltaDefinizione, WordPress o DLE");}
 }
 private abstract static class HtmlCatalog implements CatalogAdapter {
  abstract Pattern blocks();
  public List<Title> titles(Page page){LinkedHashMap<String,Title> out=new LinkedHashMap<>();Matcher blocks=blocks().matcher(page.text());while(blocks.find()&&out.size()<100){String body=blocks.group();Matcher anchors=Pattern.compile("(?is)<a\\b[^>]*>").matcher(body);Matcher img=Pattern.compile("(?is)<img\\b[^>]*>").matcher(body);if(!img.find())continue;Map<String,String> a=attrs(img.group());String poster=absolute(page.url,a.getOrDefault("data-src",a.getOrDefault("data-original",a.getOrDefault("src",""))));String title=a.getOrDefault("alt",a.getOrDefault("title",""));String url="";while(anchors.find()){Map<String,String> link=attrs(anchors.group());String candidate=absolute(page.url,link.getOrDefault("href",""));if(!candidate.isEmpty()&&host(candidate).equals(host(page.url))&&!candidate.equals(page.url)&&!URI.create(candidate).getPath().matches(".*/(?:category|tag|author)/.*")){url=candidate;if(title.isEmpty())title=link.getOrDefault("title","");break;}}
   if(title.isEmpty()){Matcher h=Pattern.compile("(?is)<h[1-4]\\b[^>]*>(.*?)</h[1-4]>").matcher(body);if(h.find())title=h.group(1).replaceAll("<[^>]*>"," ").trim();}
   if(!url.isEmpty()&&!poster.isEmpty()&&title.length()>1)out.putIfAbsent(url,new Title(decode(title),poster,url));}return new ArrayList<>(out.values());}
  public List<Source> sources(Page page)throws Exception{return declaredSources(page);}
 }
 public static final class EuroCatalog extends HtmlCatalog {
  public String name(){return "EuroStreaming · film e VixSrc";}
  Pattern blocks(){return Pattern.compile("(?is)<(?:div|article)\\b[^>]*class=[\"'][^\"']*\\bpost-thumb\\b[^\"']*[\"'][^>]*>.*?(?=<(?:div|article)\\b[^>]*class=[\"'][^\"']*\\bpost-thumb\\b|$)");}
  public String search(String b,String q)throws Exception{return origin(b)+"/search?type=movie&q="+java.net.URLEncoder.encode(q,"UTF-8");}
  public List<Title> titles(Page page){List<Title> out=new ArrayList<>();for(Title t:super.titles(page))if(URI.create(t.url).getPath().matches("/detail/film-[1-9][0-9]{0,9}-[^/]+"))out.add(t);return out;}
  public List<Source> sources(Page page)throws Exception {
   List<Source> all=declaredSources(page);Matcher id=Pattern.compile("^/detail/film-([1-9][0-9]{0,9})-[^/]+/?$").matcher(https(page.url).getPath());
   if(!id.matches())throw new Failure(Stage.HOST_SEARCH,"EURO_FILM_ID_MISSING","Scheda EuroStreaming senza identificativo film TMDB; serie ed episodi non ancora integrati");
   String expected=id.group(1);boolean embed=false;List<Source> out=new ArrayList<>();
   for(Source source:all){URI u=https(source.url);if("vixsrc.to".equalsIgnoreCase(u.getHost())){if(u.getPath().equals("/movie/"+expected)||u.getPath().equals("/api/movie/"+expected))embed=true;else throw new Failure(Stage.HOST_SEARCH,"EURO_VIX_ID_CONFLICT","L'identificativo dell'embed VixSrc non coincide con la scheda del titolo");}else out.add(source);}
   if(!embed)throw new Failure(Stage.HOST_SEARCH,"EURO_VIX_EMBED_MISSING","Nessun embed pubblico VixSrc associato al film; nessuna fonte inventata");
   out.add(0,new Source("https://vixsrc.to/api/movie/"+expected+"?lang=it","Diretto · VixSrc · resolver R. ITA",page.url));return out;
  }
 }
 public static final class Cb01Catalog extends HtmlCatalog {public String name(){return "CB01 · articoli";}Pattern blocks(){return Pattern.compile("(?is)<article\\b[^>]*>.*?</article>|<(?:div|section)\\b[^>]*class=[\"'][^\"']*\\bcard(?:-body)?\\b[^\"']*[\"'][^>]*>.*?(?=<(?:div|section)\\b[^>]*class=[\"'][^\"']*\\bcard\\b|$)");}public String search(String b,String q)throws Exception{return origin(b)+"/?s="+java.net.URLEncoder.encode(q,"UTF-8");}}
 public static final class AltaCatalog extends HtmlCatalog {
  public String name(){return "AltaDefinizione · locandine e schede DLE";}
  Pattern blocks(){return Pattern.compile("(?is)<article\\b[^>]*>.*?</article>|<div\\b[^>]*class=[\"'][^\"']*(?:shortstory|short-item|movie-item|box-film|boxgrid)[^\"']*[\"'][^>]*>.*?(?=<div\\b[^>]*class=[\"'][^\"']*(?:shortstory|short-item|movie-item|box-film|boxgrid)|$)");}
  public List<Title> titles(Page page){
   LinkedHashMap<String,Title> out=new LinkedHashMap<>();
   Matcher cards=Pattern.compile("(?is)<div\\b(?=[^>]*\\bdata-link\\s*=)([^>]{1,8192})>(.*?)(?=<div\\b[^>]*\\bdata-link\\s*=|$)").matcher(page.text());
   while(cards.find()&&out.size()<100){Map<String,String> a=attrs(cards.group(1));if(!a.getOrDefault("class","").matches("(?s)(?:.*\\s)?movie(?:\\s.*)?"))continue;String url=absolute(page.url,a.getOrDefault("data-link","")),title=a.getOrDefault("data-title","");if(url.isEmpty()||!host(url).equals(host(page.url))||title.length()<2)continue;
    String body=cards.group(2);if(body.length()>50000)body=body.substring(0,50000);Matcher image=Pattern.compile("(?is)<img\\b[^>]{0,8192}>").matcher(body);String poster="";while(image.find()){Map<String,String> img=attrs(image.group());if(!img.getOrDefault("class","").matches("(?s)(?:.*\\s)?layer-image(?:\\s.*)?"))continue;poster=absolute(page.url,first(img,"data-src","data-original","src"));break;}if(!poster.isEmpty())out.putIfAbsent(url,new Title(decode(title),poster,url));
   }
   // Current public layout: poster anchors carry data-title/data-id; hero logos are not posters.
   for(int pass=0;pass<2;pass++){
    Matcher anchors=Pattern.compile("(?is)<a\\b([^>]*)>(.*?)</a\\s*>").matcher(page.text());
    while(anchors.find()&&out.size()<100){Map<String,String> a=attrs(anchors.group(1));if(pass==0&&!a.containsKey("data-title"))continue;
     String url=absolute(page.url,a.getOrDefault("href",""));if(url.isEmpty()||!host(url).equals(host(page.url)))continue;
     if(!URI.create(url).getPath().matches(".*/[0-9]+-[^/]+\\.html"))continue;
     Matcher images=Pattern.compile("(?is)<img\\b[^>]*>").matcher(anchors.group(2));if(!images.find())continue;Map<String,String> image=attrs(images.group());
     String poster=absolute(page.url,first(image,"data-src","data-original","src"));
     String title=first(a,"data-title");if(title.isEmpty())title=first(image,"alt","title");
     String lower=poster.toLowerCase(Locale.ROOT);if(poster.isEmpty()||title.length()<2||lower.contains("/logo")||lower.contains("/original/")||lower.contains("/w780/")||lower.contains("/w1280/"))continue;
     out.putIfAbsent(url,new Title(decode(title),poster,url));
    }
   }
   if(out.isEmpty())return super.titles(page);return new ArrayList<>(out.values());
  }
  public String search(String b,String q)throws Exception{if(host(b).startsWith("altadefinizione-01"))return origin(b)+(q.isEmpty()?"/cinema/":"/index.php?do=search&subaction=search&story="+java.net.URLEncoder.encode(q,"UTF-8"));return origin(b)+"/search?q="+java.net.URLEncoder.encode(q,"UTF-8");}
  public List<Source> sources(Page page){
   List<Source> result=new ArrayList<>();Set<String> originRefs=new HashSet<>();Matcher frames=Pattern.compile("(?is)<iframe\\b[^>]*>").matcher(page.text());
   while(frames.find()){Map<String,String> a=attrs(frames.group());if(!"origin".equalsIgnoreCase(a.getOrDefault("referrerpolicy","")))continue;String u=absolute(page.url,first(a,"data-src","src"));if(!u.isEmpty())originRefs.add(u);}
   Set<String> imdbIds=new LinkedHashSet<>();
   Matcher declaredImdb=Pattern.compile("\\bvar\\s+imdb\\s*=\\s*['\"](?:tt)?([0-9]{7,12})['\"]").matcher(page.text());while(declaredImdb.find())imdbIds.add(declaredImdb.group(1));
   for(Source source:declaredSources(page)){String ref=source.referer;if(originRefs.contains(source.url))try{ref=origin(page.url)+"/";}catch(Exception ignored){}result.add(new Source(source.url,source.label,ref));
    if(source.kind!=SourceKind.TRAILER&&host(source.url).equals("v.vidxgo.co")){Matcher id=Pattern.compile("^/(?:tt)?([0-9]{7,12})/?$").matcher(URI.create(source.url).getPath());if(id.find())imdbIds.add(id.group(1));}
   }
   if(imdbIds.size()==1&&!URI.create(page.url).getPath().contains("/serie-tv/"))result.add(0,new Source("https://vixsrc.to/api/movie/tt"+imdbIds.iterator().next()+"?lang=it","Diretto · VixSrc · verifica disponibilità",page.url));return result;
  }
 }
 public static final class WordpressCatalog extends HtmlCatalog {public String name(){return "WordPress · articoli";}Pattern blocks(){return Pattern.compile("(?is)<article\\b[^>]*>.*?</article>");}public String search(String b,String q)throws Exception{return origin(b)+"/?s="+java.net.URLEncoder.encode(q,"UTF-8");}}
 public static final class DleCatalog extends HtmlCatalog {public String name(){return "DLE · shortstory";}Pattern blocks(){return new AltaCatalog().blocks();}public String search(String b,String q)throws Exception{return origin(b)+"/index.php?do=search&subaction=search&story="+java.net.URLEncoder.encode(q,"UTF-8");}}
}
