package it.carmine.streamplayer.lab211;
import org.json.*;
import java.util.*;
/** Explicit owned/authorized catalogue schema. No private provider API assumptions. */
public final class JsonCatalog implements LabPipeline.CatalogAdapter {
 public String name(){return "JSON LAB · schema 1";}
 public String search(String base,String query){return base;}
 public List<LabPipeline.Title> titles(LabPipeline.Page page)throws Exception {
  JSONObject root=new JSONObject(page.text());if(root.optInt("labSchema")!=1)throw new LabPipeline.Failure(LabPipeline.Stage.CATALOG,"JSON_SCHEMA","Richiesto labSchema: 1");
  JSONArray titles=root.getJSONArray("titles");List<LabPipeline.Title> out=new ArrayList<>();
  for(int i=0;i<titles.length()&&out.size()<100;i++){JSONObject t=titles.getJSONObject(i);String name=t.getString("name"),poster=LabPipeline.absolute(page.url,t.getString("poster")),detail=LabPipeline.absolute(page.url,t.getString("detail"));if(!poster.isEmpty()&&!detail.isEmpty())out.add(new LabPipeline.Title(name,poster,detail));}return out;
 }
 public List<LabPipeline.Source> sources(LabPipeline.Page page)throws Exception {
  JSONObject root=new JSONObject(page.text());JSONArray sources=root.getJSONArray("sources");List<LabPipeline.Source> out=new ArrayList<>();
  for(int i=0;i<sources.length()&&out.size()<100;i++){JSONObject s=sources.getJSONObject(i);String u=LabPipeline.absolute(page.url,s.getString("url"));if(!u.isEmpty())out.add(new LabPipeline.Source(u,s.optString("label",LabPipeline.host(u)),page.url));}return out;
 }
}
