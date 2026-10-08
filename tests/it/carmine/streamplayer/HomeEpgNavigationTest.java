package it.carmine.streamplayer;
import android.view.*;import android.widget.*;import java.lang.reflect.*;import java.util.*;
import org.junit.Test;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.android.controller.ActivityController;import org.robolectric.annotation.Config;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk={31,35},qualifiers="w1280dp-h720dp-land-mdpi")
public class HomeEpgNavigationTest {
 public static class Offline extends MainActivity {protected boolean imagesEnabled(){return false;}protected void load(){}protected void loadEpg(boolean force){}}
 void press(Offline a,int key){a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,key));a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,key));}
 @Test public void rightEntersGuideAndArrowsReadFullDescriptionWithoutChangingChannel()throws Exception{
  new android.app.Instrumentation().setInTouchMode(false);
  try(ActivityController<Offline> c=Robolectric.buildActivity(Offline.class).setup()){
   Offline a=c.get();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
   Field catalog=MainActivity.class.getDeclaredField("channels");catalog.setAccessible(true);catalog.set(a,new ArrayList<>(Arrays.asList(new VavooClient.Channel("Rai 1","Italy","one"),new VavooClient.Channel("Rai 2","Italy","two"))));
   Method filter=MainActivity.class.getDeclaredMethod("filter");filter.setAccessible(true);filter.invoke(a);
   View decor=a.getWindow().getDecorView();TextView description=decor.findViewWithTag("guideDescription");StringBuilder longText=new StringBuilder();for(int n=0;n<100;n++)longText.append("Riga ").append(n).append(": trama completa del programma.\n");description.setText(longText);
   Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();decor.measure(View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));decor.layout(0,0,1280,720);
   ListView list=decor.findViewWithTag("homeChannels");ScrollView guide=decor.findViewWithTag("homeGuide");assertTrue(list.requestFocus());int selected=list.getSelectedItemPosition();
   press(a,KeyEvent.KEYCODE_DPAD_RIGHT);assertTrue(guide.isFocused());assertEquals(selected,list.getSelectedItemPosition());
   int start=guide.getScrollY();for(int n=0;n<60;n++)press(a,KeyEvent.KEYCODE_DPAD_DOWN);assertTrue(guide.getScrollY()>start);assertEquals(Math.max(0,guide.getChildAt(0).getHeight()-guide.getHeight()),guide.getScrollY());assertTrue(guide.isFocused());
   press(a,KeyEvent.KEYCODE_DPAD_UP);assertTrue(guide.getScrollY()<guide.getChildAt(0).getHeight()-guide.getHeight());assertEquals(selected,list.getSelectedItemPosition());
   press(a,KeyEvent.KEYCODE_DPAD_LEFT);assertTrue(list.isFocused());assertFalse(a.isFinishing());
   press(a,KeyEvent.KEYCODE_DPAD_RIGHT);a.onBackPressed();assertTrue(decor.findViewWithTag("navChannels").isFocused());assertFalse(a.isFinishing());list.requestFocus();
   press(a,KeyEvent.KEYCODE_DPAD_RIGHT);press(a,KeyEvent.KEYCODE_DPAD_CENTER);assertTrue(decor.findViewWithTag("watchChannel").isFocused());assertNull(decor.findViewWithTag("player"));press(a,KeyEvent.KEYCODE_DPAD_LEFT);assertTrue(list.isFocused());
  }
 }
 @Test public void backGoesToTopMenuThenClosesAndOtherActionsResetSequence(){
  try(ActivityController<Offline> c=Robolectric.buildActivity(Offline.class).setup()){
   Offline a=c.get();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();View decor=a.getWindow().getDecorView();
   a.onBackPressed();assertTrue(decor.findViewWithTag("navChannels").isFocused());assertFalse(a.isFinishing());
   press(a,KeyEvent.KEYCODE_DPAD_RIGHT);a.onBackPressed();assertFalse(a.isFinishing());assertTrue(decor.findViewWithTag("navChannels").isFocused());
   a.onBackPressed();assertTrue(a.isFinishing());
  }
 }

}
