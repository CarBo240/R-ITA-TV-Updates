package it.carmine.streamplayer;
import android.app.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35)
public class DaddyLiveSettingsTest {
    @Test public void homeLongPressValidatesSavesAndRestoresUrl(){
        Activity home=Robolectric.buildActivity(DaddyLiveTest.OfflineHome.class).setup().get();
        assertTrue(home.getWindow().getDecorView().findViewWithTag("navDaddyLive").performLongClick());
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();EditText input=dialog.getWindow().getDecorView().findViewWithTag("daddyHomeInput");assertNotNull(input);
        input.setText("http://bad.example");dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();assertTrue(dialog.isShowing());assertNotNull(input.getError());
        input.setText("New-Daddy.example/");dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();assertEquals("https://new-daddy.example",DaddyLiveSettings.home(home));
        assertEquals("https://new-daddy.example/player/embed.php?id=664",new DaddyLiveSource.Link("Sky","664").embedUrl(DaddyLiveSettings.home(home)));
        DaddyLiveSettings.edit(home,null);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();dialog.getButton(AlertDialog.BUTTON_NEUTRAL).performClick();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(DaddyLiveSource.HOME,DaddyLiveSettings.home(home));home.finish();
    }
    @Test public void visibleUrlButtonWorksOnTablet(){
        Activity section=Robolectric.buildActivity(DaddyLiveTest.OfflineDaddy.class).setup().get();assertTrue(section.getWindow().getDecorView().findViewWithTag("daddyUrl").performClick());AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertNotNull(dialog.getWindow().getDecorView().findViewWithTag("daddyHomeInput"));section.finish();
    }
    @Test public void rejectsCredentialsAndApiPaths(){for(String value:new String[]{"javascript:alert(1)","https://user:pass@example.com","https://example.com/channel","https://example.com?x=1","https://example.com#x","https://example.com:99999"})assertEquals(value,"",DaddyLiveSettings.normalize(value));}
    @Test public void adsAreBlockedWithoutBlockingVideoOrLookalikeHostnames(){
        WebAdPolicy policy=new WebAdPolicy(RuntimeEnvironment.getApplication());assertTrue(policy.blocked("https://sub.doubleclick.net/ad.js"));assertTrue(policy.blocked("https://chewsever.com/ad.js"));assertFalse(policy.blocked("https://notdoubleclick.net/video.m3u8"));assertFalse(policy.blocked("https://video.cloudfront.net/live.m3u8"));
        String initial="https://new-daddy.example/player/embed.php?id=664";assertFalse(policy.allowNavigation("https://advert.example/click",initial,true));assertTrue(policy.allowNavigation("https://new-daddy.example/player/embed.php?id=664&player=2",initial,true));assertTrue(policy.allowNavigation("https://player.example/embed/664",initial,false));assertFalse(policy.allowNavigation("intent://external",initial,false));
    }
}
