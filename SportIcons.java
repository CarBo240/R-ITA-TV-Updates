package com.rita.eventilive;
import java.util.Locale;
/** Compact sport badge used by the TV event catalogue. */
final class SportIcons {
 static String forTitle(String value){String s=value==null?"":value.toLowerCase(Locale.ROOT);if(s.matches(".*(?:football|soccer|calcio|serie a|champions|premier league).*"))return "⚽";if(s.matches(".*(?:basket|nba|euroleague).*"))return "🏀";if(s.matches(".*(?:tennis|atp|wta|wimbledon).*"))return "🎾";if(s.matches(".*(?:formula 1|formula one|f1|motogp|motorsport).*"))return s.contains("moto")?"🏍":"🏎";if(s.matches(".*(?:volley|pallavolo).*"))return "🏐";if(s.matches(".*(?:rugby|six nations).*"))return "🏉";if(s.matches(".*(?:hockey|nhl).*"))return "🏒";if(s.matches(".*(?:boxe|boxing|ufc|mma).*"))return "🥊";if(s.matches(".*(?:golf|pga).*"))return "🏌";if(s.matches(".*(?:cycling|ciclismo|giro d|tour de).*"))return "🚴";if(s.matches(".*(?:darts|freccette).*"))return "🎯";return "🏆";}
 private SportIcons(){}
}
