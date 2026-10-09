package it.carmine.streamplayer;

import android.content.*;import android.net.Uri;import org.json.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.Config;import java.util.*;import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=31)
public class Version128Test {
 @Before public void reset(){VodSettings.prefs(RuntimeEnvironment.getApplication()).edit().clear().commit();VodSession.reset();}

 @Test public void definitiveSourcesIgnorePersistentLegacyChoices(){Context c=RuntimeEnvironment.getApplication();List<VodSettings.Source> catalogues=VodSettings.catalogues(c);assertEquals("streamingcommunity",catalogues.get(0).id);assertEquals("tmdb",catalogues.get(1).id);assertEquals("vixsrc",catalogues.get(2).id);assertEquals("tmdb",VodSettings.catalogue(c).id);assertEquals("vixsrc",VodSettings.primary(c).id);VodSettings.prefs(c).edit().putString("catalogSource","streamingcommunity").putString("primarySource","streamingcommunity").commit();assertEquals("tmdb",VodSettings.catalogue(c).id);assertEquals("vixsrc",VodSettings.primary(c).id);}

 @Test public void vixCatalogueParsesUniqueTmdbIds()throws Exception{List<VixCatalog.Entry> list=VixCatalog.parse("[{\"tmdb_id\":42},{\"tmdb_id\":42},{\"tmdb_id\":0},{\"tmdb_id\":99}]","movie");assertEquals(2,list.size());assertEquals(42,list.get(0).id);assertEquals("movie",list.get(0).type);assertEquals(99,list.get(1).id);}

 @Test public void vixWebPlayerBlocksKnownAdsButNotVideoHosts(){assertTrue(VodBrowserActivity.blocked(Uri.parse("https://ads.doubleclick.net/banner.js")));assertTrue(VodBrowserActivity.blocked(Uri.parse("https://example.org/fmatrix.min.js")));assertFalse(VodBrowserActivity.blocked(Uri.parse("https://video.example.org/master.m3u8")));assertFalse(VodBrowserActivity.blocked(Uri.parse("https://vixsrc.to/movie/42")));}
}
