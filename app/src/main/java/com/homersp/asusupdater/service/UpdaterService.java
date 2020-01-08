package com.homersp.asusupdater.service;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.homersp.asusupdater.BuildCompat;
import com.homersp.asusupdater.Log;
import com.homersp.asusupdater.R;
import com.homersp.asusupdater.UpdaterApplication;
import com.homersp.asusupdater.activity.UpdaterActivity;
import com.homersp.asusupdater.syncml.AsusDM;
import com.homersp.asusupdater.updater.UpdaterFileUtils;
import com.homersp.asusupdater.updater.UpdaterPackage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class UpdaterService extends IntentService {
    private static final String TAG = "AsusUpdater." + UpdaterService.class.getSimpleName();

    public static final String ACTION_ACTIVITY_UPDATE = "com.homersp.asusupdater.ACTIVITY_UPDATE";
    public static final String ACTION_CHECK = "com.homersp.asusupdater.CHECK";
    public static final String ACTION_IGNORE = "com.homersp.asusupdater.IGNORE";
    public static final String ACTION_DOWNLOAD = "com.homersp.asusupdater.DOWNLOAD";
    public static final String ACTION_INSTALL = "com.homersp.asusupdater.INSTALL";

    private static final String CHANNEL_ID = "UPDATER_NOTIFICATION";

    private static final int NOTIFICATION_ID_FOUND = 1;
    private static final int NOTIFICATION_ID_REBOOT = 2;

    private DownloadManager mDownloadManager;
    private NotificationManagerCompat mNotificationManager;

    public UpdaterService() {
        super(TAG);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.i(TAG, "onCreate");

        String name = getString(R.string.notification_channel_updates);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(getString(R.string.notification_channel_updates_desc));

        mDownloadManager = getSystemService(DownloadManager.class);

        mNotificationManager = NotificationManagerCompat.from(this);
        mNotificationManager.createNotificationChannel(channel);

        UpdaterApplication.initAlarm(this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent " + (intent != null ? intent.getAction() : "none"));

        SharedPreferences prefs = getSharedPreferences("update", MODE_PRIVATE);
        if (prefs.contains("current_version")) {
            String current = prefs.getString("current_version", "");
            Log.d(TAG, "Current version " + BuildCompat.CSC_VERSION + " vs saved " + current);
            if (!current.equals(BuildCompat.CSC_VERSION)) {
                Log.d(TAG, "Purging update");
                UpdaterFileUtils.removeUpdateFile(this);

                notifyActivity();
            }
        }

        prefs.edit().putString("current_version", BuildCompat.CSC_VERSION).apply();

        if (UpdaterFileUtils.updateFileExists(this)) {
            Log.d(TAG, "Update file exists");
            updateNotification();
            return;
        }

        if (prefs.contains("download_id")) {
            if (mDownloadManager.getUriForDownloadedFile(prefs.getLong("download_id", 0)) != null) {
                installUpdate();
                return;
            }
        }

        if (intent == null) {
            return;
        }

        if (ACTION_INSTALL.equals(intent.getAction())) {
            installUpdate();
            return;
        }

        if (ACTION_DOWNLOAD.equals(intent.getAction())) {
            Log.d(TAG, "Downloading update");
            downloadUpdate();
            return;
        }

        if (ACTION_IGNORE.equals(intent.getAction())) {
            Log.d(TAG, "Setting ignore");
            prefs.edit().putBoolean("ignore", true).apply();
        }

        Log.d(TAG, "ignore: " + prefs.getBoolean("ignore", false));

        if (intent.getBooleanExtra("force", false) || !prefs.getBoolean("ignore", false)) {
            boolean startCheck = ACTION_CHECK.equals(intent.getAction());
            if (!intent.getBooleanExtra("force", false)) {
                if (prefs.contains("url")) {
                    startCheck = false;

                    updateNotification();
                }
            }

            if (startCheck) {
                doCheckUpdate();
            }
        } else {
            mNotificationManager.cancelAll();
        }
    }

    private void updateNotification()
    {
        SharedPreferences prefs = getSharedPreferences("update", MODE_PRIVATE);
        if (prefs.contains("download_id")) {
            mNotificationManager.cancelAll();
            return;
        }

        Intent intent = new Intent(this, UpdaterActivity.class);
        intent.setAction(UpdaterActivity.ACTION_SHOW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntentActivity = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(0xff003300)
                .setContentTitle(getString(R.string.system_update))
                .setContentIntent(pendingIntentActivity)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        String name = prefs.getString("name", "");
        if (!UpdaterFileUtils.updateFileExists(this)) {
            String size = String.format(Locale.getDefault(), "%.2f", prefs.getLong("size", 0) / 1024.0f / 1024.0f);

            builder.setContentText(getString(R.string.found_update, name))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.found_update_expand, name, size)));

            Intent ignoreIntent = new Intent(this, UpdaterService.class);
            ignoreIntent.setAction(ACTION_IGNORE);

            PendingIntent pendingIntentIgnore = PendingIntent.getService(this, 0, ignoreIntent, 0);
            builder.addAction(new NotificationCompat.Action(0, getString(R.string.ignore), pendingIntentIgnore));

            Intent downloadIntent = new Intent(this, UpdaterService.class);
            downloadIntent.setAction(ACTION_DOWNLOAD);

            PendingIntent pendingIntentDownload = PendingIntent.getService(this, 0, downloadIntent, 0);
            builder.addAction(new NotificationCompat.Action(0, getString(R.string.download), pendingIntentDownload));

            mNotificationManager.cancel(NOTIFICATION_ID_REBOOT);
            mNotificationManager.notify(NOTIFICATION_ID_FOUND, builder.build());
        } else {
            builder.setContentText(getString(R.string.reboot_update, name))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.reboot_update_expand, name)));

            mNotificationManager.cancel(NOTIFICATION_ID_FOUND);
            mNotificationManager.notify(NOTIFICATION_ID_REBOOT, builder.build());
        }
    }

    private void downloadUpdate()
    {
        SharedPreferences prefs = getSharedPreferences("update", MODE_PRIVATE);
        if (prefs.contains("download_id")) {
            Log.w(TAG, "Download already in progress, skipping...");
            return;
        }

        Uri pkgUri = Uri.parse(prefs.getString("url", ""));
        String filename =  pkgUri.getLastPathSegment();
        if (filename == null) {
            return;
        }

        if (UpdaterFileUtils.updateFileExists(this)) {
            installUpdate();
            return;
        }

        File file = new File(getExternalFilesDir(""), filename);
        if (file.exists()) {
            installUpdate();
            return;
        }

        long dlid = mDownloadManager.enqueue(new DownloadManager.Request(Uri.parse(prefs.getString("url", "")))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(filename)
                .setDescription("System update")
                .setDestinationInExternalFilesDir(this, "", filename));

        prefs.edit().putLong("download_id", dlid).apply();

        updateNotification();
        notifyActivity();
    }

    private void installUpdate()
    {
        SharedPreferences prefs = getSharedPreferences("update", MODE_PRIVATE);
        if (!prefs.contains("download_id")) {
            Log.w(TAG, "No download id");
            return;
        }

        if (UpdaterFileUtils.updateFileExists(this)) {
            Log.w(TAG, "Ignoring install update command");
            return;
        }

        if (UpdaterFileUtils.moveUpdaterFile(this)) {
            prefs.edit().remove("download_id").apply();
        }

        updateNotification();
        notifyActivity();
    }

    private void doCheckUpdate()
    {
        SharedPreferences prefs = getSharedPreferences("update", MODE_PRIVATE);

        String chan = (prefs.getBoolean("beta", false) ? "Beta" : "Stable");
        if (BuildCompat.haveCustomImei(this)) {
            chan = "Custom";
        }

        Log.i(TAG, "Checking for updates to " + BuildCompat.CSC_VERSION + " on " + chan + "...");

        prefs.edit().putBoolean("checking", true).apply();

        String imei = BuildCompat.getImei(this);

        UpdaterPackage pkg = null;
        AsusDM dm = new AsusDM(this, imei);
        for (int i = 1; i < 5; i++) {
            Log.d(TAG, "Checking updates, try " + i + "...");

            try {
                boolean ret = dm.sendCheckAuth();
                if (!ret) {
                    Log.d(TAG, "Check auth: NEED AUTH");

                    if (!dm.sendAuth()) {
                        Log.d(TAG, "Could not auth, this is a problem!");
                        dm.reset();
                        continue;
                    }
                }

                if (dm.sendSoftware()) {
                    String pkgUrl = dm.sendCNLocation();
                    if (pkgUrl != null) {
                        Log.d(TAG, "pkgUrl: " + pkgUrl);
                        pkg = dm.getUpdaterPackage(pkgUrl);
                    }
                } else {
                    Log.d(TAG, "No updates found");
                }

                break;
            } catch(Exception e) {
                // Ignore
            }
        }

        Log.i(TAG, "Update check done");

        prefs.edit()
                .putLong("last_check", System.currentTimeMillis())
                .putBoolean("checking", false)
                .putBoolean("ignore", false)
                .apply();

        if (pkg != null) {
            prefs.edit()
                    .putString("name", pkg.getName())
                    .putString("url", pkg.getUrl())
                    .putLong("size", pkg.getSize())
                    .apply();

            try {
                FileOutputStream fosDesc = openFileOutput("desc.txt", Context.MODE_PRIVATE);
                fosDesc.write(pkg.getDescription().getBytes());

                FileOutputStream fosNoti = openFileOutput("noti.txt", Context.MODE_PRIVATE);
                fosNoti.write(pkg.getNotification().getBytes());
            } catch (IOException e) {
                Log.e(TAG, "Failed writing data", e);
            }
        }

        notifyActivity();

        startService(new Intent(this, UpdaterService.class));
    }

    private void notifyActivity()
    {
        Intent intent = new Intent(ACTION_ACTIVITY_UPDATE);
        sendBroadcast(intent);
    }
}
