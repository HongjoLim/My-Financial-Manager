package com.hongjolim.mfmanager.model;

public class CreditCard {

    private int id;
    private String name;
    private int payDate;
    private int account_id;
    private String amount;
    private int cycleStart;
    private int cycleEnd;

    public CreditCard(){}

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

    public int getPayDate() {
        return payDate;
    }

    public void setPayDate(int payDate) {
        this.payDate = payDate;
    }

    public int getAccount_id() {
        return account_id;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getCycleStart() {
        return cycleStart;
    }

    public void setCycleStart(int cycleStart) {
        this.cycleStart = cycleStart;
    }

    public int getCycleEnd() {
        return cycleEnd;
    }

    public void setCycleEnd(int cycleEnd) {
        this.cycleEnd = cycleEnd;
    }
}
