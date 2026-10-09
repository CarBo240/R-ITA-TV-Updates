package it.carmine.streamplayer;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/** One original image, with a small blurred backdrop; no extra requests or full-size copies. */
final class ProgrammeArtwork extends ImageView {
 private Bitmap backdrop;
 private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 ProgrammeArtwork(Context c){super(c);setScaleType(ScaleType.FIT_CENTER);}
 @Override public void setImageBitmap(Bitmap bitmap){setImageDrawable(bitmap==null?null:new android.graphics.drawable.BitmapDrawable(getResources(),bitmap));}
 @Override public void setImageDrawable(Drawable image){
  if(getDrawable()==image)return;
  if(backdrop!=null){backdrop.recycle();backdrop=null;}
  if(image!=null&&image.getIntrinsicWidth()>0&&image.getIntrinsicHeight()>0){
   int w=image.getIntrinsicWidth(),h=image.getIntrinsicHeight();float scale=160f/Math.max(w,h);
   backdrop=Bitmap.createBitmap(Math.max(1,Math.round(w*scale)),Math.max(1,Math.round(h*scale)),Bitmap.Config.ARGB_8888);
   Canvas canvas=new Canvas(backdrop);Rect bounds=new Rect(image.getBounds());image.setBounds(0,0,backdrop.getWidth(),backdrop.getHeight());image.draw(canvas);image.setBounds(bounds);
   int[] pixels=new int[backdrop.getWidth()*backdrop.getHeight()];backdrop.getPixels(pixels,0,backdrop.getWidth(),0,0,backdrop.getWidth(),backdrop.getHeight());
   for(int pass=0;pass<2;pass++){pixels=blur(pixels,backdrop.getWidth(),backdrop.getHeight(),true);pixels=blur(pixels,backdrop.getWidth(),backdrop.getHeight(),false);}
   backdrop.setPixels(pixels,0,backdrop.getWidth(),0,0,backdrop.getWidth(),backdrop.getHeight());
  }
  super.setImageDrawable(image);
 }
 // A separable box blur over at most 160x160 pixels, independent of Android GPU effects.
 private static int[] blur(int[] input,int width,int height,boolean horizontal){
  int[] out=new int[input.length];int length=horizontal?width:height,lines=horizontal?height:width,radius=5,count=radius*2+1;
  for(int line=0;line<lines;line++){
   int a=0,r=0,g=0,b=0;
   for(int k=-radius;k<=radius;k++){int x=Math.max(0,Math.min(length-1,k)),v=input[horizontal?line*width+x:x*width+line];a+=v>>>24;r+=(v>>16)&255;g+=(v>>8)&255;b+=v&255;}
   for(int x=0;x<length;x++){
    out[horizontal?line*width+x:x*width+line]=(a/count<<24)|(r/count<<16)|(g/count<<8)|b/count;
    int remove=Math.max(0,x-radius),add=Math.min(length-1,x+radius+1);
    int old=input[horizontal?line*width+remove:remove*width+line],next=input[horizontal?line*width+add:add*width+line];
    a+=(next>>>24)-(old>>>24);r+=((next>>16)&255)-((old>>16)&255);g+=((next>>8)&255)-((old>>8)&255);b+=(next&255)-(old&255);
   }
  }return out;
 }
 @Override protected void onDraw(Canvas canvas){
  if(backdrop!=null&&getWidth()>0&&getHeight()>0){
   float scale=Math.max((float)getWidth()/backdrop.getWidth(),(float)getHeight()/backdrop.getHeight());float w=backdrop.getWidth()*scale,h=backdrop.getHeight()*scale;
   canvas.save();canvas.clipRect(0,0,getWidth(),getHeight());paint.setAlpha(255);paint.setShader(null);canvas.drawBitmap(backdrop,null,new RectF((getWidth()-w)/2,(getHeight()-h)/2,(getWidth()+w)/2,(getHeight()+h)/2),paint);
   paint.setColor(0x88000000);canvas.drawRect(0,0,getWidth(),getHeight(),paint);canvas.restore();
  }
  super.onDraw(canvas);
 }
}
