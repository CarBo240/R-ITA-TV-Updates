package it.carmine.streamplayer;
import android.app.*;import android.content.*;import android.widget.*;
final class VavooSettings {
 static final String HOME="https://vavoo.to";
 static String home(Context c){return c.getSharedPreferences("vavoo",0).getString("home",HOME);}
 static String[] bases(Context c){String h=home(c);return HOME.equals(h)?VavooClient.BASES.clone():new String[]{h};}
 static void edit(Activity a,Runnable changed){
  EditText input=new EditText(a);input.setTag("vavooHomeInput");input.setSingleLine(true);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);input.setText(home(a));input.setSelectAllOnFocus(true);
  AlertDialog d=new AlertDialog.Builder(a).setTitle("Indirizzo Vavoo").setMessage("Dominio HTTPS per catalogo e apertura dei canali Vavoo. Il nuovo sito deve usare lo stesso protocollo. La sessione usa il servizio Vavoo originale.").setView(input).setPositiveButton("Salva",null).setNeutralButton("Ripristina",(x,n)->{a.getSharedPreferences("vavoo",0).edit().remove("home").apply();if(changed!=null)changed.run();}).setNegativeButton("Annulla",null).create();
  d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String url=DaddyLiveSettings.normalize(input.getText().toString());if(url.isEmpty()){input.setError("Inserisci un dominio HTTPS valido");return;}a.getSharedPreferences("vavoo",0).edit().putString("home",url).apply();d.dismiss();if(changed!=null)changed.run();}));d.show();
 }
}
