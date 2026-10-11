package it.carmine.streamplayer;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

/** Isolated experiment. No writes to the production VOD source catalogue or player state. */
public final class ExperimentalHubActivity extends Activity {
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private final ArrayList<VodSettings.Source> sources=new ArrayList<>();
 private LinearLayout entries;
 private TextView state;
 private EditText query;
 private boolean closed;
 private int dp(int d){return (int)(d*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(Color.WHITE);return t;}
 private Button button(String value){Button b=new Button(this);b.setText(value);b.setAllCaps(false);b.setTextSize(15);b.setFocusable(true);return b;}
 private void add(LinearLayout container,View child){container.addView(child,new LinearLayout.LayoutParams(-1,-2));}
 private void message(String text){state.setText(text);}
 private void clearRows(){entries.removeAllViews();}
 @Override public void onCreate(Bundle saved){super.onCreate(saved);
  getWindow().getDecorView().setSystemUiVisibility(5894);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xff10151c);
  LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(28),dp(20),dp(28),dp(28));scroll.addView(content);setContentView(scroll);
  TextView heading=text("R. ITA · LABORATORIO TEST",24);heading.setTextColor(0xff4fd4b1);add(content,heading);
  TextView help=text("Ambiente separato: cataloghi provider, ricerche e player HLS/DASH/MP4. Nessuna modifica alle impostazioni o alle riproduzioni del VOD principale.",14);help.setPadding(0,dp(10),0,dp(10));add(content,help);
  state=text("Caricamento fonti…",14);state.setTextColor(0xffc5d3dc);add(content,state);
  LinearLayout tools=new LinearLayout(this);tools.setOrientation(LinearLayout.HORIZONTAL);add(content,tools);
  Button reload=button("Ricarica fonti salvate");tools.addView(reload,new LinearLayout.LayoutParams(0,-2,1));reload.setOnClickListener(v->loadLocal());
  Button remote=button("Leggi lista remota");tools.addView(remote,new LinearLayout.LayoutParams(0,-2,1));remote.setOnClickListener(v->loadRemote());
  query=new EditText(this);query.setSingleLine(true);query.setHint("Cerca titolo nei cataloghi supportati");query.setTextColor(Color.WHITE);query.setHintTextColor(0xffa0aab5);query.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);add(content,query);
  Button direct=button("Prova URL video diretto · HLS / DASH / MP4");add(content,direct);direct.setOnClickListener(v->enterDirect());
  Button inspect=button("Analizza pagina / host · MixDrop, MaxStream, AltaDefinizione");add(content,inspect);inspect.setOnClickListener(v->askInspect(null));
  TextView providerTitle=text("PROVIDER DISPONIBILI",19);providerTitle.setPadding(0,dp(15),0,dp(8));add(content,providerTitle);
  entries=new LinearLayout(this);entries.setOrientation(1);add(content,entries);
  Button addLab=button("Aggiungi / configura provider LAB 2.1.16");add(content,addLab);addLab.setOnClickListener(v->configureLab());
  loadLocal();
 }
 private void loadLocal(){
  try{
   // Read production preferences without calling sources(), which could rewrite them.
   String raw=getSharedPreferences("vod",0).getString("sources","");
   List<VodSettings.Source> all=raw.isEmpty()?VodSettings.retained(Collections.emptyList()):VodSettings.retained(VodSettings.parse(raw));
   display(all,"Fonti locali (sola lettura)");
  }catch(Exception e){message("Fonti non leggibili: "+e.getClass().getSimpleName());}
 }
 private void loadRemote(){
  String url=VodSettings.feed(this);
  if(url==null||!url.startsWith("https://")){message("URL lista assente o non HTTPS");return;}
  message("Lettura elenco remoto in corso…");
  worker.execute(()->{try{
   List<VodSettings.Source> list=VodSettings.retained(VodSettings.parse(VodNetwork.fetch(url,"")));
   runOnUiThread(()->{if(!closed)display(list,"Elenco remoto di prova (non salvato)");});
  }catch(Exception e){runOnUiThread(()->{if(!closed)message("Lista remota: "+VodNetwork.error(e));});}});
 }
 private void display(List<VodSettings.Source> all,String label){
  List<VodSettings.Source> snapshot=new ArrayList<>(all);sources.clear();sources.addAll(snapshot);clearRows();message(label+" · "+sources.size()+" provider");
  appendLabProviders();
  for(VodSettings.Source s:sources){
   Button b=button(s.name+"  ·  "+s.type+"\n"+s.url);
   b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setPadding(dp(12),dp(8),dp(12),dp(8));
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(8);entries.addView(b,lp);
   b.setOnClickListener(v->options(s));
  }
 }
 private void options(VodSettings.Source s){
  String[] options={"Catalogo con locandine → Fonti → Player", "Analizza collegamenti pagina", "Apri sito (solo confronto)", "Ricerca testuale precedente", "Informazioni provider"};
  new AlertDialog.Builder(this).setTitle("TEST · "+s.name).setItems(options,(d,n)->{
   if(n==0){if("streamingcommunity".equals(s.type)||s.vixsrc())startActivity(new Intent(this,ExperimentalCatalogActivity.class).putExtra("lab_url",s.url).putExtra("lab_name",s.name).putExtra("lab_type",s.type));else chooseLabAdapter(s.name,s.url);}
   else if(n==1)askInspect(s.url);
   else if(n==2)browser(s.url,s.name);
   else if(n==3)search(s);
   else new AlertDialog.Builder(this).setTitle(s.name).setMessage("Tipo: "+s.type+"\nURL: "+s.url+"\nI resolver del laboratorio non modificano il catalogo principale. I provider web senza API di catalogo supportata vengono esplorati nel browser TEST.").setPositiveButton("OK",null).show();
  }).setNegativeButton("Annulla",null).show();
 }
 private static final String[] LAB_KINDS={"cb01","altadefinizione","wordpress","dle","json"};
 private static final String[] LAB_LABELS={"CB01 · articoli","AltaDefinizione · schede DLE","WordPress · articoli","DLE · shortstory","JSON LAB · schema 1"};
 private void chooseLabAdapter(String name,String url){new AlertDialog.Builder(this).setTitle("Adattatore catalogo · "+name).setItems(LAB_LABELS,(d,n)->openLab(name,url,LAB_KINDS[n])).setNegativeButton("Annulla",null).show();}
 private void openLab(String name,String url,String adapter){startActivity(new Intent(this,LabMultiCatalogActivity.class).putExtra("lab_url",url).putExtra("lab_name",name).putExtra("lab_adapter",adapter));}
 private void appendLabProviders(){
  try{JSONArray all=new JSONArray(getSharedPreferences("lab211",0).getString("providers","[]"));for(int i=0;i<all.length();i++){JSONObject p=all.getJSONObject(i);String name=p.getString("name"),url=p.getString("url"),adapter=p.getString("adapter");Button b=button("LAB 2.1.16 · "+name+" · "+adapter);add(entries,b);b.setOnClickListener(v->openLab(name,url,adapter));b.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle(name).setMessage("Eliminare questa configurazione LAB?").setPositiveButton("Elimina",(d,n)->removeLab(url)).setNegativeButton("Annulla",null).show();return true;});}}
  catch(Exception e){message("Configurazioni LAB non leggibili");}
 }
 private void removeLab(String url){try{JSONArray old=new JSONArray(getSharedPreferences("lab211",0).getString("providers","[]")),next=new JSONArray();for(int i=0;i<old.length();i++)if(!url.equals(old.getJSONObject(i).optString("url")))next.put(old.getJSONObject(i));getSharedPreferences("lab211",0).edit().putString("providers",next.toString()).apply();loadLocal();}catch(Exception e){message("Eliminazione configurazione fallita");}}
 private void configureLab(){LinearLayout box=new LinearLayout(this);box.setOrientation(1);EditText name=new EditText(this),url=new EditText(this);name.setHint("Nome provider LAB");url.setHint("URL HTTPS catalogo autorizzato");url.setSingleLine(true);url.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);box.addView(name);box.addView(url);Spinner kind=new Spinner(this);kind.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,LAB_LABELS));box.addView(kind);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Provider separato dal VOD").setView(box).setPositiveButton("Salva e apri",null).setNegativeButton("Annulla",null).create();dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{try{String title=name.getText().toString().trim(),address=url.getText().toString().trim();if(title.isEmpty()||title.length()>80)throw new Exception("Nome non valido");it.carmine.streamplayer.lab211.LabPipeline.https(address);String adapter=LAB_KINDS[kind.getSelectedItemPosition()];JSONArray old=new JSONArray(getSharedPreferences("lab211",0).getString("providers","[]")),next=new JSONArray();for(int i=0;i<old.length();i++)if(!address.equals(old.getJSONObject(i).optString("url")))next.put(old.getJSONObject(i));if(next.length()>=40)throw new Exception("Massimo 40 provider LAB");next.put(new JSONObject().put("name",title).put("url",address).put("adapter",adapter));getSharedPreferences("lab211",0).edit().putString("providers",next.toString()).apply();dialog.dismiss();loadLocal();openLab(title,address,adapter);}catch(Exception e){url.setError(e.getMessage());}}));dialog.show();
 }
 private void browser(String url,String name){
  // Existing browser activity is used only for navigation; playback routes of the main app remain unchanged.
  startActivity(new Intent(this,VodBrowserActivity.class).putExtra("url",url).putExtra("title","TEST · "+name));
 }
 private void search(VodSettings.Source s){
  String term=query.getText().toString().trim();
  if(term.isEmpty()){query.setError("Inserisci un titolo");query.requestFocus();return;}
  if(!"streamingcommunity".equals(s.type)){
   new AlertDialog.Builder(this).setTitle("Ricerca non disponibile").setMessage("Questa fonte non espone un catalogo tramite il resolver di prova. Apri il sito per consultare i suoi contenuti; gli adattatori specifici saranno sviluppati e testati separatamente.").setPositiveButton("Apri sito",(d,w)->browser(s.url,s.name)).setNegativeButton("Chiudi",null).show();return;
  }
  message("TEST · ricerca "+term+" su "+s.name+"…");
  worker.execute(()->{
   try{
    List<VodSource.Title> found=VodSource.catalog(VodSource.page(s.url,"/it/search?q="+VodSource.encode(term),VodSource.HTTP),s.url);
    runOnUiThread(()->{if(!closed)showTitles(s,found);});
   }catch(Exception e){runOnUiThread(()->{if(!closed)message("Ricerca "+s.name+": "+VodNetwork.error(e));});}
  });
 }
 private void showTitles(VodSettings.Source s,List<VodSource.Title> found){
  message("TEST · "+found.size()+" risultati da "+s.name);clearRows();
  if(found.isEmpty()){add(entries,text("Nessun risultato",15));return;}
  for(VodSource.Title title:found){
   Button b=button(title.name+" · "+title.year+(title.series()?" · Serie":" · Film"));add(entries,b);
   b.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(title.name).setItems(new String[]{"Apri scheda sul sito","Controlla disponibilità della fonte","Torna ai risultati"},(d,n)->{
    if(n==0)browser(s.url+title.path(),title.name);
    if(n==1)new AlertDialog.Builder(this).setMessage("TEST: la compatibilità della sorgente richiede l'autorizzazione del servizio. La ricerca del catalogo non garantisce un flusso video accessibile dal player nativo.").setPositiveButton("OK",null).show();
   }).show());
  }
  Button back=button("Ritorna ai provider");add(entries,back);back.setOnClickListener(v->display(sources,"Fonti di prova"));
 }
 private void askInspect(String initial){
  EditText input=new EditText(this);input.setSingleLine(true);input.setHint("https://sito.example/pagina");input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);input.setTextColor(Color.WHITE);
  if(initial!=null)input.setText(initial);
  new AlertDialog.Builder(this).setTitle("LAB · Analizza pagina / host").setMessage("Legge i collegamenti dichiarati nell'HTML pubblico: nessun eseguibile remoto, codice offuscato o controllo di accesso viene aggirato.")
   .setView(input).setPositiveButton("Analizza",(d,n)->inspectUrl(input.getText().toString().trim())).setNegativeButton("Annulla",null).show();
 }
 private void inspectUrl(String url){
  try{URI uri=new URI(url);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getPort()!=-1)throw new Exception();}
  catch(Exception e){Toast.makeText(this,"Inserisci un indirizzo HTTPS valido",Toast.LENGTH_LONG).show();return;}
  message("LAB · Analisi pagina in corso…");
  worker.execute(()->{try{
    ExperimentalHostInspector.Result result=ExperimentalHostInspector.inspect(url);
    runOnUiThread(()->{if(!closed)showInspection(result);});
   }catch(Exception e){runOnUiThread(()->{if(!closed)message("Analisi fallita: "+ExperimentalHostInspector.safeError(e));});}});
 }
 private void showInspection(ExperimentalHostInspector.Result result){
  clearRows();message("LAB · "+result.label+" · HTTP "+result.status+" · "+result.links.size()+" collegamenti riconosciuti");
  add(entries,text("Pagina: "+result.displayHost+"\n"+result.message,14));
  Button back=button("← Ritorna all'elenco provider");add(entries,back);back.setOnClickListener(v->display(new ArrayList<>(sources),"Fonti di prova"));
  for(ExperimentalHostInspector.Link link:result.links){
   Button row=button(link.kind+" · "+link.displayHost+"\n"+link.hint);add(entries,row);
   row.setOnClickListener(v->{
    ArrayList<String> labels=new ArrayList<>();labels.add("Apri la pagina nel browser TEST");labels.add("Analizza questo collegamento");if(link.direct)labels.add("Prova nel player Media3");
    new AlertDialog.Builder(this).setTitle(link.kind+" · "+link.displayHost).setItems(labels.toArray(new String[0]),(d,n)->{
      if(n==0)browser(link.url,link.displayHost);else if(n==1)inspectUrl(link.url);else if(n==2&&link.direct)startActivity(new Intent(this,ExperimentalPlayerActivity.class).putExtra("stream_url",link.url));
    }).setNegativeButton("Annulla",null).show();
   });
  }
 }
 private void enterDirect(){
  EditText input=new EditText(this);input.setSingleLine(true);input.setHint("https://…/stream.m3u8");input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
  input.setTextColor(Color.WHITE);
  new AlertDialog.Builder(this).setTitle("Player interno sperimentale").setMessage("Inserisci un URL video HTTPS che puoi riprodurre legittimamente. Non sono supportate elusioni di DRM o di controlli di accesso.")
   .setView(input).setPositiveButton("Avvia",(d,n)->{
    try{URI uri=new URI(input.getText().toString().trim());if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null)throw new Exception("URL HTTPS non valido");
     startActivity(new Intent(this,ExperimentalPlayerActivity.class).putExtra("stream_url",uri.toString()));
    }catch(Exception e){Toast.makeText(this,"URL HTTPS non valido",Toast.LENGTH_LONG).show();}
   }).setNegativeButton("Annulla",null).show();
 }
 @Override protected void onDestroy(){closed=true;worker.shutdownNow();super.onDestroy();}
}
