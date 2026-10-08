package it.carmine.streamplayer;

import android.app.*;import android.content.*;import java.util.*;

/** Non-persistent VOD defaults. Choices never leak into saved application settings. */
final class VodSession {
 private static String videoId="vixsrc",catalogueId="tmdb";
 static void reset(){videoId="vixsrc";catalogueId="tmdb";}
 static VodSettings.Source video(Context c){for(VodSettings.Source s:VodSettings.sources(c))if(s.id.equals(videoId))return s;for(VodSettings.Source s:VodSettings.sources(c))if(s.vixsrc())return s;return VodSettings.sources(c).get(0);}
 static VodSettings.Source catalogue(Context c){for(VodSettings.Source s:VodSettings.catalogues(c))if(s.id.equals(catalogueId))return s;for(VodSettings.Source s:VodSettings.catalogues(c))if(s.tmdb())return s;return VodSettings.catalogues(c).get(0);}
 static void videoMenu(Activity a,Runnable changed){List<VodSettings.Source> list=VodSettings.sources(a);String[] labels=new String[list.size()];for(int i=0;i<list.size();i++)labels[i]=(list.get(i).id.equals(videoId)?"✓ ":"")+list.get(i).name;new AlertDialog.Builder(a).setTitle("Fonte per questa sessione").setItems(labels,(d,n)->{videoId=list.get(n).id;changed.run();}).setNegativeButton("Chiudi",null).show();}
 static void catalogueMenu(Activity a,Runnable changed){List<VodSettings.Source> list=VodSettings.catalogues(a);String[] labels=new String[list.size()];for(int i=0;i<list.size();i++)labels[i]=(list.get(i).id.equals(catalogueId)?"✓ ":"")+list.get(i).name;new AlertDialog.Builder(a).setTitle("Catalogo per questa sezione").setItems(labels,(d,n)->{catalogueId=list.get(n).id;changed.run();}).setNegativeButton("Chiudi",null).show();}
}
