package it.carmine.streamplayer;

final class EpisodeOrder {
 private EpisodeOrder(){}
 static int currentIndex(String[] ids,int[] numbers,String currentId,int currentNumber,int fallback){
  if(currentId!=null&&!currentId.isEmpty())for(int i=0;i<ids.length;i++)if(currentId.equals(ids[i]))return i;
  if(currentNumber>0)for(int i=0;i<numbers.length;i++)if(numbers[i]==currentNumber)return i;
  return fallback>=0&&fallback<numbers.length?fallback:-1;
 }
 static int nextIndex(String[] ids,int[] numbers,String currentId,int currentNumber,int fallback){
  int current=currentIndex(ids,numbers,currentId,currentNumber,fallback);
  int base=current>=0&&numbers[current]>0?numbers[current]:currentNumber;
  if(base>0){int best=-1,bestNumber=Integer.MAX_VALUE;for(int i=0;i<numbers.length;i++){int candidate=numbers[i];if(candidate>base&&candidate<bestNumber){best=i;bestNumber=candidate;}}if(best>=0)return best;}
  return current>=0&&current+1<numbers.length?current+1:-1;
 }
}
