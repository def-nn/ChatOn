package com.app.chaton;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
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

public class ChatActivity extends AppCompatActivity{

    private static final String NOT_EDITED = "not_edited";
    private static final String EDITED = "edited";

    private Long companionId;

    private Button btnSend;
    private ImageButton btnConfig, btnFriendList, btnAttach;
    private EditText messInput;
    private ProgressBar progressBar;

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

        uploadData();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnConfig = (ImageButton) findViewById(R.id.btnConfig);
        btnFriendList = (ImageButton) findViewById(R.id.btnFriendsList);
        btnAttach = (ImageButton) findViewById(R.id.btnAttach);
        messInput = (EditText) findViewById(R.id.messInput);
        TextView tvChat = (TextView) findViewById(R.id.tvChat);

        Typeface myriad = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro.ttf");
        tvChat.setTypeface(myriad);
        btnSend.setTypeface(myriad);
        messInput.setTypeface(myriad);

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

    private void uploadData() {
        RequestHelper requestHelper = new RequestHelper() {
            @Override
            public void onStatusOk(MessageResponseObject response) {
                Log.d("myLogs", "ok " + response.getData().toString());
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
