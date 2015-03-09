package com.terracom.qrpttbeta.preference;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.Settings;
import com.terracom.qrpttbeta.util.QRPushToTalkTrustStore;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import info.guardianproject.onionkit.ui.OrbotHelper;

public class Preferences extends PreferenceActivity {

    public static final String ACTION_PREFS_GENERAL = "com.terracom.qrpttbeta.app.PREFS_GENERAL";
    public static final String ACTION_PREFS_AUTHENTICATION = "com.terracom.qrpttbeta.app.PREFS_AUTHENTICATION";
    public static final String ACTION_PREFS_AUDIO = "com.terracom.qrpttbeta.app.PREFS_AUDIO";
    public static final String ACTION_PREFS_APPEARANCE = "com.terracom.qrpttbeta.app.PREFS_APPEARANCE";
    public static final String ACTION_PREFS_ABOUT = "com.terracom.qrpttbeta.app.PREFS_ABOUT";

    private static final String CERTIFICATE_GENERATE_KEY = "certificateGenerate";
    private static final String CERTIFICATE_PATH_KEY = "certificatePath";
    private static final String TRUST_CLEAR_KEY = "clearTrust";
    private static final String USE_TOR_KEY = "useTor";
    private static final String VERSION_KEY = "version";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();
        if (action != null) {
            if (ACTION_PREFS_GENERAL.equals(action)) {
                addPreferencesFromResource(R.xml.settings_general);
                configureOrbotPreferences(getPreferenceScreen());
            } else if (ACTION_PREFS_AUTHENTICATION.equals(action)) {
                addPreferencesFromResource(R.xml.settings_authentication);
                configureCertificatePreferences(getPreferenceScreen());
            } else if (ACTION_PREFS_AUDIO.equals(action)) {
                addPreferencesFromResource(R.xml.settings_audio);
                configureAudioPreferences(getPreferenceScreen());
            } else if (ACTION_PREFS_APPEARANCE.equals(action)) {
                addPreferencesFromResource(R.xml.settings_appearance);
            } else if (ACTION_PREFS_ABOUT.equals(action)) {
                addPreferencesFromResource(R.xml.settings_about);
                configureAboutPreferences(this, getPreferenceScreen());
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preference_headers_legacy);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return QRPushToTalkPreferenceFragment.class.getName().equals(fragmentName);
    }

    private static void configureCertificatePreferences(PreferenceScreen screen) {
        final Preference certificateGeneratePreference = screen.findPreference(CERTIFICATE_GENERATE_KEY);
        final ListPreference certificatePathPreference = (ListPreference) screen.findPreference(CERTIFICATE_PATH_KEY);
        final Preference trustClearPreference = screen.findPreference(TRUST_CLEAR_KEY);

        certificateGeneratePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                generateCertificate(certificatePathPreference);
                return true;
            }
        });
        certificatePathPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences preferences = preference.getSharedPreferences();
                preferences.edit()
                        .putString(Settings.PREF_CERT_PASSWORD, "")
                        .commit();

                if ("".equals(newValue)) return true;
                File cert = new File((String) newValue);
                try {
                    boolean needsPassword = QRPushToTalkCertificateManager.isPasswordRequired(cert);
                    if (!needsPassword) return true;
                    promptCertificatePassword(preference.getContext(), (ListPreference) preference, cert);

                } catch (Exception e) {
                    Toast.makeText(preference.getContext(), preference.getContext().getString(R.string.certificate_not_valid, cert.getName()), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                return false;
            }
        });
        trustClearPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                QRPushToTalkTrustStore.clearTrustStore(preference.getContext());
                Toast.makeText(preference.getContext(), R.string.trust_cleared, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                updateCertificatePath(certificatePathPreference);
            } catch (IOException exception) {
                certificatePathPreference.setEnabled(false);
                certificatePathPreference.setSummary(R.string.externalStorageUnavailable);
            }
        } else {
            certificatePathPreference.setEnabled(false);
            certificatePathPreference.setSummary(R.string.externalStorageUnavailable);
        }
    }

    private static void promptCertificatePassword(final Context context, final ListPreference certificatePreference, final File certificate) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle(R.string.certificatePassword);

        final EditText passwordField = new EditText(context);
        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        adb.setView(passwordField);

        adb.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = passwordField.getText().toString();
                boolean passwordValid = false;
                try {
                    passwordValid = QRPushToTalkCertificateManager.isPasswordValid(certificate, password);
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } finally {
                    if (passwordValid) {
                        certificatePreference.setValue(certificate.getAbsolutePath());
                        certificatePreference.getSharedPreferences().edit()
                                .putString(Settings.PREF_CERT_PASSWORD, password)
                                .commit();
                    } else {
                        Toast.makeText(context, R.string.invalid_password, Toast.LENGTH_SHORT).show();
                        promptCertificatePassword(context, certificatePreference, certificate);
                    }
                }
            }
        });
        adb.setNegativeButton(android.R.string.cancel, null);
        adb.show();
    }

    private static void configureOrbotPreferences(PreferenceScreen screen) {
        OrbotHelper orbotHelper = new OrbotHelper(screen.getContext());
        Preference useOrbotPreference = screen.findPreference(USE_TOR_KEY);
        useOrbotPreference.setEnabled(orbotHelper.isOrbotInstalled());
    }

    private static void configureAudioPreferences(final PreferenceScreen screen) {
        ListPreference inputPreference = (ListPreference) screen.findPreference(Settings.PREF_INPUT_METHOD);
        inputPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateAudioDependents(screen, (String) newValue);
                return true;
            }
        });

        ListPreference inputQualityPreference = (ListPreference) screen.findPreference(Settings.PREF_INPUT_RATE);
        String[] bitrateNames = new String[inputQualityPreference.getEntryValues().length];
        for (int x = 0; x < bitrateNames.length; x++) {
            int bitrate = Integer.parseInt(inputQualityPreference.getEntryValues()[x].toString());
            boolean supported = AudioRecord.getMinBufferSize(bitrate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) > 0;
            bitrateNames[x] = bitrate + "Hz" + (supported ? "" : " (unsupported)");
        }
        inputQualityPreference.setEntries(bitrateNames);

        updateAudioDependents(screen, inputPreference.getValue());
    }

    private static void updateAudioDependents(PreferenceScreen screen, String inputMethod) {
        PreferenceCategory pttCategory = (PreferenceCategory) screen.findPreference("ptt_settings");
        PreferenceCategory vadCategory = (PreferenceCategory) screen.findPreference("vad_settings");
        pttCategory.setEnabled(true);
        vadCategory.setEnabled(false);
    }

    private static void updateCertificatePath(ListPreference preference) throws NullPointerException, IOException {
        List<File> certificateFiles = QRPushToTalkCertificateManager.getAvailableCertificates();

        String[] certificateNames = new String[certificateFiles.size() + 1]; // Extra space for 'None' option
        String[] certificatePaths = new String[certificateFiles.size() + 1];
        for (int x = 0; x < certificateFiles.size(); x++) {
            certificateNames[x] = certificateFiles.get(x).getName();
            certificatePaths[x] = certificateFiles.get(x).getAbsolutePath();
        }

        certificateNames[certificateNames.length - 1] = preference.getContext().getString(R.string.noCert);
        certificatePaths[certificatePaths.length - 1] = "";

        preference.setEntries(certificateNames);
        preference.setEntryValues(certificatePaths);
    }

    private static void generateCertificate(final ListPreference certificateList) {
        QRPushToTalkCertificateGenerateTask generateTask = new QRPushToTalkCertificateGenerateTask(certificateList.getContext()) {
            @Override
            protected void onPostExecute(File result) {
                super.onPostExecute(result);

                if (result != null) {
                    try {
                        updateCertificatePath(certificateList);
                        certificateList.setValue(result.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        generateTask.execute();
    }

    private static void configureAboutPreferences(Context context, PreferenceScreen screen) {
        String version = "Unknown";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Preference versionPreference = screen.findPreference(VERSION_KEY);
        versionPreference.setSummary(version);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class QRPushToTalkPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            String section = getArguments().getString("settings");
            if ("general".equals(section)) {
                addPreferencesFromResource(R.xml.settings_general);
                configureOrbotPreferences(getPreferenceScreen());
            } else if ("authentication".equals(section)) {
                addPreferencesFromResource(R.xml.settings_authentication);
                configureCertificatePreferences(getPreferenceScreen());
            } else if ("audio".equals(section)) {
                addPreferencesFromResource(R.xml.settings_audio);
                configureAudioPreferences(getPreferenceScreen());
            } else if ("appearance".equals(section)) {
                addPreferencesFromResource(R.xml.settings_appearance);
            } else if ("about".equals(section)) {
                addPreferencesFromResource(R.xml.settings_about);
                configureAboutPreferences(getPreferenceScreen().getContext(), getPreferenceScreen());
            }
        }
    }
}
