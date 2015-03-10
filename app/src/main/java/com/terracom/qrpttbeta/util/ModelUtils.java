package com.terracom.qrpttbeta.util;

import android.os.RemoteException;

import com.terracom.jumble.IJumbleService;
import com.terracom.jumble.model.Channel;

import java.util.LinkedList;
import java.util.List;

public class ModelUtils {

    public static List<Channel> getChannelList(Channel channel,
                                               IJumbleService service) throws RemoteException {
        LinkedList<Channel> channels = new LinkedList<Channel>();
        getChannelList(channel, service, channels);
        return channels;
    }

    private static void getChannelList(Channel channel,
                                       IJumbleService service,
                                       List<Channel> channels) throws RemoteException {
        channels.add(channel);
        for (int cid : channel.getSubchannels()) {
            Channel subc = service.getChannel(cid);
            if (subc != null) {
                getChannelList(subc, service, channels);
            }
        }
    }
}
