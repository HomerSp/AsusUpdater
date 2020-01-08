package com.homersp.asusupdater.updater;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

import com.homersp.asusupdater.Log;
import com.homersp.asusupdater.UpdaterApplication;

import java.io.File;
import java.io.FileNotFoundException;

import static android.content.Context.MODE_PRIVATE;

public class UpdaterFileUtils {
    private static final String TAG = "AsusUpdater." + UpdaterFileUtils.class.getSimpleName();

    private static final String EXT_VER = "1.1.999";

    public static boolean moveUpdaterFile(Context context)
    {
        DocumentFile downloadFile = getDownloadDocument(context);
        if (downloadFile == null) {
            return false;
        }

        DocumentFile parentFile = downloadFile.getParentFile();
        DocumentFile rootFile = UpdaterApplication.getRootDocument(context);
        if (parentFile == null || rootFile == null) {
            return false;
        }

        try {
            Uri newUri = DocumentsContract.moveDocument(context.getContentResolver(), downloadFile.getUri(), parentFile.getUri(), rootFile.getUri());
            if (newUri == null) {
                return false;
            }

            return DocumentsContract.renameDocument(context.getContentResolver(), newUri, getUpdateFileName(context)) != null;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "moveDocument failed", e);
        }

        return false;
    }

    public static boolean updateFileExists(Context context)
    {
        DocumentFile file = UpdaterApplication.getRootDocument(context);
        return file != null && file.findFile(getUpdateFileName(context)) != null;
    }

    public static boolean removeUpdateFile(Context context)
    {
        DocumentFile file = UpdaterApplication.getRootDocument(context);
        if (file == null) {
            return false;
        }

        file = file.findFile(getUpdateFileName(context));
        if (file != null && file.delete()) {
            SharedPreferences prefs = context.getSharedPreferences("update", MODE_PRIVATE);
            prefs.edit().remove("name")
                    .remove("url")
                    .remove("size")
                    .remove("ignore")
                    .remove("download_id")
                    .apply();

            return true;
        }

        return false;
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

    private static DocumentFile getDownloadDocument(Context context)
    {
        File externalFile = context.getExternalFilesDir("");
        if (externalFile == null) {
            return null;
        }

        String dataPath = externalFile.getAbsolutePath();
        dataPath = dataPath.substring(dataPath.indexOf("Android"));

        String[] split = dataPath.split("/");

        int i = 0;
        DocumentFile file = UpdaterApplication.getRootDocument(context);
        while (i < split.length && file != null) {
            file = file.findFile(split[i]);
            i++;
        }

        if (file == null) {
            return null;
        }

        SharedPreferences prefs = context.getSharedPreferences("update", MODE_PRIVATE);

        String name = Uri.parse(prefs.getString("url", "")).getLastPathSegment();
        if (name == null) {
            return null;
        }

        return file.findFile(name);
    }
}
