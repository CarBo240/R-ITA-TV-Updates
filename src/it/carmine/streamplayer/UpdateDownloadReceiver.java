package it.carmine.streamplayer;
import android.app.DownloadManager;import android.content.*;
public class UpdateDownloadReceiver extends BroadcastReceiver {@Override public void onReceive(Context context,Intent intent){if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction()))AppUpdater.installDownloaded(context,intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1));}}
