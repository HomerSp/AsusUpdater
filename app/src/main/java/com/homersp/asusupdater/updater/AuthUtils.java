package com.homersp.asusupdater.updater;



import com.homersp.asusupdater.Log;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;

public class AuthUtils {
    private static final String TAG = "AsusUpdater." + AuthUtils.class.getSimpleName();

    private static final Charset ENCODING = StandardCharsets.UTF_8;

    public static String generateSecret(String imei)
    {
        StringBuilder tohash = new StringBuilder();
        tohash.append(imei);
        tohash.append("AsusDMServer");
        tohash.append("My favorite is Garmin-Asus");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(tohash.toString().getBytes(ENCODING));

            String hashed = new String(Base64.getEncoder().encode(md.digest()), ENCODING)
                    .replace("+", "m")
                    .replace("/", "f");

            if (hashed.endsWith("==")) {
                hashed = hashed.substring(0, hashed.length() - 2);
            } else if (hashed.endsWith("=")) {
                hashed = hashed.substring(0, hashed.length() - 1);
            }

            return hashed;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to digest", e);
        }

        return "";
    }

    public static String generateClientNonce()
    {
        return String.valueOf(100000.0f + (Math.random() * 999999.0f));
    }

    public static String generateClientNonce64()
    {
        return Base64.getEncoder().encodeToString(generateClientNonce().getBytes(ENCODING));
    }

    public static String generateNonceHash(String imei, String nonce)
    {
        return generateNonceHash(imei, nonce.getBytes(ENCODING));
    }

    public static String generateNonceHash64(String imei, String nonceb64)
    {
        byte[] nonce = Base64.getDecoder().decode(nonceb64.getBytes(ENCODING));
        return generateNonceHash(imei, nonce);
    }

    private static String generateNonceHash(String imei, byte[] nonce)
    {
        byte[] delim = ":".getBytes(ENCODING);
        String secret = generateSecret(imei);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(imei.getBytes(ENCODING));
            md.update(delim);
            md.update(secret.getBytes(ENCODING));

            String digest = new String(Base64.getEncoder().encode(md.digest()), ENCODING);

            md.reset();
            md.update(digest.getBytes(ENCODING));
            md.update(delim);
            md.update(nonce);

            return new String(Base64.getEncoder().encode(md.digest()), ENCODING);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to digest", e);
        }

        return "";
    }
}
