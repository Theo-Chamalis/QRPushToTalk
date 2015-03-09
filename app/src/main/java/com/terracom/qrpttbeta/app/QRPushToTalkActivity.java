package com.terracom.qrpttbeta.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.terracom.jumble.IJumbleService;
import com.terracom.jumble.JumbleService;
import com.terracom.jumble.model.Server;
import com.terracom.jumble.util.JumbleException;
import com.terracom.jumble.util.JumbleObserver;
import com.terracom.jumble.util.MumbleURLParser;
import com.terracom.jumble.util.ParcelableByteArray;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.Settings;
import com.terracom.qrpttbeta.channel.ChannelFragment;
import com.terracom.qrpttbeta.channel.ServerInfoFragment;
import com.terracom.qrpttbeta.db.DatabaseProvider;
import com.terracom.qrpttbeta.db.QRPushToTalkDatabase;
import com.terracom.qrpttbeta.db.QRPushToTalkSQLiteDatabase;
import com.terracom.qrpttbeta.db.PublicServer;
import com.terracom.qrpttbeta.preference.QRPushToTalkCertificateGenerateTask;
import com.terracom.qrpttbeta.preference.Preferences;
import com.terracom.qrpttbeta.servers.FavouriteServerListFragment;
import com.terracom.qrpttbeta.servers.ServerEditFragment;
import com.terracom.qrpttbeta.service.QRPushToTalkService;
import com.terracom.qrpttbeta.util.JumbleServiceFragment;
import com.terracom.qrpttbeta.util.JumbleServiceProvider;
import com.terracom.qrpttbeta.util.QRPushToTalkTrustStore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class QRPushToTalkActivity extends ActionBarActivity implements ListView.OnItemClickListener,
        FavouriteServerListFragment.ServerConnectHandler, JumbleServiceProvider, DatabaseProvider,
        SharedPreferences.OnSharedPreferenceChangeListener, DrawerAdapter.DrawerDataProvider,
        ServerEditFragment.ServerEditListener {

    public static final String EXTRA_DRAWER_FRAGMENT = "drawer_fragment";

    public static final String LastChannelPreference = "lastchannelloggedprefs";
    public static String nameOfSavedLastLoggedChannel = "";

    private QRPushToTalkService.QRPushToTalkBinder mService;
    private QRPushToTalkDatabase mDatabase;
    private Settings mSettings;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private DrawerAdapter mDrawerAdapter;

    private ProgressDialog mConnectingDialog;
    private AlertDialog mErrorDialog;
    private AlertDialog.Builder mDisconnectPromptBuilder;

    private List<JumbleServiceFragment> mServiceFragments = new ArrayList<JumbleServiceFragment>();

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = (QRPushToTalkService.QRPushToTalkBinder) service;
            try {
                mService.registerObserver(mObserver);
                mService.clearChatNotifications();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mDrawerAdapter.notifyDataSetChanged();

            for (JumbleServiceFragment fragment : mServiceFragments)
                fragment.setServiceBound(true);

            try {
                if (getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof JumbleServiceFragment &&
                        mService.getConnectionState() != JumbleService.STATE_CONNECTED) {
                    loadDrawerFragment(DrawerAdapter.ITEM_FAVOURITES);
                }
                updateConnectionState(getService());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private JumbleObserver mObserver = new JumbleObserver() {
        @Override
        public void onConnected() throws RemoteException {
            loadDrawerFragment(DrawerAdapter.ITEM_SERVER);
            mDrawerAdapter.notifyDataSetChanged();
            supportInvalidateOptionsMenu();

            updateConnectionState(getService());
        }

        @Override
        public void onConnecting() throws RemoteException {
            updateConnectionState(getService());
        }

        @Override
        public void onDisconnected(JumbleException e) throws RemoteException {
            if (getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof JumbleServiceFragment) {
                loadDrawerFragment(DrawerAdapter.ITEM_FAVOURITES);
            }
            mDrawerAdapter.notifyDataSetChanged();
            supportInvalidateOptionsMenu();

            updateConnectionState(getService());
        }

        @Override
        public void onTLSHandshakeFailed(ParcelableByteArray cert) throws RemoteException {
            byte[] certBytes = cert.getBytes();
            final Server lastServer = getService().getConnectedServer();

            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                final X509Certificate x509 = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));

                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-1");
                    byte[] certDigest = digest.digest(x509.getEncoded());

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                try {
                    String alias = lastServer.getHost();
                    KeyStore trustStore = QRPushToTalkTrustStore.getTrustStore(QRPushToTalkActivity.this);
                    trustStore.setCertificateEntry(alias, x509);
                    QRPushToTalkTrustStore.saveTrustStore(QRPushToTalkActivity.this, trustStore);
                    connectToServer(lastServer);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(QRPushToTalkActivity.this, R.string.trust_add_failed + "Exit from the app and try again", Toast.LENGTH_LONG).show();
                }
            } catch (CertificateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPermissionDenied(String reason) throws RemoteException {
            AlertDialog.Builder adb = new AlertDialog.Builder(QRPushToTalkActivity.this);
            adb.setTitle(R.string.perm_denied);
            adb.setMessage(reason);
            adb.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSettings = Settings.getInstance(this);
        setTheme(mSettings.getTheme());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setStayAwake(mSettings.shouldStayAwake());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        mDatabase = new QRPushToTalkSQLiteDatabase(this);
        mDatabase.open();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setOnItemClickListener(this);
        mDrawerAdapter = new DrawerAdapter(this, this);
        mDrawerList.setAdapter(mDrawerAdapter);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);

                try {
                    if (getService() != null && getService().getConnectionState() == JumbleService.STATE_CONNECTED && getService().isTalking() && !mSettings.isPushToTalkToggle()) {
                        getService().setTalkingState(false);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        int iconColor = getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimaryInverse}).getColor(0, -1);
        Drawable logo = getResources().getDrawable(R.drawable.ic_home);
        logo.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
        getSupportActionBar().setLogo(logo);

        AlertDialog.Builder dadb = new AlertDialog.Builder(this);
        dadb.setMessage(R.string.disconnectSure);
        dadb.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (mService != null && mService.getConnectionState() == JumbleService.STATE_CONNECTED)
                        mService.disconnect();
                    loadDrawerFragment(DrawerAdapter.ITEM_FAVOURITES);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        dadb.setNegativeButton(android.R.string.cancel, null);
        mDisconnectPromptBuilder = dadb;

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().hasExtra(EXTRA_DRAWER_FRAGMENT)) {
                loadDrawerFragment(getIntent().getIntExtra(EXTRA_DRAWER_FRAGMENT,
                        DrawerAdapter.ITEM_FAVOURITES));
            } else {
                loadDrawerFragment(DrawerAdapter.ITEM_FAVOURITES);
            }
        }

        if (getIntent() != null &&
                Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            String url = getIntent().getDataString();
            try {
                Server server = MumbleURLParser.parseURL(url);
                Bundle args = new Bundle();
                args.putBoolean("save", false);
                args.putParcelable("server", server);
                ServerEditFragment fragment = (ServerEditFragment) ServerEditFragment.instantiate(this, ServerEditFragment.class.getName(), args);
                fragment.show(getSupportFragmentManager(), "url_edit");
            } catch (MalformedURLException e) {
                Toast.makeText(this, getString(R.string.mumble_url_parse_failed), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        if (mSettings.isFirstRun()) showSetupWizard();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent connectIntent = new Intent(this, QRPushToTalkService.class);
        bindService(connectIntent, mConnection, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mErrorDialog != null)
            mErrorDialog.dismiss();
        if (mConnectingDialog != null)
            mConnectingDialog.dismiss();
        if (mService != null)
            try {
                for (JumbleServiceFragment fragment : mServiceFragments)
                    fragment.setServiceBound(false);
                mService.unregisterObserver(mObserver);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        unbindService(mConnection);
    }

    @Override
    protected void onDestroy() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        mDatabase.close();


        if (!ServerEditFragment.CompanyNameStr.equals("")) {
            SharedPreferences mysettings = getSharedPreferences(LastChannelPreference, 0);
            SharedPreferences.Editor myEditor = mysettings.edit();
            myEditor.putString("LastChannel", ServerEditFragment.CompanyNameStr);
            myEditor.commit();
        }


        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem disconnectButton = menu.findItem(R.id.action_disconnect);
        disconnectButton.setVisible(false);

        int foregroundColor = getSupportActionBar().getThemedContext()
                .obtainStyledAttributes(new int[]{android.R.attr.textColor})
                .getColor(0, -1);
        for (int x = 0; x < menu.size(); x++) {
            MenuItem item = menu.getItem(x);
            if (item.getIcon() != null) {
                Drawable icon = item.getIcon().mutate(); // Mutate the icon so that the color filter is exclusive to the action bar
                icon.setColorFilter(foregroundColor, PorterDuff.Mode.MULTIPLY);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.qrptt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case R.id.action_disconnect:
                try {
                    getService().disconnect();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;
        }

        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (Settings.ARRAY_INPUT_METHOD_PTT.equals(mSettings.getInputMethod()) &&
                    keyCode == mSettings.getPushToTalkKey() &&
                    mService != null &&
                    mService.getConnectionState() == JumbleService.STATE_CONNECTED) {
                if (!mService.isTalking() && !mSettings.isPushToTalkToggle()) {
                    mService.setTalkingState(true);
                }
                return true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        try {
            if (Settings.ARRAY_INPUT_METHOD_PTT.equals(mSettings.getInputMethod()) &&
                    keyCode == mSettings.getPushToTalkKey() &&
                    mService != null &&
                    mService.getConnectionState() == JumbleService.STATE_CONNECTED) {
                if (!mSettings.isPushToTalkToggle() && mService.isTalking()) {
                    mService.setTalkingState(false);
                } else {
                    mService.setTalkingState(!mService.isTalking());
                }
                return true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        try {
            if (mService != null && mService.getConnectionState() == JumbleService.STATE_CONNECTED) {
                moveTaskToBack(true);
                return;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mDrawerLayout.closeDrawers();
        loadDrawerFragment((int) id);
    }

    private void showSetupWizard() {
        if (mSettings.isUsingCertificate()) return;
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.first_run_generate_certificate_title);
        adb.setMessage(R.string.first_run_generate_certificate);
        adb.setPositiveButton(R.string.generate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                QRPushToTalkCertificateGenerateTask generateTask = new QRPushToTalkCertificateGenerateTask(QRPushToTalkActivity.this) {
                    @Override
                    protected void onPostExecute(File result) {
                        super.onPostExecute(result);
                        if (result != null) mSettings.setCertificatePath(result.getAbsolutePath());
                    }
                };
                generateTask.execute();
            }
        });
        QRPushToTalkCertificateGenerateTask generateTask = new QRPushToTalkCertificateGenerateTask(QRPushToTalkActivity.this) {
            @Override
            protected void onPostExecute(File result) {
                super.onPostExecute(result);
                if (result != null) mSettings.setCertificatePath(result.getAbsolutePath());
            }
        };
        generateTask.execute();
        mSettings.setFirstRun(false);
    }

    private void loadDrawerFragment(int fragmentId) {
        Class<? extends Fragment> fragmentClass = null;
        Bundle args = new Bundle();
        switch (fragmentId) {
            case DrawerAdapter.ITEM_SERVER:
                fragmentClass = ChannelFragment.class;
                break;
            case DrawerAdapter.ITEM_INFO:
                fragmentClass = ServerInfoFragment.class;
                break;
            case DrawerAdapter.ITEM_ACCESS_TOKENS:
                try {
                    if (mService.getConnectionState() == JumbleService.STATE_CONNECTED) {
                        mService.disconnect();
                        loadDrawerFragment(DrawerAdapter.ITEM_FAVOURITES);
                    } else {
                        loadDrawerFragment(DrawerAdapter.ITEM_FAVOURITES);
                        Toast.makeText(QRPushToTalkActivity.this, "You are not connected!", Toast.LENGTH_LONG).show();
                    }
                } catch (RemoteException e) {
                    loadDrawerFragment(DrawerAdapter.ITEM_FAVOURITES);
                    e.printStackTrace();
                }
                fragmentClass = FavouriteServerListFragment.class;
                break;
            case DrawerAdapter.ITEM_PINNED_CHANNELS:
                fragmentClass = ChannelFragment.class;
                args.putBoolean("pinned", true);
                break;
            case DrawerAdapter.ITEM_FAVOURITES:
                fragmentClass = FavouriteServerListFragment.class;
                break;
            case DrawerAdapter.ITEM_PUBLIC:
                try {
                    if (mService.getConnectionState() == JumbleService.STATE_CONNECTED) {
                        mService.disconnect();
                        finish();
                        fragmentClass = ChannelFragment.class;
                    } else {
                        finish();
                        fragmentClass = FavouriteServerListFragment.class;
                    }
                } catch (Exception e) {
                    finish();
                    fragmentClass = FavouriteServerListFragment.class;
                    e.printStackTrace();
                }

                break;
            case DrawerAdapter.ITEM_SETTINGS:
                Intent prefIntent = new Intent(this, Preferences.class);
                startActivity(prefIntent);
                return;
            default:
                return;
        }
        Fragment fragment = Fragment.instantiate(this, fragmentClass.getName(), args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment, fragmentClass.getName())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        setTitle(mDrawerAdapter.getItemWithId(fragmentId).title);
    }

    public void connectToServer(final Server server) {
        try {
            if (mService != null && mService.getConnectionState() == JumbleService.STATE_CONNECTED) {
                if (FavouriteServerListFragment.myEditedflag) {
                    try {
                        mService.registerObserver(new JumbleObserver() {
                            @Override
                            public void onDisconnected(JumbleException e) throws RemoteException {
                                connectToServer(server);
                                mService.unregisterObserver(this);
                            }
                        });
                        mService.disconnect();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    FavouriteServerListFragment.setEditedUsernameFlag(false);
                }

                loadDrawerFragment(DrawerAdapter.ITEM_SERVER);
                return;

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        SharedPreferences mysettings = getSharedPreferences(LastChannelPreference, 0);
        nameOfSavedLastLoggedChannel = mysettings.getString("LastChannel", "");


        ServerConnectTask connectTask = new ServerConnectTask(this, mDatabase);
        connectTask.execute(server);
    }

    public void connectToPublicServer(final PublicServer server) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        final Settings settings = Settings.getInstance(this);
        final EditText usernameField = new EditText(this);
        usernameField.setHint(settings.getDefaultUsername());
        alertBuilder.setView(usernameField);

        alertBuilder.setTitle(R.string.connectToServer);

        alertBuilder.setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PublicServer newServer = server;
                if (!usernameField.getText().toString().equals(""))
                    newServer.setUsername(usernameField.getText().toString());
                else
                    newServer.setUsername(settings.getDefaultUsername());
                connectToServer(newServer);
            }
        });

        alertBuilder.show();
    }

    private void setStayAwake(boolean stayAwake) {
        if (stayAwake) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void updateConnectionState(IJumbleService service) throws RemoteException {
        if (mConnectingDialog != null)
            mConnectingDialog.dismiss();
        if (mErrorDialog != null)
            mErrorDialog.dismiss();

        switch (mService.getConnectionState()) {
            case JumbleService.STATE_CONNECTING:
                Server server = service.getConnectedServer();
                mConnectingDialog = new ProgressDialog(this);
                mConnectingDialog.setIndeterminate(true);
                mConnectingDialog.setCancelable(true);
                mConnectingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        try {
                            mService.disconnect();
                            Toast.makeText(QRPushToTalkActivity.this, R.string.cancelled,
                                    Toast.LENGTH_SHORT).show();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                mConnectingDialog.setMessage(getString(R.string.connecting_to_server, server.getHost(),
                        server.getPort()));
                mConnectingDialog.show();
                break;
            case JumbleService.STATE_CONNECTION_LOST:
                if (!getService().isErrorShown()) {
                    JumbleException error = getService().getConnectionError();
                    AlertDialog.Builder ab = new AlertDialog.Builder(QRPushToTalkActivity.this);
                    ab.setTitle(R.string.connectionRefused);
                    if (mService.isReconnecting()) {
                        ab.setMessage(getString(R.string.attempting_reconnect, error.getMessage()));
                        ab.setPositiveButton(R.string.cancel_reconnect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (getService() != null) {
                                    try {
                                        getService().cancelReconnect();
                                        getService().markErrorShown();
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } else {
                        ab.setMessage(error.getMessage());
                        ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (getService() != null)
                                    getService().markErrorShown();
                            }
                        });
                    }
                    ab.setCancelable(false);
                    mErrorDialog = ab.show();
                }
                break;
        }
    }

    @Override
    public void serverInfoUpdated() {
        loadDrawerFragment(DrawerAdapter.ITEM_FAVOURITES);
    }

    @Override
    public QRPushToTalkService.QRPushToTalkBinder getService() {
        return mService;
    }

    @Override
    public QRPushToTalkDatabase getDatabase() {
        return mDatabase;
    }

    @Override
    public void addServiceFragment(JumbleServiceFragment fragment) {
        mServiceFragments.add(fragment);
    }

    @Override
    public void removeServiceFragment(JumbleServiceFragment fragment) {
        mServiceFragments.remove(fragment);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Settings.PREF_THEME.equals(key)) {
            if (Build.VERSION.SDK_INT >= 11)
                recreate();
            else {
                Intent intent = new Intent(this, QRPushToTalkActivity.class);
                finish();
                startActivity(intent);
            }
        } else if (Settings.PREF_STAY_AWAKE.equals(key)) {
            setStayAwake(mSettings.shouldStayAwake());
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return mService != null && mService.getConnectionState() == JumbleService.STATE_CONNECTED;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getConnectedServerName() {
        try {
            if (mService != null && mService.getConnectionState() == JumbleService.STATE_CONNECTED) {
                Server server = mService.getConnectedServer();
                return server.getName().equals("") ? server.getHost() : server.getName();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
