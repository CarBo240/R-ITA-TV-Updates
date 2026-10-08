package it.carmine.streamplayer;

import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.*;
import org.json.*;
import android.view.View;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
public class CatalogLoadingTest {
    public static class OfflineActivity extends MainActivity {
        @Override protected boolean imagesEnabled(){return false;}
        @Override protected void load(){}
        @Override protected void loadEpg(boolean force){}
    }
    private Object get(MainActivity a,String name)throws Exception{Field f=MainActivity.class.getDeclaredField(name);f.setAccessible(true);return f.get(a);}
    private void set(MainActivity a,String name,Object value)throws Exception{Field f=MainActivity.class.getDeclaredField(name);f.setAccessible(true);f.set(a,value);}
    private VavooClient.Channel channel(String n){return new VavooClient.Channel(n,"Italy","test:"+n);}
    private JSONObject item(String name,String country)throws Exception{return new JSONObject().put("type","iptv").put("name",name).put("group",country).put("url","test:"+name);}
    private void load(MainActivity a,boolean force)throws Exception{
        Method m=MainActivity.class.getDeclaredMethod("loadCatalog",boolean.class);m.setAccessible(true);m.invoke(a,force);
        ((ExecutorService)get(a,"worker")).submit(()->{}).get(5,TimeUnit.SECONDS);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
    }
    @Test public void freshSavedCatalogSkipsNetworkAndManualRefreshUsesNetwork()throws Exception{
        try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()){
            OfflineActivity a=c.get();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            CatalogStore store=new CatalogStore(a);store.save(Arrays.asList(channel("Rai 1"),new VavooClient.Channel("BBC One","UK","foreign")),System.currentTimeMillis());
            final int[] requests={0};set(a,"client",new VavooClient("test",(url,p,sig)->{requests[0]++;if(url.endsWith("ping"))return new JSONObject().put("addonSig","test");return new JSONObject().put("items",new JSONArray().put(item("Rai 2","Italy")));}));
            load(a,false);assertEquals(0,requests[0]);List<VavooClient.Channel> shown=(List<VavooClient.Channel>)get(a,"visible");assertEquals(1,shown.size());assertEquals("Rai 1",shown.get(0).name);
            load(a,true);assertEquals(2,requests[0]);shown=(List<VavooClient.Channel>)get(a,"visible");assertEquals("Rai 2",shown.get(0).name);assertEquals("Rai 2",store.read().channels.get(0).name);
        }
    }
    @Test public void expiredCatalogSurvivesRefreshFailure()throws Exception{
        try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()){
            OfflineActivity a=c.get();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();CatalogStore store=new CatalogStore(a);
            store.save(Collections.singletonList(channel("Rai 1")),System.currentTimeMillis()-CatalogStore.MAX_AGE-1000);
            set(a,"client",new VavooClient("test",(url,p,sig)->{throw new java.io.IOException("offline");}));load(a,false);
            assertEquals("Rai 1",((List<VavooClient.Channel>)get(a,"visible")).get(0).name);assertFalse((Boolean)get(a,"loading"));
            assertTrue(((TextView)get(a,"status")).getText().toString().contains("aggiornamento non riuscito"));assertFalse(store.read().fresh(System.currentTimeMillis()));
        }
    }
    @Test public void catalogPublishesItalianPagesBeforeCompletionAndNeverPublishesForeignChannels()throws Exception{
        final int[] pages={0};List<List<VavooClient.Channel>> snapshots=new ArrayList<>();
        VavooClient client=new VavooClient("test",(url,p,sig)->{
            if(url.endsWith("ping"))return new JSONObject().put("addonSig","test");pages[0]++;
            if(pages[0]==1)return new JSONObject().put("items",new JSONArray().put(item("Rai 1","Italy ➾ TV"))).put("nextCursor","two");
            if(pages[0]==2)return new JSONObject().put("items",new JSONArray().put(item("BBC One","UK"))).put("nextCursor","three");
            assertEquals(1,snapshots.size());return new JSONObject().put("items",new JSONArray().put(item("Rai 2","Italy")));
        });
        List<VavooClient.Channel> all=client.catalog(new VavooClient.Progress(){public void update(int n){}public void updateChannels(List<VavooClient.Channel> channels){snapshots.add(channels);}});
        assertEquals(3,pages[0]);assertEquals(2,snapshots.size());assertEquals(1,snapshots.get(0).size());assertEquals(2,all.size());
        for(List<VavooClient.Channel> channels:snapshots)for(VavooClient.Channel c:channels)assertTrue(c.isItalian());
    }
    @Test public void timedOutPrimaryStillFallsBackToAlternateService()throws Exception{
        final int[] alternate={0};VavooClient client=new VavooClient("test",(url,p,sig)->{
            if(url.endsWith("ping"))return new JSONObject().put("addonSig","test");
            if(url.startsWith(VavooClient.BASES[0]))throw new java.net.SocketTimeoutException("timeout");
            alternate[0]++;return new JSONObject().put("items",new JSONArray().put(item("Rai 1","Italy")));
        });
        assertEquals("Rai 1",client.catalog(null).get(0).name);assertEquals(1,alternate[0]);
    }

}
