package com.terracom.qrpttbeta.service.ipc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.terracom.jumble.IJumbleService;

public class TalkBroadcastReceiver extends BroadcastReceiver {
    public static final String BROADCAST_TALK = "com.terracom.qrpttbeta.action.TALK";
    public static final String EXTRA_TALK_STATUS = "status";
    public static final String TALK_STATUS_ON = "on";
    public static final String TALK_STATUS_OFF = "off";
    public static final String TALK_STATUS_TOGGLE = "toggle";

    private IJumbleService mService;

    public TalkBroadcastReceiver(IJumbleService service) {
        mService = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (BROADCAST_TALK.equals(intent.getAction())) {
                String status = intent.getStringExtra(EXTRA_TALK_STATUS);
                if (status == null) status = TALK_STATUS_TOGGLE;
                if (TALK_STATUS_ON.equals(status)) {
                    mService.setTalkingState(true);
                } else if (TALK_STATUS_OFF.equals(status)) {
                    mService.setTalkingState(false);
                } else if (TALK_STATUS_TOGGLE.equals(status)) {
                    mService.setTalkingState(!mService.isTalking());
                }
            } else {
                throw new UnsupportedOperationException();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
