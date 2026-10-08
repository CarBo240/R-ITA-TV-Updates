package it.carmine.streamplayer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.json.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35)
public class RequestGuardTest {
 @Test public void retryAfterSurvivesRestartAndIsRespectedAcrossHosts()throws Exception{
  long[] now={100000};android.content.SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences("guard-test",0);prefs.edit().clear().commit();
  RequestGuard g=new RequestGuard(prefs,()->now[0]);g.failure("first",429,"120");
  RequestGuard restored=new RequestGuard(prefs,()->now[0]);try{restored.check("other");fail();}catch(RequestGuard.Wait expected){}
  now[0]+=119000;try{restored.check("first");fail();}catch(RequestGuard.Wait expected){}
  now[0]+=1000;restored.check("other");
 }
 @Test public void repeatedNetworkErrorsBackOffAndSuccessResetsThem()throws Exception{
  long[] now={100000};RequestGuard g=new RequestGuard(null,()->now[0]);g.failure("host",0,null);g.check("host");g.failure("host",0,null);
  try{g.check("host");fail();}catch(RequestGuard.Wait expected){}g.check("alternate");now[0]+=15000;g.check("host");g.success("host");g.failure("host",0,null);g.check("host");
 }
 @Test public void accessDenialStopsFallbackAndRepeatedRequests()throws Exception{
  int[] requests={0};VavooClient c=new VavooClient("test",(url,p,sig)->{requests[0]++;if(url.endsWith("ping"))return new JSONObject().put("addonSig","test");throw new VavooClient.HttpFailure(403,null);});
  try{c.catalog(null);fail();}catch(VavooClient.HttpFailure expected){}assertEquals(2,requests[0]);
  try{c.catalog(null);fail();}catch(RequestGuard.Wait expected){}assertEquals(2,requests[0]);
 }
 @Test public void serviceSessionIsReusedForMultipleChannels()throws Exception{
  int[] pings={0},resolves={0};VavooClient c=new VavooClient("test",(url,p,sig)->{if(url.endsWith("ping")){pings[0]++;return new JSONObject().put("addonSig","test");}resolves[0]++;return new JSONObject().put("url","https://example.test/video.m3u8");});
  c.resolve(new VavooClient.Channel("Rai 1","Italy","one"));c.resolve(new VavooClient.Channel("Rai 2","Italy","two"));assertEquals(1,pings[0]);assertEquals(2,resolves[0]);
 }
}
