package com.homersp.asusupdater;

import android.annotation.SuppressLint;
import android.os.Build;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BuildCompat {
    private static final String TAG = "AsusUpdater." + BuildCompat.class.getSimpleName();

    public static final String MODEL = Build.MODEL;
    public static final String MANUFACTURER = Build.MANUFACTURER;
    public static final String REVISION = getProperty("ro.revision", "");

    public static final String CSC_VERSION = getProperty("ro.build.csc.version", "");
    public static final String FOTA_VERSION = getProperty("ro.build.fota.version", "");
    public static final String VERSION_INCREMENTAL = getProperty("ro.build.version.incremental", "");
    public static final String TYPE = getProperty("ro.build.type", "");

    public static final String ASUS_SUB_VERSION = getProperty("asus.build.sub.version", "");
    public static final String ASUS_EXT_VERSION = getProperty("asus.build.ext.version", "");
    public static final String ASUS_SKU = getProperty("ro.build.asus.sku", "");

    public static final String WW_IMEI_DEFAULT = "MzU4Mjk2MTAwNTAwMDE4";

    private static final String PRODUCT_CARRIER = getProperty("ro.product.carrier", "");

    public static String getCarrier()
    {
        String carrier = BuildCompat.PRODUCT_CARRIER;
        if (BuildCompat.ASUS_SKU.length() > 0 && !carrier.endsWith(BuildCompat.ASUS_SKU)) {
            carrier = carrier.substring(0, carrier.length() - BuildCompat.ASUS_SKU.length());
            carrier = carrier + BuildCompat.ASUS_SKU;
        }

        return carrier;
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
            Log.e(TAG, "getProperty failed");
        }

        return "";
    }
}
