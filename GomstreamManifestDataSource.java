package it.carmine.streamplayer;
import android.net.Uri;import androidx.media3.datasource.*;import java.io.*;import java.util.*;

/** Same cache-busting of live playlist refreshes as the provider's public player. */
final class GomstreamManifestDataSource implements DataSource {
 private final DataSource upstream;GomstreamManifestDataSource(DataSource source){upstream=source;}
 @Override public void addTransferListener(TransferListener l){upstream.addTransferListener(l);}
 static Uri fresh(Uri uri,long now){String q=uri.getEncodedQuery();List<String> parts=new ArrayList<>();if(q!=null)for(String p:q.split("&"))if(!p.matches("_=(.*)")&&!p.isEmpty())parts.add(p);parts.add("_="+now);return uri.buildUpon().encodedQuery(String.join("&",parts)).build();}
 @Override public long open(DataSpec spec)throws IOException{return upstream.open(spec.buildUpon().setUri(fresh(spec.uri,System.currentTimeMillis())).build());}
 @Override public int read(byte[] b,int off,int len)throws IOException{return upstream.read(b,off,len);}
 @Override public Uri getUri(){return upstream.getUri();}
 @Override public Map<String,List<String>> getResponseHeaders(){return upstream.getResponseHeaders();}
 @Override public void close()throws IOException{upstream.close();}
}
