package com.terracom.qrpttbeta.servers;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.terracom.jumble.model.Server;
import com.terracom.qrpttbeta.R;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ServerAdapter<E extends Server> extends ArrayAdapter<E> {
    private static final int MAX_ACTIVE_PINGS = 50;

    private ConcurrentHashMap<Server, ServerInfoResponse> mInfoResponses = new ConcurrentHashMap<Server, ServerInfoResponse>();
    private ExecutorService mPingExecutor = Executors.newFixedThreadPool(MAX_ACTIVE_PINGS);
    private int mViewResource;

    public ServerAdapter(Context context, int viewResource, List<E> servers) {
        super(context, 0, servers);
        mViewResource = viewResource;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View view = v;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(mViewResource, parent, false);
        }

        final E server = getItem(position);

        ServerInfoResponse infoResponse = mInfoResponses.get(server);
        boolean requestExists = infoResponse != null;
        boolean requestFailure = infoResponse != null && infoResponse.isDummy();

        TextView nameText = (TextView) view.findViewById(R.id.server_row_name);
        TextView userText = (TextView) view.findViewById(R.id.server_row_user);

        nameText.setText(server.getName());

<<<<<<< HEAD
        if (userText != null) userText.setText(server.getUsername());
=======
        if(userText != null) userText.setText(server.getUsername());
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0

        final ImageView moreButton = (ImageView) view.findViewById(R.id.server_row_more);
        if (moreButton != null) {
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onServerOptionsClick(server, moreButton);
                }
            });
        }

        TextView serverVersionText = (TextView) view.findViewById(R.id.server_row_version_status);
        TextView serverLatencyText = (TextView) view.findViewById(R.id.server_row_latency);
        TextView serverUsersText = (TextView) view.findViewById(R.id.server_row_usercount);
        ProgressBar serverInfoProgressBar = (ProgressBar) view.findViewById(R.id.server_row_ping_progress);

        serverVersionText.setVisibility(!requestExists ? View.INVISIBLE : View.VISIBLE);
        serverUsersText.setVisibility(!requestExists ? View.INVISIBLE : View.VISIBLE);
        serverInfoProgressBar.setVisibility(!requestExists ? View.VISIBLE : View.INVISIBLE);

<<<<<<< HEAD
        if (infoResponse != null && !requestFailure) {
            serverVersionText.setText("Status: " + getContext().getString(R.string.online) + "  ");
            serverUsersText.setText("Users: " + infoResponse.getCurrentUsers() + "/" + infoResponse.getMaximumUsers());
            serverLatencyText.setText(infoResponse.getLatency() + "ms");
        } else if (requestFailure) {
=======
        if(infoResponse != null && !requestFailure) {
            serverVersionText.setText("Status: "+getContext().getString(R.string.online)+"  ");
            serverUsersText.setText("Users: "+infoResponse.getCurrentUsers()+"/"+infoResponse.getMaximumUsers());
            serverLatencyText.setText(infoResponse.getLatency()+"ms");
        } else if(requestFailure) {
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0
            serverVersionText.setText("Status: Offline");
            serverUsersText.setText("");
            serverLatencyText.setText("");
        }

<<<<<<< HEAD
        if (infoResponse == null) {
=======
        if(infoResponse == null) {
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0
            ServerInfoTask task = new ServerInfoTask() {
                protected void onPostExecute(ServerInfoResponse result) {
                    super.onPostExecute(result);
                    mInfoResponses.put(server, result);
                    notifyDataSetChanged();
                }
            };

<<<<<<< HEAD
            if (Build.VERSION.SDK_INT >= 11) {
=======
            if(Build.VERSION.SDK_INT >= 11) {
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0
                task.executeOnExecutor(mPingExecutor, server);
            } else {
                task.execute(server);
            }
        }

        return view;
    }

    private void onServerOptionsClick(final Server server, View optionsButton) {
        PopupMenu popupMenu = new PopupMenu(getContext(), optionsButton);
        popupMenu.inflate(getPopupMenuResource());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onPopupItemClick(server, menuItem);
            }
        });
        popupMenu.show();
    }

    public abstract int getPopupMenuResource();

    public abstract boolean onPopupItemClick(Server server, MenuItem menuItem);
}
