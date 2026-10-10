package it.carmine.streamplayer;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Thin vector drawer icons. They stay sharp on TV, tablet and Fire TV. */
final class VodMenuIcon extends Drawable {
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final int kind;private int color;
 VodMenuIcon(int kind,int color){this.kind=kind;this.color=color;p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
 void color(int value){color=value;invalidateSelf();}
 @Override public int getIntrinsicWidth(){return 28;}@Override public int getIntrinsicHeight(){return 28;}
 @Override public void draw(Canvas c){Rect b=getBounds();float s=Math.min(b.width(),b.height())/28f,x=b.centerX()-14*s,y=b.centerY()-14*s;p.setColor(color);p.setStrokeWidth(1.8f*s);Path q=new Path();
  switch(kind){
   case 0:c.drawRoundRect(x+3*s,y+5*s,x+25*s,y+21*s,3*s,3*s,p);c.drawLine(x+10*s,y+25*s,x+18*s,y+25*s,p);c.drawLine(x+14*s,y+21*s,x+14*s,y+25*s,p);break;
   case 1:c.drawLine(x+6*s,y+22*s,x+19*s,y+7*s,p);c.drawLine(x+7*s,y+17*s,x+12*s,y+22*s,p);star(c,x+21*s,y+7*s,3*s);star(c,x+7*s,y+7*s,2*s);break;
   case 2:c.drawRoundRect(x+4*s,y+6*s,x+24*s,y+24*s,3*s,3*s,p);c.drawLine(x+4*s,y+11*s,x+24*s,y+11*s,p);c.drawLine(x+9*s,y+3*s,x+9*s,y+8*s,p);c.drawLine(x+19*s,y+3*s,x+19*s,y+8*s,p);break;
   case 3:c.drawRoundRect(x+4*s,y+4*s,x+24*s,y+24*s,3*s,3*s,p);for(int i=0;i<3;i++){c.drawCircle(x+8*s,y+(8+i*6)*s,1*s,p);c.drawCircle(x+20*s,y+(8+i*6)*s,1*s,p);}c.drawLine(x+10*s,y+8*s,x+18*s,y+8*s,p);c.drawLine(x+10*s,y+14*s,x+18*s,y+14*s,p);c.drawLine(x+10*s,y+20*s,x+18*s,y+20*s,p);break;
   case 4:c.drawRoundRect(x+3*s,y+6*s,x+19*s,y+22*s,2*s,2*s,p);c.drawRoundRect(x+9*s,y+3*s,x+25*s,y+19*s,2*s,2*s,p);q.moveTo(x+14*s,y+8*s);q.lineTo(x+21*s,y+11*s);q.lineTo(x+14*s,y+15*s);q.close();c.drawPath(q,p);break;
   case 5:q.moveTo(x+9*s,y+5*s);q.lineTo(x+23*s,y+14*s);q.lineTo(x+9*s,y+23*s);q.close();c.drawPath(q,p);break;
   case 6:q.moveTo(x+7*s,y+4*s);q.lineTo(x+21*s,y+4*s);q.lineTo(x+21*s,y+24*s);q.lineTo(x+14*s,y+19*s);q.lineTo(x+7*s,y+24*s);q.close();c.drawPath(q,p);break;
   case 7:q.moveTo(x+14*s,y+24*s);q.cubicTo(x+2*s,y+17*s,x+4*s,y+6*s,x+10*s,y+6*s);q.cubicTo(x+13*s,y+6*s,x+14*s,y+9*s,x+14*s,y+9*s);q.cubicTo(x+14*s,y+9*s,x+15*s,y+6*s,x+18*s,y+6*s);q.cubicTo(x+25*s,y+6*s,x+26*s,y+17*s,x+14*s,y+24*s);c.drawPath(q,p);break;
   case 8:c.drawLine(x+6*s,y+7*s,x+22*s,y+7*s,p);c.drawCircle(x+11*s,y+7*s,2.5f*s,p);c.drawLine(x+6*s,y+14*s,x+22*s,y+14*s,p);c.drawCircle(x+18*s,y+14*s,2.5f*s,p);c.drawLine(x+6*s,y+21*s,x+22*s,y+21*s,p);c.drawCircle(x+13*s,y+21*s,2.5f*s,p);break;
   default:c.drawRoundRect(x+5*s,y+4*s,x+18*s,y+24*s,2*s,2*s,p);c.drawLine(x+12*s,y+14*s,x+25*s,y+14*s,p);c.drawLine(x+21*s,y+10*s,x+25*s,y+14*s,p);c.drawLine(x+21*s,y+18*s,x+25*s,y+14*s,p);break;
  }
 }
 private void star(Canvas c,float x,float y,float r){c.drawLine(x-r,y,x+r,y,p);c.drawLine(x,y-r,x,y+r,p);}
 @Override public void setAlpha(int a){p.setAlpha(a);}@Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
