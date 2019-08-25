package com.ldaca.app.myexpense.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.ldaca.app.myexpense.R;

public class RegisterActivity extends AppCompatActivity {
    private EditText nickname, email, password, passwordRepeat, bornDate;
    private RadioButton rbHombre, rbMujer;
    private FirebaseAuth auth;
    private ImagePicker imagePicker;
    private CameraImagePicker cameraPicker;

    private String pickerPath;
    private Uri fotoPerfilUri;
    private long fechaDeNacimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();

        nickname = findViewById(R.id.nickname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        passwordRepeat = findViewById(R.id.password_repeat);
        bornDate = findViewById(R.id.txtFechaDeNacimiento);

        Button register = findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nick = nickname.getText().toString();
                String userE = email.getText().toString();
                String passwordE = password.getText().toString();
                String passwordER = passwordRepeat.getText().toString();
                String fecha = bornDate.getText().toString();

                if (!validarPassword()) {
                    passwordRepeat.setError("Las contraseñas no coinciden");
                    return;
                }

                if (!isValidEmail(userE)) {
                    email.setError("No es un correo válido");
                    return;
                }

                if (TextUtils.isEmpty(passwordE)) {
                    password.setError("No es un correo válido");
                    return;
                }

                if (TextUtils.isEmpty(passwordER)) {
                    passwordRepeat.setError("Todos los campos son necesarios");
                    return;
                }

                if (TextUtils.isEmpty(userE)) {
                    email.setError("Todos los campos son necesarios");
                    return;
                }

                if (TextUtils.isEmpty(nick)) {
                    nickname.setError("Todos los campos son necesarios");
                    return;
                }

                if (TextUtils.isEmpty(fecha)) {
                    bornDate.setError("Todos los campos son necesarios");
                    return;
                }

                auth.createUserWithEmailAndPassword(userE, passwordE)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    showToast("Falló al realizar el registro", R.layout.toast_error);
                                } else {
                                    Intent intent = new Intent(RegisterActivity.this, AccountsActivity.class);
                                    startActivity(intent);
                                    showToast("Se ha creado el usuario", R.layout.toast);
                                    finish();
                                }
                            }
                        });
            }
        });
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean validarPassword() {
        String Password, PasswordRepetido;
        Password = password.getText().toString();
        PasswordRepetido = passwordRepeat.getText().toString();
        if (Password.equals(PasswordRepetido)) {
            return Password.length() >= 6 && Password.length() <= 16;
        } else return false;
    }

    private void showToast(String message, int layout) {
        LayoutInflater layoutToast = getLayoutInflater();
        View view = layoutToast.inflate(layout, (ViewGroup) findViewById(R.id.root_toast));
        Toast toast = Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        TextView textView = view.findViewById(R.id.msg_toast);
        textView.setText(message);
        toast.setView(view);
        toast.show();
    }
}
