package com.ldaca.app.myexpense.Models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.ldaca.app.myexpense.Adapters.ExpenseAdapter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Expense {
    private String user_id;
    private String name;
    private int mount;
    private int category;
    private String accounts_id;
    private Date dateCreate;
    private boolean state;


    public Expense() {
    }


    public Expense(String name, int mount, String user_id, String accounts_id, Date dateCreate, boolean state) {
        this.name = name;
        this.mount = mount;
        this.category = ExpenseAdapter.DEFAULT;
        this.accounts_id = accounts_id;
        this.user_id = user_id;
        this.dateCreate = dateCreate;
        this.state = state;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMount() {
        return mount;
    }

    public void setMount(int mount) {
        this.mount = mount;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getAccounts_id() {
        return accounts_id;
    }

    public void setAccounts_id(String accounts_id) {
        this.accounts_id = accounts_id;
    }

    public Date getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "user_id='" + user_id + '\'' +
                ", name='" + name + '\'' +
                ", mount=" + mount +
                ", category=" + category +
                ", accounts_id='" + accounts_id + '\'' +
                ", dateCreate=" + dateCreate +
                ", state=" + state +
                '}';
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("userId", user_id);
        result.put("accountId", accounts_id);
        result.put("category", category);
        result.put("mount", mount);
        result.put("createDate", dateCreate);
        result.put("state", state);
        return result;
    }

    @Exclude
    public static void deleteExpense(String userId, String key) {
        DatabaseReference dbExpenses = FirebaseDatabase.getInstance().getReference();
        dbExpenses.child("expenses").child(key).removeValue();
        dbExpenses.child("user-expenses").child(userId).child(key).removeValue();
    }

    @Exclude
    public static void updateExpense(String userId, String key, Expense expense) {
        DatabaseReference dbExpenses = FirebaseDatabase.getInstance().getReference();
        dbExpenses.child("expenses/" + key).setValue(expense);
        dbExpenses.child("user-expenses/" + userId +"/" + key).setValue(expense);
    }
}
