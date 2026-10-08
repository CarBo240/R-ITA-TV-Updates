package it.carmine.streamplayer;
import android.app.*;
import android.content.*;
import android.widget.*;
import java.net.URI;
final class EventSettings {
 static String home(Context c){return c.getSharedPreferences("events",0).getString("sportsHomeUrl",EventSource.HOME);}
 static String normalize(String input){try{String value=input.trim();if(!value.contains("://"))value="https://"+value;URI u=new URI(value);if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null||u.getQuery()!=null||u.getFragment()!=null)return "";return new URI("https",null,u.getHost().toLowerCase(java.util.Locale.ROOT),u.getPort(),u.getPath().isEmpty()?"/":u.getPath(),null,null).toASCIIString();}catch(Exception e){return "";}}
 static void save(Context c,String home){c.getSharedPreferences("events",0).edit().putString("sportsHomeUrl",home).remove("sportsRetryUntil").apply();new java.io.File(c.getFilesDir(),"sportsonline-schedule.json").delete();}
 static void edit(Activity a){EditText input=new EditText(a);input.setSingleLine(true);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);input.setText(home(a));input.setSelectAllOnFocus(true);input.setTag("eventsHomeInput");AlertDialog dialog=new AlertDialog.Builder(a).setTitle("Indirizzo SportOnline").setMessage("Inserisci il nuovo sito HTTPS oppure il link del calendario prog.txt.").setView(input).setPositiveButton("Salva",null).setNeutralButton("Ripristina",(d,n)->{save(a,EventSource.HOME);Toast.makeText(a,"Indirizzo predefinito ripristinato",Toast.LENGTH_SHORT).show();}).setNegativeButton("Annulla",null).create();dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String value=normalize(input.getText().toString());if(value.isEmpty()){input.setError("Inserisci un indirizzo HTTPS valido, senza credenziali o parametri");return;}save(a,value);dialog.dismiss();Toast.makeText(a,"Indirizzo salvato · apri Eventi per aggiornare",Toast.LENGTH_SHORT).show();}));dialog.show();}
}
