package com.terracom.qrpttbeta.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;

import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.app.DrawerAdapter;
import com.terracom.qrpttbeta.app.QRPushToTalkActivity;

import java.util.ArrayList;
import java.util.List;

public class QRPushToTalkNotification {
    private static final int NOTIFICATION_ID = 1;
    private static final String BROADCAST_MUTE = "b_mute";
    private static final String BROADCAST_DEAFEN = "b_deafen";
    private static final String BROADCAST_OVERLAY = "b_overlay";

    private Service mService;
    private OnActionListener mListener;
    private List<String> mMessages;
    private String mCustomTicker;
    private String mCustomContentText;
    private boolean mActionsShown;

    private BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BROADCAST_MUTE.equals(intent.getAction())) {
                mListener.onMuteToggled();
            } else if (BROADCAST_DEAFEN.equals(intent.getAction())) {
                mListener.onDeafenToggled();
            } else if (BROADCAST_OVERLAY.equals(intent.getAction())) {
            }
        }
    };

    public static QRPushToTalkNotification showForeground(Service service, String ticker, String contentText,
                                                          OnActionListener listener) {
        QRPushToTalkNotification notification = new QRPushToTalkNotification(service, ticker, contentText, listener);
        notification.show();
        return notification;
    }

    private QRPushToTalkNotification(Service service, String ticker, String contentText,
                                     OnActionListener listener) {
        mService = service;
        mListener = listener;
        mMessages = new ArrayList<String>();
        mCustomTicker = ticker;
        mCustomContentText = contentText;
        mActionsShown = false;
    }

    public void setCustomTicker(String ticker) {
        mCustomTicker = ticker;

    }

    public void setCustomContentText(String text) {
        mCustomContentText = text;

    }

    public void setActionsShown(boolean actionsShown) {
        mActionsShown = actionsShown;
    }

    public void addMessage(String message) {
        mMessages.add(message);
        mCustomTicker = message;
        createNotification();
    }

    public void clearMessages() {
        mMessages.clear();
        createNotification();
    }

    public void show() {
        createNotification();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_DEAFEN);
        filter.addAction(BROADCAST_MUTE);

        try {
            mService.registerReceiver(mNotificationReceiver, filter);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void hide() {
        try {
            mService.unregisterReceiver(mNotificationReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        mService.stopForeground(true);
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mService);
        builder.setSmallIcon(R.drawable.ic_stat_notify);
        builder.setTicker(mCustomTicker);
        builder.setContentTitle(mService.getString(R.string.app_name));
        builder.setContentText(mCustomContentText);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setOngoing(true);

        if (mActionsShown) {
            Intent muteIntent = new Intent(BROADCAST_MUTE);
            Intent deafenIntent = new Intent(BROADCAST_DEAFEN);
            builder.addAction(R.drawable.ic_action_microphone,
                    mService.getString(R.string.mute), PendingIntent.getBroadcast(mService, 1,
                            muteIntent, PendingIntent.FLAG_CANCEL_CURRENT));
            builder.addAction(R.drawable.ic_action_audio,
                    mService.getString(R.string.deafen), PendingIntent.getBroadcast(mService, 1,
                            deafenIntent, PendingIntent.FLAG_CANCEL_CURRENT));
        }

        if (mMessages.size() > 0) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (String message : mMessages) {
                inboxStyle.addLine(message);
            }
            builder.setStyle(inboxStyle);
        }

        Intent channelListIntent = new Intent(mService, QRPushToTalkActivity.class);
        channelListIntent.putExtra(QRPushToTalkActivity.EXTRA_DRAWER_FRAGMENT, DrawerAdapter.ITEM_SERVER);
        PendingIntent pendingIntent = PendingIntent.getActivity(mService, 0, channelListIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        mService.startForeground(NOTIFICATION_ID, notification);
        return notification;
    }

    public interface OnActionListener {
        public void onMuteToggled();

        public void onDeafenToggled();
    }
}
