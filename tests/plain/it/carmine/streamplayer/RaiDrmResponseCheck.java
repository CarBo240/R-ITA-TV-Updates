package it.carmine.streamplayer;
import java.util.*;import java.nio.charset.StandardCharsets;
public class RaiDrmResponseCheck {
 public static void main(String[] args)throws Exception{byte[] binary={8,1,18,3,7,9,2};if(RaiDrmCallback.unwrap(binary)!=binary)throw new AssertionError("Binary license changed");String json="{\"license\":\""+Base64.getEncoder().encodeToString(binary)+"\",\"status\":\"OK\"}";if(!Arrays.equals(binary,RaiDrmCallback.unwrap(json.getBytes(StandardCharsets.UTF_8))))throw new AssertionError("Wrapped license mismatch");for(String invalid:new String[]{"", "{}", "{\"license\":\"not-base64!\"}"}){boolean rejected=false;try{RaiDrmCallback.unwrap(invalid.getBytes(StandardCharsets.UTF_8));}catch(Exception e){rejected=true;}if(!rejected)throw new AssertionError("Invalid response accepted");}System.out.println("5 license response fixture checks passed");}
}
