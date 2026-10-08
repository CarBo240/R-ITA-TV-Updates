package it.carmine.streamplayer;
import java.util.Locale;
final class DisplayNames {
 static String channel(String raw){
  String value=(raw==null?"":raw).trim().replaceAll("(?i)\\.[scb]$","").trim().toLowerCase(Locale.ITALIAN);
  StringBuilder result=new StringBuilder();boolean initial=true;
  for(int offset=0;offset<value.length();){int cp=value.codePointAt(offset);offset+=Character.charCount(cp);result.appendCodePoint(initial?Character.toUpperCase(cp):cp);initial=Character.isWhitespace(cp)||cp=='-'||cp=='/'||cp=='('||cp=='\''||cp==0x2019;}
  return result.toString();
 }
}
