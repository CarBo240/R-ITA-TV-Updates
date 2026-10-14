package it.carmine.streamplayer
import android.content.Context
import com.streamflixreborn.streamflix.StreamFlixApp
import com.streamflixreborn.streamflix.extractors.*
import it.carmine.streamplayer.lab211.LabPipeline
import kotlinx.coroutines.*
import java.net.URI

/** Runs the imported upstream extraction bodies and preserves their playback headers. */
object StreamflixBridge {
 @JvmStatic fun initialize(context: Context) = StreamFlixApp.initialize(context)
 @JvmStatic fun playbackUserAgent():String = com.streamflixreborn.streamflix.utils.NetworkClient.USER_AGENT
 @JvmStatic fun playbackClient(): okhttp3.OkHttpClient = com.streamflixreborn.streamflix.utils.NetworkClient.default.newBuilder().callTimeout(0,java.util.concurrent.TimeUnit.SECONDS).build()
 private fun extractor(url: String): Extractor? {
  val uri = LabPipeline.https(url)
  val host = uri.host.lowercase().removePrefix("www.")
  if (CbLinkTargets.mixdrop(url)) return MixDropExtractor()
  if (host.matches(Regex("vixcloud\\.[a-z]{2,24}"))) return VixcloudExtractor()
  val list = listOf(DroploadExtractor(), SupervideoExtractor(), VoeExtractor(), StreamtapeExtractor(), VidozaExtractor(), DoodLaExtractor(), DoodLaExtractor.DoodLiExtractor(), DoodLaExtractor.DoodExtractor(), StreamWishExtractor(), StreamWishExtractor.UqloadsXyz(), StreamWishExtractor.SwishExtractor(), StreamWishExtractor.HlswishExtractor(), StreamWishExtractor.PlayerwishExtractor(), StreamWishExtractor.SwiftPlayersExtractor())
  return list.firstOrNull { resolver ->
   (listOf(resolver.mainUrl) + resolver.aliasUrls).any { base ->
    val expected = URI(base).host.lowercase().removePrefix("www.")
    host == expected || host.endsWith(".$expected")
   } || resolver.rotatingDomain.any { it.matches(host) }
  }
 }
 @JvmStatic fun supports(url: String): Boolean = try { extractor(url) != null } catch (_: Exception) { false }
 @JvmStatic @Throws(Exception::class) fun resolve(url: String): LabPipeline.Media {
  val selected = extractor(url) ?: throw Exception("Nessun resolver Streamflix importato per ${URI(url).host}")
  if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) throw IllegalStateException("Estrazione video richiesta sul thread UI")
  val video = runBlocking(Dispatchers.IO) { withTimeout(45_000) { selected.extract(url) } }
  LabPipeline.https(video.source)
  val headers = linkedMapOf<String,String>()
  video.headers.orEmpty().forEach { (key,value) ->
   if (key.equals("Referer",true) || key.equals("Origin",true)) {
    // Upstream Supervideo currently returns the literal "mainUrl" as Referer.
    if (value.startsWith("https://")) headers[key] = value
   } else if (!value.contains('\r') && !value.contains('\n')) headers[key] = value
  }
  val referer = headers.entries.firstOrNull { it.key.equals("Referer",true) }?.value ?: ""
  val tracks = video.subtitles.mapNotNull { subtitle ->
   val file = try { LabPipeline.https(subtitle.file).toString() } catch (_: Exception) { return@mapNotNull null }
   LabPipeline.Subtitle(file,"",subtitle.label,if (URI(file).path.endsWith(".srt")) "application/x-subrip" else "text/vtt")
  }
  val mime = video.type?.takeIf { it.contains('/') } ?: LabPipeline.mime(video.source)
  // A result with empty headers must stay empty: do not add generic Referer or Origin.
  return LabPipeline.Media(video.source,mime,referer,if(selected is VixcloudExtractor) "VixCloud" else selected.name,tracks,"",headers,true,video.extraBuffering)
 }
}
