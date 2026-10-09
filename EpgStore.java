package it.carmine.streamplayer;

import android.content.Context;
import android.util.Xml;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/** XMLTV guide. Feed data is cached locally; matching uses explicit names/aliases. */
public final class EpgStore {
    public static final String DEFAULT_URL="https://raw.githubusercontent.com/Belfagor2005/vavoo-player/master/epg_it.xml";
    private static final String MAP_URL="https://raw.githubusercontent.com/OwnerPlugins/vavoo/main/epg-channel-db/vavoo_channels_it.json";
    private final Context context;
    private final Map<String,String> ids=new HashMap<>(),logos=new HashMap<>();
    private final Map<String,List<Programme>> programmes=new HashMap<>();
    public long latestEnd=0;
    public static final class Programme {
        public final long start,end;public final String title,description,image;
        Programme(long s,long e,String t,String d){this(s,e,t,d,"");}
        Programme(long s,long e,String t,String d,String i){start=s;end=e;title=t;description=d;image=i;}
    }
    public EpgStore(Context c){context=c.getApplicationContext();}
    public static String normalize(String name){
        String s=Normalizer.normalize(name==null?"":name.trim(),Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT);
        s=s.replaceAll("\\.[scb]$","").replaceAll("\\b(full hd|fhd|hd|sd|uhd)\\b","");
        // Keep + and numbers: Rai 1 and Rai 2, and time-shift channels, must remain distinct.
        return s.replaceAll("[^a-z0-9+]","");
    }
    public static long xmltvTime(String raw)throws ParseException {
        String s=raw.trim();SimpleDateFormat format=new SimpleDateFormat(s.length()>14?"yyyyMMddHHmmss Z":"yyyyMMddHHmmss",Locale.US);
        format.setLenient(false);format.setTimeZone(TimeZone.getTimeZone("UTC"));return format.parse(s).getTime();
    }
    private File fetch(String url,String name,boolean force)throws Exception {
        File f=new File(context.getCacheDir(),name);File tag=new File(context.getCacheDir(),name+".source");
        boolean same=false;
        if(tag.isFile())try(BufferedReader reader=new BufferedReader(new FileReader(tag))){same=url.equals(reader.readLine());}
        if(!force&&same&&f.isFile()&&System.currentTimeMillis()-f.lastModified()<6*3600000L)return f;
        URI uri=new URI(url);
        if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null)throw new IOException("La fonte EPG deve essere un URL HTTPS");
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(25000);
        File tmp=new File(context.getCacheDir(),name+".partial");
        try{
            if(c.getResponseCode()!=200)throw new IOException("Fonte EPG: HTTP "+c.getResponseCode());
            long size=0;
            try(InputStream in=c.getInputStream();OutputStream out=new FileOutputStream(tmp)){
                byte[] b=new byte[16384];int n;
                while((n=in.read(b))!=-1){if(Thread.currentThread().isInterrupted())throw new IOException("Download annullato");
                    size+=n;if(size>60*1024*1024)throw new IOException("Fonte EPG troppo grande");out.write(b,0,n);}
            }
            if(size==0)throw new IOException("Fonte EPG vuota");
            if(!tmp.renameTo(f))throw new IOException("Impossibile salvare la guida");
            try(FileWriter out=new FileWriter(tag)){out.write(url+"\n");}return f;
        }catch(Exception e){tmp.delete();if(same&&f.isFile())return f;throw e;}finally{c.disconnect();}
    }
    public void load(String source,boolean force)throws Exception {
        File xml=fetch(source,"epg-it.xml",force);
        try(BufferedInputStream in=new BufferedInputStream(new FileInputStream(xml))){
            in.mark(2);int a=in.read(),b=in.read();in.reset();
            if(a==0x1f&&b==0x8b){try(InputStream gz=new java.util.zip.GZIPInputStream(in)){parse(gz,System.currentTimeMillis());}}
            else parse(in,System.currentTimeMillis());
        }
        try {
            File mapping=fetch(MAP_URL,"epg-map-it.json",force);
            ByteArrayOutputStream b=new ByteArrayOutputStream();
            try(InputStream in=new FileInputStream(mapping)){byte[] data=new byte[4096];int n;while((n=in.read(data))!=-1)b.write(data,0,n);}
            addAliases(new JSONObject(b.toString("UTF-8")));
        }catch(Exception ignored){ /* Display names from XMLTV remain available. */ }
    }
    void addAliases(JSONObject mapping)throws Exception {
        Iterator<String> it=mapping.keys();while(it.hasNext()){String name=it.next();String id=mapping.optString(name,"");
            if(programmes.containsKey(id))ids.put(normalize(name),id);}
    }
    void parse(InputStream in,long now)throws Exception {
        ids.clear();logos.clear();programmes.clear();latestEnd=0;
        XmlPullParser p=Xml.newPullParser();p.setInput(in,"UTF-8");
        String channelId=null,programmeId=null,title="",description="",image="";long start=0,end=0;
        boolean validProgramme=false;int count=0;
        Map<String,String> displayIds=new HashMap<>();Set<String> ambiguous=new HashSet<>();
        for(int event=p.getEventType();event!=XmlPullParser.END_DOCUMENT;event=p.next()){
            if(Thread.currentThread().isInterrupted())throw new IOException("Lettura annullata");
            if(event==XmlPullParser.START_TAG){String tag=p.getName();
                if("channel".equals(tag)){channelId=p.getAttributeValue(null,"id");}
                else if("display-name".equals(tag)&&channelId!=null){String name=normalize(p.nextText());
                    String previous=displayIds.put(name,channelId);if(previous!=null&&!previous.equals(channelId))ambiguous.add(name);}
                else if("programme".equals(tag)){
                    programmeId=p.getAttributeValue(null,"channel");title="";description="";image="";validProgramme=false;count++;
                    try{start=xmltvTime(p.getAttributeValue(null,"start"));end=xmltvTime(p.getAttributeValue(null,"stop"));
                        latestEnd=Math.max(latestEnd,end);validProgramme=programmeId!=null&&end>start&&end>now-6*3600000L&&start<now+48*3600000L;
                    }catch(Exception ignored){}
                }else if("icon".equals(tag)){String src=safeImage(p.getAttributeValue(null,"src"));if(programmeId!=null&&image.isEmpty())image=src;else if(channelId!=null&&!logos.containsKey(channelId))logos.put(channelId,src);}
                else if("title".equals(tag)&&programmeId!=null){String t=p.nextText();if(title.isEmpty())title=t;}
                else if("desc".equals(tag)&&programmeId!=null){String d=p.nextText();if(description.isEmpty())description=d;}
            }else if(event==XmlPullParser.END_TAG){
                if("channel".equals(p.getName()))channelId=null;
                else if("programme".equals(p.getName())){
                    if(validProgramme){List<Programme> list=programmes.get(programmeId);if(list==null){list=new ArrayList<>();programmes.put(programmeId,list);}
                        list.add(new Programme(start,end,title.isEmpty()?"Programma senza titolo":title,description,image));}
                    programmeId=null;
                }
            }
        }
        if(count==0)throw new IOException("La fonte non contiene programmi XMLTV");
        for(Map.Entry<String,String> e:displayIds.entrySet())if(!ambiguous.contains(e.getKey()))ids.put(e.getKey(),e.getValue());
        for(List<Programme> l:programmes.values())Collections.sort(l,(a,b)->Long.compare(a.start,b.start));
    }
    static String safeImage(String value){try{URI u=new URI(value);return "https".equalsIgnoreCase(u.getScheme())&&u.getHost()!=null?value:"";}catch(Exception e){return "";}}
    public String logo(VavooClient.Channel c){if(!c.isItalian())return "";String id=ids.get(normalize(c.name));return logos.getOrDefault(id,"");}
    public List<Programme> guide(VavooClient.Channel channel){
        if(!channel.isItalian())return Collections.emptyList();
        String id=ids.get(normalize(channel.name));List<Programme> list=programmes.get(id);return list==null?Collections.emptyList():list;
    }
    public Programme now(VavooClient.Channel channel,long time){
        for(Programme p:guide(channel))if(p.start<=time&&time<p.end)return p;return null;
    }
    public Programme next(VavooClient.Channel channel,long time){
        for(Programme p:guide(channel))if(p.start>time)return p;return null;
    }
}
