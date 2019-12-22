package com.homersp.asusupdater.syncml.data.types.body;

import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.SyncML;
import com.homersp.asusupdater.syncml.data.types.SerializeType;

import java.io.IOException;

public class Status extends SerializeType {
    private int mCmdID = -1;
    private int mMsgRef = -1;
    private int mCmdRef = -1;
    private String mCmd = null;
    private String mTargetRef = null;
    private String mSourceRef = null;
    private Chal mChal = new Chal();
    private int mData = -1;

    public Status() {

    }

    public Status(int cmdID) {
        mCmdID = cmdID;
    }

    public int cmdRef() {
        return mCmdRef;
    }

    public int data() {
        return mData;
    }

    public Status setReference(int msgRef, int cmdRef, String cmd) {
        mMsgRef = msgRef;
        mCmdRef = cmdRef;
        mCmd = cmd;
        return this;
    }

    public Status setReferenceUri(String targetRef, String sourceRef) {
        mTargetRef = targetRef;
        mSourceRef = sourceRef;
        return this;
    }

    public Status setData(int data) {
        mData = data;
        return this;
    }

    public Chal chal() {
        return mChal;
    }

    @Override
    public boolean valid() {
        return mCmdID >= 0;
    }

    @Override
    public @NonNull
    String node() {
        return "Status";
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

        if (mCmd != null) {
            serializer.addNode("Cmd", mCmd);
        }

        if (mTargetRef != null) {
            serializer.addNode("TargetRef", mTargetRef);
        }

        if (mSourceRef != null) {
            serializer.addNode("SourceRef", mSourceRef);
        }

        mChal.serialize(serializer);

        if (mData >= 0) {
            serializer.addNode("Data", mData);
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
        } else if ("Cmd".equals(name)) {
            mCmd = parser.nextText();
        } else if ("TargetRef".equals(name)) {
            mTargetRef = parser.nextText();
        } else if ("SourceRef".equals(name)) {
            mSourceRef = parser.nextText();
        } else if (mChal.node().equals(name)) {
            mChal.parse(parser);
        } else if ("Data".equals(name)) {
            mData = parser.nextInt();
        }
    }
}
