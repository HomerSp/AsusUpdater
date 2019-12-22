package com.homersp.asusupdater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import com.homersp.asusupdater.Log;
import com.homersp.asusupdater.service.UpdaterService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "AsusUpdater." + BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + intent.getAction());

        Intent i = new Intent(context, UpdaterService.class);
        context.startService(i);
    }
}
