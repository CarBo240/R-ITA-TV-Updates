package it.carmine.streamplayer;
import android.content.*;import android.view.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.android.controller.ActivityController;import org.robolectric.annotation.Config;import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=31,qualifiers="w1280dp-h720dp-land")
public class Vod131NavigationTest {
 static class OfflineCatalog extends VodCatalogueActivity {protected void load(boolean refresh){}protected boolean imagesEnabled(){return false;}}
 @Before public void reset(){VodSession.reset();}
 @Test public void catalogueHasNoLegacyTopMenuAndDrawerIsOverlay(){Intent i=new Intent().putExtra("sectionKind","novita").putExtra("sectionTitle","Novità");try(ActivityController<OfflineCatalog> c=Robolectric.buildActivity(OfflineCatalog.class,i).setup()){View root=c.get().getWindow().getDecorView();assertNull(root.findViewWithTag("vodOptions"));assertNull(root.findViewWithTag("vodSources"));View drawer=root.findViewWithTag("vodSidebar");assertNotNull(drawer);assertEquals(View.GONE,drawer.getVisibility());}}
 @Test public void providerPageHasSearchAndOnlyTwoTypeFilters(){Intent i=new Intent().putExtra("sectionKind","platform").putExtra("sectionTitle","Netflix").putExtra("providerId",8).putExtra("providerName","Netflix");try(ActivityController<OfflineCatalog> c=Robolectric.buildActivity(OfflineCatalog.class,i).setup()){ViewGroup filters=c.get().getWindow().getDecorView().findViewWithTag("vodTypeFilters");assertEquals(2,filters.getChildCount());assertEquals(View.VISIBLE,c.get().getWindow().getDecorView().findViewWithTag("vodSearchBar").getVisibility());}}
 @Test public void settingsIsDedicatedAndBackReturns(){try(ActivityController<VodSettingsActivity> c=Robolectric.buildActivity(VodSettingsActivity.class).setup()){assertNotNull(c.get().getWindow().getDecorView().findViewWithTag("vodSettingsPage"));c.get().onBackPressed();assertTrue(c.get().isFinishing());}}
 @Test public void legacyPersistentValuesCannotReplaceDefinitiveDefaults(){Context c=RuntimeEnvironment.getApplication();VodSettings.prefs(c).edit().putString("catalogSource","streamingcommunity").putString("primarySource","streamingcommunity").commit();assertEquals("tmdb",VodSettings.catalogue(c).id);assertEquals("vixsrc",VodSettings.primary(c).id);}
}
