package com.homersp.asusupdater.syncml.data.types.body;

import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.SyncML;
import com.homersp.asusupdater.syncml.data.types.MetaFormat;
import com.homersp.asusupdater.syncml.data.types.SerializeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReplaceGet extends SerializeType {
    public static final int TYPE_GET = 0;
    public static final int TYPE_REPLACE = 1;

    private int mType;
    private int mCmdID = -1;
    private List<Item> mItems = new ArrayList<>();

    public ReplaceGet(int type) {
        this(type, -1);
    }

    public ReplaceGet(int type, int cmd) {
        mType = type;
        mCmdID = cmd;
    }

    public ReplaceGet addSource(String uri, String data) {
        Item item = new Item();
        item.meta().setFormat(MetaFormat.FORMAT_CHR);
        item.source().setURI(uri);
        item.data().setText(data);
        mItems.add(item);
        return this;
    }

    public int cmdID() {
        return mCmdID;
    }

    public int getItemCount() {
        return mItems.size();
    }

    public Item itemAt(int i) {
        return mItems.get(i);
    }

    @Override
    public boolean valid() {
        return mCmdID >= 0;
    }

    @Override
    public @NonNull
    String node() {
        if (mType == TYPE_GET) {
            return "Get";
        }

        return "Replace";
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        serializer.addNode("CmdID", mCmdID);
        for (Item i: mItems) {
            i.serialize(serializer);
        }
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        if ("CmdID".equals(name)) {
            mCmdID = parser.nextInt();
        } else if ("Item".equals(name)) {
            Item item = new Item();
            item.parse(parser);
            mItems.add(item);
        }
    }
}
