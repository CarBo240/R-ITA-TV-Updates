package it.carmine.streamplayer;
import android.app.*;import android.content.*;import android.widget.*;
final class GomstreamSettings {
 static String home(Context c){return c.getSharedPreferences("gomstream",0).getString("home",GomstreamSource.HOME);}
 static void edit(Activity a,Runnable changed){
  EditText input=new EditText(a);input.setSingleLine(true);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);input.setTag("gomstreamHomeInput");input.setText(home(a));input.setSelectAllOnFocus(true);
  AlertDialog d=new AlertDialog.Builder(a).setTitle("Indirizzo Gomstream").setMessage("Le sorgenti vengono rilette dal sito a ogni apertura. Se il servizio cambia dominio senza reindirizzamento, puoi inserire qui il nuovo dominio HTTPS. Il catalogo usa l’indirizzo Daddy Live impostato nell’app.").setView(input).setPositiveButton("Salva",null).setNeutralButton("Ripristina",(x,n)->{a.getSharedPreferences("gomstream",0).edit().remove("home").apply();if(changed!=null)changed.run();}).setNegativeButton("Annulla",null).create();
  d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String url=DaddyLiveSettings.normalize(input.getText().toString());if(url.isEmpty()){input.setError("Inserisci un dominio HTTPS valido");return;}a.getSharedPreferences("gomstream",0).edit().putString("home",url).apply();d.dismiss();if(changed!=null)changed.run();}));d.show();
 }
}
