package com.ldaca.app.myexpense.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.ldaca.app.myexpense.R;

public class LoginActivity extends AppCompatActivity {
    private CoordinatorLayout coordinatorLayout;
    private TextInputEditText email, password;
    private Button login, register;
    private SharedPreferences preferences;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        coordinatorLayout = findViewById(R.id.coordinator);
        preferences = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userE = email.getText().toString().trim();
                final String passwordE = password.getText().toString().trim();

                if (TextUtils.isEmpty(userE)){
                    email.setError("El correo es necesario");
                    return;
                }

                if (!isValidEmail(userE)){
                    email.setError("No es un correo válido");
                    return;
                }

                if (TextUtils.isEmpty(passwordE)){
                    password.setError("El password es necesario");
                    return;
                }

                auth.signInWithEmailAndPassword(userE, passwordE)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()){
                                    Snackbar.make(coordinatorLayout,"Correo o contraseña incorrectos", Snackbar.LENGTH_SHORT).show();
                                }else{
                                    Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    salvarPreferencias(userE, passwordE);
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });
    }

    private boolean isValidEmail(String email){
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void salvarPreferencias(String email, String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }
}
