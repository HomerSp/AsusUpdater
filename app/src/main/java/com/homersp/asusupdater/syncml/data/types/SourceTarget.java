package com.homersp.asusupdater.syncml.data.types;

import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.SyncML;

import java.io.IOException;

public class SourceTarget extends SerializeType {
    public static final int TYPE_SOURCE = 0;
    public static final int TYPE_TARGET = 1;

    private final int mType;
    private String mURI;
    private String mName;

    public SourceTarget(int type) {
        mType = type;
    }

    public String uri() {
        return mURI;
    }

    public String name() {
        return mName;
    }

    public SourceTarget setURI(String uri) {
        mURI = uri;
        return this;
    }

    public SourceTarget setName(String name) {
        mName = name;
        return this;
    }

    @Override
    public boolean valid() {
        return mURI != null || mName != null;
    }

    @Override
    public @NonNull
    String node() {
        if (mType == TYPE_SOURCE) {
            return "Source";
        }

        return "Target";
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        if (mURI != null) {
            serializer.addNode("LocURI", mURI);
        }

        if (mName != null) {
            serializer.addNode("LocName", mName);
        }
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        if ("LocURI".equals(name)) {
            mURI = parser.nextText();
        }

        if ("LocName".equals(name)) {
            mName = parser.nextText();
        }
    }
}
