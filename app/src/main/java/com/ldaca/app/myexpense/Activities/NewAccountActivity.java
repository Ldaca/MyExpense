package com.ldaca.app.myexpense.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ldaca.app.myexpense.Models.Account;
import com.ldaca.app.myexpense.Adapters.AccountAdapter;
import com.ldaca.app.myexpense.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class NewAccountActivity extends AppCompatActivity {
    private EditText detail, value, date;
    private FirebaseDatabase db;
    private DatabaseReference dBRefInit;
    private Toolbar aToolbar;
    private FirebaseAuth auth ;
    private String userId;
    private Date currentDate;
    private Calendar tmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);
        aToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(aToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }
        setTitle("Agregar Lista");


        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();

        db = FirebaseDatabase.getInstance();
        dBRefInit = db.getReference();

        detail = findViewById(R.id.detail);
        date = findViewById(R.id.time_date);
        currentDate = new Date();
        tmp = new GregorianCalendar();
        tmp.setTime(currentDate);
        date.setText(new SimpleDateFormat("hh:mm aaa dd/MM/yyyy", Locale.getDefault()).format(currentDate));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveAccount();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void saveAccount() {
        String mDetail = detail.getText().toString().trim();
        if (mDetail.isEmpty()) {
            Toast.makeText(this, "Por favor introducir una descripción de la cuenta", Toast.LENGTH_SHORT).show();
            return;
        }
        String key = dBRefInit.child("accounts").push().getKey();
        Account account = new Account(mDetail, userId, AccountAdapter.DEFAULT, currentDate);
        dBRefInit.child("accounts").child(key).setValue(account);
        showToast("¡Lista Creada!", R.layout.toast);
        finish();
    }

    private void showToast(String message, int layout){
        LayoutInflater layoutToast = getLayoutInflater();
        View view = layoutToast.inflate(layout, (ViewGroup) findViewById(R.id.root_toast));
        Toast toast = Toast.makeText(NewAccountActivity.this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        TextView textView = view.findViewById(R.id.msg_toast);
        textView.setText(message);
        toast.setView(view);
        toast.show();
    }
}
