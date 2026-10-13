package com.rita.eventilive;
import java.io.*;import java.net.URI;import java.util.*;import java.util.regex.*;

/** Follow the selected public player only; never substitute a different source silently. */
final class DaddyResolver {
 static final class Page {final String url,body;Page(String u,String b){url=u;body=b;}}
 interface Transport {Page get(String url,String referer)throws IOException;}
 static final class Result {final String playlist,referer;final boolean packed;Result(String p,String r,boolean b){playlist=p;referer=r;packed=b;}}
 static String https(String text)throws IOException {try{URI u=new URI(text);if(u.getHost()==null||u.getUserInfo()!=null)throw new IOException("URL non valido");String scheme=u.getScheme();if("http".equalsIgnoreCase(scheme))text="https"+text.substring(text.indexOf(':'));else if(!"https".equalsIgnoreCase(scheme))throw new IOException("Schema non supportato");return text;}catch(IllegalArgumentException|java.net.URISyntaxException e){throw new IOException("URL non valido",e);}}
 static Result resolve(String selected,String parent,Transport http)throws IOException {
  ArrayDeque<String[]> pending=new ArrayDeque<>();pending.add(new String[]{https(selected),parent,"0"});Set<String> seen=new HashSet<>();IOException last=null;
  while(!pending.isEmpty()&&seen.size()<6){String[] item=pending.remove();String url=item[0];int depth=Integer.parseInt(item[2]);if(!seen.add(url))continue;
   try{Page page=http.get(url,item[1]);Matcher src=Pattern.compile("(?:const|let|var)\\s+SRC\\s*=\\s*([\"'])([^\"'\\r\\n]+)\\1").matcher(page.body);
    if(src.find()){String media=https(URI.create(page.url).resolve(src.group(2).replace("\\/","/").replace("&amp;","&")).toString());if(StreamCandidate.mediaMime(media,"GET")!=null){boolean packed=Pattern.compile("\\b(?:TSGZ|TPIX|TRAW)\\s*=").matcher(page.body).find();return new Result(media,page.url,packed);}}
    if(depth>=3)continue;Matcher frame=Pattern.compile("<iframe\\b[^>]*\\bsrc\\s*=\\s*[\"']([^\"']+)[\"']",Pattern.CASE_INSENSITIVE).matcher(page.body);
    while(frame.find()&&pending.size()<6){try{String next=https(URI.create(page.url).resolve(frame.group(1).replace("&amp;","&")).toString());String host=URI.create(next).getHost();if(host.contains("chatango")||host.contains("histats"))continue;pending.add(new String[]{next,page.url,String.valueOf(depth+1)});}catch(Exception ignored){}}
   }catch(IOException e){last=e;}
  }throw new IOException("Flusso diretto non riconosciuto per il player selezionato",last);
 }
}
