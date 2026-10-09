package it.carmine.streamplayer;

import android.content.Intent;
import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DaddyLiveTest {
    public static class OfflineDaddy extends DaddyLiveActivity {@Override protected void load(){}}
    public static class OfflineHome extends MainActivity {
        @Override protected boolean imagesEnabled(){return false;}
        @Override protected void load(){}
        @Override protected void loadEpg(boolean force){}
    }
    @Test public void homeButtonOpensSeparateDaddySection(){
        OfflineHome home=Robolectric.buildActivity(OfflineHome.class).setup().get();
        View button=home.getWindow().getDecorView().findViewWithTag("navDaddyLive");assertNotNull(button);button.performClick();
        Intent intent=Shadows.shadowOf(home).getNextStartedActivity();assertEquals(DaddyLiveActivity.class.getName(),intent.getComponent().getClassName());
    }
    @Test public void catalogRendersAndSourceSelectionOpensGecko()throws Exception {
        OfflineDaddy activity=Robolectric.buildActivity(OfflineDaddy.class).setup().get();
        List<DaddyLiveSource.Item> fixture=Collections.singletonList(new DaddyLiveSource.Item("Italia - Francia","Football","Sunday, 4th October 2026 – Schedule Time (UK GMT)","19:00",Collections.singletonList(new DaddyLiveSource.Link("Sky Sport Italy","664"))));
        java.lang.reflect.Field field=DaddyLiveActivity.class.getDeclaredField("items");field.setAccessible(true);field.set(activity,fixture);
        for(String method:new String[]{"setCategories","filter"}){java.lang.reflect.Method m=DaddyLiveActivity.class.getDeclaredMethod(method);m.setAccessible(true);m.invoke(activity);}
        field=DaddyLiveActivity.class.getDeclaredField("list");field.setAccessible(true);android.widget.ListView list=(android.widget.ListView)field.get(activity);assertEquals(1,list.getCount());
        View root=activity.getWindow().getDecorView();root.measure(View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));root.layout(0,0,1280,720);
        android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(1280,720,android.graphics.Bitmap.Config.ARGB_8888);root.draw(new android.graphics.Canvas(bitmap));java.io.File dir=new java.io.File("build/previews");dir.mkdirs();try(java.io.OutputStream out=new java.io.FileOutputStream(new java.io.File(dir,"daddy-live-tv.png"))){bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();
        list.performItemClick(list.getAdapter().getView(0,null,list),0,0);android.app.AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertNotNull(dialog);android.widget.ListView choices=dialog.getListView();choices.performItemClick(choices.getAdapter().getView(0,null,choices),0,0);
        Intent intent=Shadows.shadowOf(activity).getNextStartedActivity();assertEquals(GeckoEventActivity.class.getName(),intent.getComponent().getClassName());assertEquals("https://daddylive.li/player/embed.php?id=664",intent.getStringExtra("url"));activity.finish();
    }
    @Test public void channelIdsAreNormalizedAndDuplicatesSkipped()throws Exception {
        List<DaddyLiveSource.Item> data=DaddyLiveSource.parseChannels("[{\"id\":\"stream-664\",\"title\":\"Sky Sport Italy\"},{\"id\":\"664\",\"title\":\"Duplicate\"},{\"title\":\"Missing ID\"}]");
        assertEquals(1,data.size());assertTrue(data.get(0).italian());assertEquals("https://daddylive.li/player/embed.php?id=664",data.get(0).links.get(0).embedUrl());
    }
    @Test public void eventPathIsEncodedForEmbed(){assertEquals("https://daddylive.li/player/embed.php?id=admin%2Fppv-event%2F1",new DaddyLiveSource.Link("Event","admin/ppv-event/1").embedUrl());}
    @Test public void winterDateUsesUkStandardTime(){DaddyLiveSource.Item i=new DaddyLiveSource.Item("Match","Football","Sunday, 1st November 2026 – Schedule Time (UK GMT)","23:30",Collections.emptyList());assertEquals("02/11 · 00:30 · ora Italia",i.timing());}
    @Test public void scheduleConvertsGmtDuringItalianSummerAndRetainsEmptySources()throws Exception {
        String json="{\"Sunday, 4th October 2026 – Schedule Time (UK GMT)\":[{\"Category\":\"Football\",\"events\":[{\"time\":\"23:30\",\"event\":\"Match\",\"channels\":[{\"channel_name\":\"Italian feed\",\"channel_id\":\"664\"}]},{\"event\":\"No source\"}]}]}";
        List<DaddyLiveSource.Item> data=DaddyLiveSource.parseEvents(json);assertEquals(2,data.size());assertEquals("05/10 · 01:30 · ora Italia",data.get(0).timing());assertTrue(data.get(0).italian());assertTrue(data.get(1).links.isEmpty());
    }
}
