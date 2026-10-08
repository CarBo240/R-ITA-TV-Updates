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
  String repo=repository(a);if(!valid(repo)){if(manual)editRepository(a,null);return;}
  android.content.SharedPreferences p=a.getSharedPreferences(PREFS,0);long now=System.currentTimeMillis();if(!manual&&now-p.getLong("last_check",0)<AUTO_INTERVAL)return;p.edit().putLong("last_check",now).apply();
  if(manual)Toast.makeText(a,"Controllo aggiornamenti…",Toast.LENGTH_SHORT).show();
  new Thread(()->{try{Release release=latest(repo);a.runOnUiThread(()->showResult(a,release,manual));}catch(Exception e){if(manual)a.runOnUiThread(()->new AlertDialog.Builder(a).setTitle("Aggiornamenti").setMessage("Controllo non riuscito. Verifica la connessione e il repository.\n\n"+safe(e.getMessage())).setPositiveButton("OK",null).show());}},"github-update-check").start();
 }
 private static Release latest(String repo)throws Exception{
  HttpURLConnection c=(HttpURLConnection)new URL("https://api.github.com/repos/"+repo+"/releases/latest").openConnection();c.setConnectTimeout(10000);c.setReadTimeout(15000);c.setRequestProperty("Accept","application/vnd.github+json");c.setRequestProperty("User-Agent","R-ITA-TV-Android");int status=c.getResponseCode();InputStream stream=status>=200&&status<300?c.getInputStream():c.getErrorStream();String body=read(stream);if(status<200||status>=300)throw new IOException("GitHub HTTP "+status);JSONObject root=new JSONObject(body);String tag=root.optString("tag_name");JSONArray assets=root.optJSONArray("assets");String apk="",name="";int score=-1;if(assets!=null)for(int i=0;i<assets.length();i++){JSONObject asset=assets.optJSONObject(i);if(asset==null)continue;String n=asset.optString("name");if(!n.toLowerCase(Locale.ROOT).endsWith(".apk"))continue;int s=n.toLowerCase(Locale.ROOT).contains("universal")?2:1;if(s>score){score=s;name=n;apk=asset.optString("browser_download_url");}}if(apk.isEmpty())throw new IOException("La release non contiene un APK");return new Release(tag,root.optString("name",tag),apk,name);
 }
 private static void showResult(Activity a,Release r,boolean manual){if(a.isFinishing())return;if(compare(r.tag,BuildConfig.VERSION_NAME)<=0){if(manual)new AlertDialog.Builder(a).setTitle("R. ITA TV è aggiornata").setMessage("Versione installata: "+BuildConfig.VERSION_NAME+"\nUltima release: "+r.tag).setPositiveButton("OK",null).show();return;}new AlertDialog.Builder(a).setTitle("Aggiornamento "+r.tag+" disponibile").setMessage("Verrà scaricato l'APK universale da GitHub. Preferiti, account Firebase, avanzamento VOD e impostazioni restano invariati.").setPositiveButton("Scarica",(d,w)->download(a,r)).setNegativeButton("Più tardi",null).show();}
 private static void download(Activity a,Release r){try{DownloadManager.Request q=new DownloadManager.Request(Uri.parse(r.url));q.setTitle("R. ITA TV "+r.tag);q.setDescription("Download aggiornamento da GitHub");q.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);q.setMimeType("application/vnd.android.package-archive");q.setDestinationInExternalFilesDir(a,Environment.DIRECTORY_DOWNLOADS,"R-ITA-TV-"+clean(r.tag)+"-universal.apk");DownloadManager dm=(DownloadManager)a.getSystemService(Context.DOWNLOAD_SERVICE);long id=dm.enqueue(q);a.getSharedPreferences(PREFS,0).edit().putLong(DOWNLOAD_ID,id).putString("download_tag",r.tag).apply();Toast.makeText(a,"Download avviato",Toast.LENGTH_LONG).show();}catch(Exception e){new AlertDialog.Builder(a).setTitle("Download non riuscito").setMessage(safe(e.getMessage())).setPositiveButton("OK",null).show();}}
 static void installDownloaded(Context c,long completedId){android.content.SharedPreferences p=c.getSharedPreferences(PREFS,0);if(completedId!=p.getLong(DOWNLOAD_ID,-1))return;DownloadManager dm=(DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE);Uri apk=dm.getUriForDownloadedFile(completedId);if(apk==null)return;Intent install=new Intent(Intent.ACTION_VIEW).setDataAndType(apk,"application/vnd.android.package-archive").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);try{c.startActivity(install);}catch(Exception e){if(Build.VERSION.SDK_INT>=26){Intent settings=new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+c.getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);try{c.startActivity(settings);Toast.makeText(c,"Autorizza R. ITA TV, poi apri il download completato",Toast.LENGTH_LONG).show();}catch(Exception ignored){}}}}
 private static String normalize(String value){if(value==null)return "";String s=value.trim();s=s.replaceFirst("^https?://github\\.com/","");s=s.replaceFirst("/+$","");if(s.endsWith(".git"))s=s.substring(0,s.length()-4);return s;}
 private static boolean valid(String s){return s!=null&&s.matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+");}
 private static int compare(String remote,String local){int[] a=numbers(remote),b=numbers(local);for(int i=0;i<Math.max(a.length,b.length);i++){int x=i<a.length?a[i]:0,y=i<b.length?b[i]:0;if(x!=y)return x>y?1:-1;}return 0;}
 private static int[] numbers(String s){String[] p=(s==null?"":s.replaceFirst("^[vV]","")).split("[^0-9]+");ArrayList<Integer> n=new ArrayList<>();for(String x:p)if(!x.isEmpty())try{n.add(Integer.parseInt(x));}catch(Exception ignored){}int[] out=new int[n.size()];for(int i=0;i<n.size();i++)out[i]=n.get(i);return out;}
 private static String read(InputStream in)throws IOException{if(in==null)return "";try(InputStream x=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];for(int n;(n=x.read(b))>=0;)out.write(b,0,n);return out.toString("UTF-8");}}
 private static String clean(String s){return s==null?"update":s.replaceAll("[^A-Za-z0-9._-]","-");}
 private static String safe(String s){return s==null||s.trim().isEmpty()?"Errore sconosciuto":s;}
 static final class Release{final String tag,title,url,file;Release(String t,String n,String u,String f){tag=t;title=n;url=u;file=f;}}
}
