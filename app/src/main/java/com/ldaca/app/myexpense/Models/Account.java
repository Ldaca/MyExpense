package com.ldaca.app.myexpense.Models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.ldaca.app.myexpense.Adapters.AccountAdapter;

import java.util.Date;

@IgnoreExtraProperties
public class Account {
    private String name_account;
    private String user_id;
    // cuatro valores *normal *corazón *casa *trabajo
    private int category;
    private Date create_date_account;
    private boolean state;

    public Account() {
    }

    public Account(String name_account, String user_id, Date currentDate) {
        this.name_account = name_account;
        this.user_id = user_id;
        this.category = AccountAdapter.DEFAULT;
        this.create_date_account = currentDate;
        this.state = true;
    }

    public Account(String name_account, String user_id, int category, Date currentDate) {
        this.name_account = name_account;
        this.user_id = user_id;
        this.category = category;
        this.create_date_account = currentDate;
        this.state = true;
    }

    public String getName_account() {
        return name_account;
    }

    public void setName_account(String name_account) {
        this.name_account = name_account;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public Date getCreate_date_account() {
        return create_date_account;
    }

    public void setCreateDateAccount(Date create_date_account) {
        this.create_date_account = create_date_account;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Account{" +
                "name_account='" + name_account + '\'' +
                ", user_id='" + user_id + '\'' +
                ", category=" + category +
                ", create_date_account=" + create_date_account +
                ", state=" + state +
                '}';
    }
}
