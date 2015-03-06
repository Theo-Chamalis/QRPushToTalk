package com.terracom.qrpttbeta.channel;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.terracom.jumble.IJumbleService;
import com.terracom.jumble.JumbleService;
import com.terracom.jumble.net.JumbleUDPMessageType;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.util.JumbleServiceFragment;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerInfoFragment extends JumbleServiceFragment {

    private static final int POLL_RATE = 1000;

    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private TextView mProtocolView;
    private TextView mOSVersionView;
    private TextView mTCPLatencyView;
    private TextView mUDPLatencyView;
    private TextView mHostView;
    private TextView mCodecView;
    private TextView mMaxBandwidthView;
    private TextView mCurrentBandwidthView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_info, container, false);
        mProtocolView = (TextView) view.findViewById(R.id.server_info_protocol);
        mOSVersionView = (TextView) view.findViewById(R.id.server_info_os_version);
        mTCPLatencyView = (TextView) view.findViewById(R.id.server_info_tcp_latency);
        mUDPLatencyView = (TextView) view.findViewById(R.id.server_info_udp_latency);
        mHostView = (TextView) view.findViewById(R.id.server_info_host);
        mMaxBandwidthView = (TextView) view.findViewById(R.id.server_info_max_bandwidth);
        mCurrentBandwidthView = (TextView) view.findViewById(R.id.server_info_current_bandwidth);
        mCodecView = (TextView) view.findViewById(R.id.server_info_codec);
        return view;
    }

    public void updateData() throws RemoteException {
        if(getService() == null || getService().getConnectionState() != JumbleService.STATE_CONNECTED)
            return;

        mProtocolView.setText(getString(R.string.server_info_protocol, getService().getServerRelease()));
        mOSVersionView.setText(getString(R.string.server_info_version, getService().getServerOSName(), getService().getServerOSVersion()));
        mTCPLatencyView.setText(getString(R.string.server_info_latency, (float)getService().getTCPLatency()*Math.pow(10, -3)));
        mUDPLatencyView.setText(getString(R.string.server_info_latency, (float)getService().getUDPLatency()*Math.pow(10, -3)));
        mHostView.setText(getString(R.string.server_info_host, getService().getConnectedServer().getHost(), getService().getConnectedServer().getPort()));

        String codecName;
        JumbleUDPMessageType codecType = JumbleUDPMessageType.values()[getService().getCodec()];
        switch (codecType) {
            case UDPVoiceOpus:
                codecName = "Opus";
                break;
            case UDPVoiceCELTBeta:
                codecName = "CELT 0.11.0";
                break;
            case UDPVoiceCELTAlpha:
                codecName = "CELT 0.7.0";
                break;
            case UDPVoiceSpeex:
                codecName = "Speex";
                break;
            default:
                codecName = "???";
        }

        mMaxBandwidthView.setText(getString(R.string.server_info_max_bandwidth, (float)getService().getMaxBandwidth()/1000f));
        mCurrentBandwidthView.setText(getString(R.string.server_info_current_bandwidth, (float)getService().getCurrentBandwidth()/1000f));
        mCodecView.setText(getString(R.string.server_info_codec, codecName));
    }

    @Override
    public void onServiceBound(IJumbleService service) {
        mExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(!isDetached()) updateData();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 0, POLL_RATE, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDestroy() {
        mExecutorService.shutdownNow();
        super.onDestroy();
    }
}
