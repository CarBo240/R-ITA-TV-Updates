package com.streamflixreborn.streamflix.utils
import android.webkit.CookieManager
import it.carmine.streamplayer.VodNetwork
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
/** Upstream CookieManager session contract, attached to R. ITA's selected DNS and TLS client. */
object NetworkClient {
 const val USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
 val cookieJar = object : CookieJar {
  override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
   val manager = CookieManager.getInstance()
   cookies.forEach { manager.setCookie(url.toString(),it.toString()) }
   manager.flush()
  }
  override fun loadForRequest(url: HttpUrl): List<Cookie> {
   val raw = CookieManager.getInstance().getCookie(url.toString()) ?: return emptyList()
   return raw.split(';').mapNotNull { Cookie.parse(url,it.trim()) }
  }
 }
 val default get() = VodNetwork.client().newBuilder().cookieJar(cookieJar).addInterceptor { chain ->
  val original=chain.request();val request=original.newBuilder()
  if(original.header("User-Agent")==null)request.header("User-Agent",USER_AGENT)
  if(original.header("Accept")==null)request.header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
  if(original.header("Accept-Language")==null)request.header("Accept-Language","it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7")
  val cors=original.header("Sec-Fetch-Mode")=="cors"||original.header("Sec-Fetch-Dest")=="empty"
  if(!cors){if(original.header("Sec-Fetch-Dest")==null)request.header("Sec-Fetch-Dest","document");if(original.header("Sec-Fetch-Mode")==null)request.header("Sec-Fetch-Mode","navigate");if(original.header("Sec-Fetch-Site")==null)request.header("Sec-Fetch-Site","none");if(original.header("Upgrade-Insecure-Requests")==null)request.header("Upgrade-Insecure-Requests","1")}
  chain.proceed(request.build())
 }.build()
}
object DnsResolver { val doh get() = VodNetwork.client().dns }
object UserPreferences { val serverAutoSubtitlesDisabled = true }
