package com.homersp.asusupdater.syncml.data.types;

import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.SyncML;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MetaFormat extends SerializeType {
    public static final int FORMAT_B64 = 0;
    public static final int FORMAT_CHR = 1;

    public static final int TYPE_AUTH_SHA256 = 0;

    private int mFormat = -1;
    private int mType = -1;
    private String mNextNonce;

    public MetaFormat()
    {
    }

    public String nextNonce() {
        return mNextNonce;
    }

    public MetaFormat setFormat(int format) {
        mFormat = format;
        return this;
    }

    public MetaFormat setType(int type) {
        mType = type;
        return this;
    }

    public MetaFormat setNextNonce(String nextNonce) {
        mNextNonce = nextNonce;
        return this;
    }

    @Override
    public boolean valid() {
        return mFormat >= 0 || mType >= 0;
    }

    @Override
    public @NonNull
    String node() {
        return "Meta";
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        String f = formatStr();
        if (f != null) {
            serializer.meta("Format", f);
        }

        String t = typeStr();
        if (t != null) {
            serializer.meta("Type", t);
        }

        if (mNextNonce != null) {
            serializer.meta("NextNonce", mNextNonce);
        }
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        if ("Format".equals(name)) {
            String s = parser.nextMetaText();
            mFormat = formatFromString(s);
            if (mFormat < 0) {
                throw new XmlPullParserException("Unexpected Format " + s);
            }
        }

        if ("Type".equals(name)) {
            String s = parser.nextMetaText();
            mType = typeFromString(s);
            if (mType < 0) {
                throw new XmlPullParserException("Unexpected Type " + s);
            }
        }

        if ("NextNonce".equals(name)) {
            mNextNonce = parser.nextMetaText();
        }
    }

    private String formatStr() {
        switch (mFormat) {
            case FORMAT_B64:
                return "b64";
            case FORMAT_CHR:
                return "chr";
        }

        return null;
    }

    private int formatFromString(String s) {
        if ("b64".equals(s)) {
            return FORMAT_B64;
        } else if ("chr".equals(s)) {
            return FORMAT_CHR;
        }

        return -1;
    }

    private String typeStr() {
        switch (mType) {
            case TYPE_AUTH_SHA256:
                return "syncml:auth-SHA256";
        }

        return null;
    }

    private int typeFromString(String s) {
        if ("syncml:auth-SHA256".equals(s)) {
            return TYPE_AUTH_SHA256;
        }

        return -1;
    }
}
