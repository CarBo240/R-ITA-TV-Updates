package it.carmine.streamplayer;

import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

/** Resolves public pages afresh; no media host, playlist URL or channel IDs are bundled. */
final class GomstreamSource {
    static final String HOME="https://gomstream.xyz";
    static final class Channel {
        final String id,name;
        Channel(String id,String name){this.id=id;this.name=name;}
        String category(){String s=name.toLowerCase(Locale.ROOT);return s.contains("cinema")?"Cinema":sport(s)?"Sport":"TV";}
        private boolean sport(String s){return s.contains("sport")||s.contains("calcio")||s.contains("dazn")||s.contains("motogp")||s.contains("basket")||s.contains("tennis");}
    }
    static final class Page {final String url,body;Page(String u,String b){url=u;body=b;}}
    interface Transport {Page get(String url)throws IOException;}
    static final Transport HTTP=url->{
        HttpURLConnection c=(HttpURLConnection)new URL(https(url)).openConnection();
        c.setConnectTimeout(12000);c.setReadTimeout(25000);c.setRequestProperty("User-Agent","R-ITA-TV/1.12");c.setRequestProperty("Accept","*/*");
        try{int code=c.getResponseCode();if(code!=200)throw new IOException("HTTP "+code);try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();if(out.size()+n>8*1024*1024)throw new IOException("Pagina troppo grande");out.write(b,0,n);}return new Page(https(c.getURL().toString()),out.toString("UTF-8"));}}finally{c.disconnect();}
    };
    static List<Channel> parseCatalog(String json)throws JSONException {
        List<Channel> out=new ArrayList<>();Set<String> ids=new HashSet<>();
        for(DaddyLiveSource.Item item:DaddyLiveSource.parseChannels(json)){
            if(!Pattern.compile("\\b(italy|italia|italian|it)\\b",Pattern.CASE_INSENSITIVE).matcher(item.title).find())continue;
            String id=item.links.get(0).id;if(!id.matches("[0-9]{1,8}")||!ids.add(id))continue;
            out.add(new Channel(id,item.title));
        }return out;
    }
    static List<Channel> catalog(String daddyHome,Transport http)throws IOException,JSONException {
        List<Channel> list=parseCatalog(http.get(daddyHome+"/cache/channels.json").body);
        if(list.isEmpty())throw new IOException("Il catalogo non contiene canali italiani");return list;
    }
    static String sourcePage(String html,String gomHome)throws IOException {
        Matcher m=Pattern.compile("(?:const|let|var)\\s+PLAYERS\\s*=\\s*(\\[.*?\\])\\s*;",Pattern.DOTALL).matcher(html);
        if(!m.find())throw new IOException("Elenco player del sito cambiato");
        try{JSONArray a=new JSONArray(m.group(1));String wanted=new URI(gomHome).getHost();
            for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;String src=o.optString("src","");String h=new URI(src).getHost();if(h!=null&&(h.equalsIgnoreCase(wanted)||h.toLowerCase(Locale.ROOT).contains("gomstream")))return https(src);}
        }catch(JSONException|URISyntaxException e){throw new IOException("Elenco player non leggibile",e);}
        throw new IOException("Gomstream non presente tra le fonti del canale");
    }
    static String playlist(Page p)throws IOException {
        Matcher m=Pattern.compile("(?:const|let|var)\\s+SRC\\s*=\\s*([\"'])([^\"'\\r\\n]+)\\1").matcher(p.body);
        if(!m.find())return null;
        String value=m.group(2).replace("\\/","/").replace("&amp;","&");
        if(!value.toLowerCase(Locale.ROOT).contains(".m3u8"))throw new IOException("Formato video della fonte cambiato");
        return https(URI.create(p.url).resolve(value).toString());
    }
    static List<String> frames(Page p)throws IOException {
        List<String> result=new ArrayList<>();Matcher m=Pattern.compile("<iframe\\b[^>]*\\bsrc\\s*=\\s*[\"']([^\"']+)[\"']",Pattern.CASE_INSENSITIVE).matcher(p.body);
        while(m.find()&&result.size()<6){try{String u=https(URI.create(p.url).resolve(m.group(1).replace("&amp;","&")).toString());if(!result.contains(u))result.add(u);}catch(Exception ignored){}}
        return result;
    }
    static List<String> playerPages(String html)throws IOException {
        Matcher m=Pattern.compile("(?:const|let|var)\\s+PLAYERS\\s*=\\s*(\\[.*?\\])\\s*;",Pattern.DOTALL).matcher(html);
        if(!m.find())throw new IOException("Elenco player del sito cambiato");List<String> pages=new ArrayList<>();
        try{JSONArray a=new JSONArray(m.group(1));for(int i=0;i<Math.min(8,a.length());i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;try{String src=https(o.optString("src",""));if(!pages.contains(src))pages.add(src);}catch(IOException ignored){}}}catch(JSONException e){throw new IOException("Elenco player non leggibile",e);}return pages;
    }
    static boolean marked(Page page){return Pattern.compile("(?:const|let|var)\\s+MARK\\s*=\\s*[\"']gomstream[\"']",Pattern.CASE_INSENSITIVE).matcher(page.body).find();}
    static String resolve(Channel channel,String daddyHome,String gomHome,Transport http)throws IOException {
        if(!channel.id.matches("[0-9]{1,8}"))throw new IOException("Canale non valido");
        List<String> pages=new ArrayList<>();String preferred=null;IOException failure=null;
        try{Page entry=http.get(daddyHome+"/player/embed.php?id="+channel.id);pages=playerPages(entry.body);
            try{preferred=sourcePage(entry.body,gomHome);pages.remove(preferred);pages.add(0,preferred);}catch(IOException ignored){}
        }catch(IOException e){failure=e;}
        String configured=https(gomHome+"/live/stream-"+channel.id+".php");
        pages.remove(configured);if(!HOME.equals(gomHome))pages.add(0,configured);else pages.add(configured);
        for(String url:pages){
            try{Page player=http.get(url);boolean known=url.equals(preferred)||url.equals(configured),brand=false;
                for(int depth=0;depth<3;depth++){
                    brand|=marked(player);String direct=playlist(player);
                    if(direct!=null){if(known||brand)return direct;break;}
                    List<String> frames=frames(player);if(frames.isEmpty())break;
                    player=http.get(frames.get(0));
                }
            }catch(IOException e){failure=e;}
        }
        throw new IOException("Gomstream non disponibile o formato del sito cambiato",failure);
    }
    static String https(String value)throws IOException {
        try{URI u=new URI(value);if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null)throw new IOException("Indirizzo video non HTTPS");return u.toASCIIString();}catch(URISyntaxException e){throw new IOException("Indirizzo non valido",e);}
    }
}
