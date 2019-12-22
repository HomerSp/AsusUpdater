package com.homersp.asusupdater.updater;

import android.content.Context;

import android.util.Xml;

import com.homersp.asusupdater.Log;
import com.homersp.asusupdater.syncml.DataDecoder;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLDecoder;

public class UpdaterPackage {
    private static final String TAG = "AsusUpdater." + UpdaterPackage.class.getSimpleName();

    private String mUrl;
    private long mSize;
    private String mName;
    private String mDescription;
    private String mNotification;

    public UpdaterPackage(Context context, InputStream is)
    {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, "UTF-8");
            parser.nextTag();
            parseData(parser);
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Failed parsing XML", e);
        }
    }

    public String getUrl()
    {
        return mUrl;
    }

    public long getSize()
    {
        return mSize;
    }

    public String getName()
    {
        return mName;
    }

    public String getDescription()
    {
        return mDescription;
    }

    public String getNotification()
    {
        return mNotification;
    }

    private void parseData(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, "", "media");
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            switch (name) {
                case "objectURI":
                    mUrl = readText(parser);
                    break;
                case "size":
                    mSize = Long.parseLong(readText(parser));
                    break;
                case "name":
                    mName = readText(parser);
                    break;
                case "description":
                    mDescription = extractXML(DataDecoder.decode(readText(parser)), "description", "en_US");
                    break;
                case "notification":
                    mNotification = extractXML(DataDecoder.decode(readText(parser)), "notification", "en_US");
                    break;
            }
        }
    }

    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("Invalid data");
        }

        if (parser.next() != XmlPullParser.TEXT) {
            throw new XmlPullParserException("Invalid data");
        }

        String ret = parser.getText();

        if (parser.next() != XmlPullParser.END_TAG) {
            throw new XmlPullParserException("Invalid data");
        }

        return ret;
    }

    private String extractXML(String data, String tag, String lang)
    {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(data));
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, "", "trans");
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String name = parser.getName();
                if (lang.equals(name)) {
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        if ("name".equals(parser.getAttributeName(i)) && parser.getAttributeValue(i).equals(tag)) {
                            return readText(parser);
                        }
                    }
                }
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Failed parsing XML", e);
        }

        return "";
    }
}
