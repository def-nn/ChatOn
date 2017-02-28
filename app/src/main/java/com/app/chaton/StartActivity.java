package com.app.chaton;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity{

    private Button btnSignUp, btnLogIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_screen);

        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnLogIn = (Button) findViewById(R.id.btnLogIn);

        Typeface myriad = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro.ttf");

        btnSignUp.setTypeface(myriad);
        btnLogIn.setTypeface(myriad);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, AuthActivity.class);
                startActivity(intent);
            }
        });
    }
}
