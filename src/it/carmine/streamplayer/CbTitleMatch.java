package it.carmine.streamplayer;
import java.text.Normalizer;import java.util.Locale;
/** Compare the actual title, without CB01 quality, season and TV badges. */
final class CbTitleMatch {
 static String clean(String value){return Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT)
  .replaceAll("\\s*[\\[(](?:hd|4k|ita|sub\\s*ita|tv|19\\d{2}|20\\d{2})[^\\])]*[\\])]","")
  .replaceAll("\\s*[-–|:]?\\s*(?:stagion[ei]|season)\\s*\\d+.*$","")
  .replaceAll("\\s+streaming(?:\\s+ita)?$","").replaceAll("[^a-z0-9]","");}
 static boolean same(String wanted,String original,String found){String candidate=clean(found);return !candidate.isEmpty()&&(candidate.equals(clean(wanted))||(!original.isEmpty()&&candidate.equals(clean(original))));}
 static boolean yearMatches(String year,boolean series,String found){if(series||year.isEmpty())return true;java.util.regex.Matcher m=java.util.regex.Pattern.compile("[\\[(]((?:19|20)\\d{2})[\\])]").matcher(found);return !m.find()||year.equals(m.group(1));}
}
