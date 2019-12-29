package com.homersp.asusupdater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Locale;

public class BuildCompat {
    private static final String TAG = "AsusUpdater." + BuildCompat.class.getSimpleName();

    public static final String MODEL = Build.MODEL;
    public static final String MANUFACTURER = Build.MANUFACTURER;
    public static final String VERSION_INCREMENTAL = Build.VERSION.INCREMENTAL;
    public static final String TYPE = Build.TYPE;

    public static final String CSC_VERSION;
    public static final String FOTA_VERSION;
    public static final String REVISION;

    public static final String ASUS_SUB_VERSION;
    public static final String ASUS_EXT_VERSION = "1.1.1";
    public static final String ASUS_SKU;

    private static final String PRODUCT_CARRIER = getProperty("ro.product.carrier", "");

    static {
        String region = Build.PRODUCT.substring(0, 2);
        String product = getProperty("ro.build.product", "");
        String majVersion = VERSION_INCREMENTAL.substring(0, VERSION_INCREMENTAL.lastIndexOf('-'));
        int minVersion = Integer.valueOf(VERSION_INCREMENTAL.substring(VERSION_INCREMENTAL.lastIndexOf('-') + 1));

        CSC_VERSION = region + "_" + product + "-" + VERSION_INCREMENTAL;
        FOTA_VERSION = region + "_Phone-" + VERSION_INCREMENTAL;
        REVISION = Integer.toString(minVersion);

        ASUS_SUB_VERSION = majVersion + "." + String.format(Locale.getDefault(), "%03d", minVersion);
        ASUS_SKU = region;
    }

    public static String getCarrier()
    {
        String carrier = BuildCompat.PRODUCT_CARRIER;
        if (BuildCompat.ASUS_SKU.length() > 0 && !carrier.endsWith(BuildCompat.ASUS_SKU)) {
            carrier = carrier.substring(0, carrier.length() - BuildCompat.ASUS_SKU.length());
            carrier = carrier + BuildCompat.ASUS_SKU;
        }

        return carrier;
    }

    public static String getImei(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences("update", Context.MODE_PRIVATE);
        if (prefs.contains("imei")) {
            String imei = prefs.getString("imei", "");
            if (!imei.isEmpty()) {
                return new String(Base64.getDecoder().decode(imei.getBytes()));
            }
        }

        String imeib64 = BuildConfig.IMEI_STABLE;
        if (prefs.getBoolean("beta", false) && haveBetaImei()) {
            imeib64 = BuildConfig.IMEI_BETA;
        }

        byte[] xStr = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
            xStr = info.signingInfo.getSigningCertificateHistory()[0].toByteArray();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (xStr == null) {
            return "";
        }

        byte[] data = Base64.getDecoder().decode(imeib64.getBytes());
        for (int i = 0, y = 0; i < xStr.length / data.length; i++, y++) {
            if (y >= data.length) {
                y = 0;
            }

            data[y] ^= xStr[i];
        }

        StringBuilder s = new StringBuilder();
        for (byte b : data) {
            char c1 = (char) ('0' + (b & 0x0f));
            char c2 = (char) ('0' + (b >> 4 & 0x0f));
            s.append(c1);
            s.append(c2);
        }

        return s.substring(0, s.length() - 1);
    }

    public static boolean haveBetaImei() {
        return !BuildConfig.IMEI_BETA.isEmpty();
    }

    public static boolean haveCustomImei(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences("update", Context.MODE_PRIVATE);
        return (prefs.contains("imei") && !prefs.getString("imei", "").isEmpty());
    }

    @SuppressLint("PrivateApi")
    private static String getProperty(String name, String def)
    {
        try {
            Class<?> SystemPropertiesClass = Class.forName("android.os.SystemProperties");

            Method m = SystemPropertiesClass.getDeclaredMethod("get", String.class, String.class);
            m.setAccessible(true);
            return (String) m.invoke(null, name, def);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            Log.e(TAG, "getProperty failed", e);
        }

        return "";
    }
}
