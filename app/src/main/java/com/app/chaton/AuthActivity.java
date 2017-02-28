package com.app.chaton;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.chaton.API_helpers.CallService;
import com.app.chaton.API_helpers.MapResponseObject;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.API_helpers.RequestObject;
import com.app.chaton.API_helpers.ServiceGenerator;
import com.app.chaton.API_helpers.User;
import com.app.chaton.Utils.ImageDownloader;
import com.app.chaton.Utils.DbHelper;
import com.app.chaton.Utils.PreferenceHelper;
import com.app.chaton.Utils.ToastHelper;
import com.app.chaton.ui.ProgressDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.io.IOException;


public class AuthActivity extends AppCompatActivity{

    private static final String NOT_EDITED = "not_edited";
    private static final String EDITED = "edited";

    ViewGroup rootView;
    EditText emailInput, passInput;
    Button btnLogIn;
    ProgressDialog progressDialog;

    CallService callService;
    PreferenceHelper preferenceHelper;
    DbHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth);

        callService = ServiceGenerator.createService(CallService.class);
        preferenceHelper = new PreferenceHelper(getSharedPreferences(
                getResources().getString(R.string.PREFERENCE_FILE), MODE_PRIVATE));
        dbHelper = new DbHelper(getApplicationContext());

        rootView = (ViewGroup) findViewById(R.id.rootView);

        emailInput = (EditText) findViewById(R.id.emailInput);
        passInput = (EditText) findViewById(R.id.passInput);

        emailInput.setOnFocusChangeListener(onFocusChangeListener);
        passInput.setOnFocusChangeListener(onFocusChangeListener);

        btnLogIn = (Button) findViewById(R.id.btnLogIn);
        btnLogIn.setOnClickListener(onAuth);

        Typeface myriad = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro.ttf");
        emailInput.setTypeface(myriad);
        passInput.setTypeface(myriad);
        btnLogIn.setTypeface(myriad);
        ((TextView) findViewById(R.id.tvReg)).setTypeface(myriad);
        ((TextView) findViewById(R.id.tvPass)).setTypeface(myriad);

        progressDialog = new ProgressDialog();
        progressDialog.setWindow(getWindow());

        passInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                hideKeyboard();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnLogIn.callOnClick();
                    handled = true;
                }
                return handled;
            }
        });
    }

    private View.OnClickListener onAuth = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!validateInput()) return;

            progressDialog.show(getSupportFragmentManager(), "ProgressDialog");
            User user = new User(emailInput.getText().toString(), passInput.getText().toString(), null);

            RequestHelper helper = new RequestHelper() {
                @Override
                public void onStatusOk(MapResponseObject response) {
                    Log.d("myLogs", response.getData().toString());
                    User user = new User(response.getData());
                    preferenceHelper.authUser(user);

                    new ImageDownloader(getApplicationContext(), user.getAvatar()).execute();

                    ((WeTuneApplication) getApplication()).connectToSocket(preferenceHelper.getId(),
                            preferenceHelper.getSecretKey());

                    progressDialog.dismiss();
                    Intent intent = new Intent(AuthActivity.this, DialogActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onStatusServerError(MapResponseObject response) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onStatus405(MapResponseObject response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            passInput.setText("");
                            progressDialog.dismiss();

                            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(100);
                            YoYo.with(Techniques.Shake).duration(600).playOn(passInput);
                            YoYo.with(Techniques.Shake).duration(600).playOn(findViewById(R.id.iconPass));
                            ToastHelper.makeToast(R.string.error_pass);
                        }
                    });
                }

                @Override
                public void onStatus406(MapResponseObject response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            passInput.setText("");
                            progressDialog.dismiss();

                            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(100);
                            YoYo.with(Techniques.Shake).duration(600).playOn(emailInput);
                            YoYo.with(Techniques.Shake).duration(600).playOn(passInput);
                            YoYo.with(Techniques.Shake).duration(600).playOn(findViewById(R.id.iconUser));
                            YoYo.with(Techniques.Shake).duration(600).playOn(findViewById(R.id.iconPass));
                            ToastHelper.makeToast(R.string.error_user);
                        }
                    });
                }

                @Override
                public void onFail(Throwable t) {
                    progressDialog.dismiss();
                    try {
                        throw t;
                    } catch (IOException e) {
                        ToastHelper.makeToast(R.string.error_connection);
                        ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(100);
                    } catch (Throwable e) {
                        ToastHelper.makeToast(R.string.error_oops);
                        ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(100);
                        e.printStackTrace();
                    }
                }
            };
            helper.auth(callService, new RequestObject(user));
        }
    };

    private boolean validateInput() {
        if (emailInput.getText().length() == 0 || passInput.getText().length() == 0 ||
                emailInput.getTag().equals(NOT_EDITED) || passInput.getTag().equals(NOT_EDITED)) {
            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(100);
            YoYo.with(Techniques.Shake).duration(600).playOn(emailInput);
            YoYo.with(Techniques.Shake).duration(600).playOn(passInput);
            YoYo.with(Techniques.Shake).duration(600).playOn(findViewById(R.id.iconUser));
            YoYo.with(Techniques.Shake).duration(600).playOn(findViewById(R.id.iconPass));
            ToastHelper.makeToast(R.string.error_empty);
            return false;
        }
        return true;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", emailInput.getText().toString());
        outState.putString("pass", passInput.getText().toString());
        outState.putString("email_tag", emailInput.getTag().toString());
        outState.putString("pass_tag", passInput.getTag().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        emailInput.setText(savedInstanceState.getString("email"));
        passInput.setText(savedInstanceState.getString("pass"));
        emailInput.setTag(savedInstanceState.getString("email_tag"));
        passInput.setTag(savedInstanceState.getString("pass_tag"));
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
