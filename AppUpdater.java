package it.carmine.streamplayer;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** GitHub Releases updater. It never replaces application preferences or cloud data. */
final class AppUpdater {
 static final String PREFS="app_updates", REPOSITORY="repository", DOWNLOAD_ID="download_id";
 private static final String VERSION_BEFORE="download_version_code";
 private static final long AUTO_INTERVAL=12L*60L*60L*1000L;
 private AppUpdater(){}
 static String repository(Context c){
  String saved=c.getSharedPreferences(PREFS,0).getString(REPOSITORY,"").trim();
  if(!saved.isEmpty())return normalize(saved);
  try(BufferedReader r=new BufferedReader(new InputStreamReader(c.getAssets().open("update-repository.txt"),StandardCharsets.UTF_8))){return normalize(r.readLine());}catch(Exception ignored){return "";}
 }
 static void editRepository(Activity a,Runnable changed){
  EditText input=new EditText(a);input.setSingleLine(true);input.setHint("proprietario/repository");input.setText(repository(a));input.setSelectAllOnFocus(true);input.setTextColor(UiTheme.TEXT);input.setHintTextColor(0xff9a9a9a);input.setTag("githubRepositoryInput");
  AlertDialog d=new AlertDialog.Builder(a).setTitle("Repository aggiornamenti GitHub").setMessage("Inserisci proprietario/repository oppure l'indirizzo GitHub completo. Le release devono contenere l'APK universale firmato con lo stesso certificato.").setView(input).setPositiveButton("Salva",null).setNegativeButton("Annulla",null).create();
  d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String repo=normalize(input.getText().toString());if(!valid(repo)){input.setError("Formato richiesto: proprietario/repository");return;}a.getSharedPreferences(PREFS,0).edit().putString(REPOSITORY,repo).apply();d.dismiss();if(changed!=null)changed.run();check(a,true);}));d.show();
 }
 static void check(Activity a,boolean manual){
  cleanupAfterSuccessfulUpdate(a);
  String repo=repository(a);if(!valid(repo)){if(manual)editRepository(a,null);return;}
  android.content.SharedPreferences p=a.getSharedPreferences(PREFS,0);long now=System.currentTimeMillis();if(!manual&&now-p.getLong("last_check",0)<AUTO_INTERVAL)return;p.edit().putLong("last_check",now).apply();
  if(manual)Toast.makeText(a,"Controllo aggiornamenti…",Toast.LENGTH_SHORT).show();
  new Thread(()->{try{Release release=latest(repo);a.runOnUiThread(()->showResult(a,release,manual));}catch(Exception e){if(manual)a.runOnUiThread(()->new AlertDialog.Builder(a).setTitle("Aggiornamenti").setMessage("Controllo non riuscito. Verifica la connessione e il repository.\n\n"+safe(e.getMessage())).setPositiveButton("OK",null).show());}},"github-update-check").start();
 }
 private static Release latest(String repo)throws Exception{
  HttpURLConnection c=(HttpURLConnection)new URL("https://api.github.com/repos/"+repo+"/releases/latest").openConnection();c.setConnectTimeout(10000);c.setReadTimeout(15000);c.setRequestProperty("Accept","application/vnd.github+json");c.setRequestProperty("User-Agent","R-ITA-TV-Android");int status=c.getResponseCode();InputStream stream=status>=200&&status<300?c.getInputStream():c.getErrorStream();String body=read(stream);if(status<200||status>=300)throw new IOException("GitHub HTTP "+status);JSONObject root=new JSONObject(body);String tag=root.optString("tag_name");JSONArray assets=root.optJSONArray("assets");String apk="",name="";int score=-1;if(assets!=null)for(int i=0;i<assets.length();i++){JSONObject asset=assets.optJSONObject(i);if(asset==null)continue;String n=asset.optString("name");if(!n.toLowerCase(Locale.ROOT).endsWith(".apk"))continue;int s=n.toLowerCase(Locale.ROOT).contains("universal")?2:1;if(s>score){score=s;name=n;apk=asset.optString("browser_download_url");}}if(apk.isEmpty())throw new IOException("La release non contiene un APK");return new Release(tag,root.optString("name",tag),apk,name);
 }
 private static void showResult(Activity a,Release r,boolean manual){if(a.isFinishing())return;if(compare(r.tag,BuildConfig.VERSION_NAME)<=0){if(manual)new AlertDialog.Builder(a).setTitle("R. ITA TV è aggiornata").setMessage("Versione installata: "+BuildConfig.VERSION_NAME+"\nUltima release: "+r.tag).setPositiveButton("OK",null).show();return;}new AlertDialog.Builder(a).setTitle("Aggiornamento "+r.tag+" disponibile").setMessage("Verrà scaricato l'APK universale da GitHub. Preferiti, account Firebase, avanzamento VOD e impostazioni restano invariati.").setPositiveButton("Scarica",(d,w)->download(a,r)).setNegativeButton("Più tardi",null).show();}
 /** DownloadManager downloads remain private to this app; no other storage permissions required. */
 private static void download(Activity a,Release r){
  try{
   DownloadManager dm=(DownloadManager)a.getSystemService(Context.DOWNLOAD_SERVICE);
   long prior=a.getSharedPreferences(PREFS,0).getLong(DOWNLOAD_ID,-1);
   if(prior>0)try{dm.remove(prior);}catch(Exception ignored){}
   DownloadManager.Request q=new DownloadManager.Request(Uri.parse(r.url));
   q.setTitle("R. ITA TV "+r.tag);
   q.setDescription("Aggiornamento app");
   q.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
   q.setMimeType("application/vnd.android.package-archive");
   q.setDestinationInExternalFilesDir(a,Environment.DIRECTORY_DOWNLOADS,"R-ITA-TV-"+clean(r.tag)+"-universal.apk");
   long id=dm.enqueue(q);
   a.getSharedPreferences(PREFS,0).edit().putLong(DOWNLOAD_ID,id)
      .putString("download_tag",r.tag).putInt(VERSION_BEFORE,BuildConfig.VERSION_CODE).apply();
   progress(a,id);
  }catch(Exception e){errorDialog(a,"Download non riuscito",safe(e.getMessage()));}
 }
 /** Called by the download receiver; never deletes the APK before package installation. */
 static void installDownloaded(Context c,long completedId){
  if(completedId!=c.getSharedPreferences(PREFS,0).getLong(DOWNLOAD_ID,-1))return;
  // A broadcast is not necessarily allowed to open an Activity on modern Android TV.
  // The foreground settings screen offers an explicit install button instead.
  if(c instanceof Activity)install((Activity)c);
 }
 static void manage(Activity a){
  cleanupAfterSuccessfulUpdate(a);
  long id=a.getSharedPreferences(PREFS,0).getLong(DOWNLOAD_ID,-1);
  if(id<=0){new AlertDialog.Builder(a).setTitle("Aggiornamenti")
   .setMessage("Nessun APK scaricato. Premi Controlla aggiornamenti per cercarne uno nuovo.")
   .setPositiveButton("OK",null).show();return;}
  progress(a,id);
 }
 private static void progress(Activity a,long id){
  DownloadManager dm=(DownloadManager)a.getSystemService(Context.DOWNLOAD_SERVICE);
  LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);int pad=(int)(a.getResources().getDisplayMetrics().density*22);body.setPadding(pad,pad,pad,pad);
  TextView percent=new TextView(a);percent.setTextColor(UiTheme.TEXT);percent.setTextSize(17);percent.setText("Preparazione download…");body.addView(percent);
  ProgressBar bar=new ProgressBar(a,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(100);bar.setIndeterminate(true);body.addView(bar);
  AlertDialog dialog=new AlertDialog.Builder(a).setTitle("Aggiornamento R. ITA TV")
   .setView(body).setPositiveButton("Installa aggiornamento",null)
   .setNegativeButton("Chiudi",(d,w)->{}).create();
  Handler handler=new Handler(Looper.getMainLooper());
  Runnable[] poll={null};
  poll[0]=()->{
   if(!dialog.isShowing()||a.isFinishing())return;
   try(android.database.Cursor cursor=dm.query(new DownloadManager.Query().setFilterById(id))){
    if(cursor==null||!cursor.moveToFirst()){percent.setText("Download non disponibile. Riprova.");bar.setIndeterminate(false);return;}
    int status=cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
    long done=cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
    long total=cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
    if(total>0){int value=(int)Math.min(100,(done*100L)/total);bar.setIndeterminate(false);bar.setProgress(value);percent.setText("Download: "+value+"%  ("+(done/1048576)+" / "+(total/1048576)+" MB)");}
    else{bar.setIndeterminate(true);percent.setText("Download in corso · "+(done/1048576)+" MB");}
    if(status==DownloadManager.STATUS_SUCCESSFUL){bar.setIndeterminate(false);bar.setProgress(100);percent.setText("Download completato · 100%\nPremi Installa aggiornamento.");dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);return;}
    if(status==DownloadManager.STATUS_FAILED){bar.setIndeterminate(false);percent.setText("Download fallito. Ripeti il controllo aggiornamenti.");return;}
   }catch(Exception e){percent.setText("Controllo download non riuscito: "+safe(e.getMessage()));return;}
   handler.postDelayed(poll[0],700);
  };
  dialog.setOnShowListener(v->{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x->install(a));handler.post(poll[0]);});
  dialog.setOnDismissListener(v->handler.removeCallbacks(poll[0]));dialog.show();
 }
 static void install(Activity a){
  long id=a.getSharedPreferences(PREFS,0).getLong(DOWNLOAD_ID,-1);
  if(id<=0){errorDialog(a,"Installazione","Nessun aggiornamento scaricato.");return;}
  DownloadManager dm=(DownloadManager)a.getSystemService(Context.DOWNLOAD_SERVICE);
  try(android.database.Cursor cursor=dm.query(new DownloadManager.Query().setFilterById(id))){
   if(cursor==null||!cursor.moveToFirst()||cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))!=DownloadManager.STATUS_SUCCESSFUL){
    errorDialog(a,"Installazione","Il download non è ancora terminato.");return;
   }
  }catch(Exception e){errorDialog(a,"Installazione",safe(e.getMessage()));return;}
  if(Build.VERSION.SDK_INT>=26&&!a.getPackageManager().canRequestPackageInstalls()){
   new AlertDialog.Builder(a).setTitle("Autorizzazione Android")
    .setMessage("Per installare l'aggiornamento abilita R. ITA TV come origine consentita. Se TIM Box non mostra questa impostazione, apri l'APK tramite un gestore file autorizzato.")
    .setPositiveButton("Apri impostazioni",(d,w)->{
      try{a.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+a.getPackageName())));}
      catch(Exception e){errorDialog(a,"Autorizzazione non disponibile","Il firmware TIM Box non espone questa schermata. Usa un gestore file autorizzato per installare l'APK.");}
    }).setNegativeButton("Chiudi",null).show();return;
  }
  Uri uri=dm.getUriForDownloadedFile(id);
  if(uri==null){errorDialog(a,"Installazione","File scaricato non disponibile.");return;}
  Intent install=new Intent(Intent.ACTION_VIEW);
  install.setDataAndType(uri,"application/vnd.android.package-archive");
  install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
  install.setClipData(ClipData.newUri(a.getContentResolver(),"Aggiornamento R. ITA TV",uri));
  try{a.startActivity(install);}catch(Exception e){errorDialog(a,"Installer non disponibile","Android non ha aperto l'APK: "+safe(e.getMessage())+". Controlla le autorizzazioni di installazione del box.");}
 }
 /** On first launch after a successful package update, delete only our downloaded APK. */
 static void cleanupAfterSuccessfulUpdate(Context c){
  android.content.SharedPreferences p=c.getSharedPreferences(PREFS,0);
  long id=p.getLong(DOWNLOAD_ID,-1);int older=p.getInt(VERSION_BEFORE,-1);
  if(id<=0||older<0||BuildConfig.VERSION_CODE<=older)return;
  try{((DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE)).remove(id);}catch(Exception ignored){}
  p.edit().remove(DOWNLOAD_ID).remove("download_tag").remove(VERSION_BEFORE).apply();
 }
 private static void errorDialog(Activity a,String title,String msg){if(!a.isFinishing())new AlertDialog.Builder(a).setTitle(title).setMessage(msg).setPositiveButton("OK",null).show();}
 private static String normalize(String value){if(value==null)return "";String s=value.trim();s=s.replaceFirst("^https?://github\\.com/","");s=s.replaceFirst("/+$","");if(s.endsWith(".git"))s=s.substring(0,s.length()-4);return s;}
 private static boolean valid(String s){return s!=null&&s.matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+");}
 private static int compare(String remote,String local){int[] a=numbers(remote),b=numbers(local);for(int i=0;i<Math.max(a.length,b.length);i++){int x=i<a.length?a[i]:0,y=i<b.length?b[i]:0;if(x!=y)return x>y?1:-1;}return 0;}
 private static int[] numbers(String s){String[] p=(s==null?"":s.replaceFirst("^[vV]","")).split("[^0-9]+");ArrayList<Integer> n=new ArrayList<>();for(String x:p)if(!x.isEmpty())try{n.add(Integer.parseInt(x));}catch(Exception ignored){}int[] out=new int[n.size()];for(int i=0;i<n.size();i++)out[i]=n.get(i);return out;}
 private static String read(InputStream in)throws IOException{if(in==null)return "";try(InputStream x=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];for(int n;(n=x.read(b))>=0;)out.write(b,0,n);return out.toString("UTF-8");}}
 private static String clean(String s){return s==null?"update":s.replaceAll("[^A-Za-z0-9._-]","-");}
 private static String safe(String s){return s==null||s.trim().isEmpty()?"Errore sconosciuto":s;}
 static final class Release{final String tag,title,url,file;Release(String t,String n,String u,String f){tag=t;title=n;url=u;file=f;}}
}
