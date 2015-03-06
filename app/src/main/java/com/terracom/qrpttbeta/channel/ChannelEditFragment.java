package com.terracom.qrpttbeta.channel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.terracom.jumble.model.Channel;
import com.terracom.jumble.net.Permissions;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.util.JumbleServiceProvider;

public class ChannelEditFragment extends DialogFragment {

    private JumbleServiceProvider mServiceProvider;
    private TextView mNameField;
    private TextView mDescriptionField;
    private TextView mPositionField;
    private CheckBox mTemporaryBox;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mServiceProvider = (JumbleServiceProvider) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement JumbleServiceProvider");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.fragment_channel_edit, null, false);
        mNameField = (TextView) view.findViewById(R.id.channel_edit_name);
        mDescriptionField = (TextView) view.findViewById(R.id.channel_edit_description);
        mPositionField = (TextView) view.findViewById(R.id.channel_edit_position);
        mTemporaryBox = (CheckBox) view.findViewById(R.id.channel_edit_temporary);

        try {
            Channel parentChannel = mServiceProvider.getService().getChannel(getParent());
            int combinedPermissions = mServiceProvider.getService().getPermissions() | parentChannel.getPermissions();
            boolean canMakeChannel = (combinedPermissions & Permissions.MakeChannel) > 0;
            boolean canMakeTempChannel = (combinedPermissions & Permissions.MakeTempChannel) > 0;
            boolean onlyTemp = canMakeTempChannel && !canMakeChannel;
            mTemporaryBox.setChecked(onlyTemp);
            mTemporaryBox.setEnabled(!onlyTemp);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(isAdding() ? R.string.channel_add : R.string.channel_edit)
                .setView(view)
                .setPositiveButton(isAdding() ? R.string.add : R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if(isAdding()) {
                                mServiceProvider.getService().createChannel(getParent(),
                                        mNameField.getText().toString(),
                                        mDescriptionField.getText().toString(),
                                        Integer.parseInt(mPositionField.getText().toString()), // We can guarantee this to be an int. InputType is numberSigned.
                                        mTemporaryBox.isChecked());
                            } else {
                                // TODO
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    public boolean isAdding() {
        return getArguments().getBoolean("adding");
    }

    public int getParent() {
        return getArguments().getInt("parent");
    }

    public int getChannel() {
        return getArguments().getInt("channel");
    }
}
