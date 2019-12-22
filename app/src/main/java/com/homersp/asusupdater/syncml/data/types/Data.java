package com.homersp.asusupdater.syncml.data.types;



import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.SyncML;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Data extends SerializeType {
    private String mText;
    private boolean mCdata;

    public Data() {
        this(false);
    }

    public Data(boolean cdata) {
        mCdata = cdata;
    }

    public String text() {
        return mText;
    }

    public Data setText(String text) {
        mText = text;
        return this;
    }

    @Override
    public boolean valid() {
        return mText != null;
    }

    @Override
    public @NonNull
    String node() {
        return "Data";
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        if (mCdata) {
            serializer.cdata(mText);
        } else {
            serializer.text(mText);
        }
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        AtomicBoolean cdata = new AtomicBoolean(false);
        mText = parser.nextText(cdata);
        mCdata = cdata.get();
    }
}
