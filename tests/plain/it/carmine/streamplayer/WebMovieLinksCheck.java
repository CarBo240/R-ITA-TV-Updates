package it.carmine.streamplayer;
import java.util.*;
public final class WebMovieLinksCheck {
 static void check(boolean value){if(!value)throw new AssertionError();}
 public static void main(String[] args){
  check(WebMovieLinks.supported("https://cb01uno.wiki"));
  check(WebMovieLinks.supported("https://www.altadefinizionex.surf"));
  check(!WebMovieLinks.supported("https://altadefinizionex.surf.attacker.example"));
  check(!WebMovieLinks.supported("https://streamingcommunity.example"));
  String observed="<iframe src='https://v.vidxgo.co/0111438?content=trailer&amp;player=false' title='Trailer'></iframe><iframe src='https://v.vidxgo.co/0111438' title='Player'></iframe>";
  check(WebMovieLinks.players(observed,"https://altadefinizionex.surf/movie").equals(Arrays.asList("https://v.vidxgo.co/0111438")));
  String cb="<a href='https://host.example/embed/42'>Guarda in streaming</a><a href='https://ads.example/'>Pubblicità</a><iframe data-src='/embed/lazy'></iframe><iframe src='https://popads.net/ad'></iframe>";
  check(WebMovieLinks.players(cb,"https://cb01uno.wiki/title").equals(Arrays.asList("https://host.example/embed/42","https://cb01uno.wiki/embed/lazy")));
  check(WebMovieLinks.players("<iframe src='javascript:alert(1)'></iframe><iframe src='https://user:pass@host.example/v'></iframe>","https://cb01uno.wiki/title").isEmpty());
  check(WebMovieLinks.media("https://cdn.example/master.m3u8?token=test"));
  check(WebMovieLinks.media("https://cdn.example/video.MP4?expires=1"));
  check(!WebMovieLinks.media("https://cdn.example/trailer.mp4"));
  check(!WebMovieLinks.media("https://cdn.example/index.html?file=x.m3u8"));
  check(!WebMovieLinks.media("blob:https://cdn.example/42"));
  check(!WebMovieLinks.media(null));
  System.out.println("WebMovieLinks: 14 assertions passed");
 }
}
