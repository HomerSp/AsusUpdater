package com.homersp.asusupdater.updater;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;


import com.homersp.asusupdater.Log;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;

public class UpdaterFileUtils {
    private static final String TAG = "AsusUpdater." + UpdaterFileUtils.class.getSimpleName();

    private static final String EXT_VER = "1.1.999";

    public static boolean moveUpdaterFile(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences("update", MODE_PRIVATE);

        Uri pkgUri = Uri.parse(prefs.getString("url", ""));
        String name = pkgUri.getLastPathSegment();
        if (name == null) {
            Log.e(TAG, "Invalid URI");
            return false;
        }

        File file = new File(context.getExternalFilesDir(""), name);
        if (!file.exists()) {
            Log.e(TAG, "Update file does not exist");
            return false;
        }

        String filename = getUpdateFileName(context);
        if (!file.renameTo(new File(Environment.getExternalStorageDirectory(), filename))) {
            Log.e(TAG, "Failed moving update file");
            return false;
        }

        return UpdaterFileUtils.updateFileExists(context);
    }

    public static boolean updateFileExists(Context context)
    {
        return new File(Environment.getExternalStorageDirectory(), getUpdateFileName(context)).exists();
    }

    public static boolean removeUpdateFile(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences("update", MODE_PRIVATE);
        prefs.edit().remove("name")
                .remove("url")
                .remove("size")
                .remove("ignore")
                .remove("download_id")
                .apply();

        return new File(Environment.getExternalStorageDirectory(), getUpdateFileName(context)).delete();
    }

    private static String getUpdateFileName(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences("update", MODE_PRIVATE);
        Uri pkgUri = Uri.parse(prefs.getString("url", ""));
        String type = pkgUri.getLastPathSegment();
        if (type != null) {
            try {
                type = type.substring(0, type.lastIndexOf('.'));
                type = type.substring(type.lastIndexOf('-') + 1);
            } catch (IndexOutOfBoundsException e) {
                type = null;
            }
        }

        if (type == null) {
            type = "user";
        }

        return "UL-ASUS_I001_1-ASUS-" + prefs.getString("name", "") + "-" + EXT_VER + "-" + type + ".zip";
    }
}
