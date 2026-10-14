package com.streamflixreborn.streamflix.extractors
import com.streamflixreborn.streamflix.models.Video
/** Upstream extractor contract. R. ITA supplies an exact-host registry for the imported subset. */
abstract class Extractor {
 abstract val name: String
 abstract val mainUrl: String
 open val aliasUrls: List<String> = emptyList()
 open val rotatingDomain: List<Regex> = emptyList()
 abstract suspend fun extract(link: String): Video
 open suspend fun extract(link: String, server: Video.Server? = null): Video = extract(link)
}
