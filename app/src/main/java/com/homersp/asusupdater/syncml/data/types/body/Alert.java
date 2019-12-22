package com.homersp.asusupdater.syncml.data.types.body;

import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.SyncML;
import com.homersp.asusupdater.syncml.data.types.SerializeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Alert extends SerializeType {
    private int mCmdID = -1;
    private int mData = -1;

    public Alert() {

    }

    public Alert(int cmd, int data) {
        mCmdID = cmd;
        mData = data;
    }


    @Override
    public boolean valid() {
        return mCmdID >= 0;
    }

    @Override
    public @NonNull String node() {
        return "Alert";
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        serializer.addNode("CmdID", mCmdID);
        if (mData >= 0) {
            serializer.addNode("Data", mData);
        }
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        if ("CmdID".equals(name)) {
            mCmdID = parser.nextInt();
        } else if ("Data".equals(name)) {
            mData = parser.nextInt();
        }
    }
}
