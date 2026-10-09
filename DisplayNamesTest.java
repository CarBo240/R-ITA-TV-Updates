package it.carmine.streamplayer;
import org.junit.Test;import static org.junit.Assert.*;
public class DisplayNamesTest {
 @Test public void wordInitialsAreOnlyDisplayFormatting(){
  VavooClient.Channel source=new VavooClient.Channel("SKY CINEMA ACTION.s","Italy","one");String key=source.key();
  assertEquals("Sky Cinema Action",DisplayNames.channel(source.name));assertEquals("SKY CINEMA ACTION.s",source.name);assertEquals(key,source.key());
  assertEquals("Rai News 24 Hd",DisplayNames.channel("RAI NEWS 24 HD"));assertEquals("Dazn 1",DisplayNames.channel("DAZN 1"));assertEquals("È Tv",DisplayNames.channel("è TV"));assertEquals("",DisplayNames.channel(null));
 }
}
