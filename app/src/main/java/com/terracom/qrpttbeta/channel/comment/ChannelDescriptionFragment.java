package com.terracom.qrpttbeta.channel.comment;

import android.os.RemoteException;

import com.terracom.jumble.IJumbleService;
import com.terracom.jumble.model.Channel;
import com.terracom.jumble.util.JumbleObserver;

public class ChannelDescriptionFragment extends AbstractCommentFragment {

    @Override
    public void requestComment(final IJumbleService service) throws RemoteException {
        service.registerObserver(new JumbleObserver() {
            @Override
            public void onChannelStateUpdated(Channel channel) throws RemoteException {
                if (channel.getId() == getChannelId() &&
                        channel.getDescription() != null) {
                    loadComment(channel.getDescription());
                    service.unregisterObserver(this);
                }
            }
        });
        service.requestChannelDescription(getChannelId());
    }

    @Override
    public void editComment(IJumbleService service, String comment) throws RemoteException {
    }

    private int getChannelId() {
        return getArguments().getInt("channel");
    }
}
