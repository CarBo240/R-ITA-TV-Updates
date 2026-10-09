package it.carmine.streamplayer;

import android.view.*;
import androidx.media3.ui.PlayerView;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk={31,35},qualifiers="w800dp-h1280dp")
public class PlayerNavigationTest {
    public static class OfflineActivity extends MainActivity {
        @Override protected boolean imagesEnabled(){return false;}
        VavooClient.Channel lastRequested;
        @Override protected void load(){}
        @Override protected void loadEpg(boolean force){}
        @Override protected void resolveChannel(VavooClient.Channel c,int generation){lastRequested=c;}
    }
    @Test @org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
    public void channelListFocusIsVisibleAboveCardsAndMovesWithRemote()throws Exception{
        new android.app.Instrumentation().setInTouchMode(false);
        try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()){
            OfflineActivity a=c.get();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            VavooClient.Channel one=new VavooClient.Channel("Rai 1","Italy","one"),two=new VavooClient.Channel("Rai 2","Italy","two");
            java.lang.reflect.Field catalog=MainActivity.class.getDeclaredField("channels");catalog.setAccessible(true);catalog.set(a,new java.util.ArrayList<>(java.util.Arrays.asList(one,two)));
            a.play(one);a.onBackPressed();View decor=a.getWindow().getDecorView();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            decor.measure(View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));decor.layout(0,0,1280,720);
            android.widget.ListView list=decor.findViewWithTag("playerChannels");assertTrue(list.isDrawSelectorOnTop());assertTrue(list.requestFocus());list.setSelection(0);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            assertEquals(0,list.getSelectedItemPosition());android.graphics.drawable.Drawable selector=list.getSelector();selector.setState(new int[]{android.R.attr.state_focused});selector.setBounds(0,0,300,80);
            android.graphics.Bitmap image=android.graphics.Bitmap.createBitmap(300,80,android.graphics.Bitmap.Config.ARGB_8888);selector.draw(new android.graphics.Canvas(image));assertTrue(android.graphics.Color.red(image.getPixel(150,40))>android.graphics.Color.blue(image.getPixel(150,40)));assertEquals(UiTheme.AMBER,image.getPixel(150,0));
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN));a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DPAD_DOWN));Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(1,list.getSelectedItemPosition());assertTrue(list.isFocused());
            assertEquals(one.key(),a.lastRequested.key());
        }
    }
    @Test public void firstBackKeepsVideoSecondBackClosesPlayer(){
        try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()){
            OfflineActivity a=c.get();a.play(new VavooClient.Channel("Rai 1","Italy","test:1"));
            View decor=a.getWindow().getDecorView();PlayerView video=decor.findViewWithTag("video");assertNotNull(video);
            a.onBackPressed();assertEquals(View.VISIBLE,((View)decor.findViewWithTag("channelPanel")).getVisibility());
            assertSame(video,decor.findViewWithTag("video"));
            a.onBackPressed();assertNull(decor.findViewWithTag("player"));assertNull(decor.findViewWithTag("video"));
        }
    }
    @Test public void controlsHideAndChannelKeysWorkWithoutReplacingVideoView()throws Exception{
        try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()){
            OfflineActivity a=c.get();VavooClient.Channel one=new VavooClient.Channel("Rai 1","Italy","test:1");
            VavooClient.Channel two=new VavooClient.Channel("Rai 2","Italy","test:2");
            java.lang.reflect.Field f=MainActivity.class.getDeclaredField("visible");f.setAccessible(true);
            java.util.List<VavooClient.Channel> filtered=(java.util.List<VavooClient.Channel>)f.get(a);filtered.add(one);filtered.add(two);
            a.play(one);View decor=a.getWindow().getDecorView();View video=decor.findViewWithTag("video");
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(6,TimeUnit.SECONDS);
            assertEquals(View.GONE,((View)decor.findViewWithTag("playerControls")).getVisibility());
            assertEquals(View.GONE,((View)decor.findViewWithTag("playerHeader")).getVisibility());
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_CHANNEL_UP));
            assertEquals(two.key(),a.lastRequested.key());
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_CHANNEL_DOWN));
            assertEquals(one.key(),a.lastRequested.key());assertSame(video,decor.findViewWithTag("video"));
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(6,TimeUnit.SECONDS);
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT));assertEquals(one.key(),a.lastRequested.key());
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_LEFT));assertEquals(one.key(),a.lastRequested.key());
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_UP));assertEquals(two.key(),a.lastRequested.key());
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(6,TimeUnit.SECONDS);
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN));assertEquals(one.key(),a.lastRequested.key());
        }
    }
    @Test public void channelPanelReturnsToVideoAfterInactivityAndKeysExtendTimeout(){
        try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()){
            OfflineActivity a=c.get();a.play(new VavooClient.Channel("Rai 1","Italy","test:1"));
            View decor=a.getWindow().getDecorView(),video=decor.findViewWithTag("video"),panel=decor.findViewWithTag("channelPanel");
            a.onBackPressed();
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(7,TimeUnit.SECONDS);
            assertEquals(View.VISIBLE,panel.getVisibility());
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN));
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(7,TimeUnit.SECONDS);
            assertEquals(View.VISIBLE,panel.getVisibility());
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(2,TimeUnit.SECONDS);
            assertEquals(View.GONE,panel.getVisibility());assertSame(video,decor.findViewWithTag("video"));assertNotNull(decor.findViewWithTag("player"));
            a.onBackPressed();assertEquals(View.VISIBLE,panel.getVisibility());
            a.onBackPressed();assertNull(decor.findViewWithTag("player"));
        }
    }

    @Test public void categoriesInsidePlayerChangeZappingWithoutClosingVideo()throws Exception{
        try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()){
            OfflineActivity a=c.get();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            VavooClient.Channel rai=new VavooClient.Channel("Rai 1","Italy","one"),sport=new VavooClient.Channel("Sky Sport Uno","Italy","two");
            java.lang.reflect.Field catalog=MainActivity.class.getDeclaredField("channels");catalog.setAccessible(true);catalog.set(a,new java.util.ArrayList<>(java.util.Arrays.asList(rai,sport)));
            a.play(rai);a.onBackPressed();View decor=a.getWindow().getDecorView(),video=decor.findViewWithTag("video");
            android.widget.Spinner categories=decor.findViewWithTag("playerCategories");categories.setSelection(java.util.Arrays.asList(ChannelCategories.ALL).indexOf("Sport"));Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_CHANNEL_UP));assertEquals(sport.key(),a.lastRequested.key());assertSame(video,decor.findViewWithTag("video"));
            a.onBackPressed();android.widget.CheckBox favorites=decor.findViewWithTag("playerFavorites");favorites.setChecked(true);assertNotNull(decor.findViewWithTag("player"));assertSame(video,decor.findViewWithTag("video"));
        }
    }
    @Test public void seekControlsStayDisabledUntilStreamAllowsSeekingAndClampPositions()throws Exception{
        try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()){
            OfflineActivity a=c.get();a.play(new VavooClient.Channel("Rai 1","Italy","one"));View decor=a.getWindow().getDecorView();android.widget.Button back=decor.findViewWithTag("seekBack");assertFalse(back.isEnabled());
            java.lang.reflect.Field f=MainActivity.class.getDeclaredField("engine");f.setAccessible(true);androidx.media3.common.Player old=(androidx.media3.common.Player)f.get(a);((PlayerView)decor.findViewWithTag("video")).setPlayer(null);old.release();
            final boolean[] seekable={false};final long[] position={5000},sought={-1};
            androidx.media3.common.Player fake=(androidx.media3.common.Player)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{androidx.media3.common.Player.class},(proxy,m,args)->{
                switch(m.getName()){
                    case "getPlaybackState":return androidx.media3.common.Player.STATE_READY;
                    case "isCommandAvailable":return true;
                    case "isCurrentMediaItemSeekable":return seekable[0];
                    case "getCurrentPosition":return position[0];case "getDuration":return 20000L;
                    case "seekTo":sought[0]=(Long)args[0];return null;
                    case "getPlayWhenReady":return false;
                    case "equals":return proxy==args[0];case "hashCode":return System.identityHashCode(proxy);
                    default:return null;
                }
            });f.set(a,fake);java.lang.reflect.Field requested=MainActivity.class.getDeclaredField("requestedChannel");requested.setAccessible(true);requested.set(a,null);
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_REWIND));assertEquals(-1,sought[0]);
            seekable[0]=true;java.lang.reflect.Method update=MainActivity.class.getDeclaredMethod("updateSeekButtons");update.setAccessible(true);update.invoke(a);assertTrue(back.isEnabled());
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_REWIND));assertEquals(0,sought[0]);position[0]=15000;
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(6,TimeUnit.SECONDS);
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT));assertEquals(20000,sought[0]);
            position[0]=15000;Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(6,TimeUnit.SECONDS);
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_LEFT));assertEquals(5000,sought[0]);
            seekable[0]=false;update.invoke(a);assertFalse(back.isEnabled());sought[0]=-1;Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(6,TimeUnit.SECONDS);
            a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT));assertEquals(-1,sought[0]);
        }
    }

}
