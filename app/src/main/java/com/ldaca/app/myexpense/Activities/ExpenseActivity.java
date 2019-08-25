package com.ldaca.app.myexpense.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ldaca.app.myexpense.Adapters.ExpenseAdapter;
import com.ldaca.app.myexpense.Models.Expense;
import com.ldaca.app.myexpense.R;

import static com.ldaca.app.myexpense.Adapters.ExpenseAdapter.CERRAR_CONTEXT;
import static com.ldaca.app.myexpense.Adapters.ExpenseAdapter.EDIT_CONTEXT;
import static com.ldaca.app.myexpense.Adapters.ExpenseAdapter.ELIMINAR_CONTEXT;

public class ExpenseActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private FirebaseDatabase db;
    private DatabaseReference dBRef;
    private FloatingActionButton fab;
    private FirebaseAuth auth;
    private TextView total;
    private Toolbar aToolbar;
    private String userId;
    private String accountId;
    private String accountName;
    private SharedPreferences preferences;
    private CardView cardViewTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);


        aToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(aToolbar);
        aToolbar.setLogo(R.mipmap.ic_launcher_expense);
        setTitle("Gastos:");
        aToolbar.setTitleTextColor(getResources().getColor(R.color.colorDefault));
        preferences = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        Intent intent = getIntent();

        if (intent.getStringExtra("account_id") != null) {
            accountId = intent.getStringExtra("account_id");
            accountName = intent.getStringExtra("account_name");
        } else {
            accountId = preferences.getString("accountId", "");
            accountName = preferences.getString("accountName", "");
        }

        aToolbar.setSubtitle(accountName);
        aToolbar.setSubtitleTextColor(getResources().getColor(R.color.colorDefault));
        dBRef = db.getReference();
//        db.setPersistenceEnabled(true);
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recycler);
        total = findViewById(R.id.total);
        cardViewTotal = findViewById(R.id.cardview_total);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        Query query = dBRef.child("user-expenses").child(userId).orderByChild("accounts_id").equalTo(accountId);

        FirebaseRecyclerOptions<Expense> options =
                new FirebaseRecyclerOptions.Builder<Expense>()
                        .setQuery(query, Expense.class)
                        .build();

        adapter = new ExpenseAdapter(options, getApplicationContext());


        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show();
                    cardViewTotal.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 || dy < 0 && fab.isShown()) {
                    fab.hide();
                    cardViewTotal.setVisibility(View.GONE);
                }
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direccion) {
                adapter.changeColor(viewHolder.getAdapterPosition(), direccion);
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new ExpenseAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(Expense expense) {
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpenseActivity.this, NewExpenseActivity.class);
                intent.putExtra("accountId", accountId);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("accountId", accountId);
                editor.putString("accountName", accountName);
                editor.apply();
                startActivity(intent);
            }
        });

        adapter.setChangedValueListener(new ExpenseAdapter.OnDataChangedValueListener() {
            @Override
            public void OnDataChangedValue(int suma) {
                total.setText("$ " + String.valueOf(suma));
            }
        });

        adapter.setOnItemCheckListener(new ExpenseAdapter.OnItemCheckListener() {
            @Override
            public void OnItemCheck(String key) {
                changeState(key);
            }
        });
    }

    private void showDialogForDelete(String title, final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (title != null) builder.setTitle(title);

        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Expense.deleteExpense(auth.getUid(), key);
                showToast("¡Gasto eliminado!", R.layout.toast);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final String key = adapter.getSnapshots().getSnapshot(item.getGroupId()).getRef().getKey();
        if (key != null) {
            switch (item.getItemId()) {
                case EDIT_CONTEXT:
                    Intent intent = new Intent(ExpenseActivity.this, EditExpenseActivity.class);
                    intent.putExtra("key", key);
                    startActivity(intent);
                    return true;
                case CERRAR_CONTEXT:
                    changeState(key);
                    return true;
                case ELIMINAR_CONTEXT:
                    showDialogForDelete("Eliminar Gasto", key);
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        } else {
            return false;
        }
    }

    private void changeState(final String key){
        Query query = dBRef.child("expenses").orderByKey().equalTo(key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Expense expense = new Expense();
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    expense = item.getValue(Expense.class);
                }
                if (expense.isState()) {
                    dBRef.child("user-expenses").child(userId).child(key).child("state").setValue(false);
                    dBRef.child("expenses").child(key).child("state").setValue(false);
                    showToast("Gasto Cerrado", R.layout.toast);
                } else {
                    dBRef.child("user-expenses").child(userId).child(key).child("state").setValue(true);
                    dBRef.child("expenses").child(key).child("state").setValue(true);
                    showToast("Gasto Habilitado", R.layout.toast);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showToast(String message, int layout){
        LayoutInflater layoutToast = getLayoutInflater();
        View view = layoutToast.inflate(layout, (ViewGroup) findViewById(R.id.root_toast));
        Toast toast = Toast.makeText(ExpenseActivity.this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        TextView textView = view.findViewById(R.id.msg_toast);
        textView.setText(message);
        toast.setView(view);
        toast.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
