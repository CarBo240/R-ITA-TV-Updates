package it.carmine.streamplayer;

import android.view.*;
import android.widget.*;
import android.webkit.WebView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk={31,35},qualifiers="w1280dp-h720dp-mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class EventsActivityTest {
    public static class OfflineActivity extends EventsActivity {
        @Override protected boolean imagesEnabled(){return false;}
        @Override protected void loadEvents(boolean force){}
    }
    @Test public void sourceOpensAutomaticallyInGeckoAndPreservesFilters()throws Exception{
      try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()){
       OfflineActivity a=c.get();EventSource.Snapshot data=EventSource.parse(EventSourceTest.FEED,EventSource.HOME,1791053100000L);a.setSnapshot(data);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();View decor=a.getWindow().getDecorView();Spinner state=decor.findViewWithTag("eventStates");state.setSelection(1);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(1,((ListView)decor.findViewWithTag("eventsList")).getAdapter().getCount());
       EventSource.Event e=data.events.get(0);a.openSource(e,e.sources.get(1));android.content.Intent intent=Shadows.shadowOf(a).getNextStartedActivity();assertEquals(GeckoEventActivity.class.getName(),intent.getComponent().getClassName());assertEquals(e.sources.get(1).url,intent.getStringExtra("url"));assertNull(decor.findViewWithTag("eventWeb"));assertEquals(1,state.getSelectedItemPosition());assertFalse(a.isFinishing());
      }
    }
    @Test public void homeMenuOpensSeparateEventsActivity()throws Exception{
        try(ActivityController<StartupTest.OfflineActivity> c=Robolectric.buildActivity(StartupTest.OfflineActivity.class).setup()){
            StartupTest.OfflineActivity a=c.get();((View)a.getWindow().getDecorView().findViewWithTag("navEvents")).performClick();android.content.Intent intent=Shadows.shadowOf(a).getNextStartedActivity();assertEquals(EventsActivity.class.getName(),intent.getComponent().getClassName());
        }
    }
    @Test public void remoteCanSelectBothEventFiltersAndCancelWithoutChanging()throws Exception{
        try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()){
            OfflineActivity a=c.get();a.setSnapshot(EventSource.parse(EventSourceTest.FEED,EventSource.HOME,1791053100000L));Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            View decor=a.getWindow().getDecorView();
            for(String tag:new String[]{"eventSports","eventStates"}){
                Spinner spinner=decor.findViewWithTag(tag);assertNotNull(spinner);assertTrue(spinner.performClick());Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
                android.app.AlertDialog d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();
                assertTrue(d.getListView().hasFocus());
                key(d,KeyEvent.KEYCODE_DPAD_DOWN);assertEquals(0,spinner.getSelectedItemPosition());key(d,KeyEvent.KEYCODE_DPAD_CENTER);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
                assertEquals(1,spinner.getSelectedItemPosition());assertFalse(d.isShowing());assertTrue(spinner.hasFocus());
                spinner.performClick();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();key(d,KeyEvent.KEYCODE_DPAD_UP);d.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).performClick();assertEquals(1,spinner.getSelectedItemPosition());
                spinner.setSelection(0);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            }
        }
    }
    private static void key(android.app.AlertDialog dialog,int code){dialog.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,code));dialog.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,code));}

}
