package it.carmine.streamplayer;
import java.util.*;import okhttp3.HttpUrl;
/** Follows RaiPlay's public player mapping for issuer-provided Nagra authorization. */
final class RaiLicenseConfig {
 static String normalize(String url,String operator,Map<String,String> headers){if(!"nagra".equalsIgnoreCase(operator)||url.isEmpty())return url;HttpUrl parsed=HttpUrl.get(url);String authorization=parsed.queryParameter("Authorization");if(authorization!=null&&!authorization.isEmpty()){headers.keySet().removeIf(k->k.equalsIgnoreCase("nv-authorizations"));headers.put("nv-authorizations",authorization);}return parsed.newBuilder().removeAllQueryParameters("Authorization").build().toString();}
}
