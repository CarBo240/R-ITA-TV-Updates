package it.carmine.streamplayer;

import android.content.*;import android.view.*;import android.widget.*;
import org.json.*;import org.junit.*;import org.junit.runner.RunWith;
import org.robolectric.*;import org.robolectric.annotation.Config;import org.robolectric.android.controller.ActivityController;
import java.util.*;import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=31,qualifiers="w1280dp-h720dp-land")
public class Version127Test {
 @Before public void reset(){VodSettings.prefs(RuntimeEnvironment.getApplication()).edit().clear().commit();}

 @Test public void vixSrcIsAlwaysFirstAlternativeAndCanBePrimary(){Context c=RuntimeEnvironment.getApplication();List<VodSettings.Source> list=VodSettings.sources(c);assertEquals("streamingcommunity",list.get(0).id);assertEquals("vixsrc",list.get(1).id);assertEquals("scuapi",list.get(2).id);assertEquals("list_streamingunity_fun",list.get(3).id);VodSettings.choose(c,list.get(1));assertEquals("vixsrc",VodSettings.primary(c).id);assertEquals("streamingcommunity",VodSettings.sources(c).get(0).id);}

 @Test public void vixSrcBuildsOfficialMovieAndEpisodeUrls()throws Exception{VodSource.Title movie=TmdbClient.convert(new JSONObject().put("id",786892).put("title","Film"),"movie"),show=TmdbClient.convert(new JSONObject().put("id",1396).put("name","Serie"),"tv");assertEquals("https://vixsrc.to/movie/786892?lang=it&autoplay=true&primaryColor=D3A84C&secondaryColor=3A3020&startAt=61",VixSrc.movie(VixSrc.tmdbId(movie),61000));assertEquals("https://vixsrc.to/tv/1396/2/3?lang=it&autoplay=true&primaryColor=D3A84C&secondaryColor=3A3020",VixSrc.episode(VixSrc.tmdbId(show),2,3,0));assertNotNull(VodProviders.search(VodSettings.sources(RuntimeEnvironment.getApplication()).get(1),show,(u,r)->"").title);}

 @Test public void loginCanReachEveryControlBelowIt(){try(ActivityController<CloudActivity> ctl=Robolectric.buildActivity(CloudActivity.class).setup()){View root=ctl.get().getWindow().getDecorView();List<Button> buttons=new ArrayList<>();collect(root,buttons);int login=index(buttons,"Accedi"),last=index(buttons,"Configura cloud · incolla configurazione");assertTrue(login>=0&&last>login);View current=buttons.get(login);boolean reached=false;for(int i=0;i<16;i++){if(current instanceof Button&&"Configura cloud · incolla configurazione".contentEquals(((Button)current).getText())){reached=true;break;}int next=current.getNextFocusDownId();assertTrue(next!=View.NO_ID);current=root.findViewById(next);assertNotNull(current);}assertTrue(reached);}}

 private static int index(List<Button> list,String label){for(int i=0;i<list.size();i++)if(label.contentEquals(list.get(i).getText()))return i;return -1;}
 private static void collect(View v,List<Button> buttons){if(v instanceof Button)buttons.add((Button)v);if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)collect(((ViewGroup)v).getChildAt(i),buttons);}
}
