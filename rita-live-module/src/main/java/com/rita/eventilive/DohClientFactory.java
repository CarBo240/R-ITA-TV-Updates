package com.rita.eventilive;
import java.net.*;import java.util.concurrent.TimeUnit;import okhttp3.*;import okhttp3.dnsoverhttps.DnsOverHttps;
/** The DoH endpoint is bootstrapped by literal addresses, without a system DNS lookup. */
public final class DohClientFactory {
 public static final String ENDPOINT="https://cloudflare-dns.com/dns-query";
 private static OkHttpClient cloud;
 public static synchronized OkHttpClient cloud()throws UnknownHostException {
  if(cloud==null){OkHttpClient bootstrap=new OkHttpClient.Builder().connectTimeout(15,TimeUnit.SECONDS).readTimeout(15,TimeUnit.SECONDS).build();
   DnsOverHttps dns=new DnsOverHttps.Builder().client(bootstrap).url(HttpUrl.get(ENDPOINT)).bootstrapDnsHosts(InetAddress.getByAddress(new byte[]{1,1,1,1}),InetAddress.getByAddress(new byte[]{1,0,0,1})).includeIPv6(true).build();
   cloud=bootstrap.newBuilder().dns(dns).followSslRedirects(false).build();
  }return cloud;
 }
}
