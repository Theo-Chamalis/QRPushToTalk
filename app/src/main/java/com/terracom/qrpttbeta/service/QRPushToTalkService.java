package com.terracom.qrpttbeta.service;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;

import com.terracom.jumble.Constants;
import com.terracom.jumble.JumbleService;
import com.terracom.jumble.model.Message;
import com.terracom.jumble.model.User;
import com.terracom.jumble.util.JumbleException;
import com.terracom.jumble.util.JumbleObserver;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.Settings;
import com.terracom.qrpttbeta.service.ipc.TalkBroadcastReceiver;

public class QRPushToTalkService extends JumbleService implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        QRPushToTalkNotification.OnActionListener,
        QRPushToTalkReconnectNotification.OnActionListener {

    public static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;
    public static final int TTS_THRESHOLD = 250;
    public static final int RECONNECT_DELAY = 10000;

    private QRPushToTalkBinder mBinder = new QRPushToTalkBinder();
    private Settings mSettings;
    private QRPushToTalkNotification mNotification;
    private QRPushToTalkReconnectNotification mReconnectNotification;
    private QRPushToTalkOverlay mChannelOverlay;
    private PowerManager.WakeLock mProximityLock;
    private boolean mPTTSoundEnabled;
    private boolean mErrorShown;

    private TextToSpeech mTTS;
    private TextToSpeech.OnInitListener mTTSInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.ERROR)
                log(Message.Type.WARNING, getString(R.string.tts_failed));
        }
    };

    private QRPushToTalkHotCorner mHotCorner;
    private QRPushToTalkHotCorner.QRPushToTalkHotCornerListener mHotCornerListener = new QRPushToTalkHotCorner.QRPushToTalkHotCornerListener() {
        @Override
        public void onHotCornerDown() {
            try {
                mBinder.setTalkingState(!mSettings.isPushToTalkToggle() || !mBinder.isTalking());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onHotCornerUp() {
            if (!mSettings.isPushToTalkToggle()) {
                try {
                    mBinder.setTalkingState(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private BroadcastReceiver mTalkReceiver;

    private JumbleObserver mObserver = new JumbleObserver() {

        @Override
        public void onConnecting() throws RemoteException {
            if (mReconnectNotification != null) {
                mReconnectNotification.hide();
                mReconnectNotification = null;
            }

            mNotification = QRPushToTalkNotification.showForeground(QRPushToTalkService.this,
                    getString(R.string.qrpttConnecting),
                    getString(R.string.connecting),
                    QRPushToTalkService.this);
            mErrorShown = false;
        }

        @Override
        public void onConnected() throws RemoteException {
            if (mNotification != null) {
                mNotification.setCustomTicker(getString(R.string.qrpttConnected));
                mNotification.setCustomContentText(getString(R.string.connected));
                mNotification.setActionsShown(true);
                mNotification.show();
            }
        }

        @Override
        public void onDisconnected(JumbleException e) throws RemoteException {
            if (mNotification != null) {
                mNotification.hide();
                mNotification = null;
            }
            if (e != null) {
                mReconnectNotification =
                        QRPushToTalkReconnectNotification.show(QRPushToTalkService.this, e.getMessage(),
                                getBinder().isReconnecting(),
                                QRPushToTalkService.this);
            }
        }

        @Override
        public void onUserConnected(User user) throws RemoteException {
            if (user.getTextureHash() != null &&
                    user.getTexture() == null) {
                getBinder().requestAvatar(user.getSession());
            }
        }

        @Override
        public void onUserStateUpdated(User user) throws RemoteException {
            if (user.getSession() == mBinder.getSession()) {
                mSettings.setMutedAndDeafened(user.isSelfMuted(), user.isSelfDeafened());
                if (mNotification != null) {
                    String contentText;
                    if (user.isSelfMuted() && user.isSelfDeafened())
                        contentText = getString(R.string.status_notify_muted_and_deafened);
                    else if (user.isSelfMuted())
                        contentText = getString(R.string.status_notify_muted);
                    else
                        contentText = getString(R.string.connected);
                    mNotification.setCustomContentText(contentText);
                    mNotification.show();
                }
            }

            if (user.getTextureHash() != null &&
                    user.getTexture() == null) {
                getBinder().requestAvatar(user.getSession());
            }
        }

        @Override
        public void onMessageLogged(Message message) throws RemoteException {
            String strippedMessage = message.getMessage().replaceAll("<[^>]*>", "");

            if (message.getType() == Message.Type.TEXT_MESSAGE) {
                String formattedMessage = getString(R.string.notification_message,
                        message.getActorName(), strippedMessage);

                if (mSettings.isChatNotifyEnabled() && mNotification != null) {
                    mNotification.addMessage(formattedMessage);
                    mNotification.show();
                }

                if (mSettings.isTextToSpeechEnabled() &&
                        mTTS != null &&
                        message.getType() == Message.Type.TEXT_MESSAGE &&
                        strippedMessage.length() <= TTS_THRESHOLD &&
                        getBinder().getSessionUser() != null &&
                        !getBinder().getSessionUser().isSelfDeafened()) {
                    mTTS.speak(formattedMessage, TextToSpeech.QUEUE_ADD, null);
                }
            }
        }

        @Override
        public void onPermissionDenied(String reason) throws RemoteException {
            if (mSettings.isChatNotifyEnabled() &&
                    mNotification != null) {
                mNotification.setCustomTicker(reason);
                mNotification.show();
            }
        }

        @Override
        public void onUserTalkStateUpdated(User user) throws RemoteException {
            if (isConnected() &&
                    mBinder.getSession() == user.getSession() &&
                    mBinder.getTransmitMode() == Constants.TRANSMIT_PUSH_TO_TALK &&
                    user.getTalkState() == User.TalkState.TALKING &&
                    mPTTSoundEnabled) {
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, -1);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            getBinder().registerObserver(mObserver);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mSettings = Settings.getInstance(this);
        mPTTSoundEnabled = mSettings.isPttSoundEnabled();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        setTheme(R.style.Theme_QRPushToTalk);

        mChannelOverlay = new QRPushToTalkOverlay(this);
        mHotCorner = new QRPushToTalkHotCorner(this, mSettings.getHotCornerGravity(), mHotCornerListener);

        if (mSettings.isTextToSpeechEnabled())
            mTTS = new TextToSpeech(this, mTTSInitListener);

        mTalkReceiver = new TalkBroadcastReceiver(getBinder());
    }

    @Override
    public void onDestroy() {
        if (mNotification != null) {
            mNotification.hide();
            mNotification = null;
        }
        if (mReconnectNotification != null) {
            mReconnectNotification.hide();
            mReconnectNotification = null;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        try {
            unregisterReceiver(mTalkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        try {
            getBinder().unregisterObserver(mObserver);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (mTTS != null) mTTS.shutdown();
        super.onDestroy();
    }

    @Override
    public void onConnectionEstablished() {
        super.onConnectionEstablished();
        if (mSettings.isMuted() || mSettings.isDeafened()) {
            try {
                getBinder().setSelfMuteDeafState(mSettings.isMuted(), mSettings.isDeafened());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onConnectionSynchronized() {
        super.onConnectionSynchronized();

        registerReceiver(mTalkReceiver, new IntentFilter(TalkBroadcastReceiver.BROADCAST_TALK));

        if (mSettings.isHotCornerEnabled()) {
            mHotCorner.setShown(true);
        }
        if (mSettings.isHandsetMode()) {
            setProximitySensorOn(true);
        }
    }

    @Override
    public void onConnectionDisconnected(JumbleException e) {
        super.onConnectionDisconnected(e);
        try {
            unregisterReceiver(mTalkReceiver);
        } catch (IllegalArgumentException iae) {
        }

        mChannelOverlay.hide();

        mHotCorner.setShown(false);

        setProximitySensorOn(false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Settings.PREF_INPUT_METHOD:
                int inputMethod = mSettings.getJumbleInputMethod();
                try {
                    if (isConnected()) {
                        getBinder().setTransmitMode(inputMethod);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                mChannelOverlay.setPushToTalkShown(inputMethod == Constants.TRANSMIT_PUSH_TO_TALK);
                break;
            case Settings.PREF_HANDSET_MODE:
                setProximitySensorOn(isConnected() && mSettings.isHandsetMode());
                break;
            case Settings.PREF_THRESHOLD:
                try {
                    if (isConnected()) {
                        getBinder().setVADThreshold(mSettings.getDetectionThreshold());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case Settings.PREF_HOT_CORNER_KEY:
                mHotCorner.setGravity(mSettings.getHotCornerGravity());
                mHotCorner.setShown(isConnected() && mSettings.isHotCornerEnabled());
                break;
            case Settings.PREF_USE_TTS:
                if (mTTS == null && mSettings.isTextToSpeechEnabled())
                    mTTS = new TextToSpeech(this, mTTSInitListener);
                else if (mTTS != null && !mSettings.isTextToSpeechEnabled()) {
                    mTTS.shutdown();
                    mTTS = null;
                }
                break;
            case Settings.PREF_AMPLITUDE_BOOST:
                try {
                    if (isConnected()) {
                        getBinder().setAmplitudeBoost(mSettings.getAmplitudeBoostMultiplier());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case Settings.PREF_HALF_DUPLEX:
                try {
                    if (isConnected()) {
                        getBinder().setHalfDuplex(mSettings.isHalfDuplex());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case Settings.PREF_PTT_SOUND:
                mPTTSoundEnabled = mSettings.isPttSoundEnabled();
                break;
        }
    }

    private void setProximitySensorOn(boolean on) {
        if (on) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            mProximityLock = pm.newWakeLock(PROXIMITY_SCREEN_OFF_WAKE_LOCK, "qrptt_proximity");
            mProximityLock.acquire();
        } else {
            if (mProximityLock != null) mProximityLock.release();
            mProximityLock = null;
        }
    }

    @Override
    public QRPushToTalkBinder getBinder() {
        return mBinder;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onMuteToggled() {
        try {
            User user = mBinder.getSessionUser();
            if (isConnected() && user != null) {
                boolean muted = !user.isSelfMuted();
                boolean deafened = user.isSelfDeafened() && muted;
                mBinder.setSelfMuteDeafState(muted, deafened);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeafenToggled() {
        try {
            User user = mBinder.getSessionUser();
            if (isConnected() && user != null) {
                mBinder.setSelfMuteDeafState(!user.isSelfDeafened(), !user.isSelfDeafened());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReconnectNotificationDismissed() {
        mErrorShown = true;
    }

    @Override
    public void reconnect() {
        connect();
    }

    @Override
    public void cancelReconnect() {
        try {
            mBinder.cancelReconnect();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public class QRPushToTalkBinder extends JumbleBinder {

        public void clearChatNotifications() throws RemoteException {
            if (mNotification != null) {
                mNotification.clearMessages();
                mNotification.show();
            }
        }

        public void markErrorShown() {
            mErrorShown = true;
        }

        public boolean isErrorShown() {
            return mErrorShown;
        }

        public void cancelReconnect() throws RemoteException {
            if (mReconnectNotification != null) {
                mReconnectNotification.hide();
                mReconnectNotification = null;
            }

            super.cancelReconnect();
        }
    }
}
