package com.app.chaton;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.app.chaton.API_helpers.CallService;
import com.app.chaton.API_helpers.MapResponseObject;
import com.app.chaton.API_helpers.Message;
import com.app.chaton.API_helpers.MessageResponseObject;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.API_helpers.RequestObject;
import com.app.chaton.API_helpers.ServiceGenerator;
import com.app.chaton.API_helpers.User;
import com.app.chaton.Utils.PreferenceHelper;
import com.app.chaton.Utils.ToastHelper;
import com.app.chaton.adapter.DialogAdapter;
import com.app.chaton.sockets.SocketHelper;
import com.baoyz.widget.PullRefreshLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DialogActivity extends AppCompatActivity implements PullRefreshLayout.OnRefreshListener{

    private static final String NOT_EDITED = "not_edited";
    private static final String EDITED = "edited";

    public static final String AUTH_REQUEST_SENT = "auth_sent";

    private boolean isDataUploaded, asyncRunning;

    private CallService callService;
    private PreferenceHelper preferenceHelper;
    private RequestHelper requestHelper;
    private SocketHelper socketHelper;

    private RecyclerView dialogsView;
    private LinearLayoutManager dialogsManager;
    private DialogAdapter dialogAdapter;
    private Button btnTryAgain;

    private PullRefreshLayout refreshDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        callService = ServiceGenerator.createService(CallService.class);
        preferenceHelper = new PreferenceHelper(getSharedPreferences(
                getResources().getString(R.string.PREFERENCE_FILE), MODE_PRIVATE));

        setContentView(R.layout.dialog_list);

        btnTryAgain = (Button) findViewById(R.id.btnTryAgain);
        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.tvTryAgain).setVisibility(View.GONE);
                btnTryAgain.setVisibility(View.GONE);
                findViewById(R.id.progressView).setVisibility(View.VISIBLE);
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

                ((WeTuneApplication) getApplication()).connectToSocket(preferenceHelper.getId(),
                        preferenceHelper.getSecretKey());
                uploadDialogsList();
            }
        });

        refreshDialog = (PullRefreshLayout) findViewById(R.id.refreshDialogs);
        refreshDialog.setOnRefreshListener(this);

        dialogsView = (RecyclerView) findViewById(R.id.dialogsList);
        dialogsView.setHasFixedSize(false);

        dialogsManager = new LinearLayoutManager(getApplicationContext());
        dialogsView.setLayoutManager(dialogsManager);

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
        btnTryAgain.setTypeface(myriad);
        ((TextView) findViewById(R.id.tvTryAgain)).setTypeface(myriad);

        if (!getIntent().getBooleanExtra(AUTH_REQUEST_SENT, true)) authUser(savedInstanceState);
//        else if (savedInstanceState == null) uploadDialogsList();
        else uploadDialogsList();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (socketHelper != null && socketHelper.hasNewMess()) uploadDialogsList();
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putBoolean("isDataUploaded", isDataUploaded);
//        if (isDataUploaded)
//            outState.putSerializable("messageList", new ArrayList<>(dialogAdapter.getMessageList()));
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        if (savedInstanceState.getBoolean("isDataUploaded"))
//            if (savedInstanceState.getSerializable("messageList") != null)
//                setDialogPanel((List<Message>) savedInstanceState.getSerializable("messageList"));
//            else uploadDialogsList();
//    }

    @Override
    public void onRefresh() {
        if (!RequestHelper.isConnected(getApplicationContext()))
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(100);
                    refreshDialog.setRefreshing(false);
                    ToastHelper.makeToast(R.string.error_connection);
                }
            }, 1000);
        else uploadDialogsList();
    }

    private void uploadDialogsList() {
        asyncRunning = true;

        requestHelper = new RequestHelper() {
            @Override
            public void onStatusOk(MessageResponseObject response) {
                refreshDialog.setRefreshing(false);
                setDialogPanel(response.getData());
                asyncRunning = false;
            }

            @Override
            public void onStatusServerError(MessageResponseObject response) {
                refreshDialog.setRefreshing(false);
                ToastHelper.makeToast("Server error");
                asyncRunning = false;
            }

            @Override
            public void onFail(Throwable t) {
                refreshDialog.setRefreshing(false);
                try {
                    throw t;
                } catch (IOException e) {
                    refreshDialog.setRefreshing(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            findViewById(R.id.tvTryAgain).setVisibility(View.VISIBLE);
                            btnTryAgain.setVisibility(View.VISIBLE);
                        }
                    }, 600);
                } catch (Throwable e) {
                    ToastHelper.makeToast(R.string.error_oops);
                    ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(100);
                    e.printStackTrace();
                }
                asyncRunning = false;
            }
        };

        Long id = preferenceHelper.getId();
        String secret_key = preferenceHelper.getSecretKey();

        requestHelper.getDialogs(callService, new RequestObject(new Object(), id, secret_key), id, secret_key);
    }

    private void setDialogPanel(List<Message> messageList) {
        isDataUploaded = true;
        dialogAdapter = new DialogAdapter(this, messageList, new DialogListenerFactory(), preferenceHelper);
        dialogsView.setAdapter(dialogAdapter);
        dialogsView.setVisibility(View.VISIBLE);
        findViewById(R.id.progressView).setVisibility(View.GONE);
    }

    private void authUser(@Nullable final Bundle savedInstanceState) {
        final User user = new User(preferenceHelper.getEmail(), null, preferenceHelper.getSecretKey());

        RequestHelper helper = new RequestHelper() {
            @Override
            public void onStatusOk(MapResponseObject response) {
                User user = new User(response.getData());
                preferenceHelper.authUser(user);

                ((WeTuneApplication) getApplication()).connectToSocket(preferenceHelper.getId(),
                        preferenceHelper.getSecretKey());
                socketHelper = ((WeTuneApplication) getApplication()).getSocketHelper();

                uploadDialogsList();
            }

            @Override
            public void onStatusServerError(MapResponseObject response) {
                preferenceHelper.reset();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(DialogActivity.this, StartActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onStatus405(MapResponseObject response) { onStatusServerError(response); }

            @Override
            public void onStatus406(MapResponseObject response) { onStatusServerError(response); }

            @Override
            public void onFail(final Throwable t) {
                try { throw t; }
                catch (IOException e) { uploadDialogsList(); }
                catch (Throwable e) { e.printStackTrace(); }
            }
        };
        helper.auth(callService, new RequestObject(user));
    }

    public class DialogListenerFactory {
        public View.OnClickListener createListener(final Long companionId, final String companionName,
                                                   final String companionAvatar) {
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
