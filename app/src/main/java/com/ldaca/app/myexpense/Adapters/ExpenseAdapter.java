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
import com.ldaca.app.myexpense.Models.Expense;
import com.ldaca.app.myexpense.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExpenseAdapter extends FirebaseRecyclerAdapter<Expense, ExpenseAdapter.ViewHolder> {
    public static final int DEFAULT = 0;
    public static final int BLUE = 1;
    public static final int GREEN = 2;
    public static final int ROSE = 3;
    public static final int EDIT_CONTEXT = 100;
    public static final int CERRAR_CONTEXT = 101;
    public static final int ELIMINAR_CONTEXT = 102;
    private OnItemClickListener listener;
    private OnDataChangedValueListener changedValueListener;
    private OnItemCheckListener checkListener;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String userId;
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private Context context;

    public ExpenseAdapter(@NonNull FirebaseRecyclerOptions<Expense> options, Context context) {
        super(options);
        this.context = context;
    }


    public void changeColor(int posicion, int direccion) {

        Expense expense = getSnapshots().get(posicion);

        int category = expense.getCategory();
        if (category > 3 || category < 0) category = 0;
        if (direccion == ItemTouchHelper.LEFT) {
            if (category == 3) {
                category = 0;
            } else {
                category++;
            }
        } else if (direccion == ItemTouchHelper.RIGHT) {
            if (category == 0) {
                category = 3;
            } else {
                category--;
            }
        }
        expense.setCategory(category);
        userId = auth.getUid();
        String key = getSnapshots().getSnapshot(posicion).getRef().getKey();
        dbRef.child("expenses/" + key).setValue(expense);
        dbRef.child("user-expenses/" + userId + "/" + key).setValue(expense);
    }

    public int sumatoria() {
        int total = 0;
        for (Expense note : getSnapshots()) {
            total = total + note.getMount();
        }
        return total;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, final int position, @NonNull final Expense model) {
        holder.mount.setText("$ " + model.getMount());
        holder.name.setText(model.getName());
        holder.currentDate.setText(new SimpleDateFormat("hh:mm aaa dd/MM/yyyy", Locale.getDefault()).format(model.getDateCreate()));

        if (model.isState()) {
            holder.imgCheck.setVisibility(View.INVISIBLE);
        } else {
            holder.imgCheck.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case BLUE:
                return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.note_item_blue, viewGroup, false));
            case GREEN:
                return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.note_item_green, viewGroup, false));
            case ROSE:
                return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.note_item_rose, viewGroup, false));
            default:
                return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.note_item_default, viewGroup, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getCategory();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private TextView mount, name;
        private TextView currentDate;
        private ImageView imgCheck;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mount = itemView.findViewById(R.id.mount);
            name = itemView.findViewById(R.id.name);
            currentDate = itemView.findViewById(R.id.fecha);
            imgCheck = itemView.findViewById(R.id.checkbox);
            cardView = itemView.findViewById(R.id.cardview);
            cardView.setOnCreateContextMenuListener(this);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int posicion = getAdapterPosition();
                    if (posicion != RecyclerView.NO_POSITION && listener != null) {
                        listener.OnItemClick(getItem(posicion));
                    }
                }
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if (getSnapshots().get(getAdapterPosition()).isState()) {
                menu.add(this.getAdapterPosition(), EDIT_CONTEXT, 0, "Editar Gasto");
                menu.add(this.getAdapterPosition(), CERRAR_CONTEXT, 1, "Cerrar Gasto");
                menu.add(this.getAdapterPosition(), ELIMINAR_CONTEXT, 2, "Eliminar Gasto");
            } else {
                menu.add(this.getAdapterPosition(), CERRAR_CONTEXT, 1, "Habilitar Gasto");
                menu.add(this.getAdapterPosition(), ELIMINAR_CONTEXT, 2, "Eliminar Gasto");
            }
        }
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        changedValueListener.OnDataChangedValue(sumatoria());
    }

    public interface OnItemClickListener {
        public void OnItemClick(Expense expense);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnDataChangedValueListener {
        public void OnDataChangedValue(int suma);
    }

    public void setChangedValueListener(OnDataChangedValueListener changedValueListener) {
        this.changedValueListener = changedValueListener;
    }

    public interface OnItemCheckListener {
        public void OnItemCheck(String key);
    }

    public void setOnItemCheckListener(OnItemCheckListener onItemCheckListener) {
        this.checkListener = onItemCheckListener;
    }
}
