package com.homersp.asusupdater.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.DateFormat;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.homersp.asusupdater.BuildCompat;
import com.homersp.asusupdater.Log;
import com.homersp.asusupdater.R;
import com.homersp.asusupdater.UpdaterApplication;
import com.homersp.asusupdater.service.UpdaterService;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class UpdaterActivity extends Activity {
    private static final String TAG = "AsusUpdater." + UpdaterActivity.class.getSimpleName();

    public static final String ACTION_SHOW = "com.homersp.asusupdater.SHOW_UPDATER";

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            updateView();
        }
    };

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);

        setContentView(R.layout.activity_main);

        Button checkButton = findViewById(R.id.check_update);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences prefs = getSharedPreferences("update", MODE_PRIVATE);
                if (prefs.contains("url")) {
                    downloadUpdate();
                } else {
                    checkUpdate();
                }
            }
        });

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 0);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(UpdaterService.ACTION_ACTIVITY_UPDATE);

        registerReceiver(mUpdateReceiver, filter);

        startService(new Intent(this, UpdaterService.class));

        UpdaterApplication.initAlarm(this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        unregisterReceiver(mUpdateReceiver);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateView();
    }

    private void updateView()
    {
        SharedPreferences prefs = getSharedPreferences("update", MODE_PRIVATE);

        TextView updateText = (TextView) findViewById(R.id.update_text);
        if (prefs.contains("url")) {
            String name = prefs.getString("name", "");
            String description = getDescription();
            String size = String.format(Locale.getDefault(), "%.2f", prefs.getLong("size", 0) / 1024.0f / 1024.0f);
            updateText.setText(getString(R.string.update_text, name, description, size));
        } else {
            String date;
            if (prefs.contains("last_check")) {
                Date d = new Date(prefs.getLong("last_check", 0));
                date = DateFormat.getDateFormat(this).format(d) + " " + DateFormat.getTimeFormat(this).format(d);
            } else {
                date = getString(R.string.never);
            }

            updateText.setText(getString(R.string.no_update, BuildCompat.CSC_VERSION, date));
        }

        Button checkButton = findViewById(R.id.check_update);
        checkButton.setText(prefs.contains("url") ? R.string.install_update : R.string.check_update);
    }

    private String getDescription()
    {
        try {
            FileInputStream fis = openFileInput("desc.txt");

            StringBuilder sb = new StringBuilder();

            byte[] buffer = new byte[1024];
            int n;
            while ((n = fis.read(buffer)) != -1)
            {
                sb.append(new String(buffer, 0, n));
            }

            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Could not open description", e);
        }

        return "";
    }

    private void downloadUpdate()
    {
        Intent intent = new Intent(UpdaterActivity.this, UpdaterService.class);
        intent.setAction(UpdaterService.ACTION_DOWNLOAD);
        startService(intent);
    }

    private void checkUpdate()
    {
        Intent intent = new Intent(UpdaterActivity.this, UpdaterService.class);
        intent.setAction(UpdaterService.ACTION_CHECK);
        intent.putExtra("force", true);
        startService(intent);
    }
}
