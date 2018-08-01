package com.hongjolim.mfmanager;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hongjolim.mfmanager.adapter.AccountHistoryAdapter;
import com.hongjolim.mfmanager.database.AccountsTable;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;

import java.math.BigDecimal;
import java.util.Calendar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class SettingAccountsActivity extends AppCompatActivity {

    private EditText accName, accType, accBal;
    private ListView accHistory;

    private String action, filter;

    private String oldBalance;

    private Cursor cursor;
    private DataSource mDataSource;

    SharedPreferences prefs;
    String currencyCode;

    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_accounts);

        mDataSource = new DataSource(this);
        mDataSource.open();

        accName = findViewById(R.id.credit_name);
        accType = findViewById(R.id.credit_account);
        accBal = findViewById(R.id.account_balance);
        accHistory = findViewById(R.id.account_history);
        TextView historyTv = findViewById(R.id.account_history_tv);

        Intent intent = getIntent();
        uri = intent.getParcelableExtra(ShowingAccountsActivity.ACCOUNT_DETAIL);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currencyCode = prefs.getString("CURRENCY", "Canada");

        if(uri==null){
            action = Intent.ACTION_INSERT;
            accHistory.setVisibility(View.GONE);
            historyTv.setVisibility(GONE);
        }else{
            action = Intent.ACTION_EDIT;
            setTitle(getString(R.string.account_detail));
            filter = AccountsTable.COL1 + "=" + uri.getLastPathSegment();

            setAccountHistory();

            cursor = getContentResolver().query(uri, AccountsTable.ALL_COLS, filter, null, null);

            try {
                while (cursor.moveToNext()) {

                    String oldName = cursor.getString(cursor.getColumnIndex(AccountsTable.COL2));
                    String oldType = cursor.getString(cursor.getColumnIndex(AccountsTable.COL3));
                    //if this is not a new account, the old balance is shown from the accounts table column 5
                    oldBalance = cursor.getString(cursor.getColumnIndex(AccountsTable.COL5));

                    accName.setText(oldName);
                    accType.setText(oldType);
                    accBal.setText(oldBalance);
                    accHistory.setVisibility(VISIBLE);
                }
            }finally{
                if(cursor!=null&&!cursor.isClosed()){
                    cursor.close();
                }
            }
        }
    }

    private void setAccountHistory(){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showing_adjustments = prefs.getBoolean("SHOW ADJUSTMENTS", false);

        String selection = "";
        if(showing_adjustments) {
            selection = TransactionTable.COL6 + "=" + uri.getLastPathSegment();
        }else {
            selection = TransactionTable.COL6 + "=" + uri.getLastPathSegment() + " AND " + TransactionTable.COL8 + " != " +
                    TransactionTable.TRANS_TYPE6 + " AND " + TransactionTable.COL8 + " != " + TransactionTable.TRANS_TYPE5;
        }

        cursor = getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                selection, null, TransactionTable.COL2+" DESC, "+TransactionTable.COL1+" DESC");
        AccountHistoryAdapter adapter = new AccountHistoryAdapter(this, cursor, 0);
        accHistory.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_accounts_acitivity, menu);
        if (!action.equals(Intent.ACTION_EDIT)) {
            MenuItem delete = menu.findItem(R.id.account_menuItem_delete);
            delete.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPause(){
        super.onPause();
        mDataSource.close();
    }

    @Override
    public void onResume(){
        super.onResume();
        mDataSource.open();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.account_menuItem_delete:
                //if the account is the default account(originally cash) the user cannot delete it
                if(uri.getLastPathSegment().equals("1")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("You cannot delete default account").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
                }else {
                    //if the account is not default (_id!=1), then continue the process
                    deleteAccount();
                }
                return true;
            case R.id.account_menuItem_edit:
                //if the user is in this activity for viewing account detail, this part is run
                if(action.equals(Intent.ACTION_EDIT))
                    checkUpdateValidity();
                //if the user is in this activity for setting up a new account, this part is run
                else
                    insertAccount();
                return true;
        }
        return false;
    }

    public void checkUpdateValidity() {

        final String newName = accName.getText().toString();
        final String newType = accType.getText().toString();

        double newBalance;

        if (accBal.getText().toString().trim().length() == 0){
            newBalance = BigDecimalCalculator.roundValue(0.0, currencyCode);
        }
        else {
            try {
                newBalance = BigDecimalCalculator.roundValue(Double.parseDouble(accBal.getText().toString()), currencyCode);
            } catch (NumberFormatException e) {
                newBalance = BigDecimalCalculator.roundValue(0.0, currencyCode);
            }
        }

        if (newName.trim().length() == 0) {
            showAccNameCannotNull();
            return;
        }

        //if the user chooses to change the current balance, alert dialog should be displayed
        if (Double.parseDouble(oldBalance) != newBalance) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final BigDecimal difference = BigDecimalCalculator.subtract(String.valueOf(newBalance), oldBalance);
            final double finalNewBalance = newBalance;
            builder.setMessage("Balance adjustment " + difference.toString()
                    + " will be added to your transactions").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //if the user changes current balance,
                    //the difference should be inserted into the transaction table
                    insertIntoTransaction(difference.doubleValue(), Integer.parseInt(uri.getLastPathSegment()));
                    updateDetail(newName, newType, finalNewBalance);
                }
                //if the user chose not to adjust the balance, get out of this method
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).create().show();
        } else {
            updateDetail(newName, newType, newBalance);
        }
    }

    private void updateDetail(String newName, String newType, double newBalance){
        ContentValues values = new ContentValues();

        values.put(AccountsTable.COL2, newName);
        values.put(AccountsTable.COL3, newType);
        values.put(AccountsTable.COL5, newBalance);

        getContentResolver().update(DataProvider.ACCOUNTS_URI, values,
                AccountsTable.COL1+"="+uri.getLastPathSegment(),
                null);

        setResult(RESULT_OK);
        finish();
    }

    private void insertIntoTransaction(double amount, int account_id) {

        Calendar mCalendar = Calendar.getInstance();
        String date = mCalendar.get(Calendar.YEAR)+"-"+
                (mCalendar.get(Calendar.MONTH)+1)+"-"+mCalendar.get(Calendar.DAY_OF_MONTH);

        ContentValues values = new ContentValues();
        values.put(TransactionTable.COL2, date);

        /*store the difference as an absolute value
        since it does not matter whether the amount is bigger than zero or less than zero in this column
        it will be distinguished by COL7
        */
        values.put(TransactionTable.COL3, String.valueOf(Math.abs(amount)));
        values.put(TransactionTable.COL4, getString(R.string.balance_adjustment));
        values.put(TransactionTable.COL6, account_id);

        if(amount<0){
            //if the new balance is less than old balance, this adjustment is stored as expense
            values.put(TransactionTable.COL7, TransactionTable.TRANS_TYPE5);
        }else{
            //if the new balance is greater than old balance, this adjustment is stored as income
            values.put(TransactionTable.COL7, TransactionTable.TRANS_TYPE6);
        }

        getContentResolver().insert(DataProvider.TRANSACTION_URI, values);
    }

    private void insertAccount(){
        String name = accName.getText().toString();
        String type = accType.getText().toString();
        double startingBalance;

        if (accBal.getText().toString().trim().length() == 0){
            startingBalance = BigDecimalCalculator.roundValue(0.0, currencyCode);
        }
        else{
            try {
                startingBalance = BigDecimalCalculator.roundValue(Double.parseDouble(accBal.getText().toString()), currencyCode);
            }catch(Exception e){
                startingBalance = BigDecimalCalculator.roundValue(0.0, currencyCode);
            }
        }

        if(name.trim().length()==0){
            showAccNameCannotNull();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(AccountsTable.COL2, name);
        values.put(AccountsTable.COL3, type);
        //because the user is opening an account, the starting balance and the current balance are identical
        values.put(AccountsTable.COL4, String.valueOf(startingBalance));
        values.put(AccountsTable.COL5, String.valueOf(startingBalance));
        getContentResolver().insert(DataProvider.ACCOUNTS_URI, values);

        //find the 'id' of 'the account' that was just put into the account table
        String[] filter = {name};
        cursor = getContentResolver().query(DataProvider.ACCOUNTS_URI, AccountsTable.ALL_COLS, AccountsTable.COL2+"=?",
                filter, AccountsTable.COL1);

        try {
            while (cursor.moveToNext()) {
                int account_id = cursor.getInt(cursor.getColumnIndex(AccountsTable.COL1));
                insertIntoTransaction(startingBalance, account_id);
            }
        }finally{
            cursor.close();
        }

        setResult(RESULT_OK);
        finish();
    }

    public void deleteAccount(){
        //alert dialog to make sure the user wants to delete this account
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Deleting this account will remove all the transactions belonging to this account. Are you sure to delete this account?").
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                getContentResolver().delete(DataProvider.ACCOUNTS_URI, filter, null);

                String[] selectionArgs = {uri.getLastPathSegment()};

                //also delete all the data belong to this account in the transaction table to prevent orphanage data
                getContentResolver().delete(DataProvider.TRANSACTION_URI, TransactionTable.COL6+"=?", selectionArgs);

                //show toast message
                Toast.makeText(SettingAccountsActivity.this, "Account deleted",
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
        return;
    }

    //this method is to alert user that the account name cannot be empty
    private void showAccNameCannotNull(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Account name cannot be empty").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                accName.requestFocus();
            }
        }).create().show();
    }
}
