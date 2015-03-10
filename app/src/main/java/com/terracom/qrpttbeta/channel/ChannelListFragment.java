package com.terracom.qrpttbeta.channel;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.CursorWrapper;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.terracom.jumble.IJumbleObserver;
import com.terracom.jumble.IJumbleService;
import com.terracom.jumble.JumbleService;
import com.terracom.jumble.model.Channel;
import com.terracom.jumble.model.Server;
import com.terracom.jumble.model.User;
import com.terracom.jumble.util.JumbleException;
import com.terracom.jumble.util.JumbleObserver;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.channel.actionmode.ChannelActionModeCallback;
import com.terracom.qrpttbeta.channel.actionmode.UserActionModeCallback;
import com.terracom.qrpttbeta.db.DatabaseProvider;
import com.terracom.qrpttbeta.db.QRPushToTalkDatabase;
import com.terracom.qrpttbeta.util.JumbleServiceFragment;

;

public class ChannelListFragment extends JumbleServiceFragment implements UserActionModeCallback.LocalUserUpdateListener, OnChannelClickListener, OnUserClickListener {

    private IJumbleObserver mServiceObserver = new JumbleObserver() {
        @Override
        public void onDisconnected(JumbleException e) throws RemoteException {
            mChannelView.setAdapter(null);
        }

        @Override
        public void onUserJoinedChannel(User user, Channel newChannel, Channel oldChannel) throws RemoteException {
            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
            if (getService().getSession() == user.getSession()) {
                scrollToChannel(newChannel.getId());
            }
        }

        @Override
        public void onChannelAdded(Channel channel) throws RemoteException {
            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChannelRemoved(Channel channel) throws RemoteException {
            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChannelStateUpdated(Channel channel) throws RemoteException {
            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onUserConnected(User user) throws RemoteException {
            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onUserRemoved(User user, String reason) throws RemoteException {
            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onUserStateUpdated(User user) throws RemoteException {
            mChannelListAdapter.animateUserStateUpdate(user, mChannelView);
        }

        @Override
        public void onUserTalkStateUpdated(User user) throws RemoteException {
            mChannelListAdapter.animateUserStateUpdate(user, mChannelView);
        }
    };

    private RecyclerView mChannelView;
    private ChannelListAdapter mChannelListAdapter;
    private ChatTargetProvider mTargetProvider;
    private DatabaseProvider mDatabaseProvider;
    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mTargetProvider = (ChatTargetProvider) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getParentFragment().toString() + " must implement ChatTargetProvider");
        }
        try {
            mDatabaseProvider = (DatabaseProvider) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement DatabaseProvider");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel_list, container, false);
        mChannelView = (RecyclerView) view.findViewById(R.id.channelUsers);
        mChannelView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(mChannelView);
    }

    @Override
    public IJumbleObserver getServiceObserver() {
        return mServiceObserver;
    }

    @Override
    public void onServiceBound(IJumbleService service) {
        try {
            if (mChannelListAdapter == null) {
                setupChannelList();
            } else {
                mChannelListAdapter.setService(service);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem muteItem = menu.findItem(R.id.menu_mute_button);
        MenuItem deafenItem = menu.findItem(R.id.menu_deafen_button);

        try {

            if (getService() != null && getService().getConnectionState() == JumbleService.STATE_CONNECTED && getService().getSessionUser() != null) {
                int foregroundColor = getActivity().getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimaryInverse}).getColor(0, -1);

                User self = getService().getSessionUser();
                muteItem.setIcon(self.isSelfMuted() ? R.drawable.ic_action_microphone_muted : R.drawable.ic_action_microphone);
                deafenItem.setIcon(self.isSelfDeafened() ? R.drawable.ic_action_audio_muted : R.drawable.ic_action_audio);
                muteItem.getIcon().mutate().setColorFilter(foregroundColor, PorterDuff.Mode.MULTIPLY);
                deafenItem.getIcon().mutate().setColorFilter(foregroundColor, PorterDuff.Mode.MULTIPLY);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_channel_list, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                CursorWrapper cursor = (CursorWrapper) searchView.getSuggestionsAdapter().getItem(i);
                int typeColumn = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
                int dataIdColumn = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA);
                String itemType = cursor.getString(typeColumn);
                int itemId = cursor.getInt(dataIdColumn);
                if (ChannelSearchProvider.INTENT_DATA_CHANNEL.equals(itemType)) {
                    try {
                        if (getService().getSessionChannel().getId() != itemId) {
                            getService().joinChannel(itemId);
                        } else {
                            scrollToChannel(itemId);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    return true;
                } else if (ChannelSearchProvider.INTENT_DATA_USER.equals(itemType)) {
                    scrollToUser(itemId);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_mute_button:
                try {
                    User self = getService().getSessionUser();

                    boolean muted = !self.isSelfMuted();
                    boolean deafened = self.isSelfDeafened();
                    deafened &= muted;
                    self.setSelfMuted(muted);
                    self.setSelfDeafened(deafened);
                    getService().setSelfMuteDeafState(self.isSelfMuted(), self.isSelfDeafened());

                    getActivity().supportInvalidateOptionsMenu();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.menu_deafen_button:
                try {
                    User self = getService().getSessionUser();

                    boolean deafened = self.isSelfDeafened();
                    self.setSelfDeafened(!deafened);
                    self.setSelfMuted(!deafened);
                    getService().setSelfMuteDeafState(self.isSelfDeafened(), self.isSelfDeafened());

                    getActivity().supportInvalidateOptionsMenu();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.menu_search:
                return false;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupChannelList() throws RemoteException {
        mChannelListAdapter = new ChannelListAdapter(getActivity(), getService(), mDatabaseProvider.getDatabase(), isShowingPinnedChannels());
        mChannelListAdapter.setOnChannelClickListener(this);
        mChannelListAdapter.setOnUserClickListener(this);
        mChannelView.setAdapter(mChannelListAdapter);
        mChannelListAdapter.notifyDataSetChanged();
    }

    public void scrollToChannel(int channelId) {
        int channelPosition = mChannelListAdapter.getChannelPosition(channelId);
        mChannelView.smoothScrollToPosition(channelPosition);
    }

    public void scrollToUser(int userId) {
        int userPosition = mChannelListAdapter.getUserPosition(userId);
        mChannelView.smoothScrollToPosition(userPosition);
    }

    private boolean isShowingPinnedChannels() {
        return getArguments().getBoolean("pinned");
    }

    @Override
    public void onLocalUserStateUpdated(final User user) {
        try {
            mChannelListAdapter.notifyDataSetChanged();
            final QRPushToTalkDatabase database = mDatabaseProvider.getDatabase();
            final Server server = getService().getConnectedServer();

            if (user.getUserId() >= 0 && server.isSaved()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (user.isLocalMuted()) {
                            database.addLocalMutedUser(server.getId(), user.getUserId());
                        } else {
                            database.removeLocalMutedUser(server.getId(), user.getUserId());
                        }
                        if (user.isLocalIgnored()) {
                            database.addLocalIgnoredUser(server.getId(), user.getUserId());
                        } else {
                            database.removeLocalIgnoredUser(server.getId(), user.getUserId());
                        }
                    }
                }).start();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChannelClick(Channel channel) {
        if (mTargetProvider.getChatTarget() != null &&
                channel.equals(mTargetProvider.getChatTarget().getChannel()) &&
                mActionMode != null) {
            mActionMode.finish();
        } else {
            ActionMode.Callback cb = new ChannelActionModeCallback(getActivity(),
                    getService(), channel, mTargetProvider, mDatabaseProvider.getDatabase(),
                    getChildFragmentManager()) {
                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    super.onDestroyActionMode(actionMode);
                    mActionMode = null;
                }
            };
            mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(cb);
        }
    }

    @Override
    public void onUserClick(User user) {
        if (mTargetProvider.getChatTarget() != null &&
                user.equals(mTargetProvider.getChatTarget().getUser()) &&
                mActionMode != null) {
            mActionMode.finish();
        } else {
            ActionMode.Callback cb = new UserActionModeCallback(getActivity(), getService(), user, mTargetProvider, getChildFragmentManager(), this) {
                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    super.onDestroyActionMode(actionMode);
                    mActionMode = null;
                }
            };
            mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(cb);
        }
    }
}
