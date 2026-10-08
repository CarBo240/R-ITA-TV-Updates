package it.carmine.streamplayer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.widget.Button;

final class UiTheme {
 static final int AMBER=0xfff2b245, TEXT=0xfff4f3ef;
 static Typeface medium(Context c){return c.getResources().getFont(R.font.inter_medium);}
 static void navigation(Button b){
  b.setTypeface(medium(b.getContext()));b.setElevation(0);b.setStateListAnimator(null);
  b.setTextColor(new ColorStateList(new int[][]{new int[]{android.R.attr.state_focused},new int[]{android.R.attr.state_pressed},new int[]{}},new int[]{AMBER,AMBER,TEXT}));
  StateListDrawable states=new StateListDrawable();
  float density=b.getResources().getDisplayMetrics().density;
  states.addState(new int[]{android.R.attr.state_focused},new Underline(density));
  states.addState(new int[]{android.R.attr.state_pressed},new Underline(density));
  states.addState(new int[]{},new ColorDrawable(Color.TRANSPARENT));b.setBackground(states);
 }
 private static final class Underline extends Drawable {
  final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final float density;
  Underline(float density){this.density=density;paint.setColor(AMBER);}
  public void draw(Canvas c){Rect r=getBounds();float inset=12*density;c.drawRoundRect(r.left+inset,r.bottom-2*density,r.right-inset,r.bottom,density,density,paint);}
  public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
 }
}
