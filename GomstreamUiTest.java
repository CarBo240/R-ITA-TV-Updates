package it.carmine.streamplayer;
import android.content.Intent;import android.graphics.*;import java.io.*;import android.view.*;import android.widget.*;import java.lang.reflect.*;import java.time.Duration;import java.util.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.Config;import org.robolectric.annotation.GraphicsMode;import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;import static org.robolectric.Shadows.shadowOf;
@RunWith(RobolectricTestRunner.class) @Config(sdk={31,35})
public class GomstreamUiTest {
 public static class OfflineCatalog extends GomstreamActivity {int loads;protected void load(){loads++;}}
 public static class OfflinePlayer extends GomstreamPlayerActivity {protected boolean engineEnabled(){return false;}}
 @Test public void refreshesOnReturnAndEveryTenMinutesButNotWhilePaused(){try(ActivityController<OfflineCatalog> c=Robolectric.buildActivity(OfflineCatalog.class).setup()){
  OfflineCatalog a=c.get();assertEquals(1,a.loads);shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMinutes(10));assertEquals(2,a.loads);c.pause();shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMinutes(11));assertEquals(2,a.loads);c.resume();assertEquals(3,a.loads);assertNotNull(a.getWindow().getDecorView().findViewWithTag("gomUrl"));
 }}
 @Test public void searchAndLaunchPassChannelIdentityInsteadOfACachedMediaUrl()throws Exception {try(ActivityController<OfflineCatalog> c=Robolectric.buildActivity(OfflineCatalog.class).setup()){
  OfflineCatalog a=c.get();Field f=GomstreamActivity.class.getDeclaredField("items");f.setAccessible(true);f.set(a,Arrays.asList(new GomstreamSource.Channel("45","Cinema Italy"),new GomstreamSource.Channel("55","Sport Italia")));Method filter=GomstreamActivity.class.getDeclaredMethod("filter");filter.setAccessible(true);filter.invoke(a);
  EditText search=a.getWindow().getDecorView().findViewWithTag("gomSearch");search.setText("Cinema");ListView list=a.getWindow().getDecorView().findViewWithTag("gomList");assertEquals(1,list.getCount());list.performItemClick(null,0,45);Intent next=shadowOf(a).getNextStartedActivity();assertEquals(GomstreamPlayerActivity.class.getName(),next.getComponent().getClassName());assertEquals("45",next.getStringExtra("id"));assertNull(next.getStringExtra("url"));
 }}
 @Test public void nativePlayerHasDoubleBackAndNoCursor(){try(ActivityController<OfflinePlayer> c=Robolectric.buildActivity(OfflinePlayer.class,new Intent().putExtra("id","45").putExtra("title","Cinema Italy")).setup()){
  OfflinePlayer a=c.get();View menu=a.getWindow().getDecorView().findViewWithTag("gomPlayerMenu");assertEquals(View.GONE,menu.getVisibility());a.onBackPressed();assertEquals(View.VISIBLE,menu.getVisibility());assertFalse(a.isFinishing());a.onBackPressed();assertTrue(a.isFinishing());assertNull(a.getWindow().getDecorView().findViewWithTag("geckoCursor"));
 }}
 @Test @GraphicsMode(GraphicsMode.Mode.NATIVE) @Config(qualifiers="w1280dp-h720dp-land") public void renderTvCatalog()throws Exception {try(ActivityController<OfflineCatalog> c=Robolectric.buildActivity(OfflineCatalog.class).setup()){
  OfflineCatalog a=c.get();Field f=GomstreamActivity.class.getDeclaredField("items");f.setAccessible(true);f.set(a,GomstreamSource.parseCatalog(new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of("tests/fixtures/gom-catalog.json")),"UTF-8")));Method filter=GomstreamActivity.class.getDeclaredMethod("filter");filter.setAccessible(true);filter.invoke(a);
  View root=a.getWindow().getDecorView();int w=1920,h=1080;root.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));root.layout(0,0,w,h);Bitmap image=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);root.draw(new Canvas(image));File dir=new File("build/previews");dir.mkdirs();try(OutputStream out=new FileOutputStream(new File(dir,"gomstream-tv.png"))){assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));}image.recycle();
 }}

}
