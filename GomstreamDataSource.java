package it.carmine.streamplayer;

import android.net.Uri;
import androidx.media3.common.C;
import androidx.media3.datasource.*;
import java.io.*;
import java.util.*;

/** Buffers and adapts media segments only; playlists and key requests stay standard HTTP. */
final class GomstreamDataSource implements DataSource {
    private final DataSource upstream;private byte[] decoded;private int position,end;
    GomstreamDataSource(DataSource upstream){this.upstream=upstream;}
    @Override public void addTransferListener(TransferListener listener){upstream.addTransferListener(listener);}
    @Override public long open(DataSpec spec)throws IOException {
        decoded=null;position=end=0;
        // Byte ranges refer to decoded media, not to the PNG envelope.
        DataSpec full=spec.buildUpon().setPosition(0).setLength(C.LENGTH_UNSET).build();
        try{upstream.open(full);
            InputStream in=new InputStream(){public int read()throws IOException {byte[] b=new byte[1];return read(b,0,1)==-1?-1:b[0]&255;}public int read(byte[] b,int off,int len)throws IOException{return upstream.read(b,off,len);}};
            decoded=GomstreamSegments.unwrap(GomstreamSegments.read(in));
            if(spec.position>decoded.length)throw new IOException("Posizione segmento non valida");position=(int)spec.position;end=decoded.length;
            if(spec.length!=C.LENGTH_UNSET)end=(int)Math.min(end,spec.position+spec.length);return end-position;
        }catch(IOException|RuntimeException e){decoded=null;try{upstream.close();}catch(IOException ignored){}throw e;}
    }
    @Override public int read(byte[] b,int offset,int length)throws IOException {if(length==0)return 0;if(decoded==null)throw new IOException("Segmento non aperto");if(position>=end)return C.RESULT_END_OF_INPUT;int n=Math.min(length,end-position);System.arraycopy(decoded,position,b,offset,n);position+=n;return n;}
    @Override public Uri getUri(){return upstream.getUri();}
    @Override public Map<String,List<String>> getResponseHeaders(){return upstream.getResponseHeaders();}
    @Override public void close()throws IOException {decoded=null;position=end=0;upstream.close();}
}
