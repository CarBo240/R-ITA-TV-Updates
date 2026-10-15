package it.carmine.streamplayer;

/** The home guide and player share one channel model; providers resolve their own IDs. */
enum ChannelSource {
 VAVOO("Vavoo"), GOMSTREAM("Gomstream"), FREETVSPOR("FreeTVSpor");
 final String label;ChannelSource(String label){this.label=label;}
 static ChannelSource saved(android.content.Context c){try{return valueOf(c.getSharedPreferences("player",0).getString("channel_source","VAVOO"));}catch(Exception e){return VAVOO;}}
 static VavooClient.Channel gomstream(GomstreamSource.Channel c){
  String name=c.name.replaceAll("(?i)\\s+(?:italy|italia|italian|it)\\s*$","").trim();
  return new VavooClient.Channel(name,"Italy",c.id,GOMSTREAM);
 }
 static VavooClient.Channel freetvspor(FreetvsporSource.Channel c){
  String name=c.name.replaceAll("(?i)\\s+(?:italy|italia|italian|it)\\s*$","").trim();
  return new VavooClient.Channel(name,"Italy",c.id,FREETVSPOR);
 }
}
