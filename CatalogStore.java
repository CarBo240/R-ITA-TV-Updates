package it.carmine.streamplayer;

import android.content.Context;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.util.*;

/** Persistent channel IDs; stream URLs are still resolved at playback time. */
final class CatalogStore {
    static final long MAX_AGE=6*3600000L;
    private final AtomicFile file;private final String source;
    CatalogStore(Context context){source=VavooSettings.home(context);file=new AtomicFile(new File(context.getFilesDir(),"italian-catalog.json"));}
    static final class Snapshot {
        final long savedAt;final List<VavooClient.Channel> channels;
        Snapshot(long at,List<VavooClient.Channel> c){savedAt=at;channels=c;}
        boolean fresh(long now){return !channels.isEmpty()&&now>=savedAt&&now-savedAt<MAX_AGE;}
    }
    Snapshot read(){
        try(InputStream in=file.openRead();ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>8*1024*1024)throw new IOException("Catalogo troppo grande");}
            JSONObject data=new JSONObject(out.toString("UTF-8"));if(!source.equals(data.optString("source",VavooSettings.HOME)))return new Snapshot(0,Collections.emptyList());JSONArray items=data.getJSONArray("channels");
            List<VavooClient.Channel> channels=new ArrayList<>();
            for(int i=0;i<items.length();i++){JSONObject c=items.getJSONObject(i);VavooClient.Channel channel=new VavooClient.Channel(c.getString("name"),c.getString("country"),c.getString("url"));if(channel.isItalian()&&!channel.url.isEmpty())channels.add(channel);}
            return new Snapshot(data.getLong("savedAt"),channels);
        }catch(Exception e){return new Snapshot(0,Collections.emptyList());}
    }
    void save(List<VavooClient.Channel> channels,long now)throws Exception {
        JSONArray items=new JSONArray();for(VavooClient.Channel c:channels)if(c.isItalian()&&c.source==ChannelSource.VAVOO)items.put(new JSONObject().put("name",c.name).put("country",c.country).put("url",c.url));
        if(items.length()==0)return;
        byte[] bytes=new JSONObject().put("source",source).put("savedAt",now).put("channels",items).toString().getBytes("UTF-8");
        FileOutputStream out=file.startWrite();try{out.write(bytes);file.finishWrite(out);}catch(Exception e){file.failWrite(out);throw e;}
    }
}
