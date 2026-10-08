package it.carmine.streamplayer;

import android.content.Context;
import android.graphics.*;
import android.os.*;
import android.util.LruCache;
import android.widget.ImageView;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

/** Artwork cache only. No video data, recordings or downloaded media. */
final class ArtworkStore implements AutoCloseable {
    private final Context context;private final File directory;private final boolean network;
    private final ExecutorService worker=Executors.newFixedThreadPool(2);
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final LruCache<String,Bitmap> memory=new LruCache<String,Bitmap>(12*1024*1024){protected int sizeOf(String k,Bitmap b){return b.getAllocationByteCount();}};
    private final Map<ImageView,String> requests=new WeakHashMap<>();
    private final Set<String> pending=Collections.synchronizedSet(new HashSet<>());
    private final Map<String,Long> failed=new ConcurrentHashMap<>();
    private JSONObject logos=new JSONObject(),bundled=new JSONObject();private volatile boolean closed;
    ArtworkStore(Context c,boolean enabled){context=c;network=enabled;directory=new File(c.getCacheDir(),"artwork");directory.mkdirs();
        logos=readAsset("channel_logos.json");bundled=readAsset("bundled_logos.json");}
    private JSONObject readAsset(String name){try(InputStream in=context.getAssets().open(name)){return new JSONObject(new String(bytes(in,1024*1024),"UTF-8"));}catch(Exception e){return new JSONObject();}}
    String logo(VavooClient.Channel c,EpgStore guide){String name=EpgStore.normalize(c.name);String local=bundled.optString(name,"");if(!local.isEmpty())return "asset:"+local;
        String epg=guide==null?"":guide.logo(c);return epg.isEmpty()?logos.optString(name,""):epg;}
    void bind(ImageView view,String url){bind(view,url,context.getDrawable(R.drawable.icon));}
    void bind(ImageView view,String url,android.graphics.drawable.Drawable fallback){
        if(closed)return;url=url==null?"":url;
        if(url.equals(requests.get(view)))return;requests.put(view,url);
        view.setImageDrawable(fallback);
        if(url.isEmpty())return;Bitmap cached=memory.get(url);if(cached!=null){view.setImageBitmap(cached);return;}
        if(url.startsWith("asset:")){try(InputStream in=context.getAssets().open(url.substring(6))){Bitmap b=decode(bytes(in,3*1024*1024));if(b!=null){memory.put(url,b);view.setImageBitmap(b);}}catch(Exception ignored){}return;}
        if(!network||failed.getOrDefault(url,0L)>System.currentTimeMillis()||!pending.add(url))return;
        final String key=url;
        worker.execute(()->{Bitmap result=null;try{File file=new File(directory,hash(key));byte[] data;
                if(file.isFile()){try(InputStream in=new FileInputStream(file)){data=bytes(in,3*1024*1024);}file.setLastModified(System.currentTimeMillis());}
                else {URI u=new URI(key);if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null)throw new IOException();
                    HttpURLConnection connection=(HttpURLConnection)u.toURL().openConnection();connection.setConnectTimeout(8000);connection.setReadTimeout(10000);
                    try{if(connection.getResponseCode()!=200)throw new IOException();try(InputStream in=connection.getInputStream()){data=bytes(in,3*1024*1024);}}finally{connection.disconnect();}
                    Bitmap valid=decode(data);if(valid==null)throw new IOException("Immagine non valida");
                    File temporary=new File(directory,file.getName()+".tmp");try(OutputStream out=new FileOutputStream(temporary)){out.write(data);}temporary.renameTo(file);trim();result=valid;
                }
                if(result==null)result=decode(data);if(result==null){file.delete();throw new IOException();}memory.put(key,result);
            }catch(Exception e){failed.put(key,System.currentTimeMillis()+3600000);}
            pending.remove(key);final Bitmap image=result;
            ui.post(()->{if(closed||image==null)return;for(Map.Entry<ImageView,String> entry:new ArrayList<>(requests.entrySet()))if(key.equals(entry.getValue())&&entry.getKey()!=null)entry.getKey().setImageBitmap(image);});
        });
    }
    private static byte[] bytes(InputStream in,int max)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();out.write(b,0,n);if(out.size()>max)throw new IOException("Immagine troppo grande");}return out.toByteArray();}
    static Bitmap decode(byte[] data){BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,o);if(o.outWidth<=0||o.outHeight<=0)return null;o.inSampleSize=1;while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>960)o.inSampleSize*=2;o.inJustDecodeBounds=false;return BitmapFactory.decodeByteArray(data,0,data.length,o);}
    private String hash(String url)throws Exception{byte[] b=MessageDigest.getInstance("SHA-256").digest(url.getBytes("UTF-8"));StringBuilder s=new StringBuilder();for(byte v:b)s.append(String.format(Locale.ROOT,"%02x",v&255));return s.toString();}
    private void trim(){File[] files=directory.listFiles();if(files==null)return;Arrays.sort(files,Comparator.comparingLong(File::lastModified));long total=0;for(File f:files)total+=f.length();for(File f:files)if(total>32*1024*1024&&!f.getName().endsWith(".tmp")){long n=f.length();if(f.delete())total-=n;}}
    @Override public void close(){closed=true;requests.clear();ui.removeCallbacksAndMessages(null);worker.shutdownNow();memory.evictAll();}
}
