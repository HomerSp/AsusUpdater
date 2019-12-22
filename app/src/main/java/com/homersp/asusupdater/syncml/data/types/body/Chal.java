package com.homersp.asusupdater.syncml.data.types.body;

import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.SyncML;
import com.homersp.asusupdater.syncml.data.types.MetaFormat;
import com.homersp.asusupdater.syncml.data.types.SerializeType;

import java.io.IOException;

public class Chal extends SerializeType {
    private MetaFormat mMeta = new MetaFormat();

    public MetaFormat meta() {
        return mMeta;
    }

    @Override
    public boolean valid() {
        return mMeta.valid();
    }

    @Override
    public @NonNull
    String node() {
        return "Chal";
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        mMeta.serialize(serializer);
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        if (mMeta.node().equals(name)) {
            mMeta.parse(parser);
        }
    }
}
