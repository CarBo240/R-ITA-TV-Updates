package it.carmine.streamplayer
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class) @Config(sdk=[31])
class StreamflixImportedTest {
 private val fixtures=mutableMapOf<String,String>()
 private val field=VodNetwork::class.java.getDeclaredField("client").apply { isAccessible=true }
 private var previous:OkHttpClient?=null
 @Before fun setup(){
  previous=field.get(null) as OkHttpClient?
  field.set(null,OkHttpClient.Builder().addInterceptor { chain ->
   val req=chain.request();val html=fixtures[req.url.toString()] ?: error("No synthetic response for ${req.url}")
   Response.Builder().request(req).protocol(Protocol.HTTP_1_1).code(200).message("OK").body(html.toResponseBody("text/html".toMediaType())).build()
  }.build())
  StreamflixBridge.initialize(RuntimeEnvironment.getApplication())
 }
 @After fun cleanup(){field.set(null,previous)}
 private fun resolve(url:String):it.carmine.streamplayer.lab211.LabPipeline.Media {
  val worker=Executors.newSingleThreadExecutor()
  return try { worker.submit<it.carmine.streamplayer.lab211.LabPipeline.Media>{StreamflixBridge.resolve(url)}.get(30,TimeUnit.SECONDS) }finally{worker.shutdownNow()}
 }
 @Test fun actualMixDropExtractorReachesNativeMediaWithItsHeadersAndBufferFlag(){
  fixtures["https://mxdrop.click/e/abc123"]="""<html><body><script>eval(function(p,a,c,k,e,d){}('0.1="//2/3.4?5=6";',7,7,'MDCore|wurl|cdn.example|movie|mp4|token|fixture'.split('|')))</script></body></html>"""
  val media=resolve("https://mxdrop.click/f/abc123?ref=old")
  assertEquals("https://cdn.example/movie.mp4?token=fixture",media.url)
  assertFalse(media.headers.containsKey("Referer"));assertFalse(media.headers.containsKey("Origin"));assertTrue(media.extraBuffering)
  assertTrue(media.headers["User-Agent"]!!.contains("Chrome/124"))
 }
 @Test fun actualVixCloudExtractorPreservesFreshParametersAndOriginReferer(){
  val embed="https://vixcloud.co/embed/123?b=1&canPlayFHD=true"
  fixtures[embed]="""<html><body><script>window.video = {id: 123, filename: "fixture"};window.masterPlaylist = {params: {token: "fresh", expires: "1999999999"}, url: "https://vixcloud.co/playlist/123?b=1"};</script></body></html>"""
  val media=resolve(embed)
  assertTrue(media.url.contains("/playlist/123?"));assertTrue(media.url.contains("token=fresh"));assertTrue(media.url.contains("expires=1999999999"));assertTrue(media.url.contains("b=1"));assertTrue(media.url.contains("h=1"))
  assertEquals("https://vixcloud.co/",media.headers["Referer"]);assertTrue(media.headers["User-Agent"]!!.contains("Chrome/150"))
 }
}
