package com.homersp.asusupdater;

public class Log {
    private static final boolean DEBUG = false;

    public static void d(String tag, String msg) {
        if (!DEBUG) {
            return;
        }

        android.util.Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable t) {
        android.util.Log.e(tag, msg, t);
    }

    public static void xml(String tag, String msg) {
        if (!DEBUG) {
            return;
        }

        int i = 0;
        StringBuilder sb = new StringBuilder(msg);
        while (i < sb.length()) {
            android.util.Log.d(tag, sb.substring(i, Math.min(sb.length(), i + 4000)));

            i += 4000;
        }
    }
}
