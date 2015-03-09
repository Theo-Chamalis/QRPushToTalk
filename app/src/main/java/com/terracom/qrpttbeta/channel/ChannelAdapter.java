package com.terracom.qrpttbeta.channel;

import android.content.Context;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.terracom.jumble.IJumbleService;
import com.terracom.jumble.model.Channel;
import com.terracom.jumble.model.User;
import com.terracom.qrpttbeta.R;

public class ChannelAdapter extends BaseAdapter {

    private Context mContext;
    private IJumbleService mService;
    private Channel mChannel;

    public ChannelAdapter(Context context, IJumbleService service, Channel channel) {
        mContext = context;
        mService = service;
        mChannel = channel;
    }

    @Override
    public int getCount() {
        return mChannel.getUsers().size();
    }

    @Override
    public Object getItem(int position) {
        try {
            return mService.getUser(mChannel.getUsers().get(position));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return mChannel.getUsers().get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            v = layoutInflater.inflate(R.layout.overlay_user_row, parent, false);
        }
        User user = (User) getItem(position);
        TextView titleView = (TextView) v.findViewById(R.id.user_row_name);
        titleView.setText(user.getName());

        ImageView state = (ImageView) v.findViewById(R.id.user_row_state);
        if (user.isSelfDeafened())
            state.setImageResource(R.drawable.ic_deafened);
        else if (user.isSelfMuted())
            state.setImageResource(R.drawable.ic_muted);
        else if (user.isDeafened())
            state.setImageResource(R.drawable.ic_server_deafened);
        else if (user.isMuted())
            state.setImageResource(R.drawable.ic_server_muted);
        else if (user.isSuppressed())
            state.setImageResource(R.drawable.ic_suppressed);
        else if (user.getTalkState() == User.TalkState.TALKING)
            state.setImageResource(R.drawable.ic_talking_on);
        else
            state.setImageResource(R.drawable.ic_talking_off);

        return v;
    }

    public void setChannel(Channel channel) {
        mChannel = channel;
        notifyDataSetChanged();
    }

    public Channel getChannel() {
        return mChannel;
    }
}
