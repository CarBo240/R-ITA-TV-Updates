package it.carmine.streamplayer;

import android.app.*;
import android.content.*;
import android.widget.*;
import java.net.URI;
import java.util.Locale;

final class DaddyLiveSettings {
    static String home(Context c){return c.getSharedPreferences("daddyLive",0).getString("home",DaddyLiveSource.HOME);}
    static String normalize(String input){
        try{String value=input.trim();if(!value.contains("://"))value="https://"+value;URI u=new URI(value);
            if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null||u.getQuery()!=null||u.getFragment()!=null||u.getPort()>65535)return "";
            String path=u.getPath();if(path!=null&&!path.isEmpty()&&!"/".equals(path))return "";
            return new URI("https",null,u.getHost().toLowerCase(Locale.ROOT),u.getPort(),null,null,null).toASCIIString();
        }catch(Exception e){return "";}
    }
    static void save(Context c,String home){c.getSharedPreferences("daddyLive",0).edit().putString("home",home).apply();}
    static void edit(Activity a,Runnable changed){
        EditText input=new EditText(a);input.setSingleLine(true);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);input.setText(home(a));input.setSelectAllOnFocus(true);input.setTag("daddyHomeInput");
        AlertDialog dialog=new AlertDialog.Builder(a).setTitle("Indirizzo Daddy Live").setMessage("Inserisci il nuovo dominio HTTPS di DaddyLive. Canali, eventi e player useranno questo indirizzo.").setView(input).setPositiveButton("Salva",null).setNeutralButton("Ripristina",(d,n)->{save(a,DaddyLiveSource.HOME);if(changed!=null)changed.run();Toast.makeText(a,"Indirizzo predefinito ripristinato",Toast.LENGTH_SHORT).show();}).setNegativeButton("Annulla",null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String value=normalize(input.getText().toString());if(value.isEmpty()){input.setError("Inserisci solo un dominio HTTPS valido, senza percorsi o parametri");return;}save(a,value);dialog.dismiss();if(changed!=null)changed.run();Toast.makeText(a,"Indirizzo Daddy Live salvato",Toast.LENGTH_SHORT).show();}));dialog.show();
    }
}
