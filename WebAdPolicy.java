package it.carmine.streamplayer;
import android.content.Context;
import org.json.*;
import java.net.URI;
import java.util.*;
import java.io.*;

final class WebAdPolicy {
    private final List<String> domains=new ArrayList<>();
    WebAdPolicy(Context c){try(InputStream in=c.getAssets().open("adblock/rules.json");ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);JSONArray values=new JSONObject(out.toString("UTF-8")).getJSONArray("domains");for(int i=0;i<values.length();i++)domains.add(values.getString(i));}catch(Exception e){throw new IllegalStateException("Filtro pubblicità non disponibile",e);}}
    boolean blocked(String raw){try{String host=new URI(raw).getHost();if(host==null)return false;host=host.toLowerCase(Locale.ROOT).replaceFirst("\\.$","");for(String d:domains)if(host.equals(d)||host.endsWith("."+d))return true;}catch(Exception ignored){}return false;}
    boolean allowNavigation(String raw,String initial,boolean daddyMain){
        try{URI u=new URI(raw);String scheme=u.getScheme();if(!Arrays.asList("https","http","about","blob","data").contains(scheme)||blocked(raw))return false;
            if(!daddyMain||"about".equals(scheme))return true;
            URI base=new URI(initial);return u.getHost()!=null&&u.getHost().equalsIgnoreCase(base.getHost())&&u.getPort()==base.getPort()&&u.getScheme().equalsIgnoreCase(base.getScheme());
        }catch(Exception e){return false;}
    }
}
