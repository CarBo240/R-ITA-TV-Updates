package it.carmine.streamplayer;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.RobolectricTestRunner;import org.robolectric.annotation.Config;
import java.util.*;import java.io.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=31)
public class GomstreamSourceTest {
 @Test public void catalogUsesCurrentItalianMetadataAndDeduplicates()throws Exception {String json="[{\"id\":\"stream-45\",\"title\":\"Cinema Italy\"},{\"id\":\"45\",\"title\":\"Cinema Italy\"},{\"id\":\"55\",\"title\":\"DAZN Zona IT\"},{\"id\":\"66\",\"title\":\"BBC UK\"},{\"id\":\"bad/id\",\"title\":\"Sport Italia\"}]";List<GomstreamSource.Channel> c=GomstreamSource.parseCatalog(json);assertEquals(2,c.size());assertTrue(c.stream().anyMatch(x->x.id.equals("55")));}
 private GomstreamSource.Transport pages(String edge){return url->{if(url.contains("embed.php"))return new GomstreamSource.Page(url,"const PLAYERS = [{\"src\":\"https://unrelated.test/player\"},{\"src\":\"https://gomstream-new.test/live/stream-45.php\"}];");if(url.contains("stream-45"))return new GomstreamSource.Page("https://gomstream-new.test/live/stream-45.php","<iframe src='https://player-new.test/gomstream.php?id=45'></iframe>");if(url.contains("gomstream.php"))return new GomstreamSource.Page(url,"const SRC = \"https://"+edge+"/premium45/index.m3u8\";");throw new IOException("unexpected URL "+url);};}
 @Test public void resolvesChangedPlayerAndCdnOnEveryCall()throws Exception {GomstreamSource.Channel c=new GomstreamSource.Channel("45","Cinema Italy");assertEquals("https://edge-one.test/premium45/index.m3u8",GomstreamSource.resolve(c,"https://catalog.test",GomstreamSource.HOME,pages("edge-one.test")));assertEquals("https://edge-two.test/premium45/index.m3u8",GomstreamSource.resolve(c,"https://catalog.test",GomstreamSource.HOME,pages("edge-two.test")));}
 @Test public void configuredNewDomainIsRecognizedWithoutBrandInHostname()throws Exception {assertEquals("https://new-source.test/live/stream-45.php",GomstreamSource.sourcePage("const PLAYERS=[{\"src\":\"https://new-source.test/live/stream-45.php\"}];","https://new-source.test"));}
 @Test public void redirectsAndRelativeIframePathsUseFinalPageUrl()throws Exception {assertEquals("https://new.test/video/index.m3u8",GomstreamSource.playlist(new GomstreamSource.Page("https://new.test/page/watch","const SRC = '/video/index.m3u8';")));assertEquals("https://new.test/watch/player?id=3&x=4",GomstreamSource.frames(new GomstreamSource.Page("https://new.test/watch/page","<iframe src='player?id=3&amp;x=4'></iframe>")).get(0));}
 @Test public void failsClearlyInsteadOfInventingOrReusingAUrl()throws Exception {try{GomstreamSource.sourcePage("const PLAYERS=[{\"src\":\"https://another.test\"}];",GomstreamSource.HOME);fail();}catch(IOException expected){}try{GomstreamSource.resolve(new GomstreamSource.Channel("../45","Italy"),"https://catalog.test",GomstreamSource.HOME,pages("edge.test"));fail();}catch(IOException expected){}for(String u:new String[]{"http://edge.test/a.m3u8","https://user:pass@edge.test/a.m3u8","file:///video"}){try{GomstreamSource.https(u);fail();}catch(IOException expected){}}}
 @Test public void recognizesGomstreamBrandAfterUnannouncedDomainChange()throws Exception {
  GomstreamSource.Transport http=url->{if(url.contains("embed.php"))return new GomstreamSource.Page(url,"const PLAYERS=[{\"src\":\"https://renamed.test/channel/45\"}];");return new GomstreamSource.Page(url,"const MARK='gomstream'; const SRC='https://new-edge.test/live/index.m3u8';");};
  assertEquals("https://new-edge.test/live/index.m3u8",GomstreamSource.resolve(new GomstreamSource.Channel("45","Italy"),"https://catalog.test",GomstreamSource.HOME,http));
 }
 @Test public void changedCatalogAddsAndRemovesChannelsAutomatically()throws Exception {
  GomstreamSource.Transport first=url->new GomstreamSource.Page(url,"[{\"id\":\"45\",\"title\":\"Old Italy\"}]");
  GomstreamSource.Transport next=url->new GomstreamSource.Page(url,"[{\"id\":\"55\",\"title\":\"New Italy\"}]");
  assertEquals("45",GomstreamSource.catalog("https://catalog.test",first).get(0).id);assertEquals("55",GomstreamSource.catalog("https://catalog.test",next).get(0).id);
 }

 @Test public void configuredDomainIsUsedEvenWhenOldEmbedHasNotUpdated()throws Exception {
  List<String> visited=new ArrayList<>();GomstreamSource.Transport http=url->{visited.add(url);if(url.contains("embed.php"))return new GomstreamSource.Page(url,"const PLAYERS=[{\"src\":\"https://gomstream.xyz/live/stream-45.php\"}];");if(url.startsWith("https://new-gom.test/"))return new GomstreamSource.Page(url,"const SRC='https://new-edge.test/45/index.m3u8';");throw new IOException("old domain offline");};
  assertEquals("https://new-edge.test/45/index.m3u8",GomstreamSource.resolve(new GomstreamSource.Channel("45","Cinema Italy"),"https://catalog.test","https://new-gom.test",http));assertFalse(visited.contains("https://gomstream.xyz/live/stream-45.php"));
 }

}
