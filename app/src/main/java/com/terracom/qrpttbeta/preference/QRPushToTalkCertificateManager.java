package com.terracom.qrpttbeta.preference;

import android.os.Environment;

import com.terracom.jumble.net.JumbleCertificateGenerator;

import org.spongycastle.operator.OperatorCreationException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QRPushToTalkCertificateManager {

    private static final String CERTIFICATE_FOLDER = "QRPushToTalk";
    private static final String CERTIFICATE_FORMAT = "qrptt-%s.p12";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public static File generateCertificate() throws NoSuchAlgorithmException, OperatorCreationException, CertificateException, KeyStoreException, NoSuchProviderException, IOException {
        File certificateDirectory = getCertificateDirectory();

        String date = DATE_FORMAT.format(new Date());
        String certificateName = String.format(Locale.US, CERTIFICATE_FORMAT, date);
        File certificateFile = new File(certificateDirectory, certificateName);
        FileOutputStream outputStream = new FileOutputStream(certificateFile);
        JumbleCertificateGenerator.generateCertificate(outputStream);
        return certificateFile;
    }

<<<<<<< HEAD
    public static List<File> getAvailableCertificates() throws IOException {
        File certificateDirectory = getCertificateDirectory();

        File[] p12Files = certificateDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("pfx") ||
                        pathname.getName().endsWith("p12");
            }
        });

        return Arrays.asList(p12Files);
    }
=======
	public static List<File> getAvailableCertificates() throws IOException {
		File certificateDirectory = getCertificateDirectory();
		
		File[] p12Files = certificateDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith("pfx") ||
                       pathname.getName().endsWith("p12");
			}
		});
		
		return Arrays.asList(p12Files);
	}
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0

    public static boolean isPasswordRequired(File certificateFile) throws KeyStoreException, IOException, NoSuchAlgorithmException {
        KeyStore p12store = KeyStore.getInstance("PKCS12");
        FileInputStream inputStream = new FileInputStream(certificateFile);
        try {
            p12store.load(inputStream, new char[0]);
            return false; // If loading succeeded, we can be assured that no password was required.
        } catch (IOException e) {
            e.printStackTrace();
            return true; // FIXME: this is a very coarse attempt at password detection.
        } catch (CertificateException e) {
            e.printStackTrace();
            return true; // FIXME: this is a very coarse attempt at password detection.
        }
    }

    public static boolean isPasswordValid(File certificateFile, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException {
        KeyStore p12store = KeyStore.getInstance("PKCS12");
        FileInputStream inputStream = new FileInputStream(certificateFile);
        try {
            p12store.load(inputStream, password.toCharArray());
            return true; // If loading succeeded, we can be assured that the password is valid
        } catch (CertificateException e) {
            e.printStackTrace();
            return false; // FIXME: this is a very coarse attempt at password detection.
        }
    }

<<<<<<< HEAD
    public static File getCertificateDirectory() throws IOException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
=======
	public static File getCertificateDirectory() throws IOException {
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0
            File certificateDirectory = new File(Environment.getExternalStorageDirectory(), CERTIFICATE_FOLDER);
            if (!certificateDirectory.exists())
                certificateDirectory.mkdir();
            return certificateDirectory;
        } else {
            throw new IOException("External storage not available.");
        }
    }
}
