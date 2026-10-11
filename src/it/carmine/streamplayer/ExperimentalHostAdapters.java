package it.carmine.streamplayer;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * LAB ONLY. Host-specific dispatch with a one-hop inspection of publicly declared media.
 * Every host is dispatched separately so an adapter can be replaced without affecting a
 * catalogue, another host, or the original VOD. Does not execute protected scripts,
 * reconstruct signed links or bypass access controls.
 */
final class ExperimentalHostAdapters {
 private ExperimentalHostAdapters() {}
 private static final String[][] HOSTS={
  {"mixdrop","MixDrop"},{"maxstream","MaxStream"},{"streamtape","StreamTape"},
  {"voe","VOE"},{"dood","DoodStream"},{"supervideo","SuperVideo"},
  {"streamsb","StreamSB"},{"streamhide","StreamHide"},{"streamwish","StreamWish"},
  {"upstream","UpStream"},{"vidoza","Vidoza"},{"vidlox","VidLox"},
  {"videovard","VideoVard"},{"fembed","Fembed"},{"filemoon","FileMoon"},
  {"streamhg","StreamHG"},{"streamlare","StreamLare"},{"turbovid","TurboVid"},
  {"wolfstream","WolfStream"},{"dropload","DropLoad"},{"userload","UserLoad"},
  {"jetload","JetLoad"},{"vidto","VidTo"}
 };
 static String adapterFor(String host){
  if(host==null)return "";
  String h=host.toLowerCase(Locale.ROOT);
  for(String[] a:HOSTS)if(h.contains(a[0]))return a[1];
  return "";
 }
 private static boolean safeHostLink(ExperimentalHostInspector.Link link){
  if(link.direct)return false;
  try{
   URI uri=new URI(link.url);
   return "https".equalsIgnoreCase(uri.getScheme())&&uri.getPort()==-1&&uri.getUserInfo()==null&&
    uri.getRawQuery()==null&&uri.getRawFragment()==null&&!adapterFor(uri.getHost()).isEmpty();
  }catch(Exception ignored){return false;}
 }
 static ExperimentalHostInspector.Result inspect(String url)throws Exception{
  ExperimentalHostInspector.Result first=ExperimentalMultiHostResolver.inspect(url);
  LinkedHashMap<String,ExperimentalHostInspector.Link> merged=new LinkedHashMap<>();
  for(ExperimentalHostInspector.Link link:first.links)merged.put(link.url,link);
  int checked=0,found=0,unavailable=0;
  // At most five public host landing pages; do not follow arbitrary links or recurse.
  List<ExperimentalHostInspector.Link> candidates=new ArrayList<>(first.links);
  for(ExperimentalHostInspector.Link link:candidates){
   if(!safeHostLink(link)||checked>=5||merged.size()>=80)continue;
   checked++;
   try{
    ExperimentalHostInspector.Result host=ExperimentalMultiHostResolver.inspect(link.url);
    for(ExperimentalHostInspector.Link foundLink:host.links){
     if(foundLink.direct && !merged.containsKey(foundLink.url) && merged.size()<80){
      merged.put(foundLink.url,new ExperimentalHostInspector.Link(foundLink.url,
       adapterFor(link.displayHost)+" · "+foundLink.kind,foundLink.displayHost,
       "Sorgente dichiarata pubblicamente nella pagina dell'host; accessibilità da verificare",true));found++;
     }
    }
   }catch(Exception ignored){unavailable++;}
  }
  String message="LAB: "+checked+" host esaminati singolarmente, "+found+
   " collegamenti video pubblicamente dichiarati, "+unavailable+
   " host non analizzabili. Nessuna sorgente è garantita: i link protetti o generati da script richiedono l'integrazione supportata dal servizio.";
  return new ExperimentalHostInspector.Result(first.label,first.displayHost,message,first.status,new ArrayList<>(merged.values()));
 }
}
