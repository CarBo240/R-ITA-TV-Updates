package it.carmine.streamplayer;

import android.util.Base64;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import okhttp3.*;

/** LAB-only loopback HLS relay. It preserves the Vix page context for every playlist/key/segment. */
final class LabHlsProxy implements Closeable {
 private static final Pattern URI_ATTR=Pattern.compile("URI=\"([^\"]+)\"");
 private final ServerSocket server;
 private final ExecutorService pool=Executors.newCachedThreadPool(r->{Thread t=new Thread(r,"LabHlsProxy");t.setDaemon(true);return t;});
 private final OkHttpClient http;
 private final String referer,origin;
 private volatile boolean closed;

 LabHlsProxy(String referer)throws Exception{
  URI page=new URI(referer);if(!"https".equalsIgnoreCase(page.getScheme())||page.getHost()==null)throw new IOException("Referer Vix non valido");
  this.referer=referer;this.origin="https://"+page.getHost();
  http=VodNetwork.client().newBuilder().followRedirects(true).followSslRedirects(false).callTimeout(0,TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS).build();
  server=new ServerSocket();server.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),0),8);
  pool.execute(this::accept);
 }
 String url(String upstream)throws Exception{return "http://127.0.0.1:"+server.getLocalPort()+"/hls?u="+enc(checked(upstream).toString());}
 private void accept(){while(!closed)try{Socket socket=server.accept();pool.execute(()->serve(socket));}catch(IOException e){if(!closed)closed=true;}}
 private void serve(Socket socket){try(Socket s=socket){s.setSoTimeout(15000);BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream(),StandardCharsets.US_ASCII));String first=in.readLine();if(first==null)return;String range="";for(String line;(line=in.readLine())!=null&&!line.isEmpty();)if(line.regionMatches(true,0,"Range:",0,6))range=line.substring(6).trim();
   String[] words=first.split(" ");if(words.length<2||!"GET".equals(words[0])){error(s,405,"Metodo non supportato");return;}URI local=new URI(words[1]);String query=local.getRawQuery(),raw=query!=null&&query.startsWith("u=")?query.substring(2):"";if(raw.isEmpty()){error(s,400,"URL assente");return;}URI upstream=checked(dec(raw));
   Request.Builder request=new Request.Builder().url(upstream.toString()).header("User-Agent",VodSource.USER_AGENT).header("Accept","*/*").header("Accept-Language","it-IT,it;q=0.9,en;q=0.7").header("Referer",referer).header("Origin",origin).header("Connection","close");if(!range.isEmpty())request.header("Range",range);
   try(Response response=http.newCall(request.build()).execute()){ResponseBody body=response.body();if(body==null){error(s,502,"Risposta vuota");return;}String type=response.header("Content-Type","application/octet-stream");boolean playlist=type.toLowerCase(Locale.ROOT).contains("mpegurl")||upstream.getPath().toLowerCase(Locale.ROOT).contains(".m3u8");
    if(playlist){byte[] bytes=body.bytes();String text=new String(bytes,StandardCharsets.UTF_8);if(response.isSuccessful()&&text.trim().startsWith("#EXTM3U"))bytes=rewrite(text,upstream).getBytes(StandardCharsets.UTF_8);headers(s,response.code(),"application/vnd.apple.mpegurl",bytes.length,response.header("Content-Range"));s.getOutputStream().write(bytes);}
    else {long length=body.contentLength();headers(s,response.code(),type,length,response.header("Content-Range"));try(InputStream data=body.byteStream()){byte[] b=new byte[32768];for(int n;(n=data.read(b))!=-1;)s.getOutputStream().write(b,0,n);}}
   }
  }catch(Exception ignored){try{error(socket,502,"Relay HLS non disponibile");}catch(Exception ignoredAgain){}}
 }
 private String rewrite(String playlist,URI base)throws Exception{StringBuilder out=new StringBuilder();for(String line:playlist.split("\\r?\\n",-1)){Matcher m=URI_ATTR.matcher(line);StringBuffer replaced=new StringBuffer();while(m.find())m.appendReplacement(replaced,Matcher.quoteReplacement("URI=\""+url(base.resolve(m.group(1)).toString())+"\""));m.appendTail(replaced);String value=replaced.toString();String trim=value.trim();if(!trim.isEmpty()&&!trim.startsWith("#"))value=url(base.resolve(trim).toString());out.append(value).append('\n');}return out.toString();}
 private static URI checked(String value)throws Exception{URI uri=new URI(value);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getPort()!=-1)throw new IOException("Destinazione HLS non valida");String h=uri.getHost().toLowerCase(Locale.ROOT);if(h.equals("localhost")||h.endsWith(".local")||h.matches("(?:127|10|0)\\..*")||h.matches("192\\.168\\..*")||h.matches("172\\.(?:1[6-9]|2[0-9]|3[01])\\..*"))throw new IOException("Destinazione locale rifiutata");return uri;}
 private static String enc(String s){return Base64.encodeToString(s.getBytes(StandardCharsets.UTF_8),Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);}
 private static String dec(String s){return new String(Base64.decode(s,Base64.URL_SAFE|Base64.NO_WRAP),StandardCharsets.UTF_8);}
 private static void headers(Socket s,int code,String type,long length,String range)throws IOException{OutputStream out=s.getOutputStream();String reason=code==200?"OK":code==206?"Partial Content":"Upstream";StringBuilder h=new StringBuilder("HTTP/1.1 ").append(code).append(' ').append(reason).append("\r\nContent-Type: ").append(type).append("\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\n");if(length>=0)h.append("Content-Length: ").append(length).append("\r\n");if(range!=null)h.append("Content-Range: ").append(range).append("\r\n");h.append("\r\n");out.write(h.toString().getBytes(StandardCharsets.US_ASCII));}
 private static void error(Socket s,int code,String message)throws IOException{byte[] b=message.getBytes(StandardCharsets.UTF_8);headers(s,code,"text/plain; charset=utf-8",b.length,null);s.getOutputStream().write(b);}
 @Override public void close(){closed=true;try{server.close();}catch(IOException ignored){}pool.shutdownNow();}
}
