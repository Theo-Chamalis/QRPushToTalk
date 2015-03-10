package com.terracom.qrpttbeta.channel.comment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TabHost;

import com.terracom.jumble.IJumbleService;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.util.JumbleServiceProvider;

public abstract class AbstractCommentFragment extends DialogFragment {

    private TabHost mTabHost;
    private WebView mCommentView;
    private EditText mCommentEdit;
    private JumbleServiceProvider mProvider;
    private String mComment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mComment = getArguments().getString("comment");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mProvider = (JumbleServiceProvider) activity;
        } catch (ClassCastException e) {
            throw new RuntimeException(activity.getClass().getName() + " must implement JumbleServiceProvider!");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_comment, null, false);

        mCommentView = (WebView) view.findViewById(R.id.comment_view);
        mCommentEdit = (EditText) view.findViewById(R.id.comment_edit);

        mTabHost = (TabHost) view.findViewById(R.id.comment_tabhost);
        mTabHost.setup();

        if (mComment == null) {
            mCommentView.loadData("Loading...", null, null);
            try {
                requestComment(mProvider.getService());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            loadComment(mComment);
        }

        TabHost.TabSpec viewTab = mTabHost.newTabSpec("View");
        viewTab.setIndicator(getString(R.string.comment_view));
        viewTab.setContent(R.id.comment_tab_view);

        TabHost.TabSpec editTab = mTabHost.newTabSpec("Edit");
        editTab.setIndicator(getString(isEditing() ? R.string.comment_edit_source : R.string.comment_view_source));
        editTab.setContent(R.id.comment_tab_edit);

        mTabHost.addTab(viewTab);
        mTabHost.addTab(editTab);

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if ("View".equals(tabId)) {
                    mCommentView.loadData(mCommentEdit.getText().toString(), "text/html", "UTF-8");
                } else if ("Edit".equals(tabId) && "".equals(mCommentEdit.getText().toString())) {
                    mCommentEdit.setText(mComment);
                }
            }
        });

        mTabHost.setCurrentTab(isEditing() ? 1 : 0);

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setView(view);
        if (isEditing()) {
            adb.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        editComment(mProvider.getService(), mCommentEdit.getText().toString());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        adb.setNegativeButton(R.string.close, null);
        return adb.create();
    }

    protected void loadComment(String comment) {
        if (mCommentView == null) return;
        mCommentView.loadData(comment, "text/html", "UTF-8");
        mComment = comment;
    }

    public boolean isEditing() {
        return getArguments().getBoolean("editing");
    }

    public abstract void requestComment(IJumbleService service) throws RemoteException;

    public abstract void editComment(IJumbleService service, String comment) throws RemoteException;
}
