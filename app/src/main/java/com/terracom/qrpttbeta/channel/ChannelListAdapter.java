package com.terracom.qrpttbeta.channel;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.terracom.jumble.IJumbleService;
import com.terracom.jumble.model.Channel;
import com.terracom.jumble.model.User;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.app.QRPushToTalkActivity;
import com.terracom.qrpttbeta.db.QRPushToTalkDatabase;
import com.terracom.qrpttbeta.drawable.CircleDrawable;
import com.terracom.qrpttbeta.drawable.FlipDrawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ChannelListAdapter extends RecyclerView.Adapter {
    public static final long CHANNEL_ID_MASK = (0x1L << 32);
    public static final long USER_ID_MASK = (0x1L << 33);
    String nameOfChannel = QRPushToTalkActivity.nameOfSavedLastLoggedChannel;

    private static final long FLIP_DURATION = 350;

    private Context mContext;
    private IJumbleService mService;
    private QRPushToTalkDatabase mDatabase;
    private List<Integer> mRootChannels;
    private List<Node> mNodes;
    private HashMap<Integer, Boolean> mExpandedChannels;
    private OnUserClickListener mUserClickListener;
    private OnChannelClickListener mChannelClickListener;

    public ChannelListAdapter(Context context, IJumbleService service, QRPushToTalkDatabase database, boolean showPinnedOnly) throws RemoteException {
        setHasStableIds(true);
        mContext = context;
        mService = service;
        mDatabase = database;

        mRootChannels = new ArrayList<Integer>();
        if(showPinnedOnly) {
            mRootChannels = mDatabase.getPinnedChannels(mService.getConnectedServer().getId());
        } else {
            mRootChannels.add(0);
        }

        mNodes = new LinkedList<Node>();
        mExpandedChannels = new HashMap<Integer, Boolean>();
        updateChannels();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(viewType, viewGroup, false);
        if (viewType == R.layout.channel_row) {
            return new ChannelViewHolder(view);
        } else if (viewType == R.layout.channel_user_row) {
            return new UserViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final Node node = mNodes.get(position);

        if(!nameOfChannel.equals("")){
            if(node.isChannel() && !node.getChannel().getName().equals(nameOfChannel) && !node.getChannel().getName().equals("QR-PushToTalk Server")){

                try {
                    updateChannels(); // FIXME: very inefficient.
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                ChannelViewHolder cvh = (ChannelViewHolder) viewHolder;
                cvh.mChannelExpandToggle.setVisibility(View.GONE);
                cvh.mChannelName.setText("");
                cvh.mChannelName.setVisibility(View.GONE);
                cvh.mChannelUserCount.setText("");
                cvh.mChannelUserCount.setVisibility(View.GONE);
                cvh.mChannelHolder.setVisibility(View.GONE);
                cvh.itemView.setVisibility(View.GONE);
                try {
                    updateChannels(); // FIXME: very inefficient.
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            } else if(node.isUser() && !node.getParent().getChannel().getName().equals(nameOfChannel)){

                try {
                    updateChannels(); // FIXME: very inefficient.
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                UserViewHolder uvh = (UserViewHolder) viewHolder;
                uvh.mUserName.setText("");
                uvh.mUserName.setVisibility(View.GONE);
                uvh.mUserTalkHighlight.setVisibility(View.GONE);
                uvh.mPicofMic.setVisibility(View.GONE);
                uvh.mUserHolder.setVisibility(View.GONE);
                uvh.itemView.setVisibility(View.GONE);

                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                float margin = (node.getDepth() + 1) * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, metrics);
                uvh.mUserHolder.setPadding((int) margin,0,uvh.mUserHolder.getPaddingRight(),0);

                try {
                    updateChannels(); // FIXME: very inefficient.
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            else if (node.isChannel() ) {
                final Channel channel = node.getChannel();
                ChannelViewHolder cvh = (ChannelViewHolder) viewHolder;
                cvh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mChannelClickListener != null) {
                            mChannelClickListener.onChannelClick(channel);
                        }
                    }
                });

                final boolean expandUsable = channel.getSubchannels().size() > 0 ||
                        channel.getSubchannelUserCount() > 0;
                cvh.mChannelExpandToggle.setImageResource(node.isExpanded() ?
                        R.drawable.ic_action_expanded : R.drawable.ic_action_collapsed);
                cvh.mChannelExpandToggle.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mExpandedChannels.put(channel.getId(), !node.isExpanded());
                        try {
                            updateChannels(); // FIXME: very inefficient.
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        notifyDataSetChanged();
                    }
                });
                cvh.mChannelExpandToggle.setEnabled(expandUsable);
                cvh.mChannelExpandToggle.setVisibility(expandUsable ? View.VISIBLE : View.INVISIBLE);

                cvh.mChannelName.setText(channel.getName());

                int userCount = channel.getSubchannelUserCount();
                if(node.getChannel().getName().equals("QR-PushToTalk Server"))
                    cvh.mChannelUserCount.setText("");
                else{
                    cvh.mChannelUserCount.setText(String.format("%d", userCount));
                }

                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                float margin = node.getDepth() * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, metrics);
                cvh.mChannelHolder.setPadding((int) margin,
                        cvh.mChannelHolder.getPaddingTop(),
                        cvh.mChannelHolder.getPaddingRight(),
                        cvh.mChannelHolder.getPaddingBottom());
                try {
                    updateChannels(); // FIXME: very inefficient.
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (node.isUser()) {
                try {
                    updateChannels(); // FIXME: very inefficient.
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                final User user = node.getUser();
                UserViewHolder uvh = (UserViewHolder) viewHolder;
                uvh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mUserClickListener != null) {
                            mUserClickListener.onUserClick(user);
                        }
                    }
                });

                uvh.mUserName.setText(user.getName());
                try {
                    uvh.mUserName.setTypeface(null, user.getSession() == mService.getSession() ? Typeface.BOLD : Typeface.NORMAL);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                uvh.mUserTalkHighlight.setImageDrawable(getTalkStateDrawable(user));

                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                float margin = (node.getDepth() + 1) * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, metrics);
                uvh.mUserHolder.setPadding((int) margin,
                        uvh.mUserHolder.getPaddingTop(),
                        uvh.mUserHolder.getPaddingRight(),
                        uvh.mUserHolder.getPaddingBottom());
                try {
                    updateChannels(); // FIXME: very inefficient.
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }else{
            if(node.isChannel() && !node.getChannel().getName().equals("Demo Channel") && !node.getChannel().getName().equals("QR-PushToTalk Server")){
                ChannelViewHolder cvh = (ChannelViewHolder) viewHolder;
                cvh.mChannelExpandToggle.setVisibility(View.GONE);
                cvh.mChannelName.setText("");
                cvh.mChannelName.setVisibility(View.GONE);
                cvh.mChannelUserCount.setText("");
                cvh.mChannelUserCount.setVisibility(View.GONE);
                cvh.mChannelHolder.setVisibility(View.GONE);
                cvh.itemView.setVisibility(View.GONE);
                try {
                    updateChannels(); // FIXME: very inefficient.
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            } else if(node.isUser() && !node.getParent().getChannel().getName().equals("Demo Channel")){
                UserViewHolder uvh = (UserViewHolder) viewHolder;
                uvh.mUserName.setText("");
                uvh.mUserName.setVisibility(View.GONE);
                uvh.mUserTalkHighlight.setVisibility(View.GONE);
                uvh.mPicofMic.setVisibility(View.GONE);
                uvh.mUserHolder.setVisibility(View.GONE);
                uvh.itemView.setVisibility(View.GONE);

                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                float margin = (node.getDepth() + 1) * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, metrics);
                uvh.mUserHolder.setPadding((int) margin,
                        uvh.mUserHolder.getPaddingTop(),
                        uvh.mUserHolder.getPaddingRight(),
                        uvh.mUserHolder.getPaddingBottom());
                try {
                    updateChannels(); // FIXME: very inefficient.
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            else if (node.isChannel()) {
                final Channel channel = node.getChannel();
                ChannelViewHolder cvh = (ChannelViewHolder) viewHolder;
                cvh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mChannelClickListener != null) {
                            mChannelClickListener.onChannelClick(channel);
                        }
                    }
                });

                final boolean expandUsable = channel.getSubchannels().size() > 0 ||
                        channel.getSubchannelUserCount() > 0;
                cvh.mChannelExpandToggle.setImageResource(node.isExpanded() ?
                        R.drawable.ic_action_expanded : R.drawable.ic_action_collapsed);
                cvh.mChannelExpandToggle.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mExpandedChannels.put(channel.getId(), !node.isExpanded());
                        try {
                            updateChannels(); // FIXME: very inefficient.
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        notifyDataSetChanged();
                    }
                });

                cvh.mChannelExpandToggle.setEnabled(expandUsable);
                cvh.mChannelExpandToggle.setVisibility(expandUsable ? View.VISIBLE : View.INVISIBLE);

                cvh.mChannelName.setText(channel.getName());

                int userCount = channel.getSubchannelUserCount();
                if(node.getChannel().getName().equals("QR-PushToTalk Server"))
                    cvh.mChannelUserCount.setText("");
                else{
                    cvh.mChannelUserCount.setText(String.format("%d", userCount));
                }

                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                float margin = node.getDepth() * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, metrics);
                cvh.mChannelHolder.setPadding((int) margin,
                        cvh.mChannelHolder.getPaddingTop(),
                        cvh.mChannelHolder.getPaddingRight(),
                        cvh.mChannelHolder.getPaddingBottom());
                try {
                    updateChannels(); // FIXME: very inefficient.
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (node.isUser() ) {
                final User user = node.getUser();
                UserViewHolder uvh = (UserViewHolder) viewHolder;
                uvh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mUserClickListener != null) {
                            mUserClickListener.onUserClick(user);
                        }
                    }
                });

                uvh.mUserName.setText(user.getName());
                try {
                    uvh.mUserName.setTypeface(null, user.getSession() == mService.getSession() ? Typeface.BOLD : Typeface.NORMAL);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                uvh.mUserTalkHighlight.setImageDrawable(getTalkStateDrawable(user));

                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                float margin = (node.getDepth() + 1) * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, metrics);
                uvh.mUserHolder.setPadding((int) margin,
                        uvh.mUserHolder.getPaddingTop(),
                        uvh.mUserHolder.getPaddingRight(),
                        uvh.mUserHolder.getPaddingBottom());
                try {
                    updateChannels(); // FIXME: very inefficient.
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    @Override
    public int getItemCount() {
        return mNodes.size();
    }

    @Override
    public int getItemViewType(int position) {
        Node node = mNodes.get(position);

        if (node.isChannel()) {
            return R.layout.channel_row;
        } else if (node.isUser()) {
            return R.layout.channel_user_row;
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        return mNodes.get(position).getId();
    }

    public void updateChannels() throws RemoteException {
        mNodes.clear();
        for (int cid : mRootChannels) {
            Channel channel = mService.getChannel(cid);
            if (channel != null) {
                constructNodes(null, mService.getChannel(cid), 0, mNodes);
            }
        }
    }

    public void animateUserStateUpdate(User user, RecyclerView view) {
        long itemId = user.getSession() | USER_ID_MASK;
        UserViewHolder uvh = (UserViewHolder) view.findViewHolderForItemId(itemId);
        if (uvh != null) {
            Drawable newState = getTalkStateDrawable(user);
            Drawable oldState = uvh.mUserTalkHighlight.getDrawable().getCurrent();

            if (!newState.getConstantState().equals(oldState.getConstantState())) {
                if (Build.VERSION.SDK_INT >= 12) {
                    FlipDrawable drawable = new FlipDrawable(oldState, newState);
                    uvh.mUserTalkHighlight.setImageDrawable(drawable);
                    drawable.start(FLIP_DURATION);
                } else {
                    uvh.mUserTalkHighlight.setImageDrawable(newState);
                }
            }
        }
    }

    private Drawable getTalkStateDrawable(User user) {
        Resources resources = mContext.getResources();
        if (user.isSelfDeafened()) {
            return resources.getDrawable(R.drawable.outline_circle_deafened);
        } else if (user.isDeafened()) {
            return resources.getDrawable(R.drawable.outline_circle_server_deafened);
        } else if (user.isSelfMuted()) {
            return resources.getDrawable(R.drawable.outline_circle_muted);
        } else if (user.isMuted()) {
            return resources.getDrawable(R.drawable.outline_circle_server_muted);
        } else if (user.isSuppressed()) {
            return resources.getDrawable(R.drawable.outline_circle_suppressed);
        } else if (user.getTalkState() == User.TalkState.TALKING ||
                user.getTalkState() == User.TalkState.SHOUTING ||
                user.getTalkState() == User.TalkState.WHISPERING) {
            // TODO: add whisper and shouting resources
            return resources.getDrawable(R.drawable.outline_circle_talking_on);
        } else {
            if (user.getTexture() != null) {
                return new CircleDrawable(mContext.getResources(), user.getTexture());
            } else {
                return resources.getDrawable(R.drawable.outline_circle_talking_off);
            }
        }
    }

    public int getUserPosition(int session) {
        long itemId = session | USER_ID_MASK;
        for (int i = 0; i < mNodes.size(); i++) {
            Node node = mNodes.get(i);
            if (node.getId() == itemId) {
                return i;
            }
        }
        return -1;
    }

    public int getChannelPosition(int channelId) {
        long itemId = channelId | CHANNEL_ID_MASK;
        for (int i = 0; i < mNodes.size(); i++) {
            Node node = mNodes.get(i);
            if (node.getId() == itemId) {
                return i;
            }
        }
        return -1;
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        mUserClickListener = listener;
    }

    public void setOnChannelClickListener(OnChannelClickListener listener) {
        mChannelClickListener = listener;
    }

    private void constructNodes(Node parent, Channel channel, int depth,
                                List<Node> nodes) throws RemoteException {
        Node channelNode = new Node(parent, depth, channel);
        nodes.add(channelNode);

        Boolean expandSetting = mExpandedChannels.get(channel.getId());
        if ((expandSetting == null && channel.getSubchannelUserCount() == 0)
                || (expandSetting != null && !expandSetting)) {
            channelNode.setExpanded(false);
            return;
        }

        for (int uid : channel.getUsers()) {
            User user = mService.getUser(uid);
            if (user == null) {
                continue;
            }
            nodes.add(new Node(channelNode, depth, user));
        }
        for (int cid : channel.getSubchannels()) {
            Channel subchannel = mService.getChannel(cid);
            constructNodes(channelNode, subchannel, depth + 1, nodes);
        }
    }

    public void setService(IJumbleService service) throws RemoteException {
        mService = service;
        updateChannels();
        notifyDataSetChanged();
    }

    private static class UserViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mUserHolder;
        public TextView mUserName;
        public FrameLayout mPicofMic;
        public ImageView mUserTalkHighlight;

        public UserViewHolder(View itemView) {
            super(itemView);
            mPicofMic = (FrameLayout) itemView.findViewById(R.id.eikonitsa);
            mUserHolder = (LinearLayout) itemView.findViewById(R.id.user_row_title);
            mUserTalkHighlight = (ImageView) itemView.findViewById(R.id.user_row_talk_highlight);

            mUserName = (TextView) itemView.findViewById(R.id.user_row_name);
        }
    }

    private static class ChannelViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mChannelHolder;
        public ImageView mChannelExpandToggle;
        public TextView mChannelName;
        public TextView mChannelUserCount;

        public ChannelViewHolder(View itemView) {
            super(itemView);
            mChannelHolder = (LinearLayout) itemView.findViewById(R.id.channel_row_title);
            mChannelExpandToggle = (ImageView) itemView.findViewById(R.id.channel_row_expand);
            mChannelName = (TextView) itemView.findViewById(R.id.channel_row_name);
            mChannelUserCount = (TextView) itemView.findViewById(R.id.channel_row_count);
        }
    }

    private static class Node {
        private Node mParent;
        private Channel mChannel;
        private User mUser;
        private int mDepth;
        private boolean mExpanded;

        public Node(Node parent, int depth, Channel channel) {
            mParent = parent;
            mChannel = channel;
            mDepth = depth;
            mExpanded = true;
        }

        public Node(Node parent, int depth, User user) {
            mParent = parent;
            mUser = user;
            mDepth = depth;
        }

        public boolean isChannel() {
            return mChannel != null;
        }

        public boolean isUser() {
            return mUser != null;
        }

        public Node getParent() {
            return mParent;
        }

        public Channel getChannel() {
                return mChannel;
        }

        public User getUser() {
            return mUser;
        }

        public Long getId() {
            if (isChannel()) {
                return CHANNEL_ID_MASK | mChannel.getId();
            } else if (isUser()) {
                return USER_ID_MASK | mUser.getSession();
            }
            return null;
        }

        public int getDepth() {
            return mDepth;
        }

        public boolean isExpanded() {
            return mExpanded;
        }

        public void setExpanded(boolean expanded) {
            mExpanded = expanded;
        }
    }
}