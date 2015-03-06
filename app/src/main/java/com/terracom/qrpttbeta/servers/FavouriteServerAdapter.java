package com.terracom.qrpttbeta.servers;

import android.content.Context;
import android.view.MenuItem;

import com.terracom.jumble.model.Server;
import com.terracom.qrpttbeta.R;
import java.util.List;

/**
 * Created by andrew on 11/05/14.
 */
public class FavouriteServerAdapter extends ServerAdapter<Server> {

    private FavouriteServerAdapterMenuListener mListener;

    public FavouriteServerAdapter(Context context, List<Server> servers, FavouriteServerAdapterMenuListener listener) {
        super(context, R.layout.server_list_row, servers);
        mListener = listener;
    }

    @Override
    public int getPopupMenuResource() {
        return R.menu.popup_favourite_server;
    }


    @Override
    public boolean onPopupItemClick(Server server, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_server_edit:
                mListener.editServer(server);
                return true;
            case R.id.menu_server_delete:
                mListener.deleteServer(server);
                return true;
            default:
                return false;
        }
    }

    public static interface FavouriteServerAdapterMenuListener {
        public void editServer(Server server);
        public void deleteServer(Server server);
    }
}
