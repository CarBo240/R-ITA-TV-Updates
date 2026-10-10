package it.carmine.streamplayer;
import java.util.*;
public class CbSourceCheck {
 public static void main(String[] args){
 String page="https://cb01uno.wiki/serie/";
 Map<String,String> entries=CbEpisodeLinks.parse("<a href='/episodio-1/'>S1E1 Episodio 1</a><a href='https://ads.example.org/video'>Episodio pubblicità</a><a href='/category/serie/'>Episodi archivio</a>",page);
 if(entries.size()!=1||!entries.containsKey("https://cb01uno.wiki/episodio-1/"))throw new AssertionError(entries);
 java.util.List<CbStreamChoices.Choice> sources=CbStreamChoices.parse("<a title='HD' href='https://mixdrop.co/e/abc'>Mixdrop streaming</a>",page);
 if(sources.size()!=1||!sources.get(0).label.contains("HD"))throw new AssertionError("HD missing");
 System.out.println("2 CB01 source checks passed");
 }
}