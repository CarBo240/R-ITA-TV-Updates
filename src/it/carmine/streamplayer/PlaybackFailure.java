package it.carmine.streamplayer;
import androidx.media3.common.PlaybackException;
import androidx.media3.datasource.HttpDataSource;
/** Reports upstream status and request stage without exposing signed URLs. */
public final class PlaybackFailure {
 public static String describe(Throwable error){for(Throwable cause=error;cause!=null;cause=cause.getCause())if(cause instanceof HttpDataSource.InvalidResponseCodeException){HttpDataSource.InvalidResponseCodeException http=(HttpDataSource.InvalidResponseCodeException)cause;android.net.Uri uri=http.dataSpec.uri;String path=uri.getPath()==null?"":uri.getPath();String stage=path.contains(".m3u8")||path.contains("/playlist/")?"playlist":path.endsWith(".mp4")?"video MP4":"segmento / richiesta video";return "HTTP "+http.responseCode+" · "+stage+" · "+uri.getHost();}return error instanceof PlaybackException?((PlaybackException)error).getErrorCodeName():VodNetwork.error(error);}
 private PlaybackFailure(){}
}
