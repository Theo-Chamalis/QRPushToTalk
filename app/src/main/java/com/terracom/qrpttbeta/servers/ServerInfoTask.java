package com.terracom.qrpttbeta.servers;

import android.os.AsyncTask;
import android.util.Log;

import com.terracom.jumble.model.Server;
import com.terracom.qrpttbeta.Constants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class ServerInfoTask extends AsyncTask<Server, Void, ServerInfoResponse> {

	private Server server;
	
	@Override
	protected ServerInfoResponse doInBackground(Server... params) {
		server = params[0];
		try {
			InetAddress host = InetAddress.getByName(server.getHost());

			ByteBuffer buffer = ByteBuffer.allocate(12);
			buffer.putInt(0);
			buffer.putLong(server.getId());
			DatagramPacket requestPacket = new DatagramPacket(buffer.array(), 12, host, server.getPort());

			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(1000);
			socket.setReceiveBufferSize(1024);

            long startTime = System.nanoTime();

            socket.send(requestPacket);
			
			byte[] responseBuffer = new byte[24];
			DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
			socket.receive(responsePacket);

            int latencyInMs = (int) ((System.nanoTime()-startTime)/1000000);
			
			ServerInfoResponse response = new ServerInfoResponse(server, responseBuffer, latencyInMs);
							
			Log.i(Constants.TAG, "DEBUG: Server version: "+response.getVersionString()+"\nUsers: "+response.getCurrentUsers()+"/"+response.getMaximumUsers());
			
			return response;
			
		} catch (Exception e) {
		}

		return new ServerInfoResponse();
	}
	
}