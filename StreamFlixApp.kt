package com.streamflixreborn.streamflix
import android.content.Context
/** Compatibility context for the upstream StreamWish WebView; not a second Android application. */
class StreamFlixApp private constructor(val applicationContext: Context) {
 companion object {
  lateinit var instance: StreamFlixApp
   private set
  fun initialize(context: Context) { if (!::instance.isInitialized) instance = StreamFlixApp(context.applicationContext) }
 }
}
