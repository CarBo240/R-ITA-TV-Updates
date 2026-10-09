package it.carmine.streamplayer;

import androidx.media3.common.C;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy;
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy;
import java.util.*;

/** At most two delayed retries on transient errors, none on denials/rate limits. */
final class StreamErrorPolicy extends DefaultLoadErrorHandlingPolicy {
    private final VavooClient client;
    StreamErrorPolicy(VavooClient client){super(2);this.client=client;}
    @Override public long getRetryDelayMsFor(LoadErrorHandlingPolicy.LoadErrorInfo info){
        if(info.exception instanceof HttpDataSource.InvalidResponseCodeException){
            HttpDataSource.InvalidResponseCodeException e=(HttpDataSource.InvalidResponseCodeException)info.exception;String retry=null;
            for(Map.Entry<String,List<String>> header:e.headerFields.entrySet())if("Retry-After".equalsIgnoreCase(header.getKey())&&!header.getValue().isEmpty())retry=header.getValue().get(0);
            if(e.responseCode==401||e.responseCode==403||e.responseCode==429||e.responseCode==503&&retry!=null){client.reportPlaybackFailure(e.responseCode,retry);return C.TIME_UNSET;}
        }
        if(info.errorCount>2||super.getRetryDelayMsFor(info)==C.TIME_UNSET)return C.TIME_UNSET;
        return info.errorCount*2000L;
    }
    @Override public FallbackSelection getFallbackSelectionFor(FallbackOptions options,LoadErrorInfo info){return null;}
}
