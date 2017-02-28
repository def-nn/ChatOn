package com.app.chaton;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.chaton.API_helpers.CallService;
import com.app.chaton.API_helpers.Message;
import com.app.chaton.API_helpers.MessageResponseObject;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.API_helpers.ServiceGenerator;
import com.app.chaton.Utils.ToastHelper;
import com.app.chaton.sockets.SocketHelper;
import com.app.chaton.Utils.PreferenceHelper;
import com.app.chaton.adapter.ChatAdapter;
import com.app.chaton.sockets.SocketListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements SocketListener{

    private static final String NOT_EDITED = "not_edited";
    private static final String EDITED = "edited";

    private Long companionId;
    private String companionName, companionAvatar;
    private boolean isDataUploaded, isTyping;

    private ActionBar actionBar;
    private Button btnSend, btnTryAgain;
    private ImageButton btnAttach;
    private EditText messInput;
    private ProgressBar progressBar;
    private RecyclerView chatView;
    private TextView tvNoMess;
    private ImageView icTyping;
    private AnimationDrawable animation;

    private LinearLayoutManager chatManager;
    private ChatAdapter chatAdapter;

    private CallService callService;
    private PreferenceHelper preferenceHelper;
    private SocketHelper socketHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        callService = ServiceGenerator.createService(CallService.class);
        preferenceHelper = new PreferenceHelper(getSharedPreferences(
                getResources().getString(R.string.PREFERENCE_FILE), MODE_PRIVATE));

        socketHelper = ((WeTuneApplication) getApplication()).getSocketHelper();
        socketHelper.setSocketListener(this);

        Intent intent = getIntent();
        companionId = intent.getLongExtra(PreferenceHelper.ID, 0);
        companionName = intent.getStringExtra(PreferenceHelper.NAME);
        companionAvatar = intent.getStringExtra(PreferenceHelper.AVATAR);

        chatView = (RecyclerView) findViewById(R.id.chatView);
        chatView.setHasFixedSize(true);

        chatManager = new LinearLayoutManager(getApplicationContext());
        chatManager.setStackFromEnd(true);
        chatView.setLayoutManager(chatManager);

        tvNoMess = (TextView) findViewById(R.id.tvNoMess);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnTryAgain = (Button) findViewById(R.id.btnTryAgain);

        if (savedInstanceState == null) uploadData();

        setActionBar();

        btnSend = (Button) findViewById(R.id.btnSend);
        btnAttach = (ImageButton) findViewById(R.id.btnAttach);
        messInput = (EditText) findViewById(R.id.messInput);

        icTyping = (ImageView) findViewById(R.id.icTyping);
        animation = (AnimationDrawable)icTyping.getBackground();

        TextView actionBarTitle = (TextView) actionBar.getCustomView().findViewById(R.id.actionBarTitle);
        actionBarTitle.setText(companionName);

        final Typeface myriad = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro.ttf");
        btnSend.setTypeface(myriad);
        btnTryAgain.setTypeface(myriad);
        messInput.setTypeface(myriad);
        actionBarTitle.setTypeface(myriad);
        ((TextView) findViewById(R.id.tvTryAgain)).setTypeface(myriad);

        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.tryAgainView).setVisibility(View.GONE);
                uploadData();
            }
        });

        messInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    switch (view.getTag().toString()) {
                        case NOT_EDITED:
                            ((EditText) view).setText("");
                            view.setTag(EDITED);
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        messInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    socketHelper.notifyTyping(companionId);
                } catch (Exception e) {}
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, Object> mess_data = new HashMap<>();
                mess_data.put(Message.ID, 0L);
                mess_data.put(Message.OWNER, preferenceHelper.getId());
                mess_data.put(Message.RECEIVER, companionId);
                mess_data.put(Message.MESSAGE, messInput.getText().toString());

                Message message = new Message(mess_data);
                message.setCompanion(companionId);

                chatAdapter.getMessageList().add(message);
                chatAdapter.notifyItemInserted(chatAdapter.getItemCount() - 1);
                chatManager.scrollToPosition(chatAdapter.getItemCount() - 1);

                messInput.setText("");

                try {
                    socketHelper.send(companionId, message.getBody());
                } catch (Exception e) {
                    chatView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                chatView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                chatView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }

                            chatView.getLayoutManager().findViewByPosition(chatAdapter.getItemCount() - 1)
                                                       .findViewById(R.id.icError).setVisibility(View.VISIBLE);
                        }
                    });
                    ToastHelper.makeToast(R.string.error_data_uploading);
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!socketHelper.isConnected()) socketHelper.reconnect();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mess", messInput.getText().toString());
        outState.putBoolean("isDataUploaded", isDataUploaded);
        if (isDataUploaded)
            outState.putSerializable("messageList", new ArrayList<>(chatAdapter.getMessageList()));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        messInput.setText(savedInstanceState.getString("mess"));
        showMessages((List<Message>) savedInstanceState.getSerializable("messageList"));
    }

    private void setActionBar() {
        actionBar = getSupportActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.custom_toolbar);

        Toolbar toolbar=(Toolbar) actionBar.getCustomView().getParent();
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.getContentInsetEnd();
        toolbar.setPadding(0, 0, 0, 0);
    }

    private void showMessages(List<Message> messageList) {
        isDataUploaded = true;

        if (messageList.size() != 0) {
            chatAdapter = new ChatAdapter(getApplicationContext(), messageList,
                    preferenceHelper.getName(), companionName, preferenceHelper.getAvatar(), companionAvatar);
            chatView.setAdapter(chatAdapter);
        } else
            tvNoMess.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void uploadData() {
        progressBar.setVisibility(View.VISIBLE);

        if (!isConnected()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    findViewById(R.id.tryAgainView).setVisibility(View.VISIBLE);
                }
            }, 600);
            return;
        }

        RequestHelper requestHelper = new RequestHelper() {
            @Override
            public void onStatusOk(MessageResponseObject response) {
                showMessages(response.getData());
            }

            @Override
            public void onStatusServerError(MessageResponseObject response) {
                Log.d("myLogs", "server error");
            }

            @Override
            public void onFail(Throwable t) {
                Log.d("myLogs", "fail " + t.toString());
                t.printStackTrace();
            }
        };
        requestHelper.getDialogsById(callService, companionId,
                preferenceHelper.getId(), preferenceHelper.getSecretKey());
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public Long getCompanionId() { return this.companionId; }

    @Override
    public void setTyping(boolean typing) { isTyping = typing; }

    @Override
    public void onMessageReceived(final Message message) {
        message.setCompanion(companionId);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatAdapter.getMessageList().add(message);
                chatAdapter.notifyItemInserted(chatAdapter.getItemCount() - 1);
                chatManager.scrollToPosition(chatAdapter.getItemCount() - 1);

                messInput.setText("");
            }
        });
        Log.d("myLogs", message.getBody());
    }

    @Override
    public void onTypePending() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatView.setPadding(0, 0, 0, 100);
                findViewById(R.id.icTyping).setVisibility(View.VISIBLE);
                animation.start();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isTyping = false;

                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isTyping) return;

                                chatView.setPadding(0,0,0,0);
                                findViewById(R.id.icTyping).setVisibility(View.GONE);
                            }
                        }, 1500);
                    }
                }, 500);
            }
        });
    }

    @Override
    public void stopTyping() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatView.setPadding(0, 0, 0, 0);
                findViewById(R.id.icTyping).setVisibility(View.GONE);
            }
        });
    }
}
