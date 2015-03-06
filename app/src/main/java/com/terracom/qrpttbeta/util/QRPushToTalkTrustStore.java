package com.terracom.qrpttbeta.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class QRPushToTalkTrustStore {

    private static final String STORE_FILE = "qrptt-store.bks";
    private static final String STORE_PASS = "";
    private static final String STORE_FORMAT = "BKS";

    public static KeyStore getTrustStore(Context context) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        KeyStore store = KeyStore.getInstance(STORE_FORMAT);
        try {
            FileInputStream fis = context.openFileInput(STORE_FILE);
            store.load(fis, STORE_PASS.toCharArray());
            fis.close();
        } catch (FileNotFoundException e) {
            store.load(null, null);
        }
        return store;
    }

    public static void saveTrustStore(Context context, KeyStore store) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        FileOutputStream fos = context.openFileOutput(STORE_FILE, Context.MODE_PRIVATE);
        store.store(fos, STORE_PASS.toCharArray());
        fos.close();
    }

    public static void clearTrustStore(Context context) {
        context.deleteFile(STORE_FILE);
    }

    public static String getTrustStorePath(Context context) {
        File trustPath = new File(context.getFilesDir(), STORE_FILE);
        if(trustPath.exists()) return trustPath.getAbsolutePath();
        return null;
    }

    public static String getTrustStoreFormat() {
        return STORE_FORMAT;
    }

    public static String getTrustStorePassword() {
        return STORE_PASS;
    }
}
