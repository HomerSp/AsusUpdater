package com.homersp.asusupdater.syncml.data.types.body;

import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.SyncML;
import com.homersp.asusupdater.syncml.data.types.SerializeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Results extends SerializeType {
    private int mCmdID = -1;
    private int mMsgRef = -1;
    private int mCmdRef = -1;
    private List<Item> mItems = new ArrayList<>();

    public Results() {

    }

    public Results(int cmdID) {
        mCmdID = cmdID;
    }

    public Results setReference(int msg, int cmd) {
        mMsgRef = msg;
        mCmdRef = cmd;
        return this;
    }

    public Results addItem(Item item) {
        mItems.add(item);
        return this;
    }

    public int cmdID() {
        return mCmdID;
    }

    @Override
    public boolean valid() {
        return mCmdID >= 0;
    }

    @Override
    public @NonNull
    String node() {
        return "Results";
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        serializer.addNode("CmdID", mCmdID);
        if (mMsgRef >= 0) {
            serializer.addNode("MsgRef", mMsgRef);
        }

        if (mCmdRef >= 0) {
            serializer.addNode("CmdRef", mCmdRef);
        }

        for (Item i: mItems) {
            i.serialize(serializer);
        }
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        if ("CmdID".equals(name)) {
            mCmdID = parser.nextInt();
        } else if ("MsgRef".equals(name)) {
            mMsgRef = parser.nextInt();
        } else if ("CmdRef".equals(name)) {
            mCmdRef = parser.nextInt();
        } else if ("Item".equals(name)) {
            Item item = new Item();
            item.parse(parser);
            mItems.add(item);
        }
    }
}
