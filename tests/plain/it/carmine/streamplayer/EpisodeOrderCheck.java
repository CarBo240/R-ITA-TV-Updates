package it.carmine.streamplayer;
public final class EpisodeOrderCheck {
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  check(EpisodeOrder.nextIndex(new String[]{"4","6","5"},new int[]{4,6,5},"4",4,0)==2,"4 deve avanzare a 5 anche con coda fuori ordine");
  check(EpisodeOrder.nextIndex(new String[]{"a","b","c"},new int[]{4,4,5},"a",4,0)==2,"un duplicato di 4 non deve essere riprodotto");
  check(EpisodeOrder.nextIndex(new String[]{"a","b"},new int[]{0,0},"a",0,0)==1,"fallback per cataloghi senza numerazione");
  check(EpisodeOrder.nextIndex(new String[]{"4","5"},new int[]{4,5},"5",5,1)==-1,"fine coda");
  System.out.println("PASS: ordine episodi numerico e fallback");
 }
}
