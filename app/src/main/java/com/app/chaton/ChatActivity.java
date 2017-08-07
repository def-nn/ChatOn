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
import com.app.chaton.Utils.DbHelper;
import com.app.chaton.Utils.ToastHelper;
import com.app.chaton.sockets.SocketHelper;
import com.app.chaton.Utils.PreferenceHelper;
import com.app.chaton.adapter.ChatAdapter;
import com.app.chaton.sockets.SocketListener;

import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity implements SocketListener{

    private static final String NOT_EDITED = "not_edited";
    private static final String EDITED = "edited";

    private Long companionId;
    private String companionName;
    private boolean isTyping;

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
    private DbHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        callService = ServiceGenerator.createService(CallService.class);
        preferenceHelper = new PreferenceHelper(getSharedPreferences(
                getResources().getString(R.string.PREFERENCE_FILE), MODE_PRIVATE));
        dbHelper = new DbHelper(getApplicationContext());

        socketHelper = ((WeTuneApplication) getApplication()).getSocketHelper();
        socketHelper.setSocketListener(this);

        Intent intent = getIntent();
        companionId = intent.getLongExtra(PreferenceHelper.ID, 0);
        companionName = intent.getStringExtra(PreferenceHelper.NAME);

        chatView = (RecyclerView) findViewById(R.id.chatView);
        chatView.setHasFixedSize(true);

        chatManager = new LinearLayoutManager(getApplicationContext());
        chatManager.setStackFromEnd(true);
        chatView.setLayoutManager(chatManager);

        tvNoMess = (TextView) findViewById(R.id.tvNoMess);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnTryAgain = (Button) findViewById(R.id.btnTryAgain);

        uploadData();
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
                final HashMap<String, Object> mess_data = new HashMap<>();
                mess_data.put(Message.ID, 0L);
                mess_data.put(Message.OWNER, preferenceHelper.getId());
                mess_data.put(Message.RECEIVER, companionId);
                mess_data.put(Message.MESSAGE, messInput.getText().toString());
                mess_data.put(Message.CREATED_AT, new Date().getTime() / 1000);

                final Message message = new Message(mess_data);
                message.setCompanion(companionId);
                message.setState(Message.STATE_PROCESS);

                dbHelper.addMessToDb(message, companionId);
                chatAdapter.setCursor(dbHelper.getMessageList(companionId));
                updateChatView();

                try {
                    socketHelper.send(companionId, message.getBody());
                } catch (Exception e) {
                    dbHelper.changeMessageState(message.getTempId(), Message.STATE_FAILURE, companionId);
//                    chatView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                            chatView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                        } else {
//                            chatView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                        }
//
//                        View view = chatView.getLayoutManager().findViewByPosition(chatAdapter.getItemCount() - 1);
//                        view.findViewById(R.id.icProcess).setVisibility(View.GONE);
//                        view.findViewById(R.id.icError).setVisibility(View.VISIBLE);
//                    }
//                    });
                    chatAdapter.setCursor(dbHelper.getMessageList(companionId));
                    updateChatView();
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
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        messInput.setText(savedInstanceState.getString("mess"));
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

    private void loadMessagesFromDb() {
        chatAdapter = new ChatAdapter(dbHelper.getMessageList(companionId));
        chatView.setAdapter(chatAdapter);
        progressBar.setVisibility(View.GONE);
    }

    private void uploadData() {
        progressBar.setVisibility(View.VISIBLE);

        if (dbHelper.messagesInDb(companionId)) {
            loadMessagesFromDb();
            return;
        }

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
                dbHelper.writeMessagesToDb(response.getData(), companionId);
                loadMessagesFromDb();
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
        if (!message.to().equals(companionId) ||
                (message.to().equals(companionId) &&
                 dbHelper.changeMessageState(
                         message.getTempId(), Message.STATE_SUCCESS, message.getId()) == 0)) {
            stopTyping();
            message.setState(Message.STATE_SUCCESS);
            message.setCompanion(companionId);
            dbHelper.addMessToDb(message, companionId);

            chatAdapter.setCursor(dbHelper.getMessageList(companionId));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateChatView();
                }
            });
        }
    }

    private void updateChatView() {
        chatAdapter.notifyItemInserted(chatAdapter.getItemCount() - 1);
        chatManager.scrollToPosition(chatAdapter.getItemCount() - 1);
        messInput.setText("");

    }

    @Override
    public void onTypePending() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatView.setPadding(0, 0, 0, 100);
                findViewById(R.id.icTyping).setVisibility(View.VISIBLE);
                animation.start();
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
