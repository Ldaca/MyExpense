package com.ldaca.app.myexpense.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.ldaca.app.myexpense.Models.Account;
import com.ldaca.app.myexpense.Adapters.AccountAdapter;
import com.ldaca.app.myexpense.Models.Expense;
import com.ldaca.app.myexpense.R;


public class AccountsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private FirebaseDatabase db;
    private DatabaseReference dBRef;
    private FloatingActionButton fab;
    private FirebaseAuth auth;
    private Toolbar aToolbar;
    private String userId;
    private String accountLogin;
    private String accountName;
    private boolean isActiveExpense;
    private EditText editText;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);


        aToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(aToolbar);
        aToolbar.setLogo(R.mipmap.ic_launcher_expense);
        setTitle("Mis Listas");
        aToolbar.setTitleTextColor(getResources().getColor(R.color.colorDefault));
        preferences = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);

        db = FirebaseDatabase.getInstance();
//        db.setPersistenceEnabled(true);
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        accountLogin = auth.getCurrentUser().getEmail();

        aToolbar.setSubtitle(accountLogin);
        aToolbar.setSubtitleTextColor(getResources().getColor(R.color.colorDefault));
        dBRef = db.getReference("accounts");
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        Query query = dBRef.orderByChild("user_id").equalTo(userId);
        FirebaseRecyclerOptions<Account> options =
                new FirebaseRecyclerOptions.Builder<Account>()
                        .setQuery(query, Account.class)
                        .build();

        adapter = new AccountAdapter(options, getApplicationContext());


        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 || dy < 0 && fab.isShown()) {
                    fab.hide();
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
                if (direccion == ItemTouchHelper.RIGHT) {
                    adapter.changeCategory(viewHolder.getAdapterPosition(), direccion);
                } else {
                    String key = adapter.getSnapshots().getSnapshot(viewHolder.getAdapterPosition()).getRef().getKey();
                    showDialogForDeleteAccount("¿Desea Eliminar Lista?", key);
                }
                adapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new AccountAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(String id, String name) {
                Intent intent = new Intent(AccountsActivity.this, ExpenseActivity.class);
                intent.putExtra("account_id", id);
                intent.putExtra("account_name", name);
                startActivity(intent);
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountsActivity.this, NewAccountActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_session, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                cerrarSession();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void cerrarSession() {
        preferences.edit().clear().apply();
        Intent intent = new Intent(AccountsActivity.this, PreLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        auth.signOut();
    }

    private void showDialogForDeleteAccount(String title, final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (title != null) builder.setTitle(title);

        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAccount(key);
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    public void deleteAccount(final String key) {
        final DatabaseReference dbAccounts = FirebaseDatabase.getInstance().getReference("accounts");
        Query query = dbAccounts.getRoot().child("expenses").orderByChild("accounts_id").equalTo(key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    //Procedimiento seguido si tiene gastos asociados
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        Expense expense = item.getValue(Expense.class);
                        isActiveExpense = expense.isState();
                        if (isActiveExpense) {
                            showToast("Lista con gastos pendientes", R.layout.toast_error);
                            break;
                        }
                    }
                    if (!isActiveExpense) {
                        //Aqui elimino las listas que tienen gastos asociados, eliminando los gastos primero
                        AlertDialog.Builder builder = new AlertDialog.Builder(AccountsActivity.this);
                        builder.setTitle("Eliminar lista");
                        builder.setMessage("Confirma eliminar la lista y sus gastos asociados");
                        builder.setIcon(R.drawable.ic_warning_yellow);
                        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (DataSnapshot item : dataSnapshot.getChildren()) {
                                    Expense.deleteExpense(userId, item.getRef().getKey());
                                }
                                dbAccounts.child(key).removeValue();
                                showToast("¡Lista eliminada!", R.layout.toast);
                            }
                        });
                        builder.setNegativeButton("Cancelar", null);
                        builder.create().show();
                    }
                } else {
                    // Aqui elimino las listas que no tienen gastos asociados
                    dbAccounts.child(key).removeValue();
                    showToast("¡Lista eliminada!", R.layout.toast);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final String key = adapter.getSnapshots().getSnapshot(item.getGroupId()).getRef().getKey();
        switch (item.getItemId()) {
            case AccountAdapter.EDIT_CONTEXT:
                editAccount(key, new EditAccountDialog() {
                    @Override
                    public void setNameAccount(String accountNameEdit) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AccountsActivity.this);
                        builder.setTitle("Editar lista");
                        builder.setIcon(R.drawable.ic_edit);
                        View view = LayoutInflater.from(AccountsActivity.this).inflate(R.layout.dialog_edit_account, null);

                        builder.setView(view);
                        editText = view.findViewById(R.id.edit_name_account);
                        editText.setText(accountNameEdit);
                        builder.setPositiveButton("Editar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                accountName = editText.getText().toString().trim();
                                saveAccount(accountName, key);
                            }
                        });
                        builder.setNegativeButton("Cancelar", null);
                        builder.create().show();
                    }
                });
                return true;
            case AccountAdapter.DELETE_CONTEXT:
                showDialogForDeleteAccount("¿Desea Eliminar Lista?", key);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void editAccount(final String key, final EditAccountDialog editAccountDialog){
        Query query = dBRef.orderByKey().equalTo(key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    accountName = item.getValue(Account.class).getName_account();
                    editAccountDialog.setNameAccount(accountName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void saveAccount(String accountName, String key) {
        if (accountName.isEmpty()) {
            displayMessage("Por favor introducir una descripción de la cuenta");
            return;
        }
        dBRef.child(key).child("name_account").setValue(accountName);
        showToast("¡Lista Salvada!", R.layout.toast);
    }

    private void showToast(String message, int layout){
        LayoutInflater layoutToast = getLayoutInflater();
        View view = layoutToast.inflate(layout, (ViewGroup) findViewById(R.id.root_toast));
        Toast toast = Toast.makeText(AccountsActivity.this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        TextView textView = view.findViewById(R.id.msg_toast);
        textView.setText(message);
        toast.setView(view);
        toast.show();
    }

    private void displayMessage(String message){
        Snackbar.make(findViewById(R.id.rootView), message, Snackbar.LENGTH_LONG).show();
    }

    public interface EditAccountDialog{
        public void setNameAccount(String accountNameEdit);
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
