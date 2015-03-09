package com.terracom.qrpttbeta.servers;

import com.terracom.jumble.model.Server;

import java.nio.ByteBuffer;

public class ServerInfoResponse {

    private long mIdentifier;
    private int mVersion;
    private int mCurrentUsers;
    private int mMaximumUsers;
    private int mAllowedBandwidth;
    private int mLatency;
    private Server mServer;

    private boolean mDummy = false;

    public ServerInfoResponse(Server server, byte[] response, int latency) {
        ByteBuffer buffer = ByteBuffer.wrap(response);
        mVersion = buffer.getInt();
        mIdentifier = buffer.getLong();
        mCurrentUsers = buffer.getInt();
        mMaximumUsers = buffer.getInt();
        mAllowedBandwidth = buffer.getInt();
        mLatency = latency;
        mServer = server;
    }

    public ServerInfoResponse() {
        this.mDummy = true;
    }

    public int getVersion() {
        return mVersion;
    }

    public String getVersionString() {
        byte[] versionBytes = ByteBuffer.allocate(4).putInt(mVersion).array();
        return String.format("%d.%d.%d", (int) versionBytes[1], (int) versionBytes[2], (int) versionBytes[3]);
    }

    public int getCurrentUsers() {
        return mCurrentUsers;
    }

    public int getMaximumUsers() {
        return mMaximumUsers;
    }

    public int getLatency() {
        return mLatency;
    }

    public Server getServer() {
        return mServer;
    }

    public boolean isDummy() {
        return mDummy;
    }
}
