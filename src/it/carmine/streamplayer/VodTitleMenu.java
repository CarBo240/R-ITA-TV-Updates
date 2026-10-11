package it.carmine.streamplayer;
import android.app.*;
/** One menu for home posters and catalogue grids; changes happen only after a choice. */
final class VodTitleMenu {
 static void show(Activity a,VodSource.Title title,VodSettings.Source source,Runnable changed){
  String scope=title.json.optBoolean("_rai")?"raiplay":TmdbClient.scope(title,source),id=TmdbClient.identity(title);
  String[] labels={VodLibrary.watchlisted(a,scope,id)?"Da guardare · rimuovi":"Da guardare · aggiungi",VodLibrary.favorite(a,scope,id)?"Preferiti · rimuovi":"Preferiti · aggiungi","Continua a guardare · rimuovi dalla cronologia"};
  new AlertDialog.Builder(a).setTitle(title.name).setItems(labels,(dialog,n)->{
   if(n==0)VodLibrary.watchlist(a,scope,title);
   else if(n==1)VodLibrary.favorite(a,scope,title);
   else {VodLibrary.removeHistory(a,scope,id);if(!"tmdb".equals(scope)&&!title.json.optBoolean("_rai"))VodLibrary.removeHistory(a,"tmdb",id);}
   changed.run();
  }).setNegativeButton("Chiudi",null).show();
 }
}
