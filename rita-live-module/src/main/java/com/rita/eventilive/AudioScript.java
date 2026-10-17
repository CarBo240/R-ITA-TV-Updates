package com.rita.eventilive;
/** Enable automatic audio only for the assisted WebView experiment. */
public final class AudioScript {
 public static String forRun(String monitor,MainActivity.Run run){
  return "window.__ritaMuteAfterHandoff="+(!run.daddy)+";\nwindow.__ritaResolverOnly="+InternalPlayer.MODE.equals(run.engine)+";\nwindow.__ritaAutoAudio="+("WebView assistita".equals(run.engine)?"true":"false")+";\n"+monitor;
 }
}
