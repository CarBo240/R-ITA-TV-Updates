package it.carmine.streamplayer;
import androidx.media3.exoplayer.DefaultLoadControl;
/** Preserve the imported resolver's extraBuffering preference, as in the upstream player. */
final class StreamflixBuffering {
 static DefaultLoadControl create(boolean extra){return new DefaultLoadControl.Builder().setBufferDurationsMs(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,extra?300000:DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS).build();}
 private StreamflixBuffering(){}
}
