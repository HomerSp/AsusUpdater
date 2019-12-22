package com.homersp.asusupdater.syncml.data;


import android.util.Xml;

import com.homersp.asusupdater.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.util.concurrent.atomic.AtomicBoolean;

public class SyncML {
    private static final String TAG = "AsusUpdater." + SyncML.class.getSimpleName();

    private SyncMLHeader mHeader;
    private SyncMLBody mBody;

    public SyncML()
    {
        mHeader = new SyncMLHeader();
        mBody = new SyncMLBody();
    }

    public SyncMLHeader header()
    {
        return mHeader;
    }

    public SyncMLBody body()
    {
        return mBody;
    }

    public String serialize() {
        try {
            Serializer serializer = new Serializer();
            mHeader.serialize(serializer);
            mBody.serialize(serializer);
            return serializer.finish();
        } catch (IOException e) {
            Log.e(TAG, "Could not generate SyncML", e);
        }

        return null;
    }

    public boolean parse(InputStream is)
    {
        try {
            Parser parser = new Parser(this);
            parser.parse(is);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed parsing XML", e);
        }

        return false;
    }

    public static class Serializer {
        private XmlSerializer mSerializer;
        private StringWriter mWriter;

        Serializer() throws IOException {
            mSerializer = Xml.newSerializer();
            mWriter = new StringWriter();

            mSerializer.setOutput(mWriter);
            mSerializer.startDocument("UTF-8", null);
            mSerializer.startTag("", "SyncML");
            mSerializer.attribute("", "xmlns", "SYNCML:SYNCML1.2");
        }

        public void addNode(String name, String value) throws IOException {
            mSerializer.startTag("", name);
            if (value != null) {
                mSerializer.text(value);
            }
            mSerializer.endTag("", name);
        }

        public void addNode(String name, int value) throws IOException {
            addNode(name, String.valueOf(value));
        }

        public void startTag(String name) throws IOException {
            mSerializer.startTag("", name);
        }

        public void endTag(String name) throws IOException {
            mSerializer.endTag("", name);
        }

        public void attribute(String name, String value) throws IOException {
            mSerializer.attribute("", name, value);
        }

        public void text(String text) throws IOException {
            mSerializer.text(text);
        }

        public void cdata(String cdata) throws IOException {
            mSerializer.cdsect(cdata);
        }

        public void meta(String node, String text) throws IOException {
            startTag(node);
            attribute("xmlns", "syncml:metinf");
            text(text);
            endTag(node);
        }

        String finish() throws IOException {
            mSerializer.endTag("", "SyncML");
            mSerializer.endDocument();

            return mWriter.toString();
        }
    }

    public static class Parser {
        private SyncML mML;
        private XmlPullParser mParser;

        public Parser(SyncML ml) throws Exception {
            mML = ml;
            mParser = Xml.newPullParser();
            mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        }

        public void parse(InputStream is) throws Exception {
            mParser.setInput(is, "UTF-8");
            mParser.nextTag();

            mParser.require(XmlPullParser.START_TAG, "", "SyncML");
            while (mParser.next() != XmlPullParser.END_DOCUMENT) {
                if (!isStart()) {
                    continue;
                }

                String name = name();
                if (mML.mHeader.node().equals(name)) {
                    mML.mHeader.parse(this);
                } else if (mML.mBody.node().equals(name)) {
                    mML.mBody.parse(this);
                }
            }
        }

        public boolean nodeNextEnd(String name) throws Exception {
            return mParser.next() == XmlPullParser.END_TAG && name.equals(mParser.getName());
        }

        public String nextText() throws Exception {
            return nextText(null);
        }

        public String nextText(AtomicBoolean cdata) throws Exception {
            String name = mParser.getName();
            if (name == null) {
                throw new XmlPullParserException("Unexpected node name");
            }

            if (mParser.getEventType() != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("Unexected node type, supposed to be start");
            }

            if (mParser.nextToken() == XmlPullParser.CDSECT && cdata != null) {
                cdata.set(true);
            } else if (mParser.getEventType() != XmlPullParser.TEXT) {
                throw new XmlPullParserException("Unexpected node type, supposed to be text");
            }

            String ret = mParser.getText();
            while (mParser.getEventType() != XmlPullParser.END_TAG) {
                mParser.next();
            }

            if (!name.equals(mParser.getName())) {
                throw new XmlPullParserException("Unexpected end tag name");
            }

            return ret;
        }

        public int nextInt() throws Exception {
            return Integer.parseInt(nextText());
        }

        public int nextHex() throws Exception {
            return Integer.parseInt(nextText(), 16);
        }

        public String nextMetaText() throws Exception {
            String name = mParser.getName();
            if (name == null) {
                throw new XmlPullParserException("Unexpected node name");
            }

            if (mParser.getEventType() != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("Unexected node type, supposed to be start");
            }

            if (mParser.getAttributeCount() != 1) {
                throw new XmlPullParserException("Unexpected attribute count");
            }

            if (!"xmlns".equals(mParser.getAttributeName(0)) || !"syncml:metinf".equals(mParser.getAttributeValue(0))) {
                throw new XmlPullParserException("Unexpected meta attribute value");
            }

            String ret = mParser.nextText();
            if (!name.equals(mParser.getName())) {
                throw new XmlPullParserException("Unexpected end tag name");
            }

            return ret;
        }

        public String name() throws Exception {
            return mParser.getName();
        }

        public boolean isStart() throws Exception {
            return mParser.getEventType() == XmlPullParser.START_TAG;
        }
    }
}
