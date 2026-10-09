package it.carmine.streamplayer;

/** Name-based categorisation. Unrecognised channels remain accessible in Tutti/Altri. */
public final class ChannelCategories {
    public static final String[] ALL={"Tutti","Generalisti","Sport","Cinema e serie","Intrattenimento","Documentari","Bambini","Notizie","Musica","Altri"};
    private static boolean has(String n,String... words){for(String w:words)if(n.contains(w))return true;return false;}
    public static String of(VavooClient.Channel channel){
        String n=EpgStore.normalize(channel.name);
        if(has(n,"sport","dazn","eurosport","skycalcio","skysupercalcio","skyf1","skymotogp","tennis","formula1","football","intertv","milantv","juventus","raisport"))return "Sport";
        if(has(n,"news","tg24","tgcom","rainews","bloomberg","cnn","euronews","aljazeera","classcnbc"))return "Notizie";
        if(has(n,"cartoon","boing","boomerang","nickelodeon","nickjr","nicktoons","frisbee","super!","superplus","raiyoyo","raigulp","baby","deakids","k2" )||n.equals("super"))return "Bambini";
        if(has(n,"cinema","movie","film","cine34","iris","raipremium","topcrime","giallo","skyatlantic","skyserie","skyprimafila","paramount" )||n.equals("rai4")||n.equals("20")||n.startsWith("20mediaset"))return "Cinema e serie";
        if(has(n,"discovery","nationalgeographic","natgeo","history","focus","animalplanet","document","dmax","raistoria","raiscuola","marcopolo"))return "Documentari";
        if(has(n,"music","mtv","vh1","rtl1025","radio","deejay","rds","italiamia","radionorba","radioitalia"))return "Musica";
        if(n.equals("rai1")||n.equals("rai2")||n.equals("rai3")||n.equals("rete4")||n.equals("canale5")||n.equals("italia1")||n.equals("la7")||n.equals("la7d")||n.equals("tv8")||n.equals("nove")||n.equals("cielo")||n.equals("tv2000"))return "Generalisti";
        if(has(n,"skyuno","realtime","tv8","comedy","foodnetwork","gamberorosso","hgtv","mediasetextra","la5","italia2","twentyseven","27","skyarte"))return "Intrattenimento";
        return "Altri";
    }
}
