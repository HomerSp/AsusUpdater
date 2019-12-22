package com.homersp.asusupdater.syncml.data.types.header;

import com.homersp.asusupdater.syncml.data.SyncML;
import com.homersp.asusupdater.syncml.data.types.MetaFormat;
import com.homersp.asusupdater.syncml.data.types.SerializeType;

import java.io.IOException;

public class MetaMax extends MetaFormat {
    private long mMaxMsgSize;
    private long mMaxObjSize;

    public MetaMax setSize(long maxMsgSize, long maxObjSize) {
        mMaxMsgSize = maxMsgSize;
        mMaxObjSize = maxObjSize;
        return this;
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        serializer.meta("MaxMsgSize", String.valueOf(mMaxMsgSize));
        serializer.meta("MaxObjSize", String.valueOf(mMaxObjSize));
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        switch (name) {
            case "MaxMsgSize":
                mMaxMsgSize = parser.nextInt();
                break;
            case "MaxObjSize":
                mMaxObjSize = parser.nextInt();
                break;
        }
    }
}
