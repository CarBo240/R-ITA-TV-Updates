package it.carmine.streamplayer;

import android.app.AlertDialog;
import android.view.*;import android.widget.*;
import androidx.media3.common.*;
import java.io.*;import java.lang.reflect.*;import java.util.*;import java.util.concurrent.*;
import org.json.*;import org.junit.*;import org.junit.runner.RunWith;
import org.robolectric.*;import org.robolectric.annotation.*;import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class) @Config(sdk={31,35},qualifiers="w1280dp-h720dp-land")
public class UnifiedSourcesTest {
 public static class Offline extends MainActivity {
  volatile int requests;volatile List<VavooClient.Channel> supplied=Arrays.asList(gom("850","Rai 1 Italy"),gom("461","Sky Sport Uno Italy"));
  CountDownLatch started,release;VavooClient.Channel requested;
  @Override protected VavooClient createClient(String id){return new VavooClient(id,this,(url,p,sig)->{if(url.endsWith("ping"))return new JSONObject().put("addonSig","test");return new JSONObject().put("items",new JSONArray().put(new JSONObject().put("type","iptv").put("name","Rai 3").put("group","Italy").put("url","vavoo:3")));});}
  @Override protected boolean imagesEnabled(){return false;}
  @Override protected void load(){}
  @Override protected void loadEpg(boolean force){}
  @Override protected List<VavooClient.Channel> fetchGomCatalog(String daddy)throws Exception {requests++;if(started!=null){started.countDown();while(release.getCount()>0)try{release.await();}catch(InterruptedException ignored){}}return supplied;}
  @Override protected void resolveChannel(VavooClient.Channel c,int generation){requested=c;}
 }
 void capture(View root,String name)throws Exception {
  root.measure(View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));root.layout(0,0,1280,720);
  android.graphics.Bitmap b=android.graphics.Bitmap.createBitmap(1280,720,android.graphics.Bitmap.Config.ARGB_8888);root.draw(new android.graphics.Canvas(b));File dir=new File("build/previews");dir.mkdirs();try(OutputStream out=new FileOutputStream(new File(dir,name))){assertTrue(b.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out));}b.recycle();
 }
 static VavooClient.Channel gom(String id,String name){return ChannelSource.gomstream(new GomstreamSource.Channel(id,name));}
 Object get(MainActivity a,String n)throws Exception{Field f=MainActivity.class.getDeclaredField(n);f.setAccessible(true);return f.get(a);}
 void set(MainActivity a,String n,Object value)throws Exception{Field f=MainActivity.class.getDeclaredField(n);f.setAccessible(true);f.set(a,value);}
 void settle(MainActivity a,String worker)throws Exception{((ExecutorService)get(a,worker)).submit(()->{}).get(5,TimeUnit.SECONDS);shadowOf(android.os.Looper.getMainLooper()).idle();}
 void choose(Offline a,int source){a.getWindow().getDecorView().findViewWithTag("navChannels").performClick();AlertDialog d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertEquals(2,d.getListView().getCount());d.getListView().performItemClick(null,source,source);}
 @Test public void clickSelectsSourceOnSameHomeAndPersistsChoice()throws Exception {
  try(ActivityController<Offline> c=Robolectric.buildActivity(Offline.class).setup()){
   Offline a=c.get();new CatalogStore(a).save(Collections.singletonList(new VavooClient.Channel("Rai 3","Italy","vavoo:3")),System.currentTimeMillis());
   choose(a,1);settle(a,"gomWorker");View decor=a.getWindow().getDecorView();assertNull(decor.findViewWithTag("navGomstream"));assertEquals(2,((ListView)decor.findViewWithTag("homeChannels")).getCount());assertNull(shadowOf(a).getNextStartedActivity());assertEquals(ChannelSource.GOMSTREAM,ChannelSource.saved(a));
   assertTrue(((TextView)get(a,"status")).getText().toString().startsWith("Gomstream"));choose(a,0);settle(a,"worker");assertEquals("Rai 3",((List<VavooClient.Channel>)get(a,"visible")).get(0).name);assertEquals(ChannelSource.VAVOO,ChannelSource.saved(a));
   choose(a,1);settle(a,"gomWorker");c.recreate();assertEquals(ChannelSource.GOMSTREAM,get(c.get(),"channelSource"));
  }
 }
 @Test public void longClickOffersBothUrlsAndSavesOrResetsIndependently()throws Exception {
  try(ActivityController<Offline> c=Robolectric.buildActivity(Offline.class).setup()){
   Offline a=c.get();View button=a.getWindow().getDecorView().findViewWithTag("navChannels");assertTrue(button.performLongClick());AlertDialog menu=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertEquals("Gomstream",menu.getListView().getAdapter().getItem(1));menu.getListView().performItemClick(null,1,1);shadowOf(android.os.Looper.getMainLooper()).idle();
   AlertDialog d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();EditText input=d.findViewById(android.R.id.content).findViewWithTag("gomstreamHomeInput");input.setText("https://new-gom.test");d.getButton(AlertDialog.BUTTON_POSITIVE).performClick();assertEquals("https://new-gom.test",GomstreamSettings.home(a));assertEquals(VavooSettings.HOME,VavooSettings.home(a));
   button.performLongClick();org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().getListView().performItemClick(null,0,0);shadowOf(android.os.Looper.getMainLooper()).idle();d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();input=d.findViewById(android.R.id.content).findViewWithTag("vavooHomeInput");input.setText("http://bad.test");d.getButton(AlertDialog.BUTTON_POSITIVE).performClick();assertTrue(d.isShowing());assertEquals(VavooSettings.HOME,VavooSettings.home(a));
   input.setText("https://new-vavoo.test");d.getButton(AlertDialog.BUTTON_POSITIVE).performClick();settle(a,"worker");assertEquals("https://new-vavoo.test",VavooSettings.home(a));assertEquals("https://new-gom.test",GomstreamSettings.home(a));
   button.performLongClick();org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().getListView().performItemClick(null,0,0);shadowOf(android.os.Looper.getMainLooper()).idle();d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();d.getButton(AlertDialog.BUTTON_NEUTRAL).performClick();settle(a,"worker");assertEquals(VavooSettings.HOME,VavooSettings.home(a));
  }
 }
 @Test public void lateGomCatalogCannotOverwriteVavooAfterSwitch()throws Exception {
  try(ActivityController<Offline> c=Robolectric.buildActivity(Offline.class).setup()){
   Offline a=c.get();new CatalogStore(a).save(Collections.singletonList(new VavooClient.Channel("Rai 3","Italy","vavoo:3")),System.currentTimeMillis());a.started=new CountDownLatch(1);a.release=new CountDownLatch(1);
   choose(a,1);assertTrue(a.started.await(5,TimeUnit.SECONDS));choose(a,0);settle(a,"worker");a.release.countDown();settle(a,"gomWorker");assertEquals("Rai 3",((List<VavooClient.Channel>)get(a,"visible")).get(0).name);assertEquals(ChannelSource.VAVOO,get(a,"channelSource"));
  }
 }
 @Test @GraphicsMode(GraphicsMode.Mode.NATIVE) public void gomUsesExistingCategoriesEpgFavoritesAndPlayerZapping()throws Exception {
  try(ActivityController<Offline> c=Robolectric.buildActivity(Offline.class).setup()){
   Offline a=c.get();choose(a,1);settle(a,"gomWorker");long now=System.currentTimeMillis();EpgStore guide=new EpgStore(a);
   java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("yyyyMMddHHmmss Z",Locale.US);f.setTimeZone(TimeZone.getTimeZone("UTC"));String start=f.format(new Date(now-60000)),end=f.format(new Date(now+3600000));
   guide.parse(new ByteArrayInputStream(("<tv><channel id='r1'><display-name>Rai 1</display-name></channel><programme channel='r1' start='"+start+"' stop='"+end+"'><title>La guida condivisa</title><desc>Trama Rai 1</desc></programme></tv>").getBytes("UTF-8")),now);set(a,"epg",guide);Method filter=MainActivity.class.getDeclaredMethod("filter");filter.setAccessible(true);filter.invoke(a);
   View decor=a.getWindow().getDecorView();capture(decor,"gomstream-home-tv.png");assertEquals("La guida condivisa",((TextView)decor.findViewWithTag("guideTitle")).getText().toString());assertEquals("Generalisti",ChannelCategories.of(gom("850","Rai 1 Italy")));
   List<VavooClient.Channel> visible=(List<VavooClient.Channel>)get(a,"visible");VavooClient.Channel one=visible.get(0),sport=visible.get(1);a.play(one);View video=decor.findViewWithTag("video");assertNotNull(video);capture(decor,"gomstream-shared-player-tv.png");shadowOf(android.os.Looper.getMainLooper()).idleFor(6,TimeUnit.SECONDS);
   a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_UP));assertEquals(sport.key(),a.requested.key());assertSame(video,decor.findViewWithTag("video"));
   a.onBackPressed();Spinner cats=decor.findViewWithTag("playerCategories");cats.setSelection(Arrays.asList(ChannelCategories.ALL).indexOf("Sport"));shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(Collections.singletonList(sport),get(a,"zapChannels"));a.onBackPressed();assertNull(decor.findViewWithTag("player"));
   ListView home=decor.findViewWithTag("homeChannels");home.getOnItemLongClickListener().onItemLongClick(home,null,0,0);((Button)get(a,"favs")).performClick();assertEquals(Collections.singletonList(one),get(a,"visible"));assertTrue(((Set<String>)get(a,"favorites")).contains(one.key()));
   assertFalse(one.key().equals(new VavooClient.Channel("Rai 1","Italy","x").key()));assertEquals(one.key(),gom("850","Rai 1 HD Italy").key());assertEquals(MimeTypes.APPLICATION_M3U8,MainActivity.mediaItem(one,"https://video.test/live.m3u8").localConfiguration.mimeType);
  }
 }
 @Test public void gomCatalogRefreshesTenMinutesAndOnReturnNotWhilePaused()throws Exception {
  try(ActivityController<Offline> c=Robolectric.buildActivity(Offline.class).setup()){
   Offline a=c.get();choose(a,1);settle(a,"gomWorker");assertEquals(1,a.requests);a.supplied=Collections.singletonList(gom("851","Rai 2 Italy"));shadowOf(android.os.Looper.getMainLooper()).idleFor(10,TimeUnit.MINUTES);settle(a,"gomWorker");assertEquals(2,a.requests);assertEquals("Rai 2",((List<VavooClient.Channel>)get(a,"visible")).get(0).name);
   c.pause();shadowOf(android.os.Looper.getMainLooper()).idleFor(11,TimeUnit.MINUTES);assertEquals(2,a.requests);c.resume();settle(a,"gomWorker");assertEquals(3,a.requests);
  }
 }
 @Test public void customVavooDomainDrivesCatalogAndResolutionAndRejectsOldCache()throws Exception {
  android.content.Context context=org.robolectric.RuntimeEnvironment.getApplication();CatalogStore old=new CatalogStore(context);old.save(Collections.singletonList(new VavooClient.Channel("Rai 1","Italy","x")),System.currentTimeMillis());context.getSharedPreferences("vavoo",0).edit().putString("home","https://new-vavoo.test").commit();assertTrue(new CatalogStore(context).read().channels.isEmpty());
  List<String> urls=new ArrayList<>();VavooClient client=new VavooClient("test",context,(url,payload,sig)->{urls.add(url);if(url.endsWith("ping"))return new JSONObject().put("addonSig","session");if(url.contains("catalog"))return new JSONObject().put("items",new JSONArray().put(new JSONObject().put("type","iptv").put("name","Rai 1").put("group","Italy").put("url","id:1")));return new JSONObject().put("url","https://media.test/live.m3u8");});
  List<VavooClient.Channel> result=client.catalog(null);client.resolve(result.get(0));assertTrue(urls.contains("https://new-vavoo.test/mediahubmx-catalog.json"));assertTrue(urls.contains("https://new-vavoo.test/mediahubmx-resolve.json"));assertFalse(urls.stream().anyMatch(x->x.startsWith("https://vavoo.to/")));assertEquals("https://www.vavoo.tv/api/app/ping",urls.get(0));
 }
}
