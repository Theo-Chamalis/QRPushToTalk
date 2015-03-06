package com.terracom.qrpttbeta.channel.actionmode;

import android.support.v7.view.ActionMode;
import android.view.Menu;

import com.terracom.qrpttbeta.channel.ChatTargetProvider;

public abstract class ChatTargetActionModeCallback implements ActionMode.Callback {
    private ChatTargetProvider mProvider;

    public ChatTargetActionModeCallback(ChatTargetProvider provider) {
        mProvider = provider;
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        mProvider.setChatTarget(getChatTarget());
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mProvider.setChatTarget(null);
    }

    public abstract ChatTargetProvider.ChatTarget getChatTarget();
}
