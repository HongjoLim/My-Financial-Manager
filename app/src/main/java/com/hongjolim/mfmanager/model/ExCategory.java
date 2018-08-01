package com.hongjolim.mfmanager.model;

public class ExCategory {

    private int _id;
    private String name;
    private String amount;
    private float totalMonthlySpent;

    public ExCategory(){}

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

    public float getTotalMonthlySpent() {
        return totalMonthlySpent;
    }

    public void setTotalMonthlySpent(float totalMonthlySpent) {
        this.totalMonthlySpent = totalMonthlySpent;
    }
}
