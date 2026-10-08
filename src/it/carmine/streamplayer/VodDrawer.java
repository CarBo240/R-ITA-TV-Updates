package it.carmine.streamplayer;

import android.app.*;import android.content.*;import android.graphics.drawable.*;import android.view.*;import android.widget.*;

/** Shared hidden VOD drawer, overlaid without consuming content space. */
final class VodDrawer {
 static final class Controller {final LinearLayout view;final Button[] buttons;final View previous;View returnFocus;Controller(LinearLayout v,Button[] b,View p){view=v;buttons=b;previous=p;}boolean open(){return view.getVisibility()==View.VISIBLE;}void show(){View focused=view.getRootView().findFocus();if(focused!=null&&!inside(view,focused))returnFocus=focused;view.setVisibility(View.VISIBLE);view.bringToFront();buttons[0].requestFocus();}void hide(){view.setVisibility(View.GONE);View target=returnFocus!=null?returnFocus:previous;if(target!=null)target.requestFocus();}static boolean inside(ViewGroup parent,View child){for(View node=child;node!=null;){if(node==parent)return true;android.view.ViewParent p=node.getParent();node=p instanceof View?(View)p:null;}return false;}}
 static Controller attach(Activity a,FrameLayout root,View previous){int d=Math.round(a.getResources().getDisplayMetrics().density);LinearLayout drawer=new LinearLayout(a);drawer.setTag("vodSidebar");drawer.setOrientation(LinearLayout.VERTICAL);drawer.setPadding(18*d,20*d,18*d,14*d);GradientDrawable bg=new GradientDrawable();bg.setColor(UiTheme.panel(a));bg.setStroke(Math.max(1,d),UiTheme.accent(a));bg.setCornerRadii(new float[]{0,0,12*d,12*d,12*d,12*d,0,0});drawer.setBackground(bg);TextView logo=new TextView(a);logo.setText("R. ITA VOD");logo.setTextColor(UiTheme.accent(a));logo.setTextSize(25);logo.setTypeface(UiTheme.medium(a));logo.setGravity(Gravity.CENTER);drawer.addView(logo,new LinearLayout.LayoutParams(-1,58*d));String[] labels={"▣  TV Live","◷  Novità","◷  Nuove uscite","▤  Film","▤  Serie TV","▶  Continua a guardare","＋  Da vedere","★  Preferiti","⚙  Impostazioni","⏻  Esci"};ScrollView menuScroll=new ScrollView(a);menuScroll.setFillViewport(false);drawer.addView(menuScroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout entries=new LinearLayout(a);entries.setOrientation(LinearLayout.VERTICAL);menuScroll.addView(entries);Button[] buttons=new Button[labels.length];final Controller[] holder=new Controller[1];for(int i=0;i<labels.length;i++){final int action=i;Button b=new Button(a);b.setText(labels[i]);b.setAllCaps(false);b.setTextSize(15);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);UiTheme.navigation(b);b.setOnClickListener(v->navigate(a,action));buttons[i]=b;entries.addView(b,new LinearLayout.LayoutParams(-1,48*d));b.setOnKeyListener((v,key,event)->{if(event.getAction()==KeyEvent.ACTION_DOWN&&key==KeyEvent.KEYCODE_DPAD_RIGHT){if(holder[0]!=null)holder[0].hide();return true;}return false;});}drawer.setVisibility(View.GONE);root.addView(drawer,new FrameLayout.LayoutParams(285*d,-1,Gravity.START));Controller controller=new Controller(drawer,buttons,previous);holder[0]=controller;return controller;}
 private static void navigate(Activity a,int action){
  if(action==0){a.startActivity(new Intent(a,MainActivity.class).putExtra("openLive",true));return;}
  if(action==1)section(a,0,"novita","Novità",0,"");
  else if(action==2)section(a,0,"releases","Nuove uscite",0,"");
  else if(action==3)section(a,1,"film","Film",0,"");
  else if(action==4)section(a,2,"series","Serie TV",0,"");
  else if(action==5)section(a,4,"continue","Continua a guardare",0,"");
  else if(action==6)section(a,5,"watchlist","Da vedere",0,"");
  else if(action==7)section(a,3,"favorites","Preferiti",0,"");
  else if(action==8)a.startActivity(new Intent(a,VodSettingsActivity.class));
  else new AlertDialog.Builder(a).setTitle("Uscire da R. ITA TV?").setMessage("Interrompere la riproduzione e tornare al menu del TIM Box?")
    .setPositiveButton("Esci",(d,w)->{a.finishAffinity();}).setNegativeButton("Annulla",null).show();
 }
 static void section(Activity a,int mode,String kind,String title,int provider,String providerName){a.startActivity(new Intent(a,VodCatalogueActivity.class).putExtra("startMode",mode).putExtra("sectionKind",kind).putExtra("sectionTitle",title).putExtra("providerId",provider).putExtra("providerName",providerName));}
}
