package it.carmine.streamplayer;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/** LAB ONLY: visual catalogue -> title -> recognized sources -> native player. */
public final class ExperimentalCatalogActivity extends Activity {
 private static final class Item {
  final String name,poster,url,id,year,kind;
  Item(String n,String p,String u,String i,String y,String k){name=n;poster=p;url=u;id=i;year=y;kind=k;}
 }
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private final ArrayList<Item> items=new ArrayList<>();
 private final ArrayList<ExperimentalHostInspector.Link> hosts=new ArrayList<>();
 private ArtworkStore artwork;
 private LinearLayout root,header;
 private FrameLayout content;
 private TextView status;
 private EditText search;
 private VodSettings.Source source;
 private Item selected;
 private volatile boolean closed;
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.WHITE);return t;}
 private Button button(String label){Button b=new Button(this);b.setText(label);b.setAllCaps(false);return b;}
 private void add(LinearLayout p,View v){p.addView(v,new LinearLayout.LayoutParams(-1,-2));}
 private void state(String s){status.setText(s);}
 private void job(String loading,Callable<?> task,java.util.function.Consumer<Object> done){state(loading);worker.execute(()->{try{Object value=task.call();runOnUiThread(()->{if(!closed)done.accept(value);});}catch(Exception e){runOnUiThread(()->{if(!closed)state("LAB · "+safe(e));});}});}
 private static String safe(Throwable e){String msg=VodNetwork.error(e instanceof Exception?(Exception)e:new Exception(e));return msg.length()>160?msg.substring(0,160):msg;}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894);
  String url=getIntent().getStringExtra("lab_url"),name=getIntent().getStringExtra("lab_name"),type=getIntent().getStringExtra("lab_type");
  if(url==null||name==null||type==null){finish();return;}
  try{url=VodSource.base(url);}catch(Exception e){finish();return;}
  source=new VodSettings.Source("lab",name,url,type);
  artwork=new ArtworkStore(this,true);
  root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(12),dp(18),dp(12));root.setBackgroundColor(0xff10151c);setContentView(root);
  header=new LinearLayout(this);header.setOrientation(LinearLayout.VERTICAL);add(root,header);
  TextView title=text("LAB · "+source.name,23);title.setTextColor(0xff4fd4b1);add(header,title);
  status=text("Preparazione catalogo…",13);status.setPadding(0,dp(5),0,dp(5));add(header,status);
  LinearLayout commands=new LinearLayout(this);commands.setOrientation(LinearLayout.HORIZONTAL);add(header,commands);
  Button back=button("← Provider");commands.addView(back,new LinearLayout.LayoutParams(0,dp(48),1));back.setOnClickListener(v->finish());
  Button refresh=button("Ricarica");commands.addView(refresh,new LinearLayout.LayoutParams(0,dp(48),1));refresh.setOnClickListener(v->loadCatalog());
  search=new EditText(this);search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(0xffa0aab5);search.setHint("Cerca nel catalogo di "+source.name);search.setInputType(InputType.TYPE_CLASS_TEXT);add(header,search);
  Button find=button("Cerca titolo");add(header,find);find.setOnClickListener(v->loadSearch());
  content=new FrameLayout(this);root.addView(content,new LinearLayout.LayoutParams(-1,0,1));loadCatalog();
 }
 private boolean nativeSC(){return "streamingcommunity".equals(source.type);}
 private void loadCatalog(){selected=null;
  if(nativeSC())job("Caricamento locandine da "+source.name+"…",()->{
   String html=VodSource.fetch(source.url,source.url+"/");return fromSC(VodSource.catalog(VodSource.props(html),source.url));
  },o->showCatalog((List<Item>)o));
  else job("Lettura catalogo pubblico di "+source.name+"…",()->generic(source.url),o->showCatalog((List<Item>)o));
 }
 private void loadSearch(){String q=search.getText().toString().trim();if(q.isEmpty()){loadCatalog();return;}
  if(nativeSC())job("Ricerca «"+q+"»…",()->fromSC(VodSource.catalog(VodSource.page(source.url,"/it/search?q="+VodSource.encode(q),VodSource.HTTP),source.url)),o->showCatalog((List<Item>)o));
  else {ArrayList<Item> filtered=new ArrayList<>();for(Item i:items)if(i.name.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT)))filtered.add(i);showCatalog(filtered);}
 }
 private List<Item> fromSC(List<VodSource.Title> titles){ArrayList<Item> out=new ArrayList<>();for(VodSource.Title t:titles)out.add(new Item(t.name,t.poster,source.url+t.path(),t.id,t.year,t.series()?"Serie":"Film"));return out;}
 private void showCatalog(List<Item> found){items.clear();items.addAll(found);content.removeAllViews();state("LAB · "+found.size()+" titoli · Seleziona la locandina per cercare le fonti");
  if(found.isEmpty()){TextView msg=text("Catalogo non riconosciuto o nessuna locandina disponibile. Questo provider richiede un adattatore specifico: l'apertura del sito non equivale a un flusso nel player interno.",16);content.addView(msg);return;}
  GridView grid=new GridView(this);grid.setNumColumns(3);grid.setHorizontalSpacing(dp(12));grid.setVerticalSpacing(dp(14));grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);grid.setClipToPadding(false);
  grid.setAdapter(new BaseAdapter(){public int getCount(){return items.size();}public Object getItem(int i){return items.get(i);}public long getItemId(int i){return i;}
   public View getView(int i,View v,android.view.ViewGroup parent){LinearLayout card=new LinearLayout(ExperimentalCatalogActivity.this);card.setOrientation(1);card.setPadding(dp(4),dp(4),dp(4),dp(4));card.setBackgroundColor(0xff243343);
    ImageView poster=new ImageView(ExperimentalCatalogActivity.this);poster.setScaleType(ImageView.ScaleType.FIT_CENTER);card.addView(poster,new LinearLayout.LayoutParams(-1,dp(178)));Item x=items.get(i);artwork.bind(poster,x.poster,new ColorDrawable(0xff354354));
    TextView label=text(x.name+(x.year.isEmpty()?"":" · "+x.year),14);label.setMaxLines(2);card.addView(label,new LinearLayout.LayoutParams(-1,dp(48)));return card;
   }});
  grid.setOnItemClickListener((p,v,index,id)->openTitle(items.get(index)));content.addView(grid);
 }
 private void openTitle(Item item){selected=item;content.removeAllViews();state("LAB · "+item.name+" · ricerca fonti");
  LinearLayout pane=new LinearLayout(this);pane.setOrientation(1);ScrollView scroll=new ScrollView(this);scroll.addView(pane);content.addView(scroll);
  add(pane,text(item.name+" · "+item.kind,21));Button goBack=button("← Torna alle locandine");add(pane,goBack);goBack.setOnClickListener(v->showCatalog(new ArrayList<>(items)));
  TextView note=text("Le fonti vengono elencate separatamente dal catalogo. Solo i collegamenti video HTTPS supportati potranno aprirsi nel player LAB; i collegamenti a pagine host richiedono un adattatore compatibile.",14);add(pane,note);
  LinearLayout rows=new LinearLayout(this);rows.setOrientation(1);add(pane,rows);
  job("Analisi delle fonti di «"+item.name+"»…",()->ExperimentalHostInspector.inspect(item.url),o->{ExperimentalHostInspector.Result result=(ExperimentalHostInspector.Result)o;hosts.clear();hosts.addAll(result.links);renderSources(rows,result);});
 }
 private void renderSources(LinearLayout rows,ExperimentalHostInspector.Result result){rows.removeAllViews();state("LAB · "+selected.name+" · "+result.links.size()+" collegamenti rilevati · HTTP "+result.status);
  if(result.links.isEmpty())add(rows,text("Nessun host o flusso diretto trovato nell'HTML accessibile. Può essere necessario un resolver specifico.",16));
  for(ExperimentalHostInspector.Link link:result.links){Button b=button(link.kind+" · "+link.displayHost+(link.direct?"\n▶ Prova player interno":"\nHost rilevato · non è un flusso diretto"));add(rows,b);b.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(link.kind).setItems(link.direct?new String[]{"Prova nel player Media3","Analizza host"}:new String[]{"Analizza host","Informazioni"},(d,index)->{
   if(link.direct&&index==0)play(link.url);
   else if(link.direct||index==0)inspectHost(link.url,rows);
   else new AlertDialog.Builder(this).setMessage("Il riconoscimento di un host non rende disponibile automaticamente il video nativo.").setPositiveButton("OK",null).show();
  }).show());}
  if(nativeSC()&&selected.id.matches("[0-9]+")){Button test=button("Prova resolver Streaming Community esistente");add(rows,test);test.setOnClickListener(v->resolveSC());}
 }
 private void inspectHost(String url,LinearLayout rows){job("Analisi host…",()->ExperimentalHostInspector.inspect(url),o->renderSources(rows,(ExperimentalHostInspector.Result)o));}
 private void resolveSC(){Item t=selected;job("Verifica sorgente video per «"+t.name+"»…",()->VodSource.resolve(source.url,t.id,"",VodSource.HTTP),o->{VodSource.Stream stream=(VodSource.Stream)o;
  new AlertDialog.Builder(this).setTitle("Flusso HLS individuato").setMessage("Il resolver ha ottenuto un flusso. Vuoi tentare la riproduzione nel player LAB? Un server può comunque rifiutare l'accesso.").setPositiveButton("Player interno",(d,n)->play(stream.url)).setNegativeButton("Annulla",null).show();});}
 private void play(String url){startActivity(new Intent(this,ExperimentalPlayerActivity.class).putExtra("stream_url",url));}
 private static final Pattern LINK_IMG=Pattern.compile("(?is)<a\\b[^>]*href\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>(.{0,2500}?)</a>");
 private static final Pattern IMAGE=Pattern.compile("(?is)<img\\b[^>]*(?:data-src|src)\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
 private static final Pattern ALT=Pattern.compile("(?is)<img\\b[^>]*alt\\s*=\\s*['\"]([^'\"]+)['\"]");
 private static String absolute(URI base,String p){try{URI u=base.resolve(p.replace("&amp;","&"));return "https".equalsIgnoreCase(u.getScheme())&&u.getHost()!=null&&u.getUserInfo()==null&&u.getRawQuery()==null?u.toString():"";}catch(Exception e){return "";}}
 private List<Item> generic(String address)throws Exception{String html=VodNetwork.fetch(address,address);URI base=new URI(address);LinkedHashMap<String,Item> results=new LinkedHashMap<>();Matcher m=LINK_IMG.matcher(html);
  while(m.find()&&results.size()<75){String path=absolute(base,m.group(1));if(path.isEmpty()||path.equals(address))continue;String body=m.group(2);Matcher img=IMAGE.matcher(body);if(!img.find())continue;String poster=absolute(base,img.group(1));if(poster.isEmpty())continue;Matcher a=ALT.matcher(body);String name=a.find()?a.group(1).trim():body.replaceAll("<[^>]+>"," ").replaceAll("\\s+"," ").trim();if(name.length()<2||name.length()>110)continue;results.put(path,new Item(name,poster,path,"","","Titolo"));}
  return new ArrayList<>(results.values());
 }
 @Override protected void onDestroy(){closed=true;worker.shutdownNow();if(artwork!=null)artwork.close();super.onDestroy();}
}
