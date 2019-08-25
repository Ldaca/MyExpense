package com.ldaca.app.myexpense.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ldaca.app.myexpense.Models.Account;
import com.ldaca.app.myexpense.R;

import java.text.SimpleDateFormat;

public class AccountAdapter extends FirebaseRecyclerAdapter<Account, AccountAdapter.ViewHolder> {
    public static final int DEFAULT = 0;
    public static final int FAVORITE = 1;
    public static final int HOME = 2;
    public static final int WORK = 3;
    public static final int EDIT_CONTEXT = 100;
    public static final int DELETE_CONTEXT = 101;

    private OnItemClickListener listener;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String userId;
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private Context context;

    public AccountAdapter(@NonNull FirebaseRecyclerOptions<Account> options, Context context) {
        super(options);
        this.context = context;
    }

    public void changeCategory(int posicion, int direccion) {

        Account account = getSnapshots().get(posicion);

        int priority = account.getCategory();
        if (priority > 3 || priority < 0) priority = 0;
        if (direccion == ItemTouchHelper.LEFT) {
            if (priority == 3) {
                priority = 0;
            } else {
                priority++;
            }
        } else if (direccion == ItemTouchHelper.RIGHT) {
            if (priority == 0) {
                priority = 3;
            } else {
                priority--;
            }
        }
        account.setCategory(priority);

        userId = auth.getUid();
        String key = getSnapshots().getSnapshot(posicion).getRef().getKey();
        dbRef.child("accounts").child(key).setValue(account);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, final int position, @NonNull final Account model) {
        holder.nameAccount.setText(model.getName_account());
        holder.imgType.setImageResource(R.drawable.like);
        if (model.getCreate_date_account() != null) {
            holder.currentDateAccount.setText(new SimpleDateFormat("hh:mm aaa dd/MM/yyyy").format(model.getCreate_date_account()));
        } else {
            holder.currentDateAccount.setText("Sin fecha encontrada");
        }

        switch (model.getCategory()) {
            case HOME:
                holder.imgType.setImageResource(R.drawable.mansion);
                break;
            case WORK:
                holder.imgType.setImageResource(R.drawable.businessman);
                break;
            case FAVORITE:
                holder.imgType.setImageResource(R.drawable.heart);
                break;
            default:
                holder.imgType.setImageResource(R.drawable.like);
        }

        holder.imgType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCategory(holder.getAdapterPosition(), ItemTouchHelper.LEFT);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.account_item_default, viewGroup, false));
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private TextView nameAccount, currentDateAccount;
        private ImageView imgType;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameAccount = itemView.findViewById(R.id.name_account);
            currentDateAccount = itemView.findViewById(R.id.fecha_account);
            imgType = itemView.findViewById(R.id.icon_important);
            cardView = itemView.findViewById(R.id.cardview);
            cardView.setOnCreateContextMenuListener(this);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int posicion = getAdapterPosition();
                    if (posicion != RecyclerView.NO_POSITION && listener != null) {
                        String id = getSnapshots().getSnapshot(posicion).getRef().getKey();
                        String name = getSnapshots().get(posicion).getName_account();
                        listener.OnItemClick(id, name);
                    }
                }
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(), EDIT_CONTEXT, 0, "Editar Lista");
            menu.add(this.getAdapterPosition(), DELETE_CONTEXT, 1, "Eliminar Lista");
        }
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
    }

    public interface OnItemClickListener {
        void OnItemClick(String id, String name);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.listener = onItemClickListener;
    }
}

