package it.carmine.streamplayer;

import android.content.*;import android.view.*;import android.widget.*;
import org.json.*;import org.junit.*;import org.junit.runner.RunWith;
import org.robolectric.*;import org.robolectric.annotation.Config;import org.robolectric.android.controller.ActivityController;
import java.util.*;import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=31,qualifiers="w1280dp-h720dp-land")
public class Version126Test {
 public static class Catalog extends VodCatalogueActivity {protected void load(boolean refresh){}protected boolean imagesEnabled(){return false;}}

 @Test public void moreIsBelowCatalogueAndNotBesideSearch(){try(ActivityController<Catalog> ctl=Robolectric.buildActivity(Catalog.class).setup()){
  View root=ctl.get().getWindow().getDecorView(),more=root.findViewWithTag("vodMore"),search=root.findViewWithTag("vodSearch"),grid=root.findViewWithTag("vodGrid");
  assertNotNull(more);assertNotNull(search);assertNotNull(grid);assertNotSame(search.getParent(),more.getParent());assertSame(grid.getParent(),more.getParent());assertEquals(1,((ViewGroup)more.getParent()).indexOfChild(more));
 }}

 @Test public void recentKeepsProviderOrderAndAlphabeticalSortIsOptional()throws Exception{
  List<VodSource.Title> source=new ArrayList<>();source.add(title("2","Zulu"));source.add(title("1","Alpha"));
  assertEquals("Zulu",VodCatalogue.filter(source,"","",true).get(0).name);assertEquals("Alpha",VodCatalogue.filter(source,"","",false).get(0).name);
 }

 @Test public void passwordDownMovesToLoginAndLoginUpReturnsToPassword(){try(ActivityController<CloudActivity> ctl=Robolectric.buildActivity(CloudActivity.class).setup()){
  View root=ctl.get().getWindow().getDecorView();List<EditText> fields=new ArrayList<>();List<Button> buttons=new ArrayList<>();collect(root,fields,buttons);EditText password=fields.get(1);Button login=null;for(Button b:buttons)if("Accedi".contentEquals(b.getText()))login=b;assertNotNull(login);
  password.requestFocus();password.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN));assertTrue(login.hasFocus());login.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_UP));assertTrue(password.hasFocus());
 }}

 @Test public void startupChoiceIsRememberedPerDevice(){Context c=RuntimeEnvironment.getApplication();VodSettings.prefs(c).edit().clear().commit();assertFalse(VodSettings.startsWithVod(c));VodSettings.prefs(c).edit().putBoolean("startWithVod",true).commit();assertTrue(VodSettings.startsWithVod(c));}

 private static VodSource.Title title(String id,String name)throws Exception{return new VodSource.Title(new JSONObject().put("id",id).put("slug",name.toLowerCase(Locale.ROOT)).put("name",name),"");}
 private static void collect(View v,List<EditText> fields,List<Button> buttons){if(v instanceof EditText)fields.add((EditText)v);if(v instanceof Button)buttons.add((Button)v);if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)collect(((ViewGroup)v).getChildAt(i),fields,buttons);}
}
