package com.homersp.asusupdater.syncml;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.homersp.asusupdater.BuildCompat;
import com.homersp.asusupdater.Log;
import com.homersp.asusupdater.R;
import com.homersp.asusupdater.syncml.data.SyncML;
import com.homersp.asusupdater.syncml.data.types.MetaFormat;
import com.homersp.asusupdater.syncml.data.types.body.Item;
import com.homersp.asusupdater.syncml.data.types.body.ReplaceGet;
import com.homersp.asusupdater.syncml.data.types.body.Status;
import com.homersp.asusupdater.updater.AuthUtils;
import com.homersp.asusupdater.updater.UpdaterPackage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

public class AsusDM {
    private static final String TAG = "AsusUpdater." + AsusDM.class.getSimpleName();

    private Context mContext;
    private String mIMEI = "";
    private int mSessionID = -1;
    private String mNonce;
    private int mMessageID = 1;

    private SSLContext mSSLContext;

    public AsusDM(Context context, String imei) {
        mContext = context;
        mIMEI = imei;
        reset();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(mIMEI.getBytes());
            String ib64 = Base64.getEncoder().encodeToString(md.digest());
            SharedPreferences prefs = context.getSharedPreferences("syncml", Context.MODE_PRIVATE);
            if (ib64.equals(prefs.getString("imei", ""))) {
                mSessionID = prefs.getInt("session_id", mSessionID);
            }

            prefs.edit()
                    .putString("imei", ib64)
                    .putInt("session_id", mSessionID)
                    .apply();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate dm = cf.generateCertificate(new BufferedInputStream(context.getResources().openRawResource(R.raw.dm)));
            keyStore.setCertificateEntry("dm", dm);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            mSSLContext = SSLContext.getInstance("TLS");
            mSSLContext.init(null, tmf.getTrustManagers(), null);
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException | KeyManagementException e) {
            Log.e(TAG, "Failed setting up SSL", e);
        }
    }

    public void reset() {
        mSessionID = new Random().nextInt(255);
        mNonce = new String(Base64.getEncoder().encode("12345".getBytes(StandardCharsets.UTF_8)));
        mMessageID = 1;
    }

    public boolean sendCheckAuth() throws IOException {
        SyncML ml = new SyncML();
        ml.header()
                .setSessionID(mSessionID)
                .setMessageID(mMessageID++);

        ml.header().target()
                .setURI("https://mdm1.asus.com/DMServer/AsusDMServlet");

        ml.header().source()
                .setName(mIMEI)
                .setURI("IMEI:" + mIMEI);

        ml.header().cred().meta()
                .setFormat(MetaFormat.FORMAT_B64)
                .setType(MetaFormat.TYPE_AUTH_SHA256);

        ml.header().cred().data()
                .setText(AuthUtils.generateNonceHash64(mIMEI, mNonce));

        ml.header().metaMax()
                .setSize(3000, 100000000);

        ml.body().addAlert(1, 1201);

        addDevInfo(ml, 2);

        SyncML ret = send(ml.serialize());
        if (ret != null) {
            return updateNonce(ret);
        }

        return false;
    }

    public boolean sendAuth() throws IOException {
        SyncML ml = new SyncML();
        ml.header()
                .setSessionID(mSessionID)
                .setMessageID(mMessageID++);

        ml.header().target()
                .setURI("https://mdm1.asus.com/DMServer/AsusDMServlet");

        ml.header().source()
                .setName(mIMEI)
                .setURI("IMEI:" + mIMEI);

        ml.header().cred().meta()
                .setFormat(MetaFormat.FORMAT_B64)
                .setType(MetaFormat.TYPE_AUTH_SHA256);

        ml.header().cred().data()
                .setText(AuthUtils.generateNonceHash64(mIMEI, mNonce));

        ml.header().metaMax()
                .setSize(3000, 100000000);

        Status status5 = new Status(5)
                .setReference(1, 0, "SyncHdr")
                .setReferenceUri("IMEI:" + mIMEI, "https://mdm1.asus.com/DMServer/AsusDMServlet")
                .setData(401);

        status5.chal().meta()
                .setFormat(MetaFormat.FORMAT_B64)
                .setType(MetaFormat.TYPE_AUTH_SHA256)
                .setNextNonce(AuthUtils.generateClientNonce64());

        ml.body().addStatus(status5);

        ml.body().addAlert(6, 1201);

        addDevInfo(ml, 7);

        SyncML ret = send(ml.serialize());
        if (ret != null) {
            return updateNonce(ret);
        }

        return false;
    }

    public boolean sendSoftware() throws IOException
    {
        SyncML ml = new SyncML();
        ml.header()
                .setSessionID(mSessionID)
                .setMessageID(mMessageID++);

        ml.header().target()
                .setURI("https://mdm1.asus.com/DMServer/AsusDMServlet");

        ml.header().source()
                .setURI("IMEI:" + mIMEI);

        ml.header().metaMax()
                .setSize(3000, 100000000);

        Status status8 = new Status(8)
                .setReference(2, 0, "SyncHdr")
                .setReferenceUri("IMEI:" + mIMEI, "https://mdm1.asus.com/DMServer/AsusDMServlet")
                .setData(212);

        status8.chal().meta()
                .setFormat(MetaFormat.FORMAT_B64)
                .setType(MetaFormat.TYPE_AUTH_SHA256)
                .setNextNonce(AuthUtils.generateClientNonce64());

        ml.body().addStatus(status8);

        ml.body().addStatus(new Status(9)
                .setReference(2, 4, "Get")
                .setData(200));

        ml.body().addStatus(new Status(10)
                .setReference(2, 5, "Replace")
                .setReferenceUri("./DevInfo/Anonym/ServerMappingId", null)
                .setData(200));

        String build = BuildCompat.FOTA_VERSION;
        build = build.substring(0, build.lastIndexOf('-'));

        String swv = "9" +
                ".." +
                build +
                "-" +
                BuildCompat.TYPE +
                "-" +
                BuildCompat.REVISION +
                ".." +
                BuildCompat.getCarrier() +
                ".." +
                BuildCompat.ASUS_SUB_VERSION;

        Item swvItem = new Item();
        swvItem.source().setURI("./DevDetail/SwV");
        swvItem.meta().setFormat(MetaFormat.FORMAT_CHR);
        swvItem.data().setText(swv);

        Item srcExtVerItem = new Item();
        srcExtVerItem.source().setURI("./DevDetail/SrcExtVer");
        srcExtVerItem.meta().setFormat(MetaFormat.FORMAT_CHR);
        srcExtVerItem.data().setText(BuildCompat.ASUS_EXT_VERSION);

        ml.body().results(11)
                .setReference(2, 4)
                .addItem(swvItem)
                .addItem(swvItem)
                .addItem(srcExtVerItem);

        SyncML ret = send(ml.serialize());
        if (ret != null) {
            if (updateNonce(ret)) {
                return ret.body().getGetCount() > 0;
            }
        }

        return false;
    }

    public String sendCNLocation() throws IOException
    {
        SyncML ml = new SyncML();
        ml.header()
                .setSessionID(mSessionID)
                .setMessageID(mMessageID++);

        ml.header().target()
                .setURI("https://mdm1.asus.com/DMServer/AsusDMServlet");

        ml.header().source()
                .setURI("IMEI:" + mIMEI);

        ml.header().metaMax()
                .setSize(3000, 100000000);

        ml.body().addStatus(new Status(12)
                .setReference(3, 0, "SyncHdr")
                .setReferenceUri("IMEI:" + mIMEI, "https://mdm1.asus.com/DMServer/AsusDMServlet")
                .setData(200));

        ml.body().addStatus(new Status(12)
                .setReference(3, 2, "Get")
                .setReferenceUri("./DevDetail/IsCNLocation", null)
                .setData(200));

        Item cnLocationItem = new Item();
        cnLocationItem.source().setURI("./DevDetail/IsCNLocation");
        cnLocationItem.meta().setFormat(MetaFormat.FORMAT_CHR);
        cnLocationItem.data().setText("WWIP");

        ml.body().results(14)
                .setReference(3, 2)
                .addItem(cnLocationItem);

        SyncML ret = send(ml.serialize());
        if (ret != null) {
            if (updateNonce(ret))  {
                for (int i = 0; i < ret.body().getReplaceCount(); i++) {
                    ReplaceGet r = ret.body().replaceAt(i);
                    Log.d(TAG, "Replace i " + i + ", cmd " + r.cmdID());
                    if (r.cmdID() == 2) {
                        for (int y = 0; y < r.getItemCount(); y++) {
                            Item item = r.itemAt(y);
                            Log.d(TAG, "item " + y + ", uri " + item.target().uri());
                            if ("./FwUpdate/Flash/DownloadAndUpdate/PkgURL".equals(item.target().uri())) {
                                return item.data().text();
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public UpdaterPackage getUpdaterPackage(String infoUrl) throws IOException
    {
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(infoUrl).openConnection();
            urlConnection.setRequestMethod("GET");

            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            Log.d(TAG, "responseCode " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                return new UpdaterPackage(mContext, urlConnection.getInputStream());
            }
        } catch (IOException e) {
            Log.e(TAG, "Download failed", e);

            throw(e);
        }

        return null;
    }

    private void addDevInfo(SyncML ml, int cmd) {
        String dmVersion = "2.3.0.45_190808";
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo("com.asus.dm", 0);
            dmVersion = info.versionName;
        } catch(PackageManager.NameNotFoundException e) {
            // Ignore
        }

        ml.body().replace(cmd)
                .addSource("./DevInfo/Mod", BuildCompat.MODEL)
                .addSource("./DevInfo/Man", BuildCompat.MANUFACTURER)
                .addSource("./DevInfo/Lang", BuildCompat.getCarrier())
                .addSource("./DevInfo/DmV", dmVersion + "||" + "Unknow" + "||" + mIMEI.substring(0, 11) + "..1.4")
                .addSource("./DevInfo/DevId", "IMEI:" + mIMEI)
                .addSource("./DevInfo/Ext/AutoRebootCount", "0")
                .addSource("./DevInfo/Ext/ModemCrashCount", "0")
                .addSource("./DevInfo/Ext/BTWifiCrashCount", "0")
                .addSource("./DevInfo/Ext/ADSPCrashCount", "0")
                .addSource("./DevInfo/Ext/ADSPCrashCount", "0")
                .addSource("./DevInfo/Ext/OtherCrashCount", "0")
                .addSource("./DevInfo/Ext/PowerKeyLongPressCount", "0")
                .addSource("./DevInfo/Ext/CSCSWVer", BuildCompat.CSC_VERSION + ".." + BuildCompat.ASUS_SUB_VERSION)
                .addSource("./DevInfo/Ext/CSCVerIncremental", BuildCompat.VERSION_INCREMENTAL)
                .addSource("./DevInfo/Anonym/ServerMappingId", "GID[KEY]1933[ATTR]DISABLE_FOTA[KEY]null[ATTR]UNLOCK[KEY]0[ATTR]ACTIVATE_TIME[KEY]2019-10-09 16:08:22.0[ATTR]LOCATION[KEY]HK")
                .addSource("./DevInfo/Ext/Acc", "")
                .addSource("./DevInfo/RealDevId", mIMEI)
                .addSource("./DevInfo/Ext/Location", "Europe@")
                .addSource("./DevInfo/Ext/ActivationTime", "FALSE")
                .addSource("./DevInfo/Ext/ManualCheck", "0")
                .addSource("./DevInfo/Ext/AsusFota", "T");
    }

    private SyncML send(String data) throws IOException {
        Log.d(TAG, "Sent:");
        Log.xml(TAG, data);

        try {
            URL url = new URL("https://mdm1.asus.com:443/DMServer/AsusDMServlet");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setHostnameVerifier(new NullHostNameVerifier());
            conn.setSSLSocketFactory(mSSLContext.getSocketFactory());

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(60 * 1000);
            conn.setReadTimeout(60 * 1000);
            conn.setRequestProperty("Host", "mdm1.asus.com");
            conn.setRequestProperty("Cache-Control", "no-store");
            conn.setRequestProperty("Accept", "application/vnd.syncml.dm+xml");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Content-Type", "application/vnd.syncml.dm+xml");
            conn.setRequestProperty("User-Agent", "Asus DM Client V0.1");
            conn.connect();

            BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            writer.write(data);
            writer.flush();
            writer.close();
            out.close();

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "responseCode " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                byte [] buf = new byte[1024];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read;
                while ((read = conn.getInputStream().read(buf, 0, 1024)) > 0) {
                    bos.write(buf, 0, read);
                }

                Log.d(TAG, "Received:");
                Log.xml(TAG, new String(bos.toByteArray()));

                InputStream is = new ByteArrayInputStream(bos.toByteArray());

                SyncML ret = new SyncML();
                ret.parse(is);
                return ret;
            }
        } catch (IOException e) {
            Log.e(TAG, "Send failed", e);
            throw(e);
        }

        return null;
    }

    private boolean updateNonce(SyncML ml) {
        for (int i = 0; i < ml.body().getStatusCount(); i++) {
            Status s = ml.body().statusAt(i);
            if (s.cmdRef() == 0) {
                String n = s.chal().meta().nextNonce();
                Log.d(TAG, "Nonce: " + n + ", data: " + s.data());
                if (n != null) {
                    mNonce = n;
                }

                return s.data() == 200 || s.data() == 212;
            }
        }

        return false;
    }

    private static class NullHostNameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return hostname != null && hostname.endsWith("asus.com");
        }
    }
}
