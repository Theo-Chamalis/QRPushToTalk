package com.terracom.qrpttbeta.channel;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.terracom.jumble.IJumbleObserver;
import com.terracom.jumble.IJumbleService;
import com.terracom.jumble.model.Channel;
import com.terracom.jumble.model.Message;
import com.terracom.jumble.model.User;
import com.terracom.jumble.util.JumbleObserver;
import com.terracom.qrpttbeta.R;
import com.terracom.qrpttbeta.util.JumbleServiceFragment;
import com.terracom.qrpttbeta.util.MumbleImageGetter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelChatFragment extends JumbleServiceFragment implements ChatTargetProvider.OnChatTargetSelectedListener {
    private static final Pattern LINK_PATTERN = Pattern.compile("(https?://\\S+)");
    private static final String CHAT_DATE_FORMAT = "%I:%M %p";

    private IJumbleObserver mServiceObserver = new JumbleObserver() {

        @Override
        public void onMessageLogged(Message message) throws RemoteException {
            addChatMessage(message, true);
        }

        @Override
        public void onUserJoinedChannel(User user, Channel newChannel, Channel oldChannel) throws RemoteException {
            if (user != null && getService().getSessionUser() != null &&
                    user.equals(getService().getSessionUser()) &&
                    mTargetProvider.getChatTarget() == null) {
                updateChatTargetText(null);
            }
        }
    };

    private ListView mChatList;
    private ChannelChatAdapter mChatAdapter;
    private EditText mChatTextEdit;
    private ImageButton mSendButton;
    private ChatTargetProvider mTargetProvider;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        mTargetProvider.registerChatTargetListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTargetProvider.unregisterChatTargetListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mChatList = (ListView) view.findViewById(R.id.chat_list);
        mChatTextEdit = (EditText) view.findViewById(R.id.chatTextEdit);

        mSendButton = (ImageButton) view.findViewById(R.id.chatTextSend);
        mSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendMessage();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        mChatTextEdit.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    sendMessage();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        mChatTextEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSendButton.setEnabled(mChatTextEdit.getText().length() > 0);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        try {
            updateChatTargetText(mTargetProvider.getChatTarget());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear_chat:
                clear();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addChatMessage(Message message, boolean scroll) {
        if (mChatAdapter == null) return;

        mChatAdapter.add(message);

        if (scroll) {
            mChatList.post(new Runnable() {

                @Override
                public void run() {
                    mChatList.smoothScrollToPosition(mChatAdapter.getCount() - 1);
                }
            });
        }
<<<<<<< HEAD
    }

    private void sendMessage() throws RemoteException {
        if (mChatTextEdit.length() == 0) return;
=======
	}

	private void sendMessage() throws RemoteException {
        if(mChatTextEdit.length() == 0) return;
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0
        String message = mChatTextEdit.getText().toString();
        String formattedMessage = markupOutgoingMessage(message);
        ChatTargetProvider.ChatTarget target = mTargetProvider.getChatTarget();
        Message responseMessage = null;
        if (target == null)
            responseMessage = getService().sendChannelTextMessage(getService().getSessionChannel().getId(), formattedMessage, false);
        else if (target.getUser() != null)
            responseMessage = getService().sendUserTextMessage(target.getUser().getSession(), formattedMessage);
        else if (target.getChannel() != null)
            responseMessage = getService().sendChannelTextMessage(target.getChannel().getId(), formattedMessage, false);
        addChatMessage(responseMessage, true);
        mChatTextEdit.setText("");
    }

    private String markupOutgoingMessage(String message) {
        String formattedBody = message;
        Matcher matcher = LINK_PATTERN.matcher(formattedBody);
        formattedBody = matcher.replaceAll("<a href=\"$1\">$1</a>")
                .replaceAll("\n", "<br>");
        return formattedBody;
    }

    public void clear() {
        mChatAdapter.clear();
        try {
            getService().clearMessageLog();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
    public void updateChatTargetText(ChatTargetProvider.ChatTarget target) throws RemoteException {
        if (getService() == null) return;
=======
	public void updateChatTargetText(ChatTargetProvider.ChatTarget target) throws RemoteException {
        if(getService() == null) return;
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0

        String hint = null;
        if (target == null && getService().getSessionChannel() != null) {
            hint = getString(R.string.messageToChannel, getService().getSessionChannel().getName());
        } else if (target != null && target.getUser() != null) {
            hint = getString(R.string.messageToUser, target.getUser().getName());
        } else if (target != null && target.getChannel() != null) {
            hint = getString(R.string.messageToChannel, target.getChannel().getName());
        }
        mChatTextEdit.setHint(hint);
        mChatTextEdit.requestLayout();
<<<<<<< HEAD
    }
=======
	}
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0


    @Override
    public void onServiceBound(IJumbleService service) {
        try {
            mChatAdapter = new ChannelChatAdapter(getActivity(), service, service.getMessageLog());
            mChatList.setAdapter(mChatAdapter);
            mChatList.post(new Runnable() {
                @Override
                public void run() {
                    mChatList.setSelection(mChatAdapter.getCount() - 1);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IJumbleObserver getServiceObserver() {
        return mServiceObserver;
    }

    @Override
    public void onChatTargetSelected(ChatTargetProvider.ChatTarget target) {
        try {
            updateChatTargetText(target);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static class ChannelChatAdapter extends ArrayAdapter<Message> {

        private MumbleImageGetter mImageGetter;
        private IJumbleService mService;

        public ChannelChatAdapter(Context context, IJumbleService service, List<Message> messages) {
            super(context, 0, new ArrayList<Message>(messages));
            mService = service;
            mImageGetter = new MumbleImageGetter(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.list_chat_item, parent, false);
            }

            LinearLayout chatBox = (LinearLayout) v.findViewById(R.id.list_chat_item_box);
            TextView targetText = (TextView) v.findViewById(R.id.list_chat_item_target);
            TextView messageText = (TextView) v.findViewById(R.id.list_chat_item_text);
            TextView timeText = (TextView) v.findViewById(R.id.list_chat_item_time);

            Message message = getItem(position);
            String targetMessage = getContext().getString(R.string.unknown);
            boolean selfAuthored = false;
            try {
                selfAuthored = message.getActor() == mService.getSession();

                if (message.getChannels() != null && !message.getChannels().isEmpty()) {
                    Channel currentChannel = message.getChannels().get(0);
                    if (currentChannel != null && currentChannel.getName() != null) {
                        targetMessage = getContext().getString(R.string.chat_message_to, message.getActorName(), currentChannel.getName());
                    }
                } else if (message.getTrees() != null && !message.getTrees().isEmpty()) {
                    Channel currentChannel = message.getTrees().get(0);
                    if (currentChannel != null && currentChannel.getName() != null) {
                        targetMessage = getContext().getString(R.string.chat_message_to, message.getActorName(), currentChannel.getName());
                    }
                } else if (message.getUsers() != null && !message.getUsers().isEmpty()) {
                    User user = message.getUsers().get(0);
                    if (user != null && user.getName() != null) {
                        targetMessage = getContext().getString(R.string.chat_message_to, message.getActorName(), user.getName());
                    }
                } else {
                    targetMessage = message.getActorName();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            int gravity = selfAuthored ? Gravity.RIGHT : Gravity.LEFT;

            chatBox.setGravity(gravity);
            targetText.setVisibility(message.getType() == Message.Type.TEXT_MESSAGE ? View.VISIBLE : View.GONE);
            targetText.setText(targetMessage);
            messageText.setText(Html.fromHtml(message.getMessage(), mImageGetter, null));
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
            messageText.setGravity(gravity);
            timeText.setText(message.getReceivedTime().format(CHAT_DATE_FORMAT));

            return v;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }
}
