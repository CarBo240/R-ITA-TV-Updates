package it.carmine.streamplayer;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

public class DaddyLiveActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private List<DaddyLiveSource.Item> items=new ArrayList<>(),visible=new ArrayList<>();
    private EditText search;private TextView status;private ListView list;private Button tabs,region,refresh;private Spinner categories;
    private boolean events=true,italy=false,destroyed,updatingFilter;private int request;private String base;
    private final Map<Boolean,List<DaddyLiveSource.Item>> cache=new HashMap<>();
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(size);return t;}
    private android.graphics.drawable.StateListDrawable background(){android.graphics.drawable.StateListDrawable s=new android.graphics.drawable.StateListDrawable();for(boolean focused:new boolean[]{true,false}){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(focused?0xff40331e:0xff202125);d.setStroke(dp(2),focused?0xfff2b245:0xff38393d);d.setCornerRadius(dp(6));if(focused){s.addState(new int[]{android.R.attr.state_selected},d);s.addState(new int[]{android.R.attr.state_pressed},d);}s.addState(focused?new int[]{android.R.attr.state_focused}:new int[]{},d);}return s;}
    private Button button(String label){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setMinWidth(0);b.setMinimumWidth(0);UiTheme.navigation(b);return b;}
    @Override public void onCreate(Bundle state){super.onCreate(state);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(12),dp(12),dp(8));root.setBackgroundColor(0xff151619);setContentView(root);
        root.addView(text("Daddy Live",24));LinearLayout row=new LinearLayout(this);root.addView(row,new LinearLayout.LayoutParams(-1,dp(48)));
        Button back=button("‹ Indietro");row.addView(back,new LinearLayout.LayoutParams(0,-1,1));back.setOnClickListener(v->finish());tabs=button("Eventi ▾");row.addView(tabs,new LinearLayout.LayoutParams(0,-1,1));tabs.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Daddy Live").setItems(new String[]{"Eventi sportivi","Canali TV"},(d,n)->{events=n==0;tabs.setText(events?"Eventi ▾":"Canali ▾");load();}).show());
        region=button("Tutte le fonti");row.addView(region,new LinearLayout.LayoutParams(0,-1,1));region.setOnClickListener(v->{italy=!italy;region.setText(italy?"Fonti italiane":"Tutte le fonti");filter();});refresh=button("↻ Aggiorna");row.addView(refresh,new LinearLayout.LayoutParams(0,-1,1));refresh.setOnClickListener(v->load());
        Button url=button("Cambio URL");url.setTag("daddyUrl");row.addView(url,new LinearLayout.LayoutParams(0,-1,1));url.setOnClickListener(v->DaddyLiveSettings.edit(this,()->{cache.clear();load();}));
        search=new EditText(this);search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(0xffa6a6aa);search.setHint("Cerca evento o canale");root.addView(search,new LinearLayout.LayoutParams(-1,dp(48)));search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){filter();}public void afterTextChanged(Editable e){}});
        categories=new EventFilterSpinner(this);categories.setContentDescription("Categorie Daddy Live");root.addView(categories,new LinearLayout.LayoutParams(-1,dp(44)));categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int n,long id){if(!updatingFilter)filter();}});
        status=text("Caricamento…",12);status.setTextColor(0xffa6a6aa);root.addView(status,new LinearLayout.LayoutParams(-1,dp(40)));list=new ListView(this);list.setDividerHeight(dp(5));root.addView(list,new LinearLayout.LayoutParams(-1,0,1));list.setOnItemClickListener((p,v,n,id)->choose(visible.get(n)));load();
    }
    protected void load(){final String home=DaddyLiveSettings.home(this);if(base!=null&&!base.equals(home))cache.clear();base=home;final boolean mode=events;final int generation=++request;items=cache.getOrDefault(mode,new ArrayList<>());setCategories();filter();status.setText("Caricamento "+(mode?"eventi":"canali")+"…");refresh.setEnabled(false);
        worker.submit(()->{try{String json=DaddyLiveSource.fetch(home,mode);List<DaddyLiveSource.Item> result=mode?DaddyLiveSource.parseEvents(json):DaddyLiveSource.parseChannels(json);runOnUiThread(()->{if(destroyed||generation!=request)return;cache.put(mode,result);items=result;refresh.setEnabled(true);setCategories();filter();});}catch(Exception e){runOnUiThread(()->{if(destroyed||generation!=request)return;refresh.setEnabled(true);filter();status.setText(items.isEmpty()?"Servizio non disponibile · premi Aggiorna":"Servizio non disponibile · elenco precedente · "+visible.size()+" risultati");});}});
    }
    private void setCategories(){updatingFilter=true;Set<String> values=new TreeSet<>();for(DaddyLiveSource.Item i:items)values.add(i.category);List<String> labels=new ArrayList<>();labels.add("Tutte le categorie");labels.addAll(values);ArrayAdapter<String> a=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,labels){@Override public View getView(int n,View v,ViewGroup parent){TextView t=(TextView)super.getView(n,v,parent);t.setTextColor(Color.WHITE);return t;}};categories.setAdapter(a);categories.setSelection(0);updatingFilter=false;}
    private void filter(){if(list==null)return;String q=search.getText().toString().toLowerCase(Locale.ROOT).trim();String category=categories.getSelectedItem()==null?"":categories.getSelectedItem().toString();visible=new ArrayList<>();for(DaddyLiveSource.Item i:items){String names=i.title+" "+i.category;for(DaddyLiveSource.Link l:i.links)names+=" "+l.name;if(names.toLowerCase(Locale.ROOT).contains(q)&&(!italy||i.italian())&&(categories.getSelectedItemPosition()<=0||i.category.equals(category)))visible.add(i);}
        list.setAdapter(new BaseAdapter(){public int getCount(){return visible.size();}public Object getItem(int n){return visible.get(n);}public long getItemId(int n){return n;}public View getView(int n,View recycled,android.view.ViewGroup parent){DaddyLiveSource.Item i=visible.get(n);LinearLayout card=new LinearLayout(DaddyLiveActivity.this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(10),dp(12),dp(10));card.setBackground(background());card.addView(text(events?i.title:DisplayNames.channel(i.title),17));TextView meta=text(i.category+" · "+i.timing()+" · "+i.links.size()+" fonti",12);meta.setTextColor(0xfff2b245);card.addView(meta);return card;}});status.setText(visible.size()+" "+(events?"eventi · orari Italia":"canali")+(visible.isEmpty()?" · Nessun risultato":""));
    }
    private void choose(DaddyLiveSource.Item item){if(item.links.isEmpty()){new AlertDialog.Builder(this).setTitle(item.title).setMessage("Nessuna fonte disponibile").setPositiveButton("OK",null).show();return;}List<DaddyLiveSource.Link> links=new ArrayList<>(item.links);if(italy)links.sort(Comparator.comparing(l->!l.name.toLowerCase(Locale.ROOT).matches(".*\\b(italy|italia|italian)\\b.*")));String[] labels=new String[links.size()];for(int i=0;i<labels.length;i++)labels[i]=links.get(i).name;new AlertDialog.Builder(this).setTitle(item.title).setItems(labels,(d,n)->startActivity(new Intent(this,GeckoEventActivity.class).putExtra("url",links.get(n).embedUrl(DaddyLiveSettings.home(this))).putExtra("title",item.title).putExtra("daddyLive",true))).setNegativeButton("Annulla",null).show();}
    @Override protected void onResume(){super.onResume();if(base!=null&&!base.equals(DaddyLiveSettings.home(this)))load();}
    @Override protected void onDestroy(){destroyed=true;++request;worker.shutdownNow();super.onDestroy();}
}
