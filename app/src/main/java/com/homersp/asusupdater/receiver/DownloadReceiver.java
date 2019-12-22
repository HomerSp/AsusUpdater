package com.homersp.asusupdater.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


import com.homersp.asusupdater.Log;
import com.homersp.asusupdater.service.UpdaterService;

public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = "AsusUpdater." + DownloadReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = context.getSharedPreferences("update", Context.MODE_PRIVATE).getLong("download_id", 0);
        if (id == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)) {
            Log.i(TAG, "Download complete");

            Intent i = new Intent(context, UpdaterService.class);
            i.setAction(UpdaterService.ACTION_INSTALL);

            context.startService(i);
        }
    }
}
