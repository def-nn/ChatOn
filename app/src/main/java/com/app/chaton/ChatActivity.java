package com.app.chaton;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.chaton.API_helpers.CallService;
import com.app.chaton.API_helpers.Message;
import com.app.chaton.API_helpers.MessageResponseObject;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.API_helpers.ServiceGenerator;
import com.app.chaton.sockets.SocketHelper;
import com.app.chaton.Utils.PreferenceHelper;
import com.app.chaton.adapter.ChatAdapter;
import com.app.chaton.sockets.SocketListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements SocketListener{

    private static final String NOT_EDITED = "not_edited";
    private static final String EDITED = "edited";

    private Long companionId;
    private String companionName;
    private boolean isDataUploaded;

    private ActionBar actionBar;
    private Button btnSend;
    private ImageButton btnAttach;
    private EditText messInput;
    private ProgressBar progressBar;
    private RecyclerView chatView;
    private TextView tvNoMess;

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

        chatView = (RecyclerView) findViewById(R.id.chatView);
        chatView.setHasFixedSize(true);

        chatManager = new LinearLayoutManager(getApplicationContext());
        chatManager.setStackFromEnd(true);
        chatView.setLayoutManager(chatManager);

        tvNoMess = (TextView) findViewById(R.id.tvNoMess);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (savedInstanceState == null || !savedInstanceState.getBoolean("isDataUploaded"))
            uploadData();
        else
            showMessages((List<Message>) savedInstanceState.getSerializable("messageList"));

        setActionBar();

        btnSend = (Button) findViewById(R.id.btnSend);
        btnAttach = (ImageButton) findViewById(R.id.btnAttach);
        messInput = (EditText) findViewById(R.id.messInput);

        TextView actionBarTitle = (TextView) actionBar.getCustomView().findViewById(R.id.actionBarTitle);
        actionBarTitle.setText(companionName);

        final Typeface myriad = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro.ttf");
        btnSend.setTypeface(myriad);
        messInput.setTypeface(myriad);
        actionBarTitle.setTypeface(myriad);

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
                socketHelper.notifyTyping(companionId);
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketHelper.send(companionId, messInput.getText().toString());
            }
        });
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
                    preferenceHelper.getName(),  companionName);
            chatView.setAdapter(chatAdapter);
        } else
            tvNoMess.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void uploadData() {
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

    public Long getCompanionId() { return this.companionId; }
}
