package it.carmine.streamplayer;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import java.util.Locale;
/** Bundled generic sport symbols: they remain visible if a remote team badge is missing. */
final class SportIcons {
 static Drawable drawable(String sport){return new Icon(sport);}
 static final class Icon extends Drawable {
  final String kind;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  Icon(String sport){String s=sport==null?"":sport.toLowerCase(Locale.ROOT);kind=s.contains("soccer")||s.contains("calcio")?"soccer":s.contains("basket")?"basket":s.contains("tennis")?"tennis":s.contains("baseball")?"baseball":s.contains("american")||s.contains("football us")?"football":s.contains("rugby")?"rugby":s.contains("hockey")?"hockey":s.contains("cricket")?"cricket":s.contains("mma")||s.contains("box")||s.contains("combat")?"fight":s.contains("motor")||s.contains("formula")?"motor":s.contains("volley")||s.contains("pallavolo")?"volley":s.contains("cycl")||s.contains("cicl")?"cycling":"trophy";}
  public void draw(Canvas c){Rect b=getBounds();c.save();c.translate(b.left,b.top);c.scale(b.width()/64f,b.height()/64f);p.setStyle(Paint.Style.FILL);p.setColor(0xff202125);c.drawRoundRect(2,2,62,62,12,12,p);p.setColor(0xfff2b245);p.setStrokeWidth(2.2f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);
   switch(kind){
    case "soccer":c.drawCircle(32,32,20,p);Path pent=new Path();for(int i=0;i<5;i++){double a=-Math.PI/2+i*Math.PI*2/5;float x=32+(float)Math.cos(a)*8,y=32+(float)Math.sin(a)*8;if(i==0)pent.moveTo(x,y);else pent.lineTo(x,y);c.drawLine(x,y,32+(float)Math.cos(a)*20,32+(float)Math.sin(a)*20,p);}pent.close();c.drawPath(pent,p);break;
    case "basket":c.drawCircle(32,32,20,p);c.drawLine(12,32,52,32,p);c.drawLine(32,12,32,52,p);c.drawArc(4,12,35,52,-70,140,false,p);c.drawArc(29,12,60,52,110,140,false,p);break;
    case "tennis":c.save();c.rotate(-35,32,32);c.drawOval(20,9,44,39,p);for(int i=24;i<=40;i+=4)c.drawLine(i,13,i,35,p);for(int i=17;i<=33;i+=4)c.drawLine(23,i,41,i,p);c.drawLine(32,39,32,54,p);c.restore();break;
    case "baseball":c.drawCircle(32,32,20,p);c.drawArc(1,12,32,52,-70,140,false,p);c.drawArc(32,12,63,52,110,140,false,p);for(int i=19;i<=45;i+=6){c.drawLine(20,i,25,i+2,p);c.drawLine(39,i,44,i+2,p);}break;
    case "football":case "rugby":c.save();c.rotate(-35,32,32);c.drawOval(10,19,54,45,p);c.drawLine(20,32,44,32,p);for(int i=25;i<=39;i+=5)c.drawLine(i,27,i,37,p);c.restore();break;
    case "hockey":c.drawLine(16,11,37,42,p);c.drawLine(37,42,53,42,p);c.drawOval(12,44,31,53,p);break;
    case "cricket":c.save();c.rotate(-30,32,32);c.drawRoundRect(17,24,28,53,3,3,p);c.drawLine(22,24,22,11,p);c.restore();for(int i=38;i<=48;i+=5)c.drawLine(i,20,i,49,p);c.drawLine(36,20,50,20,p);break;
    case "fight":Path glove=new Path();glove.moveTo(22,51);glove.lineTo(19,32);glove.lineTo(16,31);glove.lineTo(16,24);glove.lineTo(23,24);glove.lineTo(23,15);glove.lineTo(45,15);glove.lineTo(48,21);glove.lineTo(48,38);glove.lineTo(42,51);glove.close();c.drawPath(glove,p);c.drawLine(23,24,23,35,p);c.drawLine(23,44,44,44,p);break;
    case "motor":c.drawLine(17,12,17,53,p);c.drawRect(17,13,51,36,p);p.setStyle(Paint.Style.FILL);for(int x=0;x<4;x++)for(int y=0;y<3;y++)if((x+y)%2==0)c.drawRect(18+x*8,14+y*7,26+x*8,21+y*7,p);break;
    case "volley":c.drawCircle(32,32,20,p);for(int a=0;a<3;a++){c.save();c.rotate(a*120,32,32);c.drawArc(12,12,52,52,190,110,false,p);c.drawLine(32,32,46,18,p);c.restore();}break;
    case "cycling":c.drawCircle(17,42,10,p);c.drawCircle(48,42,10,p);c.drawLine(17,42,28,24,p);c.drawLine(28,24,39,42,p);c.drawLine(17,42,39,42,p);c.drawLine(39,42,44,24,p);c.drawLine(28,24,44,24,p);c.drawLine(44,24,48,42,p);c.drawLine(40,19,45,19,p);break;
    default:c.drawRoundRect(21,12,43,36,4,4,p);c.drawArc(10,15,29,32,90,180,false,p);c.drawArc(35,15,54,32,-90,180,false,p);c.drawLine(32,36,32,49,p);c.drawLine(23,51,41,51,p);
   }c.restore();}
  public void setAlpha(int alpha){p.setAlpha(alpha);invalidateSelf();}public void setColorFilter(ColorFilter f){p.setColorFilter(f);invalidateSelf();}public int getOpacity(){return PixelFormat.TRANSLUCENT;}public int getIntrinsicWidth(){return 64;}public int getIntrinsicHeight(){return 64;}
 }
}
