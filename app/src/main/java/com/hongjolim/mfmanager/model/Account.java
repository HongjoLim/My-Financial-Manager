package com.hongjolim.mfmanager.model;

/**
 * Name: Hongjo Lim
 * Date: Apr 9th, 2018
 * Purpose: A model class to hold an account information that is retrieved from database
 */

public class Account {

    private int id;
    private String name;
    private String type;
    private String starting_balance;
    private String current_balance;

    public Account(){}

    public Account(String name, String type, String starting_balance, String current_balance) {

        this.name = name;
        this.type = type;
        this.starting_balance = starting_balance;
        this.current_balance = current_balance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStarting_balance() {
        return starting_balance;
    }

    public void setStarting_balance(String starting_balance) {
        this.starting_balance = starting_balance;
    }

    public String getCurrent_balance() {
        return current_balance;
    }

    public void setCurrent_balance(String current_balance) {
        this.current_balance = current_balance;
    }
}
