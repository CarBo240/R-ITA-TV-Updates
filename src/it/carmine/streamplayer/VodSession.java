package it.carmine.streamplayer;

import android.app.*;import android.content.*;import java.util.*;

/** Session video preference and independently persisted catalogue preference. */
final class VodSession {
 private static String videoId="vixsrc";
 static void reset(){videoId="vixsrc";}
 static VodSettings.Source video(Context c){for(VodSettings.Source s:VodSettings.sources(c))if(s.id.equals(videoId))return s;for(VodSettings.Source s:VodSettings.sources(c))if(s.vixsrc())return s;return VodSettings.sources(c).get(0);}
 static VodSettings.Source catalogue(Context c){for(VodSettings.Source s:VodSettings.catalogues(c))if(s.id.equals(VodSettings.prefs(c).getString("catalogSource","tmdb")))return s;for(VodSettings.Source s:VodSettings.catalogues(c))if(s.tmdb())return s;return VodSettings.catalogues(c).get(0);}
 static void videoMenu(Activity a,Runnable changed){List<VodSettings.Source> list=VodSettings.sources(a);String[] labels=new String[list.size()];for(int i=0;i<list.size();i++)labels[i]=(list.get(i).id.equals(videoId)?"✓ ":"")+list.get(i).name;new AlertDialog.Builder(a).setTitle("Fonte per questa sessione").setItems(labels,(d,n)->{videoId=list.get(n).id;changed.run();}).setNegativeButton("Chiudi",null).show();}
 static void catalogueMenu(Activity a,Runnable changed){VodSettings.catalogueMenu(a,changed);}

}
