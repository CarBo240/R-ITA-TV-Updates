package it.carmine.streamplayer;
import android.net.Uri;import androidx.media3.common.C;import androidx.media3.datasource.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.Config;
import java.io.*;import java.util.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=31)
public class GomstreamDataSourceTest {
 static class Fake implements DataSource {
  byte[] bytes;int p;DataSpec spec;boolean closed;Fake(byte[] b){bytes=b;}
  public void addTransferListener(TransferListener l){}public long open(DataSpec s){spec=s;p=0;return bytes.length;}public int read(byte[] b,int off,int len){if(p==bytes.length)return -1;int n=Math.min(len,bytes.length-p);System.arraycopy(bytes,p,b,off,n);p+=n;return n;}public Uri getUri(){return spec.uri;}public Map<String,List<String>> getResponseHeaders(){return Collections.emptyMap();}public void close(){closed=true;}
 }
 @Test public void decodedRangesDoNotSendRangesForThePngEnvelope()throws Exception {
  Fake upstream=new Fake(GomstreamSegmentsTest.png(2,4));GomstreamDataSource src=new GomstreamDataSource(upstream);DataSpec spec=new DataSpec.Builder().setUri("https://cdn.test/segment.image").setPosition(188).setLength(188).build();assertEquals(188,src.open(spec));assertEquals(0,upstream.spec.position);assertEquals(C.LENGTH_UNSET,upstream.spec.length);byte[] out=new byte[188];assertEquals(188,src.read(out,0,out.length));assertArrayEquals(Arrays.copyOfRange(GomstreamSegmentsTest.video(),188,376),out);assertEquals(C.RESULT_END_OF_INPUT,src.read(out,0,1));assertEquals(0,src.read(out,0,0));src.close();assertTrue(upstream.closed);
 }
 @Test public void malformedSegmentsCloseTheConnection()throws Exception {Fake upstream=new Fake("<html>error</html>".getBytes());GomstreamDataSource src=new GomstreamDataSource(upstream);try{src.open(new DataSpec(Uri.parse("https://cdn.test/segment")));fail();}catch(IOException expected){}assertTrue(upstream.closed);}
 @Test public void reopensWithFreshBytesWithoutReusingOldData()throws Exception {Fake upstream=new Fake(GomstreamSegmentsTest.png(2,0));GomstreamDataSource src=new GomstreamDataSource(upstream);DataSpec spec=new DataSpec(Uri.parse("https://cdn.test/segment"));src.open(spec);src.close();upstream.bytes="blocked".getBytes();try{src.open(spec);fail();}catch(IOException expected){}}
 @Test public void playlistRefreshReplacesOnlyTheCacheBuster(){Uri u=Uri.parse("https://edge.test/index.m3u8?token=a%2Fb&_=old&mode=live");assertEquals("https://edge.test/index.m3u8?token=a%2Fb&mode=live&_=42",GomstreamManifestDataSource.fresh(u,42).toString());}

}
