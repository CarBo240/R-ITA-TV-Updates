package it.carmine.streamplayer;

import android.app.*;import android.content.*;import android.widget.*;

/** Configurable host identity for the FreeTVSpor player exposed by Daddy Live. */
final class FreetvsporSettings {
 static String home(Context c){return c.getSharedPreferences("freetvspor",0).getString("home",FreetvsporSource.HOME);}
 static void edit(Activity a,Runnable changed){
  EditText input=new EditText(a);input.setSingleLine(true);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);input.setTag("freetvsporHomeInput");input.setText(home(a));input.setSelectAllOnFocus(true);
  AlertDialog d=new AlertDialog.Builder(a).setTitle("Indirizzo FreeTVSpor").setMessage("R. ITA individua automaticamente questo player nell’elenco delle fonti del canale. Se il servizio cambia dominio, inserisci qui il nuovo dominio HTTPS.").setView(input).setPositiveButton("Salva",null).setNeutralButton("Ripristina",(x,n)->{a.getSharedPreferences("freetvspor",0).edit().remove("home").apply();if(changed!=null)changed.run();}).setNegativeButton("Annulla",null).create();
  d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String url=DaddyLiveSettings.normalize(input.getText().toString());if(url.isEmpty()){input.setError("Inserisci un dominio HTTPS valido");return;}a.getSharedPreferences("freetvspor",0).edit().putString("home",url).apply();d.dismiss();if(changed!=null)changed.run();}));d.show();
 }
}
