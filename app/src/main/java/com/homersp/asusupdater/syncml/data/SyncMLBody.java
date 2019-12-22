package com.homersp.asusupdater.syncml.data;


import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.homersp.asusupdater.syncml.data.types.SerializeType;
import com.homersp.asusupdater.syncml.data.types.body.Alert;
import com.homersp.asusupdater.syncml.data.types.body.ReplaceGet;
import com.homersp.asusupdater.syncml.data.types.body.Results;
import com.homersp.asusupdater.syncml.data.types.body.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SyncMLBody extends SerializeType {
    private List<Alert> mAlert = new ArrayList<>();
    private List<Status> mStatus = new ArrayList<>();
    private SparseArray<ReplaceGet> mGet = new SparseArray<>();
    private SparseArray<ReplaceGet> mReplace = new SparseArray<>();
    private SparseArray<Results> mResults = new SparseArray<>();

    public SyncMLBody addStatus(Status status) {
        mStatus.add(status);
        return this;
    }

    public SyncMLBody addAlert(int cmd, int data) {
        mAlert.add(new Alert(cmd, data));
        return this;
    }

    public int getStatusCount() {
        return mStatus.size();
    }

    public int getGetCount() {
        return mGet.size();
    }

    public int getReplaceCount() {
        return mReplace.size();
    }

    public Status statusAt(int i) {
        return mStatus.get(i);
    }

    public ReplaceGet replace(int cmd) {
        if (mReplace.get(cmd) == null) {
            mReplace.put(cmd, new ReplaceGet(ReplaceGet.TYPE_REPLACE, cmd));
        }

        return mReplace.get(cmd);
    }

    public ReplaceGet replaceAt(int i) {
        return mReplace.valueAt(i);
    }

    public Results results(int cmd) {
        if (mResults.get(cmd) == null) {
            mResults.put(cmd, new Results(cmd));
        }

        return mResults.get(cmd);
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public @NonNull
    String node() {
        return "SyncBody";
    }

    @Override
    public void serializeNode(SyncML.Serializer serializer) throws IOException {
        for (Status s: mStatus) {
            s.serialize(serializer);
        }

        for (Alert a: mAlert) {
            a.serialize(serializer);
        }

        for (int i = 0; i < mGet.size(); i++) {
            mGet.valueAt(i).serialize(serializer);
        }

        for (int i = 0; i < mReplace.size(); i++) {
            mReplace.valueAt(i).serialize(serializer);
        }

        for (int i = 0; i < mResults.size(); i++) {
            mResults.valueAt(i).serialize(serializer);
        }

        serializer.addNode("Final", "");
    }

    @Override
    public void parseNode(SyncML.Parser parser, String name) throws Exception {
        if ("Alert".equals(name)) {
            Alert alert = new Alert();
            alert.parse(parser);
            mAlert.add(alert);
        }  else if ("Status".equals(name)) {
            Status status = new Status();
            status.parse(parser);
            mStatus.add(status);
        } else if ("Get".equals(name)) {
            ReplaceGet get = new ReplaceGet(ReplaceGet.TYPE_GET);
            get.parse(parser);
            mGet.put(get.cmdID(), get);
        } else if ("Replace".equals(name)) {
            ReplaceGet replace = new ReplaceGet(ReplaceGet.TYPE_REPLACE);
            replace.parse(parser);
            mReplace.put(replace.cmdID(), replace);
        } else if ("Results".equals(name)) {
            Results results = new Results();
            results.parse(parser);
            mResults.put(results.cmdID(), results);
        }
    }
}
