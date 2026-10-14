package it.carmine.streamplayer;
import android.content.Context;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.Config;import java.util.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=31)
public class AlternativeProviders236Test {
 @Test public void oldProviderTypeDoesNotHideCataloguesAndManualUrlIsPreserved(){
  Context context=RuntimeEnvironment.getApplication();VodSettings.prefs(context).edit().clear().putBoolean("italianAlternatives235",true).putString("sources","{\"sources\":[{\"id\":\"manual_alta\",\"name\":\"AltaDefinizione\",\"url\":\"https://altadefinizionex.surf\",\"type\":\"streamingcommunity\"}]}").commit();
  List<VodSettings.Source> sources=VodSettings.sources(context);VodSettings.Source alta=null;for(VodSettings.Source source:sources)if(source.id.equals("manual_alta"))alta=source;
  assertNotNull(alta);assertEquals("https://altadefinizionex.surf",alta.url);assertEquals("web",alta.type);assertTrue(VodLabSources.supported(alta));assertFalse(alta.nativeCatalog());
  Set<String> names=new HashSet<>();for(VodSettings.Source source:VodSettings.catalogues(context))names.add(source.name);for(String name:Arrays.asList("GuardaFlix","GuardaSerie","StreamingIta","AnimeWorld","AnimeSaturn","AnimeUnity"))assertTrue(name,names.contains(name));
 }
}
