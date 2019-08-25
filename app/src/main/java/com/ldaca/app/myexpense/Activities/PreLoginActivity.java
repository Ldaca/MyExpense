package com.ldaca.app.myexpense.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ldaca.app.myexpense.R;

public class PreLoginActivity extends AppCompatActivity {
    private Button register, login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_login);

        register = findViewById(R.id.crear_cuenta);
        login = findViewById(R.id.iniciar_sesion);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PreLoginActivity.this, RegisterActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PreLoginActivity.this, LoginActivity.class));
            }
        });
    }
}
