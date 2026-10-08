package it.carmine.streamplayer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk={31,35}, qualifiers="w800dp-h1280dp")
public class StartupTest {
    public static class OfflineActivity extends MainActivity {
        @Override protected boolean imagesEnabled(){return false;}
        @Override protected void loadEpg(boolean force) {}
        @Override protected void load() { /* Startup must not depend on a live server. */ }
    }
    private String contents(View view) {
        StringBuilder b=new StringBuilder();
        if(view instanceof TextView)b.append(((TextView)view).getText()).append('\n');
        if(view instanceof ViewGroup){ViewGroup g=(ViewGroup)view;
            for(int i=0;i<g.getChildCount();i++)b.append(contents(g.getChildAt(i)));}
        return b.toString();
    }
    @Test public void tabletStartupDisplaysCatalogControlsWithoutFailure() {
        try(ActivityController<OfflineActivity> c=Robolectric.buildActivity(OfflineActivity.class).setup()) {
            String text=contents(c.get().getWindow().getDecorView());
            assertFalse("Startup failed:\n"+text,text.contains("errore di avvio"));
            assertFalse(text.contains("R. ITA TV"));assertTrue(text.contains("Canali"));
            assertTrue(text.contains("Preferiti"));
            assertTrue(text.contains("↻"));
        }
    }
}
