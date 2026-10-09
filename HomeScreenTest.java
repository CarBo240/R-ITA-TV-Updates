package it.carmine.streamplayer;

import android.graphics.*;
import android.view.*;
import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="w800dp-h1100dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class HomeScreenTest {
    public static class OfflineActivity extends MainActivity {
        @Override protected boolean imagesEnabled(){return false;}
        @Override protected void load(){}
        @Override protected void loadEpg(boolean force){}
    }
    private void field(MainActivity a,String name,Object value)throws Exception{
        Field f=MainActivity.class.getDeclaredField(name);f.setAccessible(true);f.set(a,value);
    }
    private String contents(View v){StringBuilder b=new StringBuilder();if(v instanceof android.widget.TextView)b.append(((android.widget.TextView)v).getText()).append('\n');
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)b.append(contents(g.getChildAt(i)));}return b.toString();}
    private String stamp(long t){SimpleDateFormat f=new SimpleDateFormat("yyyyMMddHHmmss Z",Locale.US);f.setTimeZone(TimeZone.getTimeZone("UTC"));return f.format(new Date(t));}
    private void screenshot(View root,int width,int height,String name)throws Exception{
        root.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));root.layout(0,0,width,height);
        Bitmap b=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);root.draw(new Canvas(b));
        File dir=new File("build/previews");dir.mkdirs();try(OutputStream out=new FileOutputStream(new File(dir,name))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();
    }
    @Test public void onlyItalianChannelsAndProgrammeDetailsAppearOnHome()throws Exception{
        new android.app.Instrumentation().setInTouchMode(false);
        try(ActivityController<OfflineActivity> controller=Robolectric.buildActivity(OfflineActivity.class).setup()){
            OfflineActivity a=controller.get();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();long now=System.currentTimeMillis();
            String[] names={"Rai 1","Sky Sport Uno","Canale 5","Rai News 24","Sky Cinema Uno"};
            String[] shows={"Linea Blu","Il grande sport in diretta","Un appuntamento da non perdere","Notizie e approfondimenti","Il cinema a casa tua"};
            StringBuilder xml=new StringBuilder("<tv>");List<VavooClient.Channel> catalog=new ArrayList<>();
            for(int i=0;i<names.length;i++){
                catalog.add(new VavooClient.Channel(names[i],"Italy","test:"+i));xml.append("<channel id='c").append(i).append("'><display-name>").append(names[i]).append("</display-name></channel>");
                xml.append("<programme channel='c").append(i).append("' start='").append(stamp(now-1800000)).append("' stop='").append(stamp(now+1800000)).append("'><title>").append(shows[i]).append("</title><desc>Trama del programma ").append(names[i]).append(": viaggio tra storie, luoghi e protagonisti.</desc><icon src='https://example.test/programme.jpg'/></programme>");
                xml.append("<programme channel='c").append(i).append("' start='").append(stamp(now+1800000)).append("' stop='").append(stamp(now+5400000)).append("'><title>Il prossimo appuntamento</title></programme>");
            }
            catalog.add(new VavooClient.Channel("BBC One","United Kingdom","foreign"));xml.append("</tv>");
            EpgStore epg=new EpgStore(a);epg.parse(new ByteArrayInputStream(xml.toString().getBytes("UTF-8")),now);
            field(a,"channels",catalog);field(a,"epg",epg);
            Method filter=MainActivity.class.getDeclaredMethod("filter");filter.setAccessible(true);filter.invoke(a);
            ViewGroup content=a.findViewById(android.R.id.content);View root=content.getChildAt(0);
            screenshot(root,800,1100,"home-tablet.png");String text=contents(root);
            assertTrue(text.contains("Trama del programma Rai 1"));assertTrue(text.contains("Rai 1"));assertTrue(text.contains("Linea Blu"));assertTrue(text.contains("Il prossimo appuntamento"));
            assertFalse(text.contains("BBC One"));assertFalse(text.contains("Tutti i paesi"));
            screenshot(root,390,844,"home-phone.png");
            screenshot(root,1280,720,"home-tv.png");
            assertFalse(contents(root).contains("TELEVISIONE ITALIANA"));assertFalse(contents(root).contains("● DIRETTA"));
            android.widget.ImageView programmeImage=root.findViewWithTag("guideImage");assertEquals(android.widget.ImageView.ScaleType.FIT_CENTER,programmeImage.getScaleType());
            ViewGroup navigation=root.findViewWithTag("homeNavigation");assertTrue(contents(navigation).contains("↻"));assertFalse(contents(navigation).contains("EPG"));assertEquals(8,navigation.getChildCount());assertEquals(android.widget.LinearLayout.HORIZONTAL,((android.widget.LinearLayout)navigation).getOrientation());assertTrue(((android.widget.TextView)navigation.getChildAt(2)).getText().toString().contains("Eventi"));assertTrue(((android.widget.TextView)navigation.getChildAt(3)).getText().toString().contains("Daddy"));assertTrue(navigation.getChildAt(5).isLongClickable());assertEquals("VOD",((android.widget.TextView)navigation.getChildAt(6)).getText().toString());assertTrue(contents(navigation).contains("Preferiti"));
            View channels=root.findViewWithTag("homeChannels"),guide=root.findViewWithTag("homeGuide");
            View channelColumn=root.findViewWithTag("homeChannelColumn");assertTrue(channelColumn.getRight()<guide.getLeft());assertTrue(channelColumn.getWidth()>guide.getWidth());assertEquals(channelColumn.getTop(),guide.getTop());assertTrue(channels.getTop()>guide.getTop());
            android.widget.ListView list=(android.widget.ListView)channels;assertTrue(list.requestFocus());assertFalse(navigation.getChildAt(0).isSelected());assertFalse(navigation.getChildAt(0).isFocused());Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            final int[] redraws={0};list.getAdapter().registerDataSetObserver(new android.database.DataSetObserver(){@Override public void onChanged(){redraws[0]++;}});
            list.setSelection(1);root.requestLayout();screenshot(root,1280,720,"home-tv.png");Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            root.requestLayout();screenshot(root,1280,720,"home-tv.png");Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            assertEquals(0,redraws[0]);assertEquals("Sky Sport Uno",((android.widget.TextView)root.findViewWithTag("guideChannelName")).getText().toString());
            assertTrue(((android.widget.TextView)root.findViewWithTag("guideDescription")).getText().toString().contains("Trama del programma Sky Sport Uno"));
            android.widget.Spinner categories=root.findViewWithTag("categories");
            android.graphics.drawable.Drawable before=categories.getBackground();assertTrue(categories.requestFocus());
            assertTrue(categories.isFocused());assertNotSame(before,categories.getBackground());
            java.io.File fixture=new java.io.File("tests/fixtures/programme.jpg");if(fixture.isFile()){android.graphics.Bitmap b=android.graphics.BitmapFactory.decodeFile(fixture.getAbsolutePath());if(b!=null)((android.widget.ImageView)root.findViewWithTag("guideImage")).setImageBitmap(b);}
            screenshot(root,1280,720,"home-tv-focused.png");
            field(a,"selectedCategory","Musica");filter.invoke(a);
            assertEquals("Nessun canale",((android.widget.TextView)root.findViewWithTag("guideChannelName")).getText().toString());
            assertFalse(((android.widget.TextView)root.findViewWithTag("guideDescription")).getText().toString().contains("Sky Sport Uno"));
        }
    }
}
