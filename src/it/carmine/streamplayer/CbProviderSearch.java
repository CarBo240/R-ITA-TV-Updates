package it.carmine.streamplayer;
import android.app.Activity;import android.os.*;import android.widget.FrameLayout;import java.util.*;import org.json.*;
/** Provider lookup uses the same dynamic movie/series navigation as the catalogue. */
final class CbProviderSearch {
 interface Callback{void done(VodProviders.Match match);}
 private final Activity activity;private final FrameLayout root;private final VodSettings.Source source;private final VodSource.Title wanted;private final Callback callback;
 private final Handler ui=new Handler(Looper.getMainLooper());private CbCatalogLoader loader;private boolean closed;private int queryIndex;private final List<String> queries=new ArrayList<>();
 CbProviderSearch(Activity a,FrameLayout r,VodSettings.Source s,VodSource.Title t,Callback cb){activity=a;root=r;source=s;wanted=t;callback=cb;queries.add(t.name);String original=original();if(!original.isEmpty()&&!CbTitleMatch.clean(original).equals(CbTitleMatch.clean(t.name)))queries.add(original);}
 private String original(){JSONObject m=TmdbClient.metadata(wanted);return m==null?wanted.json.optString("original_name"):m.optString("original_name");}
 void start(){ui.postDelayed(()->finish(new VodProviders.Match(source,null,"Ricerca scaduta · riprova")),120000);next();}
 private void next(){if(closed)return;if(queryIndex>=queries.size()){finish(new VodProviders.Match(source,null,"Titolo esatto non trovato"));return;}loader=new CbCatalogLoader(activity,root);loader.load(source,queries.get(queryIndex++),wanted.series()?2:1,(titles,error)->{if(closed)return;VodSource.Title best=null;for(VodSource.Title t:titles){if(t.series()!=wanted.series()||!CbTitleMatch.same(wanted.name,original(),t.name)||!CbTitleMatch.yearMatches(wanted.year,wanted.series(),t.name))continue;if(best==null)best=t;}if(best==null){next();return;}try{JSONObject j=new JSONObject(wanted.json.toString());j.remove("_web_search");j.remove("_tmdb");j.put("_web_page",best.json.getString("_web_page"));JSONObject metadata=TmdbClient.metadata(wanted);if(metadata!=null)j.put("_metadata",metadata);finish(new VodProviders.Match(source,new VodSource.Title(j,""),"Disponibile · "+(wanted.series()?"serie TV":"film"),j.getString("_web_page"),false));}catch(Exception e){finish(new VodProviders.Match(source,null,"Pagina CB01 non valida"));}});}
 private void finish(VodProviders.Match match){if(closed)return;close();callback.done(match);}
 void close(){if(closed)return;closed=true;ui.removeCallbacksAndMessages(null);if(loader!=null)loader.close();}
}
