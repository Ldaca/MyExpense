package com.ldaca.app.myexpense.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ldaca.app.myexpense.Models.Expense;
import com.ldaca.app.myexpense.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class NewExpenseActivity extends AppCompatActivity {
    private EditText detail, value, date, time;
    private DatabaseReference dBRefInit;
    private String userId;
    private String accountId;
    private Date currentDate;
    private Calendar tmp;
    private int hour;
    private int minute;
    private int year;
    private int month;
    private int day;
    private int myDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_expense);

        Toolbar aToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(aToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }
        setTitle("Agregar Gasto");


        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        accountId = getIntent().getStringExtra("accountId");

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        dBRefInit = db.getReference();

        detail = findViewById(R.id.detail);
        value = findViewById(R.id.value);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        ImageView fecha = findViewById(R.id.ib_obtener_fecha);
        ImageView hora = findViewById(R.id.ib_obtener_hora);

        currentDate = new Date();
        tmp = new GregorianCalendar();
        tmp.setTime(currentDate);
        hour = tmp.get(Calendar.HOUR_OF_DAY);
        minute = tmp.get(Calendar.MINUTE);
        year = tmp.get(Calendar.YEAR);
        month = tmp.get(Calendar.MONTH);
        myDate = tmp.get(Calendar.DATE);
        day = tmp.get(Calendar.DAY_OF_MONTH);

        date.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentDate));
        time.setText(new SimpleDateFormat("hh:mm aaa", Locale.getDefault()).format(currentDate));

        hora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerHora();
            }
        });

        fecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerFecha();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerHora();
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerFecha();
            }
        });
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
                saveNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void saveNote() {
        String mDetail = detail.getText().toString().trim();
        String mValue = value.getText().toString().trim();

        if (mDetail.isEmpty() || mValue.isEmpty()) {
            Toast.makeText(NewExpenseActivity.this, "Por favor introducir una descripción y un valor", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = dBRefInit.child("expenses").push().getKey();
        Expense expense = new Expense(mDetail, Integer.valueOf(mValue), userId, accountId, currentDate, true);

        assert key != null;
        dBRefInit.child("expenses").child(key).setValue(expense);
        dBRefInit.child("user-expenses").child(userId).child(key).setValue(expense);
        showToast("Gasto Agregado", R.layout.toast);
        finish();
    }

    private void obtenerHora(){
        TimePickerDialog recogerHora = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                tmp.set(year, month, myDate, hourOfDay, minute);
                updateDate();
            }
        }, hour, minute, DateFormat.is24HourFormat(getApplicationContext()));

        recogerHora.show();
    }


    private void obtenerFecha(){
        DatePickerDialog recogerFecha = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                tmp.set(year, month, dayOfMonth, hour,minute );
                updateDate();
            }
        },year, month, day);
        recogerFecha.show();
    }

    private void updateDate(){
        currentDate.setTime(tmp.getTimeInMillis());
        tmp.setTime(currentDate);
        hour = tmp.get(Calendar.HOUR_OF_DAY);
        minute = tmp.get(Calendar.MINUTE);
        year = tmp.get(Calendar.YEAR);
        month = tmp.get(Calendar.MONTH);
        myDate = tmp.get(Calendar.DATE);
        day = tmp.get(Calendar.DAY_OF_MONTH);
        date.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentDate));
        time.setText(new SimpleDateFormat("hh:mm aaa", Locale.getDefault()).format(currentDate));
    }

    private void showToast(String message, int layout){
        LayoutInflater layoutToast = getLayoutInflater();
        View view = layoutToast.inflate(layout, (ViewGroup) findViewById(R.id.root_toast));
        Toast toast = Toast.makeText(NewExpenseActivity.this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        TextView textView = view.findViewById(R.id.msg_toast);
        textView.setText(message);
        toast.setView(view);
        toast.show();
    }
}

