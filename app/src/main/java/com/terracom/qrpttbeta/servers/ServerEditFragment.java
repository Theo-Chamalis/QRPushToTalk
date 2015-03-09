package com.terracom.qrpttbeta.servers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.terracom.jumble.Constants;
import com.terracom.jumble.model.Server;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.db.DatabaseProvider;

import org.json.JSONException;
import org.json.JSONObject;

<<<<<<< HEAD
=======
import java.net.InetAddress;
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0
import java.util.Random;


public class ServerEditFragment extends DialogFragment {
    private TextView mNameTitle;
    private EditText mNameEdit;
    private EditText mHostEdit;
    private EditText mPortEdit;
    private EditText mUsernameEdit;
    private EditText mPasswordEdit;
    private TextView mErrorText;
    public String resultFromWebService = "Noresponseyet";
    public String uname = "";
    public static String CompanyNameStr = "";
    public String GuardAliasStr = "";
    public String errorResultFromJsonStr = "";
    public String errorMessageFromJsonStr = "";
    private ServerEditListener mListener;
    private DatabaseProvider mDatabaseProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mDatabaseProvider = (DatabaseProvider) activity;
            mListener = (ServerEditListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DatabaseProvider and ServerEditListener!");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mErrorText.setVisibility(View.VISIBLE);
        mErrorText.setText("Pressing Add may take some time to fetch data from server...");
        ((AlertDialog) getDialog()).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFormData();

                try {

                    if (uname.equals("")) {
                        int failedAttempts = 0;
                        while (resultFromWebService.equals("Noresponseyet") && failedAttempts < 51) {
                            Thread.sleep(100);
                            failedAttempts++;
                        }
                    } else {
                        Thread.sleep(800);
                    }

                    resultFromWebService = resultFromWebService.replace("(", "");
                    resultFromWebService = resultFromWebService.replace(")", "");
                    try {
                        JSONObject myjson = new JSONObject(resultFromWebService);
                        errorResultFromJsonStr = myjson.get("result").toString();
                        errorMessageFromJsonStr = myjson.get("Message").toString();
                        if (myjson.get("result").toString().equals("0")) {
                            JSONObject myjsondata = myjson.getJSONObject("data");
                            CompanyNameStr = myjsondata.get("CompanyName").toString().replaceAll("\\s", "").trim();
                            GuardAliasStr = myjsondata.get("GuardAlias").toString().replaceAll("\\s", "").trim();
                        } else {
                            CompanyNameStr = "";
                        }
                    } catch (JSONException e) {
                        CompanyNameStr = "";
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (validate()) {
                    Server server = createServer(shouldSave());
                    if (!shouldSave()) mListener.connectToServer(server);
                    dismiss();
                }
            }
        });
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return WebserviceGet(urls[0]);
        }

        @Override
        protected void onPostExecute(String resultAfterPost) {
        }
    }

    public String WebserviceGet(String jsonGuard) {
        String kerverosString = "https://kerveroslive.com:29406/api/?data=";
        try {
            HttpRequest req = HttpRequest.get(kerverosString + jsonGuard).trustAllCerts().trustAllHosts();
            resultFromWebService = req.trustAllCerts().trustAllHosts().body();
            resultFromWebService = resultFromWebService.replace("(", "");
            resultFromWebService = resultFromWebService.replace(")", "");
            return resultFromWebService;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        int actionTitle;
        if (shouldSave() && getServer() == null) {
            actionTitle = R.string.add;
        } else if (shouldSave()) {
            actionTitle = R.string.add;
        } else {
            actionTitle = R.string.connect;
        }

        adb.setPositiveButton(actionTitle, null);
        adb.setNegativeButton(android.R.string.cancel, null);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_server_edit, null, false);
        mNameTitle = (TextView) view.findViewById(R.id.server_edit_name_title);
        mNameEdit = (EditText) view.findViewById(R.id.server_edit_name);
        mHostEdit = (EditText) view.findViewById(R.id.server_edit_host);
        mPortEdit = (EditText) view.findViewById(R.id.server_edit_port);
        mUsernameEdit = (EditText) view.findViewById(R.id.server_edit_username);
        mPasswordEdit = (EditText) view.findViewById(R.id.server_edit_password);
        mErrorText = (TextView) view.findViewById(R.id.server_edit_error);
        if (getServer() != null) {
            Server oldServer = getServer();
            mNameEdit.setText(oldServer.getName());
            mHostEdit.setText(oldServer.getHost());
            mPortEdit.setText(String.valueOf(oldServer.getPort()));
            mUsernameEdit.setText(oldServer.getUsername());
            mPasswordEdit.setText("");
        }

        if (!shouldSave()) {
            mNameTitle.setVisibility(View.GONE);
            mNameEdit.setVisibility(View.GONE);
        }

        adb.setInverseBackgroundForced(true);
        adb.setView(view);
        return adb.create();
    }

    private boolean shouldSave() {
        return getArguments() == null || getArguments().getBoolean("save", true);
    }

    private Server getServer() {
        return getArguments() != null ? (Server) getArguments().getParcelable("server") : null;
    }

    public Server createServer(boolean shouldCommit) {
        String name = (mNameEdit).getText().toString().trim();
        String host = (mHostEdit).getText().toString().trim();

        int port;
        try {
            port = Integer.parseInt((mPortEdit).getText().toString());
        } catch (final NumberFormatException ex) {
            port = Constants.DEFAULT_PORT;
        }

        Server server;

        if (getServer() != null) {
            String desktop = getString(R.string.desktop);
            server = getServer();
            server.setName(name);
            server.setHost(host);
            server.setPort(port);
            if (!uname.equals("")) {
                server.setUsername(uname);
            } else {
                server.setUsername(GuardAliasStr);
            }
            server.setPassword(desktop);

            if (shouldCommit) mDatabaseProvider.getDatabase().updateServer(server);
        } else {
            String desktop = getString(R.string.desktop);
            if (!uname.equals("")) {
                server = new Server(-1, name, host, port, uname, desktop);
                server.setUsername(uname);
            } else {
                server = new Server(-1, name, host, port, GuardAliasStr, desktop);
            }
            if (shouldCommit) mDatabaseProvider.getDatabase().addServer(server);
        }
        if (shouldCommit) mListener.serverInfoUpdated();
        return server;
    }


    public void getFormData() {
        Random randomNum = new Random();
        int randomNumInt;
        randomNumInt = randomNum.nextInt(9999 - 1 + 1) + 1;
        final String randomNumStr = ("demo" + randomNumInt + "").trim();
        String username = (mUsernameEdit).getText().toString().trim();
        String passwordPIN = mPasswordEdit.getText().toString().trim();

        if (username.equals("demo") || username.equals(null) || username.equals("Demo") || username.equals("DEMO")) {
            username = randomNumStr;
            uname = username;
            mUsernameEdit.setText(uname);
        } else {
            JSONObject jsonGuard = new JSONObject();
            try {
                jsonGuard.put("GuardID", username);
                jsonGuard.put("GuardPIN", passwordPIN);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new HttpAsyncTask().execute(jsonGuard.toString());
        }
    }

    public boolean validate() {
        if (mUsernameEdit.getText().equals("demo") || mUsernameEdit.getText().toString().startsWith("demo")) {
            CompanyNameStr = "";
            mErrorText.setVisibility(View.GONE);
            uname = mUsernameEdit.getText().toString().trim().replaceAll("\\s", "");
            if (uname.length() == 8) {
                resultFromWebService = "Noresponseyet";
                return true;
            } else {
                final String randomNumStr = ("demo" + ((new Random()).nextInt(9999 - 1 + 1) + 1) + "").trim();
                uname = randomNumStr;
                return true;
            }
        } else if (!errorResultFromJsonStr.equals("0")) {
            mErrorText.setVisibility(View.VISIBLE);
            if (errorMessageFromJsonStr.equals("")) {
                mErrorText.setText("You are offline. Check your internet connection.");
                resultFromWebService = "Noresponseyet";
                uname = "";
                return false;
            } else {
                errorMessageFromJsonStr = errorMessageFromJsonStr.substring(14);
                mErrorText.setText(errorMessageFromJsonStr);
                resultFromWebService = "Noresponseyet";
                uname = "";
                return false;
            }
        } else {
            mErrorText.setVisibility(View.GONE);
            resultFromWebService = "Noresponseyet";
            uname = "";
            return true;
        }
    }

    public interface ServerEditListener {
        public void serverInfoUpdated();

        public void connectToServer(Server server);
    }
}

