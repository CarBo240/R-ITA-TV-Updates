package it.carmine.streamplayer;

import android.content.Context;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

/** SportsOnline schedule metadata. Playback loads the original page without extracting video. */
public final class EventSource {
    public static final String HOME="https://sportsonline.st/";
    private final Context context; private final AtomicFile cache;
    public static final class Source {
        public final String url,label,language,name;
        Source(String u,String l,String lang){url=u;label=l;language=lang;name="SportsOnline";}
        public String description(){return label+" · "+language;}
    }
    public static final class Event {
        public final String id,title,sport,league,status,homeBadge,awayBadge;
        public final long start; public final List<Source> sources;
        Event(String t,String s,long stamp,String h,String a,List<Source> list,long now){title=t;sport=s;start=stamp;id=stamp+":"+t;league="";homeBadge=h;awayBadge=a;sources=Collections.unmodifiableList(list);status=stamp>now?"upcoming":now-stamp<3*3600000L?"live":"finished";}
    }
    public static final class Snapshot {
        public final List<Event> events;public final long fetchedAt;public final String base;
        Snapshot(List<Event> e,long time,String b){events=Collections.unmodifiableList(e);fetchedAt=time;base=b;}
    }
    public EventSource(Context c){context=c;cache=new AtomicFile(new File(c.getFilesDir(),"sportsonline-schedule.json"));}
    public static String safeUrl(String value,String base){try{URI u=new URI(base).resolve(value.trim());return "https".equalsIgnoreCase(u.getScheme())&&u.getHost()!=null&&u.getUserInfo()==null?u.toASCIIString():"";}catch(Exception e){return "";}}
    private static String language(String raw){String out=raw;String[] from={"ENGLISH","SPANISH","GERMAN","ITALIAN","BRAZILIAN","PORTUGUESE","DUTCH","ARABIC","GREEK","BULGARIAN","FRENCH"},to={"Inglese","Spagnolo","Tedesco","Italiano","Portoghese BR","Portoghese","Olandese","Arabo","Greco","Bulgaro","Francese"};for(int i=0;i<from.length;i++)out=out.replace(from[i],to[i]);return out;}
    static String key(String name){return Normalizer.normalize(name,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]","");}
    private static String sport(String title){String t=title.toLowerCase(Locale.ROOT);if(t.startsWith("tennis"))return "Tennis";if(t.contains("f1 gp")||t.startsWith("motogp")||t.startsWith("nascar"))return "Motorsport";if(t.startsWith("ufc")||t.startsWith("boxing"))return "MMA";if(t.startsWith("golf"))return "Golf";if(t.startsWith("cricket"))return "Cricket";if(t.startsWith("basketball"))return "Basketball";if(t.startsWith("rugby"))return "Rugby";return t.contains(" x ")?"Soccer":"Other";}
    public static Snapshot parse(String text,String base,long fetchedAt)throws IOException{return parse(text,base,fetchedAt,TimeZone.getTimeZone("Europe/Lisbon"),Collections.emptyMap());}
    static Snapshot parse(String text,String base,long now,TimeZone zone,Map<String,String> logos)throws IOException {
        Matcher update=Pattern.compile("LAST UPDATE:\\s*(\\d{2}-\\d{2}-\\d{2})").matcher(text);if(!update.find())throw new IOException("Calendario non riconosciuto");
        Calendar anchor=Calendar.getInstance(zone);try{SimpleDateFormat format=new SimpleDateFormat("dd-MM-yy",Locale.ROOT);format.setTimeZone(zone);format.setLenient(false);anchor.setTime(format.parse(update.group(1)));}catch(ParseException e){throw new IOException("Data calendario non valida",e);}
        Calendar day=null;int previous=-1;Map<String,String> languages=new HashMap<>();Map<String,Event> grouped=new LinkedHashMap<>();
        List<String> days=Arrays.asList("SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY");
        Pattern header=Pattern.compile("(HD\\d+|BR\\d+)\\s+([A-Z &]+)"),entry=Pattern.compile("(\\d{1,2}):(\\d{2})\\s+(.+?)\\s*\\|\\s*(https://\\S+)");
        for(String line:text.replace("\ufeff","").split("\\r?\\n")){line=line.trim();int d=days.indexOf(line);if(d>=0){day=(Calendar)anchor.clone();day.add(Calendar.DATE,(d+1-anchor.get(Calendar.DAY_OF_WEEK)+7)%7);languages.clear();previous=-1;continue;}
            Matcher h=header.matcher(line);if(h.matches()){languages.put(h.group(1).toLowerCase(Locale.ROOT),language(h.group(2)));continue;}
            Matcher m=entry.matcher(line);if(day==null||!m.matches())continue;int hour=Integer.parseInt(m.group(1)),minute=Integer.parseInt(m.group(2));if(hour>23||minute>59)continue;
            int clock=hour*60+minute;if(previous>=18*60&&clock<6*60)day.add(Calendar.DATE,1);previous=clock;Calendar stamp=(Calendar)day.clone();stamp.set(Calendar.HOUR_OF_DAY,hour);stamp.set(Calendar.MINUTE,minute);stamp.set(Calendar.SECOND,0);stamp.set(Calendar.MILLISECOND,0);
            String url=safeUrl(m.group(4),base);if(url.isEmpty())continue;String label=url.substring(url.lastIndexOf('/')+1).replaceFirst("\\.php$","").toLowerCase(Locale.ROOT);String lang=languages.get(label);if(lang==null)lang=url.contains("/pt/")?"Portoghese":url.contains("/bra/")?"Portoghese BR":"Lingua non indicata";
            String title=m.group(3).trim(),id=stamp.getTimeInMillis()+":"+title;Event old=grouped.get(id);List<Source> sources=new ArrayList<>();if(old!=null)sources.addAll(old.sources);boolean duplicate=false;for(Source s:sources)if(s.url.equals(url))duplicate=true;if(!duplicate)sources.add(new Source(url,label.toUpperCase(Locale.ROOT),lang));
            String[] teams=title.split("(?i)\\s+(?:x|vs)\\s+",2);String home=teams.length==2?logos.getOrDefault(key(teams[0]),""):"",away=teams.length==2?logos.getOrDefault(key(teams[1]),""):"";
            grouped.put(id,new Event(title,sport(title),stamp.getTimeInMillis(),home,away,sources,now));
        }
        if(grouped.isEmpty())throw new IOException("Calendario senza eventi validi");List<Event> events=new ArrayList<>(grouped.values());events.sort((a,b)->{int rankA=a.status.equals("live")?0:a.status.equals("upcoming")?1:2,rankB=b.status.equals("live")?0:b.status.equals("upcoming")?1:2;return rankA!=rankB?Integer.compare(rankA,rankB):a.status.equals("finished")?Long.compare(b.start,a.start):Long.compare(a.start,b.start);});return new Snapshot(events,now,base);
    }
    public static List<Event> filter(Snapshot snapshot,String sport,String state,String query){List<Event> result=new ArrayList<>();String needle=key(query);for(Event e:snapshot.events){if(!sport.equals("Tutti")&&!sport.equals(e.sport))continue;if(state.equals("In diretta")&&!e.status.equals("live"))continue;if(state.equals("Prossimi")&&!e.status.equals("upcoming"))continue;if(!key(e.title).contains(needle))continue;result.add(e);}return result;}
    private Map<String,String> logos(){Map<String,String> result=new HashMap<>();try(InputStream input=context.getAssets().open("team_logos.json")){JSONObject json=new JSONObject(read(input));Iterator<String> names=json.keys();while(names.hasNext()){String name=names.next();result.put(key(name),json.getString(name));}}catch(Exception ignored){}return result;}
    private Snapshot decode(JSONObject wrapper)throws Exception{String home=EventSettings.home(context);if(!wrapper.optString("home",HOME).equals(home))throw new IOException("Calendario di un altro sito");Snapshot parsed=parse(wrapper.getString("data"),home,System.currentTimeMillis(),TimeZone.getTimeZone(context.getSharedPreferences("events",0).getString("timezone","Europe/Lisbon")),logos());return new Snapshot(new ArrayList<>(parsed.events),wrapper.getLong("fetchedAt"),home);}
    public Snapshot cached(){try(InputStream input=cache.openRead()){return decode(new JSONObject(read(input)));}catch(Exception e){return null;}}
    public Snapshot fetch()throws Exception {
        String home=EventSettings.home(context);android.content.SharedPreferences prefs=context.getSharedPreferences("events",0);long now=System.currentTimeMillis();if(now<prefs.getLong("sportsRetryUntil",0))throw new IOException("Servizio in pausa");
        HttpURLConnection c=(HttpURLConnection)new URL(scheduleUrl(home)).openConnection();c.setConnectTimeout(10000);c.setReadTimeout(15000);c.setRequestProperty("Accept","text/plain");String data;
        try{int code=c.getResponseCode();if(code==401||code==403||code==429||code==503){prefs.edit().putLong("sportsRetryUntil",now+Math.max(60000,RequestGuard.retryDelay(c.getHeaderField("Retry-After"),now))).apply();}if(code!=200)throw new IOException("Calendario non disponibile ("+code+")");if(!c.getURL().getProtocol().equals("https"))throw new IOException("Calendario non sicuro");try(InputStream input=c.getInputStream()){data=read(input);}}finally{c.disconnect();}
        JSONObject wrapper=new JSONObject();wrapper.put("home",home);wrapper.put("data",data);wrapper.put("fetchedAt",System.currentTimeMillis());Snapshot snapshot=decode(wrapper);FileOutputStream output=null;try{output=cache.startWrite();output.write(wrapper.toString().getBytes("UTF-8"));cache.finishWrite(output);}catch(IOException e){if(output!=null)cache.failWrite(output);}return snapshot;
    }
    static String scheduleUrl(String home){return home.endsWith("/prog.txt")?home:home+(home.endsWith("/")?"":"/")+"prog.txt";}
    private static String read(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>1024*1024)throw new IOException("Calendario troppo grande");out.write(b,0,n);}return out.toString("UTF-8");}
}
