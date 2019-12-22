package com.homersp.asusupdater.syncml.data.types.header;



import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.SyncML;
import com.homersp.asusupdater.syncml.data.types.Data;
import com.homersp.asusupdater.syncml.data.types.MetaFormat;
import com.homersp.asusupdater.syncml.data.types.SerializeType;

import java.io.IOException;

public class HeaderCred extends SerializeType {
    private MetaFormat mMeta = new MetaFormat();
    private Data mData = new Data(false);

    public MetaFormat meta() {
        return mMeta;
    }
    public Data data() {
        return mData;
    }

    @Override
    public boolean valid() {
        return mMeta.valid() || mData.valid();
    }

    @Override
    public @NonNull
    String node() {
        return "Cred";
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        mMeta.serialize(serializer);
        mData.serialize(serializer);
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        if (mMeta.node().equals(name)) {
            mMeta.parse(parser);
        } else if (mData.node().equals(name)) {
            mData.parseNode(parser, name);
        }
    }
}
