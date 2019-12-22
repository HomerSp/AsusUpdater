package com.homersp.asusupdater.syncml.data;

import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.types.SerializeType;
import com.homersp.asusupdater.syncml.data.types.header.HeaderCred;
import com.homersp.asusupdater.syncml.data.types.header.MetaMax;
import com.homersp.asusupdater.syncml.data.types.SourceTarget;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class SyncMLHeader extends SerializeType {
    private String mVerDTD;
    private String mVerProto;
    private int mSessionID;
    private int mMessageID;

    private SourceTarget mTarget = new SourceTarget(SourceTarget.TYPE_TARGET);
    private SourceTarget mSource = new SourceTarget(SourceTarget.TYPE_SOURCE);
    private HeaderCred mCred = new HeaderCred();
    private MetaMax mMetaMax = new MetaMax();

    public SyncMLHeader() {
        mVerDTD = "1.2";
        mVerProto = "DM/" + mVerDTD;
    }

    public SyncMLHeader setVersion(String dtd, String proto) {
        mVerDTD = dtd;
        mVerProto = proto;
        return this;
    }

    public SyncMLHeader setSessionID(int sessionID) {
        mSessionID = sessionID;
        return this;
    }

    public SyncMLHeader setMessageID(int messageID) {
        mMessageID = messageID;
        return this;
    }

    public SourceTarget target() {
        return mTarget;
    }
    public SourceTarget source() {
        return mSource;
    }

    public HeaderCred cred() {
        return mCred;
    }

    public MetaMax metaMax() {
        return mMetaMax;
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public @NonNull
    String node() {
        return "SyncHdr";
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        serializer.addNode("VerDTD", mVerDTD);
        serializer.addNode("VerProto", mVerProto);
        serializer.addNode("SessionID", String.format("%x", mSessionID));
        serializer.addNode("MsgID", String.format("%x", mMessageID));

        mTarget.serialize(serializer);
        mSource.serialize(serializer);
        mCred.serialize(serializer);
        mMetaMax.serialize(serializer);
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        if ("VerDTD".equals(name)) {
            mVerDTD = parser.nextText();
        } else if ("VerProto".equals(name)) {
            mVerProto = parser.nextText();
        } else if ("SessionID".equals(name)) {
            mSessionID = parser.nextHex();
        } else if ("MsgID".equals(name)) {
            mMessageID = parser.nextHex();
        } else if (mTarget.node().equals(name)) {
            mTarget.parse(parser);
        } else if (mSource.node().equals(name)) {
            mSource.parse(parser);
        } else if (mCred.node().equals(name)) {
            mCred.parse(parser);
        } else if (mMetaMax.node().equals(name)) {
            mMetaMax.parse(parser);
        }
    }
}
