package it.carmine.streamplayer;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Premium rounded-line icons shared by the expanded menu and compact TV rail. */
final class VodMenuIcon extends Drawable {
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final int kind;private int color;
 VodMenuIcon(int kind,int color){this.kind=kind;this.color=color;p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
 void color(int value){color=value;invalidateSelf();}
 @Override public int getIntrinsicWidth(){return 32;}@Override public int getIntrinsicHeight(){return 32;}
 @Override public void draw(Canvas c){Rect b=getBounds();float s=Math.min(b.width(),b.height())/32f,x=b.centerX()-16*s,y=b.centerY()-16*s;p.setColor(color);p.setStrokeWidth(2.15f*s);p.setStyle(Paint.Style.STROKE);Path q=new Path();
  switch(kind){
   case 0:rr(c,x+4*s,y+7*s,x+28*s,y+25*s,3.5f*s);line(c,x+11*s,y+29*s,x+21*s,y+29*s);line(c,x+16*s,y+25*s,x+16*s,y+29*s);line(c,x+11*s,y+3*s,x+16*s,y+7*s);line(c,x+21*s,y+3*s,x+16*s,y+7*s);break;
   case 1:diamond(c,x+16*s,y+4*s,4.2f*s);diamond(c,x+7*s,y+15*s,2.6f*s);diamond(c,x+22.5f*s,y+21*s,3.2f*s);break;
   case 2:rr(c,x+4*s,y+7*s,x+28*s,y+27*s,3.5f*s);line(c,x+4*s,y+13*s,x+28*s,y+13*s);line(c,x+10*s,y+4*s,x+10*s,y+10*s);line(c,x+22*s,y+4*s,x+22*s,y+10*s);circle(c,x+11*s,y+19*s,1.2f*s,true);circle(c,x+17*s,y+19*s,1.2f*s,true);circle(c,x+23*s,y+19*s,1.2f*s,true);break;
   case 3:rr(c,x+4*s,y+9*s,x+28*s,y+27*s,3*s);line(c,x+4*s,y+15*s,x+28*s,y+15*s);q.moveTo(x+5*s,y+9*s);q.lineTo(x+10*s,y+15*s);q.moveTo(x+13*s,y+9*s);q.lineTo(x+18*s,y+15*s);q.moveTo(x+21*s,y+9*s);q.lineTo(x+26*s,y+15*s);c.drawPath(q,p);line(c,x+7*s,y+5*s,x+27*s,y+5*s);break;
   case 4:rr(c,x+3*s,y+8*s,x+23*s,y+25*s,3*s);rr(c,x+9*s,y+4*s,x+29*s,y+21*s,3*s);play(c,x+16*s,y+10*s,7*s);break;
   case 5:c.drawArc(x+4*s,y+4*s,x+28*s,y+28*s,-65,295,false,p);q.moveTo(x+5*s,y+7*s);q.lineTo(x+5*s,y+14*s);q.lineTo(x+11*s,y+10*s);c.drawPath(q,p);play(c,x+13*s,y+11*s,8*s);break;
   case 6:q.moveTo(x+8*s,y+4*s);q.lineTo(x+24*s,y+4*s);q.lineTo(x+24*s,y+28*s);q.lineTo(x+16*s,y+22*s);q.lineTo(x+8*s,y+28*s);q.close();c.drawPath(q,p);line(c,x+12*s,y+10*s,x+20*s,y+10*s);break;
   case 7:q.moveTo(x+16*s,y+27*s);q.cubicTo(x+3*s,y+19*s,x+4*s,y+7*s,x+11*s,y+7*s);q.cubicTo(x+14*s,y+7*s,x+16*s,y+10*s,x+16*s,y+10*s);q.cubicTo(x+16*s,y+10*s,x+18*s,y+7*s,x+21*s,y+7*s);q.cubicTo(x+28*s,y+7*s,x+29*s,y+19*s,x+16*s,y+27*s);c.drawPath(q,p);break;
   case 8:gear(c,x+16*s,y+16*s,11*s);circle(c,x+16*s,y+16*s,3.5f*s,false);break;
   default:rr(c,x+5*s,y+4*s,x+19*s,y+28*s,2.5f*s);line(c,x+13*s,y+16*s,x+29*s,y+16*s);q.moveTo(x+24*s,y+11*s);q.lineTo(x+29*s,y+16*s);q.lineTo(x+24*s,y+21*s);c.drawPath(q,p);break;
  }
 }
 private void rr(Canvas c,float l,float t,float r,float b,float radius){c.drawRoundRect(l,t,r,b,radius,radius,p);}private void line(Canvas c,float x1,float y1,float x2,float y2){c.drawLine(x1,y1,x2,y2,p);}private void circle(Canvas c,float x,float y,float r,boolean filled){Paint.Style old=p.getStyle();if(filled)p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,p);p.setStyle(old);}
 private void play(Canvas c,float x,float y,float size){Path q=new Path();q.moveTo(x,y);q.lineTo(x,y+size);q.lineTo(x+size*.72f,y+size*.5f);q.close();c.drawPath(q,p);}
 private void diamond(Canvas c,float x,float y,float r){Path q=new Path();q.moveTo(x,y-r);q.cubicTo(x+r*.25f,y-r*.25f,x+r*.25f,y-r*.25f,x+r,y);q.cubicTo(x+r*.25f,y+r*.25f,x+r*.25f,y+r*.25f,x,y+r);q.cubicTo(x-r*.25f,y+r*.25f,x-r*.25f,y+r*.25f,x-r,y);q.cubicTo(x-r*.25f,y-r*.25f,x-r*.25f,y-r*.25f,x,y-r);q.close();c.drawPath(q,p);}
 private void gear(Canvas c,float x,float y,float r){Path q=new Path();for(int i=0;i<16;i++){double a=-Math.PI/2+i*Math.PI/8;float rr=i%2==0?r:r*.78f,px=x+(float)Math.cos(a)*rr,py=y+(float)Math.sin(a)*rr;if(i==0)q.moveTo(px,py);else q.lineTo(px,py);}q.close();c.drawPath(q,p);}
 @Override public void setAlpha(int a){p.setAlpha(a);}@Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
