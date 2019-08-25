package com.ldaca.app.myexpense.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private FirebaseAuth auth;
    private String correo;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
        correo = getMailPreference();
        password = getPasswordPreference();
        auth = FirebaseAuth.getInstance();



    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null){
            startActivity(new Intent(SplashActivity.this, AccountsActivity.class));
            finish();
        }else {
            signIn(correo, password);
        }
    }

    private void signIn(final String  correo, final String password){
        if (!TextUtils.isEmpty(correo) && !TextUtils.isEmpty(password)) {
            auth.signInWithEmailAndPassword(correo, password)
                    .addOnCompleteListener(SplashActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Intent intent = new Intent(SplashActivity.this, PreLoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                salvarPreferencias(correo,password);
                                startActivity(intent);
                            }
                        }
                    });
        } else {
            Intent intent = new Intent(SplashActivity.this, PreLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    public String getMailPreference() {
        return preferences.getString("email", "");
    }

    public String getPasswordPreference() {
        return preferences.getString("password", "");
    }

    private void salvarPreferencias(String email, String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }
}