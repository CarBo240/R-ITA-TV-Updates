package it.carmine.streamplayer;
import android.widget.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
public class CategoryFilterTest {
 public static class OfflineActivity extends MainActivity {
        @Override protected boolean imagesEnabled(){return false;}
  @Override protected void load(){}
  @Override protected void loadEpg(boolean force){}
 }
 private VavooClient.Channel channel(String n){return new VavooClient.Channel(n,"Italy","test:"+n);}
 private Object get(MainActivity a,String n)throws Exception{Field f=MainActivity.class.getDeclaredField(n);f.setAccessible(true);return f.get(a);}
 private void set(MainActivity a,String n,Object v)throws Exception{Field f=MainActivity.class.getDeclaredField(n);f.setAccessible(true);f.set(a,v);}
 private void filter(MainActivity a)throws Exception{Method m=MainActivity.class.getDeclaredMethod("filter");m.setAccessible(true);m.invoke(a);}
 @Test public void familiarChannelsHaveCategoriesAndUnknownChannelsRemainAccessible(){
  String[][] examples={{"Rai 1 HD","Generalisti"},{"Sky Sport Uno.s","Sport"},{"DAZN 1","Sport"},{"Sky Cinema Uno","Cinema e serie"},{"Real Time","Intrattenimento"},{"National Geographic","Documentari"},{"Rai Yoyo","Bambini"},{"Sky TG24","Notizie"},{"Radio Italia TV","Musica"},{"Canale locale","Altri"}};
  for(String[] e:examples)assertEquals(e[0],e[1],ChannelCategories.of(channel(e[0])));
 }
 @Test public void categorySearchAndFavoritesComposeAndAllRestoresCatalog()throws Exception{
  try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()){
   OfflineActivity a=c.get();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
   VavooClient.Channel sport=channel("Sky Sport Uno"),dazn=channel("DAZN 1");
   set(a,"channels",new ArrayList<>(Arrays.asList(channel("Rai 1"),sport,dazn,channel("Canale locale"),new VavooClient.Channel("BBC One","United Kingdom","foreign"))));
   Spinner spinner=a.getWindow().getDecorView().findViewWithTag("categories");
   spinner.setSelection(Arrays.asList(ChannelCategories.ALL).indexOf("Sport"));Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();filter(a);
   assertEquals(2,((List<?>)get(a,"visible")).size());
   ((EditText)get(a,"search")).setText("dazn");assertEquals(Collections.singletonList(dazn),get(a,"visible"));
   ((Set<String>)get(a,"favorites")).add(sport.key());set(a,"favoritesOnly",true);filter(a);assertTrue(((List<?>)get(a,"visible")).isEmpty());
   ((EditText)get(a,"search")).setText("");assertEquals(Collections.singletonList(sport),get(a,"visible"));
   set(a,"favoritesOnly",false);spinner.setSelection(0);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();filter(a);
   assertEquals(4,((List<?>)get(a,"visible")).size());
  }
 }
}
