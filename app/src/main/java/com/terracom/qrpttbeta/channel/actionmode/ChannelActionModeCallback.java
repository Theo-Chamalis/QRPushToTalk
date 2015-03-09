package com.terracom.qrpttbeta.channel.actionmode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.terracom.jumble.IJumbleService;
import com.terracom.jumble.model.Channel;
import com.terracom.jumble.model.Server;
import com.terracom.jumble.net.Permissions;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.channel.ChannelEditFragment;
import com.terracom.qrpttbeta.channel.ChatTargetProvider;
import com.terracom.qrpttbeta.channel.comment.ChannelDescriptionFragment;
import com.terracom.qrpttbeta.db.QRPushToTalkDatabase;
import com.terracom.qrpttbeta.util.TintedMenuInflater;

public class ChannelActionModeCallback extends ChatTargetActionModeCallback {
    private Context mContext;
    private IJumbleService mService;
    private Channel mChannel;
    private QRPushToTalkDatabase mDatabase;
    private FragmentManager mFragmentManager;

    public ChannelActionModeCallback(Context context,
                                     IJumbleService service,
                                     Channel channel,
                                     ChatTargetProvider chatTargetProvider,
                                     QRPushToTalkDatabase database,
                                     FragmentManager fragmentManager) {
        super(chatTargetProvider);
        mContext = context;
        mService = service;
        mChannel = channel;
        mDatabase = database;
        mFragmentManager = fragmentManager;
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        super.onCreateActionMode(actionMode, menu);
        TintedMenuInflater inflater = new TintedMenuInflater(mContext, actionMode.getMenuInflater());
        inflater.inflate(R.menu.context_channel, menu);

        actionMode.setTitle(mChannel.getName());
        actionMode.setSubtitle(R.string.current_chat_target);

        try {
            if (mChannel.getPermissions() == 0)
                mService.requestPermissions(mChannel.getId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        int perms = mChannel.getPermissions();
        menu.findItem(R.id.context_channel_edit).setVisible((perms & Permissions.Write) > 0);
        menu.findItem(R.id.context_channel_remove).setVisible((perms & Permissions.Write) > 0);
        menu.findItem(R.id.context_channel_view_description)
                .setVisible(mChannel.getDescription() != null ||
                        mChannel.getDescriptionHash() != null);

        try {
            Server server = mService.getConnectedServer();
            if (server != null) {
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        boolean adding = false;
        switch (menuItem.getItemId()) {
            case R.id.context_channel_join:
                try {
                    mService.joinChannel(mChannel.getId());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.context_channel_add:
                adding = true;
            case R.id.context_channel_edit:
                ChannelEditFragment addFragment = new ChannelEditFragment();
                Bundle args = new Bundle();
                if (adding) args.putInt("parent", mChannel.getId());
                else args.putInt("channel", mChannel.getId());
                args.putBoolean("adding", adding);
                addFragment.setArguments(args);
                addFragment.show(mFragmentManager, "ChannelAdd");
                break;
            case R.id.context_channel_remove:
                AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
                adb.setTitle(R.string.confirm);
                adb.setMessage(R.string.confirm_delete_channel);
                adb.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mService.removeChannel(mChannel.getId());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                adb.setNegativeButton(android.R.string.cancel, null);
                adb.show();
                break;
            case R.id.context_channel_view_description:
                Bundle commentArgs = new Bundle();
                commentArgs.putInt("channel", mChannel.getId());
                commentArgs.putString("comment", mChannel.getDescription());
                commentArgs.putBoolean("editing", false);
                DialogFragment commentFragment = (DialogFragment) Fragment.instantiate(mContext,
                        ChannelDescriptionFragment.class.getName(), commentArgs);
                commentFragment.show(mFragmentManager, ChannelDescriptionFragment.class.getName());
                break;
        }
        actionMode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        super.onDestroyActionMode(actionMode);
    }

    @Override
    public ChatTargetProvider.ChatTarget getChatTarget() {
        return new ChatTargetProvider.ChatTarget(mChannel);
    }
}
