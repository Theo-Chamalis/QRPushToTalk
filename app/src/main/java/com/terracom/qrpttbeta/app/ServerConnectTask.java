package com.terracom.qrpttbeta.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import com.terracom.jumble.JumbleService;
import com.terracom.jumble.model.Server;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.Settings;
import com.terracom.qrpttbeta.db.QRPushToTalkDatabase;
import com.terracom.qrpttbeta.service.QRPushToTalkService;
import com.terracom.qrpttbeta.util.QRPushToTalkTrustStore;

import java.util.ArrayList;

public class ServerConnectTask extends AsyncTask<Server, Void, Intent> {
    private Context mContext;
    private QRPushToTalkDatabase mDatabase;
    private Settings mSettings;

    public ServerConnectTask(Context context, QRPushToTalkDatabase database) {
        mContext = context;
        mDatabase = database;
        mSettings = Settings.getInstance(context);
    }

    @Override
    protected Intent doInBackground(Server... params) {
        Server server = params[0];
        int inputMethod = mSettings.getJumbleInputMethod();

        int audioSource = mSettings.isHandsetMode() ?
                MediaRecorder.AudioSource.DEFAULT : MediaRecorder.AudioSource.MIC;
        int audioStream = mSettings.isHandsetMode() ?
                AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC;

        String applicationVersion = "";
        try {
            applicationVersion = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Intent connectIntent = new Intent(mContext, QRPushToTalkService.class);
        connectIntent.putExtra(JumbleService.EXTRAS_SERVER, server);
        connectIntent.putExtra(JumbleService.EXTRAS_CLIENT_NAME, mContext.getString(R.string.app_name) + " " + applicationVersion);
        connectIntent.putExtra(JumbleService.EXTRAS_TRANSMIT_MODE, inputMethod);
        connectIntent.putExtra(JumbleService.EXTRAS_DETECTION_THRESHOLD, mSettings.getDetectionThreshold());
        connectIntent.putExtra(JumbleService.EXTRAS_AMPLITUDE_BOOST, mSettings.getAmplitudeBoostMultiplier());
        connectIntent.putExtra(JumbleService.EXTRAS_CERTIFICATE, mSettings.getCertificate());
        connectIntent.putExtra(JumbleService.EXTRAS_CERTIFICATE_PASSWORD, mSettings.getCertificatePassword());
        connectIntent.putExtra(JumbleService.EXTRAS_AUTO_RECONNECT, mSettings.isAutoReconnectEnabled());
        connectIntent.putExtra(JumbleService.EXTRAS_AUTO_RECONNECT_DELAY, QRPushToTalkService.RECONNECT_DELAY);
        connectIntent.putExtra(JumbleService.EXTRAS_USE_OPUS, !mSettings.isOpusDisabled());
        connectIntent.putExtra(JumbleService.EXTRAS_INPUT_RATE, mSettings.getInputSampleRate());
        connectIntent.putExtra(JumbleService.EXTRAS_INPUT_QUALITY, mSettings.getInputQuality());
        connectIntent.putExtra(JumbleService.EXTRAS_FORCE_TCP, mSettings.isTcpForced());
        connectIntent.putExtra(JumbleService.EXTRAS_USE_TOR, mSettings.isTorEnabled());
        connectIntent.putStringArrayListExtra(JumbleService.EXTRAS_ACCESS_TOKENS, (ArrayList<String>) mDatabase.getAccessTokens(server.getId()));
        connectIntent.putExtra(JumbleService.EXTRAS_AUDIO_SOURCE, audioSource);
        connectIntent.putExtra(JumbleService.EXTRAS_AUDIO_STREAM, audioStream);
        connectIntent.putExtra(JumbleService.EXTRAS_FRAMES_PER_PACKET, mSettings.getFramesPerPacket());
        connectIntent.putExtra(JumbleService.EXTRAS_TRUST_STORE, QRPushToTalkTrustStore.getTrustStorePath(mContext));
        connectIntent.putExtra(JumbleService.EXTRAS_TRUST_STORE_PASSWORD, QRPushToTalkTrustStore.getTrustStorePassword());
        connectIntent.putExtra(JumbleService.EXTRAS_TRUST_STORE_FORMAT, QRPushToTalkTrustStore.getTrustStoreFormat());
        connectIntent.putExtra(JumbleService.EXTRAS_HALF_DUPLEX, mSettings.isHalfDuplex());
        connectIntent.putExtra(JumbleService.EXTRAS_ENABLE_PREPROCESSOR, mSettings.isPreprocessorEnabled());
        if (server.isSaved()) {
            ArrayList<Integer> muteHistory = (ArrayList<Integer>) mDatabase.getLocalMutedUsers(server.getId());
            ArrayList<Integer> ignoreHistory = (ArrayList<Integer>) mDatabase.getLocalIgnoredUsers(server.getId());
            connectIntent.putExtra(JumbleService.EXTRAS_LOCAL_MUTE_HISTORY, muteHistory);
            connectIntent.putExtra(JumbleService.EXTRAS_LOCAL_IGNORE_HISTORY, ignoreHistory);
        }
        connectIntent.setAction(JumbleService.ACTION_CONNECT);
        return connectIntent;
    }

    @Override
    protected void onPostExecute(Intent intent) {
        super.onPostExecute(intent);
        mContext.startService(intent);
    }
}
