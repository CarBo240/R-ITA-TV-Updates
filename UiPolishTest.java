package it.carmine.streamplayer;
import android.graphics.*;
import android.widget.Button;
import org.robolectric.RuntimeEnvironment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk={31,35}) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class UiPolishTest {
 @Test public void navigationHasOnlyUnderlineAndIgnoresSelectedSection(){
  Button button=new Button(RuntimeEnvironment.getApplication());UiTheme.navigation(button);
  button.setSelected(true);assertEquals(UiTheme.TEXT,button.getTextColors().getColorForState(new int[]{android.R.attr.state_selected},0));
  assertEquals(UiTheme.AMBER,button.getTextColors().getColorForState(new int[]{android.R.attr.state_focused},0));
  android.graphics.drawable.Drawable d=button.getBackground();d.setState(new int[]{android.R.attr.state_focused});d.setBounds(0,0,120,40);
  Bitmap b=Bitmap.createBitmap(120,40,Bitmap.Config.ARGB_8888);d.draw(new Canvas(b));assertEquals(Color.TRANSPARENT,b.getPixel(60,20));assertEquals(UiTheme.AMBER,b.getPixel(60,39));
  d.setState(new int[]{});b.eraseColor(Color.TRANSPARENT);d.draw(new Canvas(b));assertEquals(Color.TRANSPARENT,b.getPixel(60,39));
 }
 @Test public void posterStaysWholeWithBackdropAndRefreshesWithoutRecyclingOriginal(){
  ProgrammeArtwork view=new ProgrammeArtwork(RuntimeEnvironment.getApplication());
  Bitmap poster=Bitmap.createBitmap(40,80,Bitmap.Config.ARGB_8888);Canvas input=new Canvas(poster);input.drawColor(Color.RED);Paint paint=new Paint();paint.setColor(Color.BLUE);input.drawRect(0,40,40,80,paint);
  view.setImageBitmap(poster);view.layout(0,0,400,224);Bitmap output=Bitmap.createBitmap(400,224,Bitmap.Config.ARGB_8888);view.draw(new Canvas(output));
  assertEquals(android.widget.ImageView.ScaleType.FIT_CENTER,view.getScaleType());assertEquals(Color.RED,output.getPixel(200,20));assertEquals(Color.BLUE,output.getPixel(200,200));assertTrue((output.getPixel(10,20)&0x00ffffff)!=0);
  Bitmap second=Bitmap.createBitmap(40,80,Bitmap.Config.ARGB_8888);second.eraseColor(Color.GREEN);view.setImageBitmap(second);assertFalse(poster.isRecycled());view.draw(new Canvas(output));assertEquals(Color.GREEN,output.getPixel(200,20));assertTrue(Color.green(output.getPixel(10,20))>0);assertEquals(0,Color.red(output.getPixel(10,20)));
  view.setImageDrawable(null);output.eraseColor(Color.TRANSPARENT);view.draw(new Canvas(output));assertEquals(Color.TRANSPARENT,output.getPixel(10,20));assertFalse(second.isRecycled());
 }
}
