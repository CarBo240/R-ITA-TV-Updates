package it.carmine.streamplayer;

import android.app.*;import android.content.*;import android.os.*;import android.graphics.Color;import android.text.*;import android.view.*;import android.widget.*;
import java.util.*;import java.util.concurrent.*;

/** Italian catalog names come from the current site; sources resolve on selection. */
public class GomstreamActivity extends Activity {
 private final ExecutorService worker=Executors.newSingleThreadExecutor();private Future<?> task;
 private final Handler ui=new Handler(Looper.getMainLooper());
 private final Runnable autoRefresh=new Runnable(){public void run(){load();ui.postDelayed(this,10*60*1000);}};
 private List<GomstreamSource.Channel> items=new ArrayList<>(),visible=new ArrayList<>();
 private static final Map<String,List<GomstreamSource.Channel>> cache=new HashMap<>();
 private EditText search;private TextView status;private ListView list;private Spinner categories;private Button refresh;
 private boolean destroyed,resumed;private int request;private String base;
 private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
 private TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.WHITE);return t;}
 private android.graphics.drawable.StateListDrawable background(){android.graphics.drawable.StateListDrawable s=new android.graphics.drawable.StateListDrawable();for(boolean focus:new boolean[]{true,false}){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(focus?0xff40331e:0xff202125);d.setCornerRadius(dp(6));d.setStroke(dp(2),focus?0xfff2b245:0xff38393d);if(focus){s.addState(new int[]{android.R.attr.state_selected},d);s.addState(new int[]{android.R.attr.state_pressed},d);}s.addState(focus?new int[]{android.R.attr.state_focused}:new int[]{},d);}return s;}
 private Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setMinWidth(0);b.setMinimumWidth(0);UiTheme.navigation(b);return b;}
 @Override public void onCreate(Bundle state){super.onCreate(state);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(12),dp(12),dp(12),dp(8));root.setBackgroundColor(0xff151619);setContentView(root);root.addView(text("Gomstream · canali italiani",24));
  LinearLayout row=new LinearLayout(this);root.addView(row,new LinearLayout.LayoutParams(-1,dp(48)));
  Button back=button("‹ Indietro");row.addView(back,new LinearLayout.LayoutParams(0,-1,1));back.setOnClickListener(v->finish());
  refresh=button("↻ Aggiorna");refresh.setTag("gomRefresh");row.addView(refresh,new LinearLayout.LayoutParams(0,-1,1));refresh.setOnClickListener(v->load());
  Button url=button("Cambio URL");url.setTag("gomUrl");row.addView(url,new LinearLayout.LayoutParams(0,-1,1));url.setOnClickListener(v->GomstreamSettings.edit(this,()->load()));
  search=new EditText(this);search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(0xffa6a6aa);search.setHint("Cerca un canale");search.setTag("gomSearch");root.addView(search,new LinearLayout.LayoutParams(-1,dp(48)));search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){filter();}public void afterTextChanged(Editable e){}});
  categories=new EventFilterSpinner(this);categories.setContentDescription("Categorie Gomstream");ArrayAdapter<String> cat=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Tutte le categorie","Sport","Cinema","TV"}){@Override public View getView(int n,View v,ViewGroup p){TextView t=(TextView)super.getView(n,v,p);t.setTextColor(Color.WHITE);return t;}};categories.setAdapter(cat);root.addView(categories,new LinearLayout.LayoutParams(-1,dp(44)));categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int n,long id){filter();}});
  status=text("Caricamento…",12);status.setTag("gomStatus");status.setTextColor(0xffa6a6aa);root.addView(status,new LinearLayout.LayoutParams(-1,dp(44)));
  list=new ListView(this);list.setTag("gomList");list.setDividerHeight(dp(5));root.addView(list,new LinearLayout.LayoutParams(-1,0,1));list.setOnItemClickListener((p,v,n,id)->open(visible.get(n)));load();
 }
 protected void load(){final String home=DaddyLiveSettings.home(this);base=home;int generation=++request;items=new ArrayList<>(cache.getOrDefault(home,Collections.emptyList()));filter();status.setText("Aggiornamento dal sito…");refresh.setEnabled(false);
  task=worker.submit(()->{try{List<GomstreamSource.Channel> result=GomstreamSource.catalog(home,LiveNetwork.gom(this));runOnUiThread(()->{if(destroyed||generation!=request||!home.equals(DaddyLiveSettings.home(this)))return;cache.put(home,new ArrayList<>(result));items=result;refresh.setEnabled(true);filter();});}catch(Exception e){runOnUiThread(()->{if(destroyed||generation!=request)return;refresh.setEnabled(true);filter();status.setText(items.isEmpty()?"Catalogo non disponibile · premi Aggiorna":"Servizio non disponibile · elenco precedente · "+visible.size()+" canali");});}});
 }
 private void filter(){if(list==null)return;String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);String category=String.valueOf(categories.getSelectedItem());int selected=list.getSelectedItemPosition();String keep=selected>=0&&selected<visible.size()?visible.get(selected).id:null;boolean focus=list.hasFocus();visible=new ArrayList<>();for(GomstreamSource.Channel c:items)if(c.name.toLowerCase(Locale.ROOT).contains(q)&&(categories.getSelectedItemPosition()<=0||c.category().equals(category)))visible.add(c);
  list.setAdapter(new BaseAdapter(){public int getCount(){return visible.size();}public Object getItem(int n){return visible.get(n);}public long getItemId(int n){return Long.parseLong(visible.get(n).id);}public View getView(int n,View v,ViewGroup p){GomstreamSource.Channel c=visible.get(n);LinearLayout card=new LinearLayout(GomstreamActivity.this);card.setOrientation(1);card.setPadding(dp(12),dp(10),dp(12),dp(10));card.setBackground(background());card.addView(text(DisplayNames.channel(c.name),17));TextView meta=text(c.category()+" · player interno",12);meta.setTextColor(0xfff2b245);card.addView(meta);return card;}});
  if(focus&&keep!=null)for(int i=0;i<visible.size();i++)if(visible.get(i).id.equals(keep)){list.setSelection(i);break;}status.setText(visible.size()+" canali dal catalogo · aggiornamento automatico");
 }
 private void open(GomstreamSource.Channel c){startActivity(new Intent(this,GomstreamPlayerActivity.class).putExtra("id",c.id).putExtra("title",c.name));}
 @Override protected void onResume(){super.onResume();if(resumed||(base!=null&&!base.equals(DaddyLiveSettings.home(this))))load();resumed=true;ui.removeCallbacks(autoRefresh);ui.postDelayed(autoRefresh,10*60*1000);}
 @Override protected void onPause(){ui.removeCallbacks(autoRefresh);super.onPause();}
 @Override protected void onDestroy(){destroyed=true;++request;ui.removeCallbacksAndMessages(null);if(task!=null)task.cancel(true);worker.shutdownNow();super.onDestroy();}
}
