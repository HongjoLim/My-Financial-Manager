package com.example.hongjo.myfinancialmanager.model;

public class User {

    //this class needs 2 fields
    private String email;
    private String password;

    //the default constructor
    public User() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
