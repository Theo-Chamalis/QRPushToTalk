package com.terracom.qrpttbeta.util;

import com.terracom.jumble.IJumbleService;

public interface JumbleServiceProvider {
    public IJumbleService getService();

    public void addServiceFragment(JumbleServiceFragment fragment);

    public void removeServiceFragment(JumbleServiceFragment fragment);
}
