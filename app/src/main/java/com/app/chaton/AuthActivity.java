package com.app.chaton;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.app.chaton.API_helpers.CallService;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.API_helpers.RequestObject;
import com.app.chaton.API_helpers.ResponseObject;
import com.app.chaton.API_helpers.ServiceGenerator;
import com.app.chaton.API_helpers.User;

import retrofit2.Response;

public class AuthActivity extends AppCompatActivity{

    EditText emailInput, passInput;
    Button btnSignIn;
    TextView textError;

    CallService callService;
    PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth);

        callService = ServiceGenerator.createService(CallService.class);
        preferenceHelper = new PreferenceHelper(getSharedPreferences(
                getResources().getString(R.string.PREFERENCE_FILE), MODE_PRIVATE));

        emailInput = (EditText) findViewById(R.id.emailInput);
        passInput = (EditText) findViewById(R.id.passInput);
        textError = (TextView) findViewById(R.id.textError);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(onAuth);
    }

    private View.OnClickListener onAuth = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            User user = new User(emailInput.getText().toString(), passInput.getText().toString());

            RequestHelper helper = new RequestHelper() {
                @Override
                public void onStatusOk(Response<ResponseObject> response) {
                    User user = new User(response.body().getData());
                    preferenceHelper.authUser(user);

                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onStatusServerError(Response<ResponseObject> response) {
                    Log.d("myLogs", "Server Error");
                }

                @Override
                public void onStatus405(Response<ResponseObject> response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            passInput.setText("");
                            textError.setText(R.string.error_pass);
                        }
                    });
                }

                @Override
                public void onStatus406(Response<ResponseObject> response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            emailInput.setText("");
                            passInput.setText("");
                            textError.setText(R.string.error_user);
                        }
                    });
                }

                @Override
                public void onFail(Throwable t) {
                    Log.d("myLogs", "Failure: " + t.toString());
                }
            };
            helper.auth(callService, new RequestObject(user));
        }
    };
}
