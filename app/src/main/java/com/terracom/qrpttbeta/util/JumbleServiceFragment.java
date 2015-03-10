package com.terracom.qrpttbeta.util;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.terracom.jumble.IJumbleObserver;
import com.terracom.jumble.IJumbleService;

public abstract class JumbleServiceFragment extends Fragment {

    private JumbleServiceProvider mServiceProvider;

    private boolean mBound;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mServiceProvider = (JumbleServiceProvider) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement JumbleServiceProvider");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mServiceProvider.addServiceFragment(this);
        if (mServiceProvider.getService() != null && !mBound)
            onServiceAttached(mServiceProvider.getService());
    }

    @Override
    public void onDestroy() {
        mServiceProvider.removeServiceFragment(this);
        if (mServiceProvider.getService() != null && mBound)
            onServiceDetached(mServiceProvider.getService());
        super.onDestroy();
    }

    public void onServiceBound(IJumbleService service) {
    }

    public IJumbleObserver getServiceObserver() {
        return null;
    }

    private void onServiceAttached(IJumbleService service) {
        mBound = true;
        try {
            if (getServiceObserver() != null)
                service.registerObserver(getServiceObserver());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        onServiceBound(service);
    }

    private void onServiceDetached(IJumbleService service) {
        mBound = false;
        try {
            if (getServiceObserver() != null)
                service.unregisterObserver(getServiceObserver());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void setServiceBound(boolean bound) {
        if (bound && !mBound)
            onServiceAttached(mServiceProvider.getService());
        else if (mBound && !bound)
            onServiceDetached(mServiceProvider.getService());
    }

    public IJumbleService getService() {
        return mServiceProvider.getService();
    }
}
