package com.example.hongjo.myfinancialmanager.model;

public class InCategory {

    private int _id;
    private String name;
    private String amount;
    private float totalMonthlyEarned;

    public InCategory(){}

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public float getTotalMonthlyEarned() {
        return totalMonthlyEarned;
    }

    public void setTotalMonthlyEarned(float totalMonthlySpent) {
        this.totalMonthlyEarned = totalMonthlySpent;
    }
}