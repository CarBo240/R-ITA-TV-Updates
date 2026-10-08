package it.carmine.streamplayer;

import java.io.*;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=31)
public class EpgStoreTest {
    @Test public void matchesAliasesAndChangesNowAtExactProgrammeBoundary()throws Exception{
        String xml="<tv><channel id='news'><display-name>Rai News 24</display-name></channel>"
            +"<programme channel='news' start='20261003100000 +0200' stop='20261003110000 +0200'><title>Notizie</title></programme>"
            +"<programme channel='news' start='20261003110000 +0200' stop='20261003120000 +0200'><title>Speciale</title></programme></tv>";
        EpgStore epg=new EpgStore(RuntimeEnvironment.getApplication());long now=EpgStore.xmltvTime("20261003083000 +0000");
        epg.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")),now);
        epg.addAliases(new JSONObject().put("rai news 24","news"));
        VavooClient.Channel channel=new VavooClient.Channel("RAI NEWS 24 HD .s","Italy","test");
        assertEquals("Notizie",epg.now(channel,now).title);assertEquals("Speciale",epg.next(channel,now).title);
        assertEquals("Speciale",epg.now(channel,EpgStore.xmltvTime("20261003090000 +0000")).title);
        assertEquals(0,epg.guide(new VavooClient.Channel("Rai News 24","France","test")).size());
        assertNotEquals(EpgStore.normalize("Rai 1"),EpgStore.normalize("Rai 2"));
        assertNotEquals(EpgStore.normalize("Sky Uno"),EpgStore.normalize("Sky Uno +"));
    }
    @Test public void programmeArtworkAndChannelLogoRemainAssociatedWithCorrectIds()throws Exception{
        String xml="<tv><channel id='one'><display-name>Rai 1</display-name><icon src='https://example.test/rai1.png'/></channel><channel id='two'><display-name>Rai 2</display-name><icon src='https://example.test/rai2.png'/></channel>"
          +"<programme channel='one' start='20261003100000 +0200' stop='20261003110000 +0200'><title>Uno</title><icon src='https://example.test/one.jpg'/><desc>Trama uno</desc></programme>"
          +"<programme channel='two' start='20261003100000 +0200' stop='20261003110000 +0200'><title>Due</title><icon src='file:///private/image.png'/></programme></tv>";
        long now=EpgStore.xmltvTime("20261003083000 +0000");EpgStore epg=new EpgStore(RuntimeEnvironment.getApplication());epg.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")),now);
        VavooClient.Channel one=new VavooClient.Channel("Rai 1 HD","Italy","one"),two=new VavooClient.Channel("Rai 2","Italy","two");
        assertEquals("https://example.test/rai1.png",epg.logo(one));assertEquals("https://example.test/rai2.png",epg.logo(two));assertEquals("https://example.test/one.jpg",epg.now(one,now).image);assertEquals("Trama uno",epg.now(one,now).description);assertEquals("",epg.now(two,now).image);
    }

}
