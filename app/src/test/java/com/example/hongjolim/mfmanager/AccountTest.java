package com.example.hongjolim.mfmanager;

import com.example.hongjolim.mfmanager.model.Account;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AccountTest {

    Account account;

    @Before
    public void setUp(){

        account = new Account();

    }

    @Test
    public void getId() {

        assertNull(account.getCurrent_balance());
    }

    @Test
    public void setId() {
    }

    @Test
    public void getName() {
    }

    @Test
    public void setName() {
    }

    @Test
    public void getType() {
    }

    @Test
    public void setType() {
    }

    @Test
    public void getStarting_balance() {
    }

    @Test
    public void setStarting_balance() {
    }

    @Test
    public void getCurrent_balance() {
    }

    @Test
    public void setCurrent_balance() {
    }
}