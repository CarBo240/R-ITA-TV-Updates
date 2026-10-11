package it.carmine.streamplayer;

import android.content.*;import android.content.res.ColorStateList;import android.graphics.*;import android.graphics.drawable.*;import android.view.*;import android.widget.*;
import androidx.media3.ui.*;

/** Cinema-bright Media3 controls with a TV-visible focus ring. */
final class PlayerChrome {
 private PlayerChrome(){}
 static void apply(PlayerView player){player.post(()->style(player));}
 private static void style(PlayerView player){Context c=player.getContext();int accent=UiTheme.accent(c),dark=0xff17191c;int[][] states={new int[]{android.R.attr.state_focused},new int[]{android.R.attr.state_pressed},new int[]{}};int[] tint={dark,dark,Color.WHITE};String[] names={"exo_play_pause","exo_rew","exo_ffwd","exo_prev","exo_next","exo_subtitle","exo_settings"};for(String name:names){View v=find(player,name);if(v==null)continue;v.setFocusable(true);v.setBackground(button(c));if(v instanceof ImageView)((ImageView)v).setImageTintList(new ColorStateList(states,tint));int pad=dp(c,11);v.setPadding(pad,pad,pad,pad);v.setOnFocusChangeListener((view,focused)->{view.animate().scaleX(focused?1.16f:1f).scaleY(focused?1.16f:1f).setDuration(120).start();view.setElevation(dp(c,focused?18:0));});}
  View bar=find(player,"exo_progress");if(bar instanceof DefaultTimeBar){DefaultTimeBar t=(DefaultTimeBar)bar;t.setPlayedColor(accent);t.setScrubberColor(accent);t.setBufferedColor(0xff8a8b8e);t.setUnplayedColor(0x66ffffff);enlargeScrubber(t,c);t.setFocusable(true);t.setMinimumHeight(dp(c,34));t.setOnFocusChangeListener((v,f)->{v.animate().scaleX(f?1.015f:1f).scaleY(f?1.85f:1f).setDuration(120).start();v.setElevation(dp(c,f?20:0));});}
  View pos=find(player,"exo_position"),dur=find(player,"exo_duration");if(pos instanceof TextView)((TextView)pos).setTextColor(Color.WHITE);if(dur instanceof TextView)((TextView)dur).setTextColor(Color.WHITE);
 }
 /** VOD-only styling: leaves other players and the original LAB route unchanged. */
 static void applyVod(PlayerView player){player.post(()->{
  style(player);Context c=player.getContext();int amber=UiTheme.AMBER;
  for(String name:new String[]{"exo_play_pause","exo_rew","exo_ffwd","exo_rew_with_amount","exo_ffwd_with_amount","exo_prev","exo_next","exo_subtitle","exo_settings"}){
   View v=find(player,name);if(v==null)continue;v.setFocusable(true);if(v instanceof TextView){Object saved=v.getTag(it.carmine.streamplayer.R.id.vod_button_background);Drawable arrow=saved instanceof Drawable?(Drawable)saved:v.getBackground();if(arrow!=null){v.setTag(it.carmine.streamplayer.R.id.vod_button_background,arrow);v.setBackground(new LayerDrawable(new Drawable[]{vodButton(c),arrow}));}else v.setBackground(vodButton(c));}else v.setBackground(vodButton(c));
   ColorStateList tint=new ColorStateList(new int[][]{new int[]{android.R.attr.state_focused},new int[]{android.R.attr.state_pressed},new int[]{}},new int[]{0xff17191c,0xff17191c,Color.WHITE});
   if(v instanceof ImageView)((ImageView)v).setImageTintList(tint);
   if(v instanceof TextView)((TextView)v).setTextColor(tint);
   v.setOnFocusChangeListener((view,focused)->{view.animate().scaleX(focused?1.12f:1f).scaleY(focused?1.12f:1f).setDuration(100).start();view.setElevation(dp(c,focused?14:0));});
  }
  View view=find(player,"exo_progress");if(view instanceof DefaultTimeBar){DefaultTimeBar bar=(DefaultTimeBar)view;
   bar.animate().cancel();bar.setScaleX(1);bar.setScaleY(1);bar.setElevation(0);
   try{setSize(bar,"scrubberEnabledSize",dp(c,10));setSize(bar,"scrubberDraggedSize",dp(c,12));}catch(Exception ignored){}
   final boolean[] scrubbing={false};Runnable update=()->{int color=bar.hasFocus()||scrubbing[0]?amber:Color.WHITE;bar.setPlayedColor(color);bar.setScrubberColor(color);bar.setBufferedColor((color&0x00ffffff)|0xa0000000);bar.setUnplayedColor((color&0x00ffffff)|0x55000000);bar.invalidate();};
   bar.setOnFocusChangeListener((v,focused)->update.run());
   // Idempotent because applyVod is also called after player attachment.
   Object prior=bar.getTag(it.carmine.streamplayer.R.id.vod_scrub_listener);if(prior instanceof TimeBar.OnScrubListener)bar.removeListener((TimeBar.OnScrubListener)prior);
   TimeBar.OnScrubListener listener=new TimeBar.OnScrubListener(){public void onScrubStart(TimeBar timeBar,long position){scrubbing[0]=true;update.run();}public void onScrubMove(TimeBar timeBar,long position){}public void onScrubStop(TimeBar timeBar,long position,boolean canceled){scrubbing[0]=false;update.run();}};
   bar.setTag(it.carmine.streamplayer.R.id.vod_scrub_listener,listener);bar.addListener(listener);update.run();
  }
 });}
 private static Drawable vodButton(Context c){StateListDrawable out=new StateListDrawable();GradientDrawable focused=new GradientDrawable();focused.setShape(GradientDrawable.OVAL);focused.setColor(UiTheme.AMBER);focused.setStroke(dp(c,3),Color.WHITE);out.addState(new int[]{android.R.attr.state_focused},focused);out.addState(new int[]{android.R.attr.state_pressed},focused);GradientDrawable normal=new GradientDrawable();normal.setShape(GradientDrawable.OVAL);normal.setColor(0xb8181a1d);out.addState(new int[]{},normal);return out;}
 private static View find(PlayerView player,String name){int id=player.getResources().getIdentifier(name,"id",player.getContext().getPackageName());return id==0?null:player.findViewById(id);}
 static Drawable button(Context c){StateListDrawable out=new StateListDrawable();GradientDrawable focus=new GradientDrawable();focus.setShape(GradientDrawable.OVAL);focus.setColor(Color.WHITE);focus.setStroke(dp(c,5),UiTheme.accent(c));out.addState(new int[]{android.R.attr.state_focused},focus);out.addState(new int[]{android.R.attr.state_pressed},focus);GradientDrawable normal=new GradientDrawable();normal.setShape(GradientDrawable.OVAL);normal.setColor(0xb8181a1d);out.addState(new int[]{},normal);return out;}
 static void styleSkip(Button b){b.setAllCaps(false);b.setTextColor(new ColorStateList(new int[][]{new int[]{android.R.attr.state_focused},new int[]{}},new int[]{0xff17191c,Color.WHITE}));b.setBackground(button(b.getContext()));b.setTypeface(UiTheme.medium(b.getContext()));b.setPadding(dp(b.getContext(),20),0,dp(b.getContext(),20),0);b.setOnFocusChangeListener((v,f)->v.animate().scaleX(f?1.08f:1f).scaleY(f?1.08f:1f).setDuration(120).start());}
 private static void enlargeScrubber(DefaultTimeBar bar,Context c){try{setSize(bar,"scrubberEnabledSize",dp(c,20));setSize(bar,"scrubberDraggedSize",dp(c,28));}catch(Exception ignored){}}
 private static void setSize(DefaultTimeBar bar,String name,int value)throws Exception{java.lang.reflect.Field field=DefaultTimeBar.class.getDeclaredField(name);field.setAccessible(true);field.setInt(bar,value);}
 private static int dp(Context c,int n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
}
