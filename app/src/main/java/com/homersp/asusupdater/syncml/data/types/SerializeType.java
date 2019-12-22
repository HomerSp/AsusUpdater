package com.homersp.asusupdater.syncml.data.types;

import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.SyncML;

import java.io.IOException;

public abstract class SerializeType {
    public abstract boolean valid();
    public abstract @NonNull String node();
    public abstract void serializeNode(SyncML.Serializer serializer) throws IOException;
    public abstract void parseNode(SyncML.Parser parser, String name) throws Exception;

    public final void serialize(SyncML.Serializer serializer) throws IOException
    {
        String node = node();
        if (!valid()) {
            return;
        }

        serializer.startTag(node);
        serializeNode(serializer);
        serializer.endTag(node);
    }

    public final void parse(SyncML.Parser parser) throws Exception
    {
        while (!parser.nodeNextEnd(node())) {
            if (!parser.isStart()) {
                continue;
            }

            parseNode(parser, parser.name());
        }
    }
}
