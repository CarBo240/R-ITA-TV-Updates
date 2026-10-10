package it.carmine.streamplayer;
public class CbRaiCatalogueCheck {
 private static void check(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
 check(CbTitleMatch.same("Reacher","","Reacher [TV - 2025]"));
 check(CbTitleMatch.same("The Last of Us","","The Last of Us - Stagione 2 [HD]"));
 check(CbTitleMatch.same("Il problema dei 3 corpi","3 Body Problem","3 Body Problem [TV - 2024]"));
 check(!CbTitleMatch.same("Reacher","","Jack Reacher [HD] (2012)"));
 check(CbTitleMatch.same("Film Uno","","Film Uno [HD] (2025)"));
 check(!CbTitleMatch.yearMatches("1984",false,"Dune [HD] (2021)"));
 check(CbTitleMatch.yearMatches("2022",true,"Reacher [TV - 2025]"));
 check(RaiCatalogueScope.onDemand("https://www.raiplay.it/programmi/unprogramma"));
 check(RaiCatalogueScope.onDemand("https://www.raiplay.it/video/2026/unvideo.html"));
 check(!RaiCatalogueScope.onDemand("https://www.raiplay.it/dirette/rai1"));
 check(RaiCatalogueScope.live("https://www.raiplay.it/dirette/rai1"));
 check(!RaiCatalogueScope.live("https://www.raiplay.it/programmi/unprogramma"));
 check(!RaiCatalogueScope.live("https://raiplay.it.evil.test/dirette/rai1"));
 check(!RaiCatalogueScope.onDemand("https://raiplay.it.evil.test/video/x"));
 System.out.println("14 CB01 title/year and Rai on-demand/live checks passed");
 }
}
