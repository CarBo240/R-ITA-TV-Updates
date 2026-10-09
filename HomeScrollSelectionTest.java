package it.carmine.streamplayer;
import android.view.*;import android.widget.*;import java.util.*;import java.lang.reflect.*;import org.junit.Test;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.android.controller.ActivityController;import org.robolectric.annotation.Config;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk={31,35},qualifiers="w1280dp-h720dp-land-mdpi")
public class HomeScrollSelectionTest {
 public static class Offline extends MainActivity {protected boolean imagesEnabled(){return false;}protected void load(){}protected void loadEpg(boolean force){}}
 @Test public void recycledRowsKeepHighlightOnSelectedChannelAcrossScrollBoundary()throws Exception{
  new android.app.Instrumentation().setInTouchMode(false);
  try(ActivityController<Offline> controller=Robolectric.buildActivity(Offline.class).setup()){
   Offline a=controller.get();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();List<VavooClient.Channel> channels=new ArrayList<>();for(int n=0;n<40;n++)channels.add(new VavooClient.Channel("CANALE "+n+(n%3==0?" CON NOME PIÙ LUNGO CHE OCCUPA DUE RIGHE SULLO SCHERMO":""),"Italy","ch"+n));
   Field field=MainActivity.class.getDeclaredField("channels");field.setAccessible(true);field.set(a,channels);Method filter=MainActivity.class.getDeclaredMethod("filter");filter.setAccessible(true);filter.invoke(a);
   View decor=a.getWindow().getDecorView();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();decor.measure(View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));decor.layout(0,0,1280,720);ListView list=decor.findViewWithTag("homeChannels");list.requestFocus();list.setSelection(0);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
   for(int n=1;n<25;n++){
    a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN));a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DPAD_DOWN));Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(n,list.getSelectedItemPosition());
    assertEquals(DisplayNames.channel(channels.get(n).name),((TextView)decor.findViewWithTag("guideChannelName")).getText().toString());
    for(int i=0;i<list.getChildCount();i++){Object holder=list.getChildAt(i).getTag();Field key=holder.getClass().getDeclaredField("channelKey"),card=holder.getClass().getDeclaredField("card");key.setAccessible(true);card.setAccessible(true);assertEquals(channels.get(n).key().equals(key.get(holder)),((View)card.get(holder)).isActivated());}
   }
   assertTrue(list.getFirstVisiblePosition()>0);
  }
 }
}
