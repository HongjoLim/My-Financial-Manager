package com.hongjolim.mfmanager.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hongjolim.mfmanager.model.Account;
import com.hongjolim.mfmanager.model.CreditCard;
import com.hongjolim.mfmanager.model.ExCategory;
import com.hongjolim.mfmanager.model.InCategory;
import com.hongjolim.mfmanager.model.Transaction;
import com.hongjolim.mfmanager.model.User;

import java.util.ArrayList;


public class DataSource {

    private Context mContext;
    private SQLiteOpenHelper mHelper;
    private SQLiteDatabase db;

    private Cursor cursor;

    public DataSource(Context context) {
        this.mContext = context;
        mHelper = new DBHelper(context);

        this.db = mHelper.getWritableDatabase();

    }

    public void open(){
        db = mHelper.getWritableDatabase();
    }

    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having, String orderBy) {
        return db.query(table, columns, selection,
                selectionArgs, groupBy, having, orderBy);
    }

    public void close(){
        mHelper.close();
    }

    public User getUser() {

        User user = new User();

        cursor = mContext.getContentResolver().query(DataProvider.USER_URI,
                UserTable.ALL_COLS, null, null, null);

        if(cursor.getCount()<=0) {
            return null;
        }else{
            try {
                while (cursor.moveToNext()) {

                    user.setEmail(cursor.getString(cursor.getColumnIndex(UserTable.COL1)));
                    user.setPassword(cursor.getString(cursor.getColumnIndex(UserTable.COL2)));
                    user.setSecurityQNum(cursor.getInt(cursor.getColumnIndex(UserTable.COL3)));
                    user.setSecuirtyAnswer(cursor.getString(cursor.getColumnIndex(UserTable.COL4)));
                }
            } finally {
                if (!cursor.isClosed() && cursor != null) {
                }
                cursor.close();
            }
            return user;
        }
    }



    public Transaction getTransaction(int transaction_id){

        String selection = TransactionTable.COL1+"=?";

        String[] selectionArgs = {String.valueOf(transaction_id)};

        cursor = mContext.getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                selection, selectionArgs, null);

        Transaction expense = new Transaction();

        try {
            while (cursor.moveToNext()) {
                expense.setId(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL1)));
                expense.setDate(cursor.getString(cursor.getColumnIndex(TransactionTable.COL2)));
                expense.setAmount(cursor.getString(cursor.getColumnIndex(TransactionTable.COL3)));
                expense.setDescription(cursor.getString(cursor.getColumnIndex(TransactionTable.COL4)));
                expense.setCategory_id(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL5)));
                expense.setAccount_id(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL6)));
                expense.setCredit_id(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL7)));
                expense.setTrans_type(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL8)));
            }
        }finally{
            if(!cursor.isClosed()&&cursor!=null){
                cursor.close();
            }
        }

        return expense;
    }

    public ArrayList<Transaction> getDebts(int creditId){

        //A transaction that has credit card id but no account id means it has not been paid
        String selection = TransactionTable.COL6+" IS NULL AND "+TransactionTable.COL7+"=?";

        String[] selectionArgs = {String.valueOf(creditId)};

        ArrayList<Transaction> debts = new ArrayList<>();

        cursor = mContext.getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                selection, selectionArgs, TransactionTable.COL1);

        try {
            while (cursor.moveToNext()) {
                Transaction expense = new Transaction();
                expense.setId(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL1)));
                expense.setDate(cursor.getString(cursor.getColumnIndex(TransactionTable.COL2)));
                expense.setAmount(cursor.getString(cursor.getColumnIndex(TransactionTable.COL3)));
                expense.setDescription(cursor.getString(cursor.getColumnIndex(TransactionTable.COL4)));
                expense.setCategory_id(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL5)));
                expense.setAccount_id(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL6)));
                expense.setCredit_id(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL7)));
                expense.setTrans_type(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL8)));

                debts.add(expense);
            }
        }finally{
            if(!cursor.isClosed()&&cursor!=null){
                cursor.close();
            }
        }

        return debts;
    }

    public ArrayList<Transaction> getAllIncome(){

        String selection = TransactionTable.COL8+"=?";

        String[] selectionArgs = {String.valueOf(TransactionTable.TRANS_TYPE2)};

        ArrayList<Transaction> incomes = new ArrayList<>();

        cursor = mContext.getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                selection, selectionArgs, TransactionTable.COL1);
        try {
            while (cursor.moveToNext()) {
                Transaction income = new Transaction();
                income.setId(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL1)));
                income.setDate(cursor.getString(cursor.getColumnIndex(TransactionTable.COL2)));
                income.setAmount(cursor.getString(cursor.getColumnIndex(TransactionTable.COL3)));
                income.setDescription(cursor.getString(cursor.getColumnIndex(TransactionTable.COL4)));
                income.setCategory_id(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL5)));
                income.setAccount_id(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL6)));
                income.setCredit_id(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL7)));
                income.setTrans_type(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL8)));

                incomes.add(income);
            }
        }finally {
            if(!cursor.isClosed()&&cursor!=null) {
                cursor.close();
            }
        }

        return incomes;
    }

    //this method creates query to get all the Accounts and returns the data in ArrayList of Accounts
    public ArrayList<Account> getAllAccounts(){
        ArrayList<Account> accounts = new ArrayList<>();

        cursor = mContext.getContentResolver().query(DataProvider.ACCOUNTS_URI, AccountsTable.ALL_COLS,
                null, null, AccountsTable.COL1);

        try{

            while(cursor.moveToNext()){
                Account account = new Account();
                account.setId(cursor.getInt(cursor.getColumnIndex(AccountsTable.COL1)));
                account.setName(cursor.getString(cursor.getColumnIndex(AccountsTable.COL2)));
                account.setType(cursor.getString(cursor.getColumnIndex(AccountsTable.COL3)));
                account.setStarting_balance(cursor.getString(cursor.getColumnIndex(AccountsTable.COL4)));
                account.setCurrent_balance(cursor.getString(cursor.getColumnIndex(AccountsTable.COL5)));

                accounts.add(account);
            }

        }finally{
            if(cursor!=null&!cursor.isClosed()){
                cursor.close();
            }
        }

        return accounts;
    }

    //this method is to get a specific account by the account id
    public Account getAccount(int accountId){

        String[] selectionArgs = {String.valueOf(accountId)};

        cursor = mContext.getContentResolver().query(DataProvider.ACCOUNTS_URI, AccountsTable.ALL_COLS,
                AccountsTable.COL1+"=?", selectionArgs, null);

        Account account = new Account();

        try {
            while (cursor.moveToNext()) {

                account.setId(cursor.getInt(cursor.getColumnIndex(AccountsTable.COL1)));
                account.setName(cursor.getString(cursor.getColumnIndex(AccountsTable.COL2)));
                account.setType(cursor.getString(cursor.getColumnIndex(AccountsTable.COL3)));
                account.setStarting_balance(cursor.getString(cursor.getColumnIndex(AccountsTable.COL4)));
                account.setCurrent_balance(cursor.getString(cursor.getColumnIndex(AccountsTable.COL5)));
            }
        }finally{
            if(cursor!=null&&!cursor.isClosed()){
                cursor.close();
            }
        }

        return account;
    }

    public ExCategory getExCategory(int exCategoryId){

        String[] selectionArgs = {String.valueOf(exCategoryId)};

        cursor = mContext.getContentResolver().query(DataProvider.EX_CATEGORY_URI, ExCategoryTable.ALL_COLS,
                ExCategoryTable.COL1+"=?", selectionArgs, null);

        ExCategory exCategory = new ExCategory();

        try {
            while (cursor.moveToNext()) {

                exCategory.set_id(cursor.getInt(cursor.getColumnIndex(ExCategoryTable.COL1)));
                exCategory.setName(cursor.getString(cursor.getColumnIndex(ExCategoryTable.COL2)));
                exCategory.setAmount(cursor.getString(cursor.getColumnIndex(ExCategoryTable.COL3)));
            }
        }finally{
            if(cursor!=null&&!cursor.isClosed()){
                cursor.close();
            }
        }

        return exCategory;
    }


    //this method creates query to get all the Expense categories and returns the data in ArrayList of Expense Category
    public ArrayList<ExCategory> getAllExCategories(){
        ArrayList<ExCategory> exCategories = new ArrayList<>();

        cursor = mContext.getContentResolver().query(DataProvider.EX_CATEGORY_URI, ExCategoryTable.ALL_COLS,
                null, null, ExCategoryTable.COL1);

        try{
            while(cursor.moveToNext()){
                ExCategory exCategory = new ExCategory();
                exCategory.set_id(cursor.getInt(cursor.getColumnIndex(ExCategoryTable.COL1)));
                exCategory.setName(cursor.getString(cursor.getColumnIndex(ExCategoryTable.COL2)));
                exCategory.setAmount(cursor.getString(cursor.getColumnIndex(ExCategoryTable.COL3)));
                exCategories.add(exCategory);
            }
        }finally{
            if(cursor!=null&&!cursor.isClosed())
                cursor.close();
        }
        return exCategories;
    }

    public InCategory getInCategory(int inCategoryId){

        String[] selectionArgs = {String.valueOf(inCategoryId)};

        cursor = mContext.getContentResolver().query(DataProvider.IN_CATEGORY_URI, InCategoryTable.ALL_COLS,
                InCategoryTable.COL1+"=?", selectionArgs, null);

        InCategory inCategory = new InCategory();

        try {
            while (cursor.moveToNext()) {

                inCategory.set_id(cursor.getInt(cursor.getColumnIndex(InCategoryTable.COL1)));
                inCategory.setName(cursor.getString(cursor.getColumnIndex(InCategoryTable.COL2)));
                inCategory.setAmount(cursor.getString(cursor.getColumnIndex(InCategoryTable.COL3)));
            }
        }finally{
            if(cursor!=null&&!cursor.isClosed()){
                cursor.close();
            }
        }

        return inCategory;
    }

    //this method creates query to get all the income categories and returns the data in ArrayList of Income Category
    public ArrayList<InCategory> getAllInCategories(){
        ArrayList<InCategory> inCategories = new ArrayList<>();

        cursor = mContext.getContentResolver().query(DataProvider.IN_CATEGORY_URI, InCategoryTable.ALL_COLS,
                null, null, InCategoryTable.COL1);

        try{
            while(cursor.moveToNext()){
                InCategory inCategory = new InCategory();
                inCategory.set_id(cursor.getInt(cursor.getColumnIndex(InCategoryTable.COL1)));
                inCategory.setName(cursor.getString(cursor.getColumnIndex(InCategoryTable.COL2)));
                inCategory.setAmount(cursor.getString(cursor.getColumnIndex(InCategoryTable.COL3)));
                inCategories.add(inCategory);
            }
        }finally{
            if(cursor!=null&&!cursor.isClosed())
                cursor.close();
        }
        return inCategories;
    }

    public CreditCard getCreditCard(int creditCard_id){

        String selection = CreditTable.COL1+"=?";

        String[] selectionArgs = {String.valueOf(creditCard_id)};

        cursor = mContext.getContentResolver().query(DataProvider.CREDIT_URI, CreditTable.ALL_COLS,
                selection, selectionArgs, null);

        CreditCard card = new CreditCard();

        try{

            while(cursor.moveToNext()){
                card.setId(cursor.getInt(cursor.getColumnIndex(CreditTable.COL1)));
                card.setName(cursor.getString(cursor.getColumnIndex(CreditTable.COL2)));
                card.setPayDate(cursor.getInt(cursor.getColumnIndex(CreditTable.COL3)));
                card.setAccount_id(cursor.getInt(cursor.getColumnIndex(CreditTable.COL4)));
                card.setAmount(cursor.getString(cursor.getColumnIndex(CreditTable.COL5)));
                card.setCycleStart(cursor.getInt(cursor.getColumnIndex(CreditTable.COL6)));
                card.setCycleEnd(cursor.getInt(cursor.getColumnIndex(CreditTable.COL7)));
            }
        }finally {
            if(!cursor.isClosed()&&cursor!=null){
                cursor.close();
            }
        }
        return card;
    }

    public ArrayList<CreditCard> getAllCreditCards(){
        ArrayList<CreditCard> cards = new ArrayList<>();

        cursor = mContext.getContentResolver().query(DataProvider.CREDIT_URI, CreditTable.ALL_COLS, null,
                null, CreditTable.COL1);

        try{

            while(cursor.moveToNext()) {
                CreditCard card = new CreditCard();

                card.setId(cursor.getInt(cursor.getColumnIndex(CreditTable.COL1)));
                card.setName(cursor.getString(cursor.getColumnIndex(CreditTable.COL2)));
                card.setPayDate(cursor.getInt(cursor.getColumnIndex(CreditTable.COL3)));
                card.setAccount_id(cursor.getInt(cursor.getColumnIndex(CreditTable.COL4)));
                card.setAmount(cursor.getString(cursor.getColumnIndex(CreditTable.COL5)));
                card.setCycleStart(cursor.getInt(cursor.getColumnIndex(CreditTable.COL6)));
                card.setCycleEnd(cursor.getInt(cursor.getColumnIndex(CreditTable.COL7)));

                cards.add(card);
            }

        }finally{
            if(cursor!=null&&!cursor.isClosed()){
                cursor.close();
            }
        }

        return cards;

    }
}