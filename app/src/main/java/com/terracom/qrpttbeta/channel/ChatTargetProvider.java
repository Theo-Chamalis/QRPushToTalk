package com.terracom.qrpttbeta.channel;

import com.terracom.jumble.model.Channel;
import com.terracom.jumble.model.User;

public interface ChatTargetProvider {

    public class ChatTarget {
        private Channel mChannel;
        private User mUser;

        public ChatTarget(Channel channel) {
            mChannel = channel;
        }

        public ChatTarget(User user) {
            mUser = user;
        }

        public Channel getChannel() {
            return mChannel;
        }

        public User getUser() {
            return mUser;
        }
    }

    public interface OnChatTargetSelectedListener {
        public void onChatTargetSelected(ChatTarget target);
    }

    public ChatTarget getChatTarget();
    public void setChatTarget(ChatTarget target);
    public void registerChatTargetListener(OnChatTargetSelectedListener listener);
    public void unregisterChatTargetListener(OnChatTargetSelectedListener listener);
}