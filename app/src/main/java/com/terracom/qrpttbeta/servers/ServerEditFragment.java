/*
 * Copyright (C) 2014 Andrew Comminos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.terracom.qrpttbeta.servers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.terracom.jumble.Constants;
import com.terracom.jumble.model.Server;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.Settings;
import com.terracom.qrpttbeta.app.QRPushToTalkActivity;
import com.terracom.qrpttbeta.db.DatabaseProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.List;
import java.util.Random;
import com.terracom.qrpttbeta.servers.HttpRequest;

import ch.boye.httpclientandroidlib.client.HttpClient;

public class ServerEditFragment extends DialogFragment {
    private TextView mNameTitle;
    private EditText mNameEdit;
    private EditText mHostEdit;
    private EditText mPortEdit;
    private EditText mUsernameEdit;
    private EditText mPasswordEdit;
    private TextView mErrorText;

    public String resultFromWebService = "Tipotadenmphkeakoma";
    public String uname = "";
    public String CompanyNameStr= "";
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
            throw new ClassCastException(activity.toString()+" must implement DatabaseProvider and ServerEditListener!");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mErrorText.setVisibility(View.VISIBLE);
        mErrorText.setText("Pressing Add needs approximately 5 seconds to fetch data from server...");
        // Override positive button to not automatically dismiss on press.
        // We can't accomplish this with AlertDialog.Builder.
        ((AlertDialog)getDialog()).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFormData();
                try {
                    Thread.sleep(6000);
                    //resultFromWebService = resultFromWebService.substring(1, resultFromWebService.length()-1);
                    resultFromWebService = resultFromWebService.replace("(","");
                    resultFromWebService = resultFromWebService.replace(")","");

                    Log.d("+KALO SLEEP H MOUFA???+",resultFromWebService);
                    try {
                        JSONObject jsonaqui = new JSONObject(resultFromWebService);
                        errorResultFromJsonStr = jsonaqui.get("result").toString();
                        errorMessageFromJsonStr = jsonaqui.get("Message").toString();
                        if(jsonaqui.get("result").toString().equals("0")){
                            //EMFWLEVMENO JSON
                            JSONObject jsonaquidata = jsonaqui.getJSONObject("data");
                            CompanyNameStr = jsonaquidata.get("CompanyName").toString().replaceAll("\\s","").trim();
                            GuardAliasStr = jsonaquidata.get("GuardAlias").toString().replaceAll("\\s","").trim();
                            Log.d("Jsnqui.get(\"CompanyName\")",CompanyNameStr);
                            Log.d("--Jsonaqui.get(\"data\")-",GuardAliasStr);
                        }

                        Log.d("+++++JSONAQUI???++++++",jsonaqui.toString());
                    } catch (JSONException e) {
                        Log.d("++DEN STRING SE JSON++","++++++++++++++++");
                        e.printStackTrace();
                    }

                } catch (InterruptedException e) {
                    Log.d("++DEN EPIASE TO SLEEP++","");
                    e.printStackTrace();
                }
                if (validate()) {
                    Server server = createServer(shouldSave());

                    // If we're not committing this server, connect immediately.
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

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String resultAfterPost) {
            Log.d("~~META TO POSTEXECUTE~~", resultAfterPost);
        }
    }

    public String WebserviceGet(String jsonGuard) {
        String kerverosString = "https://kerveroslive.com:29406/api/?data=";
        try{
            HttpRequest req = HttpRequest.get(kerverosString+jsonGuard).trustAllCerts().trustAllHosts();
            resultFromWebService = req.trustAllCerts().trustAllHosts().body();
            resultFromWebService = resultFromWebService.replace("(","");
            resultFromWebService = resultFromWebService.replace(")","");
            Log.d("------REQ BODY2------",resultFromWebService);
            return resultFromWebService;
        }catch(Exception e){
            Log.d("=-=-To exception is:=-=",e.getMessage());
            return e.getMessage();
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        Settings settings = Settings.getInstance(getActivity());

        int actionTitle;
        if (shouldSave() && getServer() == null) {
            actionTitle = R.string.add;
        } else if (shouldSave()) {
            //actionTitle = R.string.save;
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
        //mUsernameEdit.setHint(settings.getDefaultUsername());
        //mUsernameEdit.setHint("Insert demo here");
        mPasswordEdit = (EditText) view.findViewById(R.id.server_edit_password);
        mErrorText = (TextView) view.findViewById(R.id.server_edit_error);
        if (getServer() != null) {
            Server oldServer = getServer();
            mNameEdit.setText(oldServer.getName());
            mHostEdit.setText(oldServer.getHost());
            mPortEdit.setText(String.valueOf(oldServer.getPort()));
            mUsernameEdit.setText(oldServer.getUsername());
            //mPasswordEdit.setText(oldServer.getPassword());
            mPasswordEdit.setText("");
        }

        if (!shouldSave()) {
            mNameTitle.setVisibility(View.GONE);
            mNameEdit.setVisibility(View.GONE);
        }

        // Fixes issues with text colour on light themes with pre-honeycomb devices.
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

    /**
     * Creates or updates a server with the information in this fragment.
     * @param shouldCommit Whether to commit the created service to the DB.
     * @return The new or updated server.
     */
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
            String password="sw@gg3rmcy0l0w1tz";
            server = getServer();
            server.setName(name);
            server.setHost(host);
            server.setPort(port);
            if(!uname.equals("")){
                server.setUsername(uname);
            }else{
                server.setUsername(GuardAliasStr);
            }

            server.setPassword(password);
            if(shouldCommit) mDatabaseProvider.getDatabase().updateServer(server);
        } else {
            String password="sw@gg3rmcy0l0w1tz";
            if(!uname.equals("")){
                server = new Server(-1, name, host, port, uname, password);
                server.setUsername(uname);
            }else{
                server = new Server(-1, name, host, port, GuardAliasStr, password);
            }

            if(shouldCommit) mDatabaseProvider.getDatabase().addServer(server);
        }

        if(shouldCommit) mListener.serverInfoUpdated();

        return server;
    }


    public void getFormData(){
        Random randomNum = new Random();
        int randomNumInt;
        randomNumInt = randomNum.nextInt(9999 - 1 + 1) + 1;
        final String randomNumStr = ("demo" + randomNumInt + "").trim();
        String username = (mUsernameEdit).getText().toString().trim();
        String passwordPIN = mPasswordEdit.getText().toString().trim();


        if(username.equals("demo") || username.equals(null) || username.equals("Demo")|| username.equals("DEMO")){
            username = randomNumStr;
            uname = username;
            //mUsernameEdit.setText(username);
            mUsernameEdit.setText(uname);
        }else{
            JSONObject jsonGuard = new JSONObject();
            try {
                jsonGuard.put("GuardID",username);
                jsonGuard.put("GuardPIN",passwordPIN);
                Log.d("=====TO JSON m====",jsonGuard.toString());
                Log.d("==jsGrd.gtStr(GrdID)==",jsonGuard.getString("GuardID"));
                Log.d("==jsGrd.gtStr(GrdPIN)==",jsonGuard.getString("GuardPIN"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new HttpAsyncTask().execute(jsonGuard.toString());

        }
    }


    /**
     * Checks all fields in this ServerEditFragment for validity.
     * If an invalid field is found, an error is shown and false is returned.
     * @return true if the inputted values are valid, false otherwise.
     */
    public boolean validate() {
        String error = null;

        if (mHostEdit.getText().length() == 0) {
            error = getString(R.string.invalid_host);
        } else if (mPortEdit.getText().length() > 0) {
            try {
                int port = Integer.parseInt(mPortEdit.getText().toString());
                if (port < 0 || port > 65535) {
                    error = getString(R.string.invalid_port_range);
                }
            } catch (NumberFormatException nfe) {
                error = getString(R.string.invalid_port_range);
            }
        }

        /*if(!mUsernameEdit.getText().equals("demo") && !mUsernameEdit.getText().toString().startsWith("demo") && !mUsernameEdit.getText().equals("Demo") && !mUsernameEdit.getText().toString().startsWith("Demo") && !mUsernameEdit.getText().equals("DEMO") && !mUsernameEdit.getText().toString().startsWith("DEMO")){
            error = "Guard ID or PIN not valid!";
        }*/

        //mErrorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
        /*if (error != null) {
            mErrorText.setText(error);
            return false;
        } else {
            return true;
        }*/
        if(mUsernameEdit.getText().equals("demo") || mUsernameEdit.getText().toString().startsWith("demo")){
            mErrorText.setVisibility(View.GONE);
            uname = mUsernameEdit.getText().toString().trim().replaceAll("\\s","");
            if(uname.length() == 8){
                return true;
            }
            else{
                final String randomNumStr = ("demo" + ((new Random()).nextInt(9999 - 1 + 1) + 1) + "").trim();
                uname = randomNumStr;
                return true;
            }
        }else if(!errorResultFromJsonStr.equals("0")){
            mErrorText.setVisibility(View.VISIBLE);
            errorMessageFromJsonStr = errorMessageFromJsonStr.substring(14);
            mErrorText.setText(errorMessageFromJsonStr);
            return false;
        }else{
            mErrorText.setVisibility(View.GONE);
            return true;
        }
    }

    public interface ServerEditListener {
        public void serverInfoUpdated();
        public void connectToServer(Server server);
    }
}