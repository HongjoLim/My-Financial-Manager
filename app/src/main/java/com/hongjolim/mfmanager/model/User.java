package com.hongjolim.mfmanager.model;

public class User {

    //this class needs 2 fields
    private String email;
    private String password;

    /**
     * The number of user's additional security question
     * in case he/she forgets his/her id and password
     */
    private int securityQNum;

    /**
     * The answer to the additional security question

     */
    private String securityAnswer;

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

    public int getSecurityQNum() {
        return securityQNum;
    }

    public void setSecurityQNum(int securityQNum) {
        this.securityQNum = securityQNum;
    }

    public String getSecuirtyAnswer() {
        return securityAnswer;
    }

    public void setSecuirtyAnswer(String secuirtyAnswer) {
        this.securityAnswer = secuirtyAnswer;
    }
}
