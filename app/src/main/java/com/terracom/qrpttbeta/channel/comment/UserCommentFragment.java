package com.terracom.qrpttbeta.channel.comment;

import android.os.RemoteException;

import com.terracom.jumble.IJumbleService;
import com.terracom.jumble.model.User;
import com.terracom.jumble.util.JumbleObserver;

public class UserCommentFragment extends AbstractCommentFragment {

    @Override
    public void requestComment(final IJumbleService service) throws RemoteException {
        service.registerObserver(new JumbleObserver() {
            @Override
            public void onUserStateUpdated(User user) throws RemoteException {
                if(user.getSession() == getSession() &&
                        user.getComment() != null) {
                    loadComment(user.getComment());
                    service.unregisterObserver(this);
                }
            }
        });
        service.requestComment(getSession());
    }

    @Override
    public void editComment(IJumbleService service, String comment) throws RemoteException {
        service.setUserComment(getSession(), comment);
    }

    public int getSession() {
        return getArguments().getInt("session");
    }
}
