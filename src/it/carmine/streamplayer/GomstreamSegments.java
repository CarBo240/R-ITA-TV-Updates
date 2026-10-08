package it.carmine.streamplayer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

/** Adapter for the public PNG/WebP/raw/gzip segment containers used by the site. */
final class GomstreamSegments {
    static final int LIMIT=16*1024*1024;
    static byte[] read(InputStream in)throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;
        while((n=in.read(b))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();if(out.size()+n>LIMIT)throw new IOException("Segmento troppo grande");out.write(b,0,n);}return out.toByteArray();
    }
    static boolean ts(byte[] b,int p){return p>=0&&p+376<b.length&&b[p]==0x47&&b[p+188]==0x47&&b[p+376]==0x47;}
    static boolean text(byte[] b,int p,String value){byte[] t=value.getBytes(StandardCharsets.US_ASCII);if(p<0||p+t.length>b.length)return false;for(int i=0;i<t.length;i++)if(b[p+i]!=t[i])return false;return true;}
    static long uint(byte[] b,int p,boolean little)throws IOException {if(p<0||p+4>b.length)throw new IOException("Segmento troncato");long v=0;for(int i=0;i<4;i++)v=(v<<8)|(b[p+(little?3-i:i)]&255);return v;}
    static byte[] inflate(byte[] b,boolean gzip)throws IOException {try(InputStream in=gzip?new GZIPInputStream(new ByteArrayInputStream(b)):new InflaterInputStream(new ByteArrayInputStream(b))){return read(in);}}
    static byte[] checked(byte[] b)throws IOException {if(!ts(b,0))throw new IOException("Segmento video non riconosciuto");return b;}
    static byte[] unwrap(byte[] bytes)throws IOException {
        if(bytes.length>LIMIT)throw new IOException("Segmento troppo grande");if(ts(bytes,0))return bytes;
        if(text(bytes,0,"RIFF")&&text(bytes,8,"WEBP")){
            for(int p=12;p+8<=bytes.length;){long n=uint(bytes,p+4,true);int start=p+8;if(n>bytes.length-start)throw new IOException("WebP troncato");if(text(bytes,p,"EXIF"))return checked(Arrays.copyOfRange(bytes,start,start+(int)n));p=start+(int)n+((int)n&1);}
            throw new IOException("Video non presente nel WebP");
        }
        if(bytes.length>=8&&(bytes[0]&255)==137&&text(bytes,1,"PNG\r\n\u001a\n"))return png(bytes);
        for(String marker:new String[]{"TIKTIKRAW","TIKTIKTSGZ"}){
            for(int i=0;i+marker.length()<bytes.length;i++)if(text(bytes,i,marker)){byte[] payload=Arrays.copyOfRange(bytes,i+marker.length(),bytes.length);return checked(marker.endsWith("GZ")?inflate(payload,true):payload);}
        }
        // Ordinary TS may have a short container prefix; require three packet syncs.
        for(int i=1;i<Math.min(1024,bytes.length);i++)if(ts(bytes,i))return checked(Arrays.copyOfRange(bytes,i,bytes.length));
        throw new IOException("Formato del segmento cambiato");
    }
    static byte[] png(byte[] bytes)throws IOException {
        int w=0,h=0,ctype=0,depth=0,compression=0,filterMethod=0,interlace=0;boolean header=false,end=false;ByteArrayOutputStream idat=new ByteArrayOutputStream();
        for(int p=8;p+12<=bytes.length;){long size=uint(bytes,p,false);if(size>bytes.length-p-12)throw new IOException("PNG troncato");int n=(int)size,start=p+8,next=p+12+n;
            if(text(bytes,p+4,"IHDR")){
                if(n!=13||header)throw new IOException("PNG non valido");long width=uint(bytes,start,false),height=uint(bytes,start+4,false);ctype=bytes[start+9]&255;
                w=(int)width;h=(int)height;depth=bytes[start+8]&255;compression=bytes[start+10]&255;filterMethod=bytes[start+11]&255;interlace=bytes[start+12]&255;header=true;
            }else if(text(bytes,p+4,"IDAT")){if(!header||idat.size()+n>LIMIT)throw new IOException("PNG non valido");idat.write(bytes,start,n);}
            else if(text(bytes,p+4,"IEND")){if(n!=0)throw new IOException("PNG non valido");if(ts(bytes,next))return checked(Arrays.copyOfRange(bytes,next,bytes.length));end=true;break;}
            p=next;
        }
        if(!header||!end||idat.size()==0)throw new IOException("PNG incompleto");
        if(w<=0||h<=0||w>8192||h>8192||(long)w*h*4>LIMIT||depth!=8||(ctype!=2&&ctype!=6)||compression!=0||filterMethod!=0||interlace!=0)throw new IOException("Formato PNG non supportato");
        int bpp=ctype==6?4:3,stride=w*bpp;byte[] rgb=new byte[w*h*3],prev=new byte[stride];int dst=0;
        try(InputStream in=new InflaterInputStream(new ByteArrayInputStream(idat.toByteArray()))){
            for(int y=0;y<h;y++){
                int filter=in.read();if(filter<0||filter>4)throw new IOException("Filtro PNG non valido");byte[] row=new byte[stride];int pos=0;while(pos<stride){int n=in.read(row,pos,stride-pos);if(n<0)throw new IOException("Pixel PNG troncati");pos+=n;}
                for(int i=0;i<stride;i++){int a=i>=bpp?row[i-bpp]&255:0,b=prev[i]&255,c=i>=bpp?prev[i-bpp]&255:0,v=row[i]&255;
                    if(filter==1)v+=a;else if(filter==2)v+=b;else if(filter==3)v+=(a+b)/2;else if(filter==4)v+=paeth(a,b,c);row[i]=(byte)v;
                }
                for(int i=0;i<stride;i+=bpp){rgb[dst++]=row[i];rgb[dst++]=row[i+1];rgb[dst++]=row[i+2];}prev=row;
            }
        }
        if(!text(rgb,0,"TIKTIKPX"))throw new IOException("Contenitore PNG del sito cambiato");long n=uint(rgb,8,false);if(n<=0||n>rgb.length-12)throw new IOException("Payload PNG non valido");return checked(inflate(Arrays.copyOfRange(rgb,12,12+(int)n),true));
    }
    private static int paeth(int a,int b,int c){int p=a+b-c,pa=Math.abs(p-a),pb=Math.abs(p-b),pc=Math.abs(p-c);return pa<=pb&&pa<=pc?a:pb<=pc?b:c;}
}
