package it.carmine.streamplayer;
import android.content.Intent;import android.view.View;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.Config;import org.robolectric.android.controller.ActivityController;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk={31,35}) public class GeckoNavigationTest {
 public static class OfflineGecko extends GeckoEventActivity {protected boolean engineEnabled(){return false;}}
 @Test public void firstBackShowsMenuSecondBackReturnsToEvents(){try(ActivityController<OfflineGecko> c=Robolectric.buildActivity(OfflineGecko.class,new Intent().putExtra("url","https://example.test/hd3.php")).setup()) {OfflineGecko a=c.get();View decor=a.getWindow().getDecorView();View menu=decor.findViewWithTag("geckoMenu");assertEquals(View.GONE,menu.getVisibility());assertNotNull(decor.findViewWithTag("geckoCursor"));assertNull(decor.findViewWithTag("geckoEventPages"));a.onBackPressed();assertEquals(View.VISIBLE,menu.getVisibility());assertTrue(decor.findViewWithTag("geckoFullscreen").hasFocus());assertFalse(a.isFinishing());a.onBackPressed();assertTrue(a.isFinishing());}}
}
