package com.app.chaton;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.chaton.API_helpers.CallService;
import com.app.chaton.API_helpers.MessageResponseObject;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.API_helpers.ServiceGenerator;
import com.app.chaton.Utils.PreferenceHelper;
import com.app.chaton.adapter.ChatAdapter;

public class ChatActivity extends AppCompatActivity{

    private static final String NOT_EDITED = "not_edited";
    private static final String EDITED = "edited";

    private Long companionId;
    private String companionName;

    private ActionBar actionBar;
    private Button btnSend;
    private ImageButton btnAttach;
    private EditText messInput;
    private ProgressBar progressBar;
    private RecyclerView chatView;
    private TextView tvNoMess;

    private LinearLayoutManager chatManager;
    private ChatAdapter chatAdapter;

    CallService callService;
    PreferenceHelper preferenceHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        callService = ServiceGenerator.createService(CallService.class);
        preferenceHelper = new PreferenceHelper(getSharedPreferences(
                getResources().getString(R.string.PREFERENCE_FILE), MODE_PRIVATE));

        Intent intent = getIntent();
        companionId = intent.getLongExtra(PreferenceHelper.ID, 0);
        companionName = intent.getStringExtra(PreferenceHelper.NAME);

        chatView = (RecyclerView) findViewById(R.id.chatView);
        chatView.setHasFixedSize(false);

        chatManager = new LinearLayoutManager(getApplicationContext());
        chatManager.setStackFromEnd(true);
        chatView.setLayoutManager(chatManager);

        tvNoMess = (TextView) findViewById(R.id.tvNoMess);

        uploadData();

        setActionBar();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnAttach = (ImageButton) findViewById(R.id.btnAttach);
        messInput = (EditText) findViewById(R.id.messInput);

        TextView actionBarTitle = (TextView) actionBar.getCustomView().findViewById(R.id.actionBarTitle);
        actionBarTitle.setText(R.string.chat);

        Typeface myriad = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro.ttf");
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

    private void uploadData() {
        RequestHelper requestHelper = new RequestHelper() {
            @Override
            public void onStatusOk(MessageResponseObject response) {
                if (response.getData().size() != 0) {
                    chatAdapter = new ChatAdapter(getApplicationContext(), response.getData(),
                            preferenceHelper.getName(),  companionName);
                    chatView.setAdapter(chatAdapter);
                } else
                    tvNoMess.setVisibility(View.VISIBLE);

                progressBar.setVisibility(View.GONE);
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
}
