package it.carmine.streamplayer;
import org.junit.*;import static org.junit.Assert.*;
import java.io.*;import java.nio.*;import java.util.*;import java.util.zip.*;
public class GomstreamSegmentsTest {
 static byte[] video(){byte[] ts=new byte[188*4];for(int i=0;i<ts.length;i++)ts[i]=(byte)(i*7);for(int i=0;i<ts.length;i+=188)ts[i]=71;return ts;}
 static byte[] gzip(byte[] b)throws IOException {ByteArrayOutputStream out=new ByteArrayOutputStream();try(GZIPOutputStream gz=new GZIPOutputStream(out)){gz.write(b);}return out.toByteArray();}
 static byte[] join(byte[]... parts)throws IOException {ByteArrayOutputStream out=new ByteArrayOutputStream();for(byte[] b:parts)out.write(b);return out.toByteArray();}
 static byte[] chunk(String name,byte[] b)throws IOException {ByteArrayOutputStream out=new ByteArrayOutputStream();DataOutputStream d=new DataOutputStream(out);d.writeInt(b.length);d.writeBytes(name);d.write(b);CRC32 crc=new CRC32();crc.update(name.getBytes("US-ASCII"));crc.update(b);d.writeInt((int)crc.getValue());return out.toByteArray();}
 static byte[] png(int color,int filter)throws IOException {
  byte[] gz=gzip(video());byte[] payload=join("TIKTIKPX".getBytes("US-ASCII"),ByteBuffer.allocate(4).putInt(gz.length).array(),gz);
  int width=16,height=(payload.length+47)/48,stride=width*(color==6?4:3);byte[] pixels=new byte[width*height*3];System.arraycopy(payload,0,pixels,0,payload.length);ByteArrayOutputStream raw=new ByteArrayOutputStream();byte[] prev=new byte[stride];
  for(int y=0;y<height;y++){byte[] row=new byte[stride];for(int x=0;x<width;x++){int source=(y*width+x)*3,dest=x*(color==6?4:3);System.arraycopy(pixels,source,row,dest,3);if(color==6)row[dest+3]=(byte)255;}raw.write(filter);
   for(int i=0;i<stride;i++){int bpp=color==6?4:3,a=i>=bpp?row[i-bpp]&255:0,b=prev[i]&255,c=i>=bpp?prev[i-bpp]&255:0,p=a+b-c,pa=Math.abs(p-a),pb=Math.abs(p-b),pc=Math.abs(p-c);int predictor=filter==1?a:filter==2?b:filter==3?(a+b)/2:filter==4?(pa<=pb&&pa<=pc?a:pb<=pc?b:c):0;raw.write((row[i]&255)-predictor);}prev=row;
  }
  ByteArrayOutputStream packed=new ByteArrayOutputStream();try(DeflaterOutputStream z=new DeflaterOutputStream(packed)){z.write(raw.toByteArray());}
  byte[] header=ByteBuffer.allocate(13).putInt(width).putInt(height).put((byte)8).put((byte)color).put(new byte[3]).array();return join(new byte[]{(byte)137,80,78,71,13,10,26,10},chunk("IHDR",header),chunk("IDAT",packed.toByteArray()),chunk("IEND",new byte[0]));
 }
 @Test public void decodesRgbAndRgbaWithAllPngFilters()throws Exception {for(int color:new int[]{2,6})for(int filter=0;filter<=4;filter++)assertArrayEquals(video(),GomstreamSegments.unwrap(png(color,filter)));}
 @Test public void supportsTsRawGzipAndPngTail()throws Exception {assertArrayEquals(video(),GomstreamSegments.unwrap(video()));assertArrayEquals(video(),GomstreamSegments.unwrap(join(new byte[5],"TIKTIKRAW".getBytes("US-ASCII"),video())));assertArrayEquals(video(),GomstreamSegments.unwrap(join("TIKTIKTSGZ".getBytes("US-ASCII"),gzip(video()))));assertArrayEquals(video(),GomstreamSegments.unwrap(join(png(2,0),video())));}
 @Test public void supportsWebpExif()throws Exception {byte[] ts=video();byte[] body=join("WEBPEXIF".getBytes("US-ASCII"),ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ts.length).array(),ts);byte[] webp=join("RIFF".getBytes("US-ASCII"),ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(body.length).array(),body);assertArrayEquals(ts,GomstreamSegments.unwrap(webp));}
 @Test public void rejectsTruncatedOrUnrelatedData()throws Exception {for(byte[] b:new byte[][]{new byte[0],"<!doctype html>blocked".getBytes(),Arrays.copyOf(png(2,0),40)}){try{GomstreamSegments.unwrap(b);fail("invalid media accepted");}catch(IOException expected){}}}
 @Test public void boundsInflationAndDimensions()throws Exception {byte[] bomb=new byte[GomstreamSegments.LIMIT+1];try{GomstreamSegments.inflate(gzip(bomb),true);fail("inflation limit ignored");}catch(IOException expected){}byte[] png=png(2,0);ByteBuffer.wrap(png).putInt(16,Integer.MAX_VALUE);try{GomstreamSegments.unwrap(png);fail("dimensions unchecked");}catch(IOException expected){}}
 @Test public void supportsTrailingTsAfterNonRgbPng()throws Exception {byte[] png=png(2,0);png[25]=0;assertArrayEquals(video(),GomstreamSegments.unwrap(join(png,video())));}

}
