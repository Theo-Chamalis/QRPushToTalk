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

<<<<<<< HEAD
    private boolean mDummy = false;

    public ServerInfoResponse(Server server, byte[] response, int latency) {
        ByteBuffer buffer = ByteBuffer.wrap(response);
        mVersion = buffer.getInt();
        mIdentifier = buffer.getLong();
        mCurrentUsers = buffer.getInt();
        mMaximumUsers = buffer.getInt();
        mAllowedBandwidth = buffer.getInt();
=======
	private boolean mDummy = false;

	public ServerInfoResponse(Server server, byte[] response, int latency) {
		ByteBuffer buffer = ByteBuffer.wrap(response);
		mVersion = buffer.getInt();
		mIdentifier = buffer.getLong();
		mCurrentUsers = buffer.getInt();
		mMaximumUsers = buffer.getInt();
		mAllowedBandwidth = buffer.getInt();
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0
        mLatency = latency;
        mServer = server;
    }

<<<<<<< HEAD
    public ServerInfoResponse() {
        this.mDummy = true;
    }
=======
	public ServerInfoResponse() {
		this.mDummy = true;
	}
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0

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

<<<<<<< HEAD
    public int getMaximumUsers() {
        return mMaximumUsers;
    }

=======
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0
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
