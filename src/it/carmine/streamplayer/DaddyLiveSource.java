package it.carmine.streamplayer;

import org.json.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/** Reads the same public metadata as DaddyLive; playback stays in its embedded web player. */
public final class DaddyLiveSource {
    public static final String HOME="https://daddylive.li";
    public static final class Link {
        public final String name,id;
        Link(String name,String id){this.name=name;this.id=id;}
        public String embedUrl(){return embedUrl(HOME);}
        public String embedUrl(String home){return home+"/player/embed.php?id="+encode(id);}
        private static String encode(String value){try{return URLEncoder.encode(value,"UTF-8");}catch(Exception e){throw new IllegalArgumentException(e);}}
    }
    public static final class Item {
        public final String title,category,day,time;
        public final List<Link> links;
        Item(String t,String c,String d,String time,List<Link> l){title=t;category=c;day=d;this.time=time;links=Collections.unmodifiableList(l);}
        public boolean italian(){return title.toLowerCase(Locale.ROOT).matches(".*\\b(italy|italia|italian)\\b.*")||links.stream().anyMatch(l->l.name.toLowerCase(Locale.ROOT).matches(".*\\b(italy|italia|italian)\\b.*"));}
        public String timing(){
            if(day.isEmpty())return "Canale TV";
            try{
                String date=day.split("[–—]")[0].trim().replaceAll("(\\d+)(st|nd|rd|th)","$1");
                SimpleDateFormat in=new SimpleDateFormat("EEEE, d MMMM yyyy HH:mm",Locale.ENGLISH);in.setLenient(false);in.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date stamp=in.parse(date+" "+time);SimpleDateFormat out=new SimpleDateFormat("dd/MM · HH:mm",Locale.ITALY);out.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));return out.format(stamp)+" · ora Italia";
            }catch(Exception e){return day+" · "+time+" · orario fonte";}
        }
    }
    public static List<Item> parseChannels(String json)throws JSONException {
        JSONArray array=new JSONArray(json);List<Item> items=new ArrayList<>();Set<String> ids=new HashSet<>();
        for(int i=0;i<array.length();i++){JSONObject c=array.optJSONObject(i);if(c==null)continue;String id=c.optString("id","").trim().replaceFirst("^stream-","");String name=c.optString("title","").trim();if(!id.isEmpty()&&!name.isEmpty()&&ids.add(id))items.add(new Item(name,"TV","","",Collections.singletonList(new Link(name,id))));}
        items.sort(Comparator.comparing(i->i.title.toLowerCase(Locale.ROOT)));return items;
    }
    public static List<Item> parseEvents(String json)throws JSONException {
        JSONObject data=new JSONObject(json);List<Item> items=new ArrayList<>();Iterator<String> days=data.keys();
        while(days.hasNext()){String day=days.next();JSONArray blocks=data.optJSONArray(day);if(blocks==null)continue;
            for(int b=0;b<blocks.length();b++){JSONObject block=blocks.optJSONObject(b);if(block==null)continue;JSONArray events=block.optJSONArray("events");if(events==null)continue;
                for(int e=0;e<events.length();e++){JSONObject event=events.optJSONObject(e);if(event==null)continue;List<Link> links=new ArrayList<>();JSONArray channels=event.optJSONArray("channels");
                    if(channels!=null)for(int n=0;n<channels.length();n++){JSONObject ch=channels.optJSONObject(n);if(ch==null)continue;String id=ch.optString("channel_id","").trim().replaceFirst("^stream-","");if(!id.isEmpty())links.add(new Link(ch.optString("channel_name","Fonte "+(n+1)),id));}
                    String title=event.optString("event","").trim();if(!title.isEmpty())items.add(new Item(title,block.optString("Category","Sport"),day,event.optString("time",""),links));
                }
            }
        }return items;
    }
    public static String fetch(boolean events)throws IOException {return fetch(HOME,events);}
    public static String fetch(String home,boolean events)throws IOException {
        HttpURLConnection c=(HttpURLConnection)new URL(home+(events?"/player/tv1.json":"/cache/channels.json")).openConnection();c.setConnectTimeout(10000);c.setReadTimeout(20000);c.setRequestProperty("Accept","application/json");
        try{int status=c.getResponseCode();if(status!=200)throw new IOException("HTTP "+status);try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){if(out.size()+n>8*1024*1024)throw new IOException("Catalogo troppo grande");out.write(buf,0,n);}return out.toString("UTF-8");}}finally{c.disconnect();}
    }
}
