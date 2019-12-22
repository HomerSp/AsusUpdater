package com.homersp.asusupdater.syncml.data.types.body;

import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.SyncML;
import com.homersp.asusupdater.syncml.data.types.Data;
import com.homersp.asusupdater.syncml.data.types.MetaFormat;
import com.homersp.asusupdater.syncml.data.types.SerializeType;
import com.homersp.asusupdater.syncml.data.types.SourceTarget;

import java.io.IOException;

public class Item extends SerializeType {
    private SourceTarget mSource = new SourceTarget(SourceTarget.TYPE_SOURCE);
    private SourceTarget mTarget = new SourceTarget(SourceTarget.TYPE_TARGET);
    private MetaFormat mMeta = new MetaFormat();
    private Data mData = new Data(true);

    public SourceTarget source() {
        return mSource;
    }

    public SourceTarget target() {
        return mTarget;
    }

    public MetaFormat meta() {
        return mMeta;
    }

    public Data data() {
        return mData;
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public @NonNull
    String node() {
        return "Item";
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        mSource.serialize(serializer);
        mMeta.serialize(serializer);
        mData.serialize(serializer);
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        if (mSource.node().equals(name)) {
            mSource.parse(parser);
        } else if (mTarget.node().equals(name)) {
            mTarget.parse(parser);
        } else if (mMeta.node().equals(name)) {
            mMeta.parse(parser);
        } else if (mData.node().equals(name)) {
            mData.parseNode(parser, name);
        }
    }
}
