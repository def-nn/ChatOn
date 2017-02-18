package com.app.chaton;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.app.chaton.API_helpers.CallService;
import com.app.chaton.API_helpers.Message;
import com.app.chaton.API_helpers.MessageResponseObject;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.API_helpers.RequestObject;
import com.app.chaton.API_helpers.ServiceGenerator;
import com.app.chaton.Utils.PreferenceHelper;
import com.app.chaton.Utils.ToastHelper;
import com.app.chaton.adapter.DialogAdapter;
import com.baoyz.widget.PullRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class DialogActivity extends AppCompatActivity implements PullRefreshLayout.OnRefreshListener{

    private static final String NOT_EDITED = "not_edited";
    private static final String EDITED = "edited";

    private boolean isDataUploaded;

    private CallService callService;
    private PreferenceHelper preferenceHelper;
    private RequestHelper requestHelper;

    private RecyclerView dialogsView;
    private LinearLayoutManager dialogsManager;
    private DialogAdapter dialogAdapter;

    private PullRefreshLayout refreshDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        callService = ServiceGenerator.createService(CallService.class);
        preferenceHelper = new PreferenceHelper(getSharedPreferences(
                getResources().getString(R.string.PREFERENCE_FILE), MODE_PRIVATE));

        setContentView(R.layout.dialog_list);

        refreshDialog = (PullRefreshLayout) findViewById(R.id.refreshDialogs);
        refreshDialog.setOnRefreshListener(this);

        dialogsView = (RecyclerView) findViewById(R.id.dialogsList);
        dialogsView.setHasFixedSize(false);

        dialogsManager = new LinearLayoutManager(getApplicationContext());
        dialogsView.setLayoutManager(dialogsManager);

        if (savedInstanceState == null || !savedInstanceState.getBoolean("isDataUploaded"))
            uploadDialogsList();
        else
            setDialogPanel((List<Message>) savedInstanceState.getSerializable("messageList"));

        refreshDialog.setRefreshStyle(PullRefreshLayout.STYLE_RING);
        refreshDialog.setColorSchemeColors(
                ContextCompat.getColor(getApplicationContext(), R.color.colorInputDark),
                ContextCompat.getColor(getApplicationContext(), R.color.colorMenuFg),
                ContextCompat.getColor(getApplicationContext(), R.color.colorInputDark),
                ContextCompat.getColor(getApplicationContext(), R.color.colorMenuFg));

        EditText searchInput = (EditText) findViewById(R.id.searchInput);
        searchInput.setOnFocusChangeListener(onFocusChangeListener);

        Typeface myriad = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro.ttf");
        searchInput.setTypeface(myriad);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDataUploaded", isDataUploaded);
        if (isDataUploaded)
            outState.putSerializable("messageList", new ArrayList<>(dialogAdapter.getMessageList()));
    }

    @Override
    public void onRefresh() {
        uploadDialogsList();
    }

    private void uploadDialogsList() {
        requestHelper = new RequestHelper() {
            @Override
            public void onStatusOk(MessageResponseObject response) {
                Log.d("myLOgs", "status ok");
                refreshDialog.setRefreshing(false);
                setDialogPanel(response.getData());
            }

            @Override
            public void onStatusServerError(MessageResponseObject response) {
                ToastHelper.makeToast("Server error");
            }

            @Override
            public void onFail(Throwable t) {
                ToastHelper.makeToast(t.toString());
                t.printStackTrace();
            }
        };

        Long id = preferenceHelper.getId();
        String secret_key = preferenceHelper.getSecretKey();

        requestHelper.getDialogs(callService, new RequestObject(new Object(), id, secret_key), id, secret_key);
    }

    private void setDialogPanel(List<Message> messageList) {
        isDataUploaded = true;
        dialogAdapter = new DialogAdapter(getApplicationContext(), messageList,
                new DialogListenerFactory());
        dialogsView.setAdapter(dialogAdapter);
        findViewById(R.id.dialogsList).setVisibility(View.VISIBLE);
        findViewById(R.id.progressView).setVisibility(View.GONE);
    }

    public class DialogListenerFactory {
        public View.OnClickListener createListener(final Long companionId, final String companionName) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DialogActivity.this, ChatActivity.class);
                    intent.putExtra(PreferenceHelper.ID, companionId);
                    intent.putExtra(PreferenceHelper.NAME, companionName);
                    startActivity(intent);
                }
            };
        }
    }

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
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
    };
}
