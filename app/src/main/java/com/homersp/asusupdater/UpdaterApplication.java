package com.homersp.asusupdater;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.text.format.DateFormat;

import androidx.documentfile.provider.DocumentFile;

import com.homersp.asusupdater.service.UpdaterService;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpdaterApplication extends Application {
    private static final String TAG = "AsusUpdater." + UpdaterApplication.class.getSimpleName();

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.i(TAG, "onCreate");
        initAlarm(this);
    }

    public static void initAlarm(Context context)
    {
        Intent intent = new Intent();
        intent.setClass(context.getApplicationContext(), UpdaterService.class);
        intent.setAction(UpdaterService.ACTION_CHECK);

        // No need to create the alarm if it already exists
        PendingIntent checkIntent = PendingIntent.getForegroundService(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager != null && checkIntent == null) {
            SharedPreferences prefs = context.getSharedPreferences("update", MODE_PRIVATE);

            Calendar cal = Calendar.getInstance(Locale.getDefault());
            cal.setTimeInMillis(prefs.getLong("last_check", System.currentTimeMillis()));
            cal.add(Calendar.HOUR, 1);

            Date date = new Date(cal.getTimeInMillis());

            Log.i(TAG, "Scheduling next check for " + DateFormat.getTimeFormat(context).format(date));

            PendingIntent alarmIntent = PendingIntent.getForegroundService(context.getApplicationContext(), 0, intent, 0);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    date.getTime(),
                    AlarmManager.INTERVAL_HOUR, alarmIntent);
        }
    }

    public static Uri getRootUri(Context context)
    {
        Uri checkUri = Uri.parse("content://com.android.externalstorage.documents/tree/" + Uri.encode("primary:"));
        for (UriPermission perm: context.getContentResolver ().getPersistedUriPermissions()) {
            if (perm.isWritePermission() && perm.getUri().equals(checkUri)) {
                return perm.getUri();
            }
        }

        return null;
    }

    public static DocumentFile getRootDocument(Context context)
    {
        Uri uri = getRootUri(context);
        return (uri != null) ? DocumentFile.fromTreeUri(context, uri) : null;
    }
}
