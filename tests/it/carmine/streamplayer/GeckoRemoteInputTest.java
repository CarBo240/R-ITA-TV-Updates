package it.carmine.streamplayer;

import android.content.Intent;
import android.view.*;
import java.time.Duration;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class) @Config(sdk={31,35},qualifiers="w1280dp-h720dp-land-mdpi")
public class GeckoRemoteInputTest {
 public static class Offline extends GeckoEventActivity {
  final List<MotionEvent> events=new ArrayList<>();
  protected boolean engineEnabled(){return false;}
  protected void sendRemotePointer(MotionEvent e,boolean mouse){events.add(MotionEvent.obtain(e));}
 }
 private ActivityController<Offline> open(boolean daddy){
  ActivityController<Offline> c=Robolectric.buildActivity(Offline.class,new Intent().putExtra("url","https://example.test/player").putExtra("daddyLive",daddy)).setup();
  View root=c.get().getWindow().getDecorView();root.measure(View.MeasureSpec.makeMeasureSpec(1920,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1080,View.MeasureSpec.EXACTLY));root.layout(0,0,1920,1080);shadowOf(android.os.Looper.getMainLooper()).idle();return c;
 }
 private void press(Offline a,int key){a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,key));a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,key));}
 @Test public void daddyMovementHoversAndOkIsSingleTouchTap(){try(ActivityController<Offline> c=open(true)){
  Offline a=c.get();press(a,KeyEvent.KEYCODE_DPAD_RIGHT);
  assertEquals(MotionEvent.ACTION_HOVER_ENTER,a.events.get(0).getActionMasked());
  assertEquals(MotionEvent.ACTION_HOVER_MOVE,a.events.get(1).getActionMasked());
  press(a,KeyEvent.KEYCODE_DPAD_CENTER);shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(1));
  MotionEvent down=a.events.stream().filter(e->e.getActionMasked()==MotionEvent.ACTION_DOWN).findFirst().get();
  assertEquals(InputDevice.SOURCE_TOUCHSCREEN,down.getSource());assertEquals(MotionEvent.TOOL_TYPE_FINGER,down.getToolType(0));assertEquals(0,down.getButtonState());
  press(a,KeyEvent.KEYCODE_DPAD_RIGHT);shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(100));
  MotionEvent up=a.events.stream().filter(e->e.getActionMasked()==MotionEvent.ACTION_UP).findFirst().get();
  assertEquals(down.getX(),up.getX(),0);assertEquals(down.getY(),up.getY(),0);assertEquals(down.getDownTime(),up.getDownTime());assertEquals(0,up.getButtonState());
  assertEquals(1,a.events.stream().filter(e->e.getActionMasked()==MotionEvent.ACTION_DOWN).count());
  shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(4));
  assertEquals(View.GONE,a.getWindow().getDecorView().findViewWithTag("geckoCursor").getVisibility());assertEquals(MotionEvent.ACTION_HOVER_EXIT,a.events.get(a.events.size()-1).getActionMasked());
 }}
 @Test public void otherEventsKeepTouchInput(){try(ActivityController<Offline> c=open(false)){
  Offline a=c.get();press(a,KeyEvent.KEYCODE_DPAD_LEFT);assertTrue(a.events.isEmpty());press(a,KeyEvent.KEYCODE_DPAD_CENTER);shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(100));
  assertEquals(2,a.events.size());assertEquals(InputDevice.SOURCE_TOUCHSCREEN,a.events.get(0).getSource());assertEquals(MotionEvent.TOOL_TYPE_FINGER,a.events.get(0).getToolType(0));assertEquals(MotionEvent.ACTION_UP,a.events.get(1).getActionMasked());
 }}
 @Test public void backExitsExpandedPlayerBeforeOpeningMenu()throws Exception{try(ActivityController<Offline> c=open(true)){
  Offline a=c.get();java.lang.reflect.Field full=GeckoEventActivity.class.getDeclaredField("pageFullscreen");full.setAccessible(true);full.setBoolean(a,true);
  a.onBackPressed();assertFalse(full.getBoolean(a));assertFalse(a.isFinishing());assertEquals(View.GONE,a.getWindow().getDecorView().findViewWithTag("geckoMenu").getVisibility());
  a.onBackPressed();assertEquals(View.VISIBLE,a.getWindow().getDecorView().findViewWithTag("geckoMenu").getVisibility());
 }}
 @Test public void backAndMenuEndHover(){try(ActivityController<Offline> c=open(true)){
  Offline a=c.get();press(a,KeyEvent.KEYCODE_DPAD_UP);a.onBackPressed();assertEquals(MotionEvent.ACTION_HOVER_EXIT,a.events.get(a.events.size()-1).getActionMasked());assertFalse(a.isFinishing());a.onBackPressed();assertTrue(a.isFinishing());
 }}
 @Test public void bothPlayerCornersAreReachable(){try(ActivityController<Offline> c=open(true)){
  Offline a=c.get();
  for(int key:new int[]{KeyEvent.KEYCODE_DPAD_LEFT,KeyEvent.KEYCODE_DPAD_DOWN}){a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,key));shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(2));a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,key));}
  press(a,KeyEvent.KEYCODE_DPAD_CENTER);shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(100));
  MotionEvent bottom=a.events.stream().filter(e->e.getActionMasked()==MotionEvent.ACTION_DOWN).findFirst().get();assertEquals(1,bottom.getX(),0);View browser=((ViewGroup)a.getWindow().getDecorView().findViewWithTag("geckoCursor").getParent()).getChildAt(0);assertEquals(browser.getHeight()-1,bottom.getY(),0);
  a.events.clear();
  for(int key:new int[]{KeyEvent.KEYCODE_DPAD_RIGHT,KeyEvent.KEYCODE_DPAD_UP}){a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,key));shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(2));a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,key));}
  press(a,KeyEvent.KEYCODE_DPAD_CENTER);shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(100));
  MotionEvent top=a.events.stream().filter(e->e.getActionMasked()==MotionEvent.ACTION_DOWN).findFirst().get();assertEquals(browser.getWidth()-1,top.getX(),0);assertEquals(1,top.getY(),0);
 }}

 @Test public void holdingKeyMovesWithoutKeyRepeatsAndStopsOnReleaseOrMenu(){try(ActivityController<Offline> c=open(true)){
  Offline a=c.get();a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT));float initial=a.events.get(a.events.size()-1).getX();shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(400));float moved=a.events.get(a.events.size()-1).getX();assertTrue("Held movement: "+initial+" -> "+moved,moved>initial+100);assertTrue("Hover events: "+a.events.size(),a.events.size()<20);
  a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DPAD_RIGHT));int events=a.events.size();shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(500));assertEquals(events,a.events.size());
  a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_LEFT));a.onBackPressed();events=a.events.size();shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(1));assertEquals(events,a.events.size());
 }}
 @Test public void mouseFallbackPersistsAndPauseCancelsTap(){try(ActivityController<Offline> c=open(true)){
  Offline a=c.get();a.onBackPressed();a.getWindow().getDecorView().findViewWithTag("geckoClickMode").performClick();press(a,KeyEvent.KEYCODE_MENU);press(a,KeyEvent.KEYCODE_DPAD_CENTER);
  MotionEvent down=a.events.stream().filter(e->e.getActionMasked()==MotionEvent.ACTION_DOWN).findFirst().get();assertEquals(InputDevice.SOURCE_MOUSE,down.getSource());assertEquals(MotionEvent.BUTTON_PRIMARY,down.getButtonState());c.pause();assertTrue(a.events.stream().anyMatch(e->e.getActionMasked()==MotionEvent.ACTION_CANCEL));assertFalse(a.events.stream().anyMatch(e->e.getActionMasked()==MotionEvent.ACTION_UP));
  c.resume();c.recreate();a=c.get();assertEquals("Clic: mouse",((android.widget.Button)a.getWindow().getDecorView().findViewWithTag("geckoClickMode")).getText().toString());
 }}

}
