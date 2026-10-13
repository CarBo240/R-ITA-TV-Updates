package com.rita.eventilive;
import android.content.Context;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.webkit.WebView;
import java.util.*;

/** Arrow overlay for a TV remote; coordinates stay in WebView-local pixels. */
final class RemotePointer extends View {
 private final WebView web; private final Handler handler=new Handler(Looper.getMainLooper());
 private final Set<Integer> held=new HashSet<>(); private final Paint paint=new Paint(3);
 private final Path arrow=new Path(); private float x=-1,y=-1; private boolean enabled=true;
 private final Runnable hide=()->{if(held.isEmpty())setVisibility(INVISIBLE);};
 private final Runnable tick=new Runnable(){public void run(){if(held.isEmpty())return;float d=9*getResources().getDisplayMetrics().density;
  move((held.contains(21)?-d:0)+(held.contains(22)?d:0),(held.contains(19)?-d:0)+(held.contains(20)?d:0));handler.postDelayed(this,30);}};
 RemotePointer(Context c,WebView w){super(c);web=w;setFocusable(false);setClickable(false);arrow.moveTo(0,0);arrow.lineTo(0,24);arrow.lineTo(6,18);arrow.lineTo(12,29);arrow.lineTo(17,26);arrow.lineTo(11,15);arrow.lineTo(21,15);arrow.close();setVisibility(INVISIBLE);}
 boolean over(View v){return enabled&&v.getVisibility()==VISIBLE&&x>=v.getLeft()&&x<v.getRight()&&y>=v.getTop()&&y<v.getBottom();}
 void toggle(){setEnabledPointer(!enabled);}
 void setEnabledPointer(boolean value){enabled=value;held.clear();handler.removeCallbacks(tick);setVisibility(INVISIBLE);}
 boolean key(KeyEvent e){if(!enabled)return false;int k=e.getKeyCode();if(k>=19&&k<=22){if(e.getAction()==KeyEvent.ACTION_DOWN){if(held.add(k)&&held.size()==1){move(0,0);handler.post(tick);}}else{held.remove(k);if(held.isEmpty()){handler.removeCallbacks(tick);handler.postDelayed(hide,3000);}}return true;}
  if(k==23||k==66){if(e.getAction()==KeyEvent.ACTION_UP){move(0,0);long t=SystemClock.uptimeMillis();MotionEvent down=MotionEvent.obtain(t,t,0,x,y,0);MotionEvent up=MotionEvent.obtain(t,t+60,1,x,y,0);web.dispatchTouchEvent(down);web.dispatchTouchEvent(up);down.recycle();up.recycle();handler.postDelayed(hide,3000);}return true;}return false;}
 private void move(float dx,float dy){handler.removeCallbacks(hide);if(x<0){x=getWidth()/2f;y=getHeight()/2f;}if((y+dy<1||y+dy>getHeight()-2)&&dy!=0)web.scrollBy(0,(int)dy);x=Math.max(1,Math.min(getWidth()-2,x+dx));y=Math.max(1,Math.min(getHeight()-2,y+dy));setVisibility(VISIBLE);invalidate();}
 @Override protected void onDraw(Canvas c){if(x<0)return;c.save();c.translate(x,y);float d=getResources().getDisplayMetrics().density;c.scale(d,d);paint.setStyle(Paint.Style.FILL);paint.setColor(Color.WHITE);c.drawPath(arrow,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.5f);paint.setColor(0xffdca64b);c.drawPath(arrow,paint);c.restore();}
 @Override protected void onDetachedFromWindow(){held.clear();handler.removeCallbacksAndMessages(null);super.onDetachedFromWindow();}
}
