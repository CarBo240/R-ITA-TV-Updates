package it.carmine.streamplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.widget.Button;

final class UiTheme {
 static final int AMBER=0xfff2b245, TEXT=0xfff4f3ef;
 static final String PREF="rita_theme";
 static final int[] BACKGROUNDS={0xff151619,0xff101827,0xff1c142b,0xff131313};
 static final int[] PANELS={0xff202125,0xff1d3048,0xff302040,0xff252525};
 static final int[] ACCENTS={AMBER,0xff3b82f6,0xffad75ed,0xffd6af59};
 static final String[] NAMES={"Originale", "Blu notte", "Viola cinema", "Nero e oro"};
 static int selected(Context c){return Math.max(0,Math.min(3,c.getSharedPreferences(PREF,0).getInt("palette",0)));}
 static int background(Context c){return BACKGROUNDS[selected(c)];}
 static int panel(Context c){return PANELS[selected(c)];}
 static int accent(Context c){return ACCENTS[selected(c)];}
 static void select(Context c,int n){c.getSharedPreferences(PREF,0).edit().putInt("palette",n).apply();}
 static Typeface medium(Context c){return c.getResources().getFont(R.font.inter_medium);}
 static void navigation(Button b){
  b.setTypeface(medium(b.getContext()));b.setElevation(0);b.setStateListAnimator(null);
  b.setTextColor(new ColorStateList(new int[][]{new int[]{android.R.attr.state_focused},new int[]{android.R.attr.state_pressed},new int[]{}},new int[]{accent(b.getContext()),accent(b.getContext()),TEXT}));
  StateListDrawable states=new StateListDrawable();
  float density=b.getResources().getDisplayMetrics().density;
  states.addState(new int[]{android.R.attr.state_focused},new Underline(density,accent(b.getContext())));
  states.addState(new int[]{android.R.attr.state_pressed},new Underline(density,accent(b.getContext())));
  states.addState(new int[]{},new ColorDrawable(Color.TRANSPARENT));b.setBackground(states);
 }
 private static final class Underline extends Drawable {
  final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final float density;
  Underline(float density,int color){this.density=density;paint.setColor(color);}
  public void draw(Canvas c){Rect r=getBounds();float inset=12*density;c.drawRoundRect(r.left+inset,r.bottom-2*density,r.right-inset,r.bottom,density,density,paint);}
  public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
 }
}
