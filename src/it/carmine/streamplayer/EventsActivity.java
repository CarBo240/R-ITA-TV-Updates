package it.carmine.streamplayer;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import android.text.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/** Independent web playback: channel player controls never handle event pages. */
public class EventsActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final int bg=Color.rgb(21,22,25),fg=Color.rgb(244,243,239),accent=Color.rgb(242,178,69),muted=Color.rgb(166,166,170);
    private FrameLayout root;private LinearLayout catalog;private ListView list;private TextView status;private EditText search;
    private Spinner sports,states;private Button refresh;private ArrayAdapter<String> adapter;private ArtworkStore artwork;private EventSource source;
    private EventSource.Snapshot snapshot;private List<EventSource.Event> visible=new ArrayList<>();private List<String> sportKeys=new ArrayList<>();
    private boolean loading,destroyed,resumed;private String failure="";
    private final Runnable tick=new Runnable(){public void run(){if(!resumed||destroyed)return;loadEvents(false);handler.postDelayed(this,120000);}};
    protected boolean imagesEnabled(){return true;}
    private int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    private TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextColor(fg);t.setTextSize(size);return t;}
    private android.graphics.drawable.Drawable background(){android.graphics.drawable.StateListDrawable d=new android.graphics.drawable.StateListDrawable();for(boolean focus:new boolean[]{true,false}){android.graphics.drawable.GradientDrawable shape=new android.graphics.drawable.GradientDrawable();shape.setColor(focus?Color.rgb(64,51,30):Color.rgb(27,28,32));shape.setCornerRadius(dp(6));shape.setStroke(dp(1),focus?accent:Color.rgb(56,57,61));d.addState(focus?new int[]{android.R.attr.state_focused}:new int[]{},shape);}return d;}
    private Button button(String label){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(fg);b.setTextSize(13);b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(dp(8),0,dp(8),0);UiTheme.navigation(b);return b;}
    @Override public void onCreate(Bundle state){super.onCreate(state);source=new EventSource(this);artwork=new ArtworkStore(this,imagesEnabled());
        root=new FrameLayout(this);root.setTag("eventsRoot");root.setBackgroundColor(bg);root.setFitsSystemWindows(true);setContentView(root);
        catalog=new LinearLayout(this);catalog.setTag("eventsCatalog");catalog.setOrientation(LinearLayout.VERTICAL);catalog.setPadding(dp(12),dp(12),dp(12),dp(8));root.addView(catalog,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);catalog.addView(header,lp(-1,dp(44)));
        Button back=button("‹ Canali");header.addView(back,lp(dp(82),-1));back.setOnClickListener(v->finish());
        TextView title=text("Eventi live",22);title.setPadding(dp(12),0,0,0);title.setGravity(Gravity.CENTER_VERTICAL);header.addView(title,new LinearLayout.LayoutParams(0,-1,1));
        refresh=button("Aggiorna");refresh.setTag("refreshEvents");header.addView(refresh,lp(dp(86),-1));refresh.setOnClickListener(v->loadEvents(true));
        search=new EditText(this);search.setTag("eventsSearch");search.setSingleLine(true);search.setHint("Cerca squadra, lega o evento");search.setTextColor(fg);search.setHintTextColor(muted);search.setTextSize(15);search.setPadding(dp(10),0,dp(10),0);search.setBackground(background());LinearLayout.LayoutParams queryParams=lp(-1,dp(38));queryParams.topMargin=dp(8);catalog.addView(search,queryParams);
        LinearLayout filters=new LinearLayout(this);LinearLayout.LayoutParams filterParams=lp(-1,dp(38));filterParams.topMargin=dp(8);catalog.addView(filters,filterParams);
        sports=new EventFilterSpinner(this);sports.setTag("eventSports");sports.setBackground(background());sports.setContentDescription("Sport");filters.addView(sports,new LinearLayout.LayoutParams(0,-1,1));
        states=new EventFilterSpinner(this);states.setTag("eventStates");states.setBackground(background());states.setContentDescription("Stato eventi");LinearLayout.LayoutParams stateParams=new LinearLayout.LayoutParams(0,-1,1);stateParams.leftMargin=dp(8);filters.addView(states,stateParams);states.setAdapter(spinnerAdapter(Arrays.asList("Tutti","In diretta","Prossimi")));
        status=text("Caricamento eventi…",12);status.setTag("eventsStatus");status.setTextColor(muted);status.setGravity(Gravity.CENTER_VERTICAL);catalog.addView(status,lp(-1,dp(38)));
        list=new ListView(this);list.setTag("eventsList");list.setDividerHeight(dp(6));list.setDivider(new android.graphics.drawable.ColorDrawable(bg));list.setSelector(new android.graphics.drawable.ColorDrawable(0x55F2B245));list.setDrawSelectorOnTop(true);catalog.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new ArrayList<>()){@Override public int getCount(){return visible.size();}@Override public String getItem(int position){return visible.get(position).title;}@Override public View getView(int position,View old,ViewGroup parent){return card(visible.get(position));}};list.setAdapter(adapter);list.setOnItemClickListener((parent,v,position,id)->chooseSources(visible.get(position)));
        AdapterView.OnItemSelectedListener selected=new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int n,long id){filter();}public void onNothingSelected(AdapterView<?> p){}};sports.setOnItemSelectedListener(selected);states.setOnItemSelectedListener(selected);
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){filter();}public void afterTextChanged(Editable e){}});
        snapshot=source.cached();if(snapshot!=null)setSnapshot(snapshot);else{sportKeys.add("Tutti");sports.setAdapter(spinnerAdapter(Arrays.asList("Tutti gli sport")));}loadEvents(false);list.requestFocus();
    }
    private ArrayAdapter<String> spinnerAdapter(List<String> labels){return new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,labels){@Override public View getView(int pos,View old,ViewGroup parent){TextView t=(TextView)super.getView(pos,old,parent);t.setTextColor(fg);t.setTextSize(14);return t;}@Override public View getDropDownView(int pos,View old,ViewGroup parent){TextView t=(TextView)super.getDropDownView(pos,old,parent);t.setTextColor(fg);t.setBackgroundColor(bg);t.setPadding(dp(12),dp(12),dp(12),dp(12));return t;}};}
    static String sportLabel(String sport){if(sport.equals("Motor"))return "Motori";String[] keys={"Soccer","Basketball","Tennis","Baseball","American Football","Ice Hockey","Rugby","Cricket","MMA","Motorsport","Other"},labels={"Calcio","Basket","Tennis","Baseball","Football US","Hockey","Rugby","Cricket","MMA","Motori","Altro"};for(int i=0;i<keys.length;i++)if(keys[i].equals(sport))return labels[i];return sport;}
    protected void setSnapshot(EventSource.Snapshot value){snapshot=value;failure="";String old=sportKeys.isEmpty()?"Tutti":sportKeys.get(Math.max(0,sports.getSelectedItemPosition()));sportKeys=new ArrayList<>();sportKeys.add("Tutti");TreeSet<String> keys=new TreeSet<>();for(EventSource.Event e:value.events)keys.add(e.sport);sportKeys.addAll(keys);List<String> labels=new ArrayList<>();for(String key:sportKeys)labels.add(key.equals("Tutti")?"Tutti gli sport":sportLabel(key));sports.setAdapter(spinnerAdapter(labels));sports.setSelection(Math.max(0,sportKeys.indexOf(old)));filter();}
    private void filter(){if(snapshot==null)return;int index=sports.getSelectedItemPosition();String sport=index>=0&&index<sportKeys.size()?sportKeys.get(index):"Tutti";String state=states.getSelectedItem()==null?"Tutti":states.getSelectedItem().toString();visible=EventSource.filter(snapshot,sport,state,search.getText().toString());adapter.notifyDataSetChanged();status.setText((failure.isEmpty()?"":"Servizio non disponibile · catalogo salvato · ")+visible.size()+" eventi · orari Italia · aggiornato "+italianTime(snapshot.fetchedAt,"dd/MM HH:mm")+" · stato stimato"+(visible.isEmpty()?" · Nessun evento trovato":""));}
    static String italianTime(long stamp,String pattern){SimpleDateFormat format=new SimpleDateFormat(pattern,Locale.ITALY);format.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));return format.format(new Date(stamp));}
    private String time(EventSource.Event event){return event.start==0?"Orario non disponibile":italianTime(event.start,"dd/MM · HH:mm");}
    private View card(EventSource.Event event){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(12),dp(12),dp(12));row.setBackground(background());
        LinearLayout badges=new LinearLayout(this);for(String url:new String[]{event.homeBadge,event.awayBadge}){ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);badges.addView(image,lp(dp(38),dp(42)));artwork.bind(image,url,SportIcons.drawable(event.sport));image.setContentDescription("Logo squadra o icona "+sportLabel(event.sport));}row.addView(badges,lp(dp(76),dp(44)));
        LinearLayout details=new LinearLayout(this);details.setOrientation(LinearLayout.VERTICAL);LinearLayout.LayoutParams detailParams=new LinearLayout.LayoutParams(0,-2,1);detailParams.leftMargin=dp(12);row.addView(details,detailParams);
        TextView name=text(event.title,17);name.setTypeface(UiTheme.medium(this));details.addView(name,lp(-1,-2));TextView metadata=text(sportLabel(event.sport)+(event.league.isEmpty()?"":" · "+event.league),13);metadata.setTextColor(muted);details.addView(metadata,lp(-1,-2));
        String label=event.status.equals("live")?"● IN DIRETTA":event.status.equals("upcoming")||event.status.equals("scheduled")?"PROSSIMO":"TERMINATO";TextView timing=text(label+" · "+time(event)+" · "+event.sources.size()+" sorgenti",12);timing.setTextColor(accent);details.addView(timing,lp(-1,-2));return row;}
    protected void loadEvents(boolean force){if(loading||destroyed)return;if(!force&&snapshot!=null&&System.currentTimeMillis()-snapshot.fetchedAt<120000){filter();return;}loading=true;refresh.setEnabled(false);status.setText(snapshot==null?"Caricamento eventi…":"Aggiornamento eventi…");worker.submit(()->{try{EventSource.Snapshot result=source.fetch();runOnUiThread(()->{if(destroyed)return;loading=false;refresh.setEnabled(true);setSnapshot(result);});}catch(Exception e){runOnUiThread(()->{if(destroyed)return;loading=false;refresh.setEnabled(true);failure="Servizio non disponibile";if(snapshot!=null)filter();else status.setText("Eventi non disponibili · premi Aggiorna per riprovare");});}});}
    private void chooseSources(EventSource.Event event){if(event.sources.isEmpty()){new AlertDialog.Builder(this).setTitle(event.title).setMessage("Nessuna sorgente disponibile per questo evento.").setPositiveButton("OK",null).show();return;}String[] labels=new String[event.sources.size()];for(int i=0;i<labels.length;i++)labels[i]=event.sources.get(i).description();new AlertDialog.Builder(this).setTitle(event.title).setItems(labels,(dialog,n)->openSource(event,event.sources.get(n))).setNegativeButton("Annulla",null).show();}
    protected void openSource(EventSource.Event event,EventSource.Source selected){startActivity(new Intent(this,GeckoEventActivity.class).putExtra("url",selected.url).putExtra("title",event.title));}
    @Override protected void onResume(){super.onResume();resumed=true;handler.removeCallbacks(tick);handler.postDelayed(tick,120000);}
    @Override protected void onPause(){resumed=false;handler.removeCallbacks(tick);super.onPause();}
    @Override protected void onDestroy(){destroyed=true;handler.removeCallbacksAndMessages(null);worker.shutdownNow();if(artwork!=null)artwork.close();super.onDestroy();}
}
