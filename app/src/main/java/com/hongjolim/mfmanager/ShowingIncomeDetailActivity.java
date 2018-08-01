package com.hongjolim.mfmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hongjolim.mfmanager.database.AccountsTable;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.model.Account;
import com.hongjolim.mfmanager.model.InCategory;
import com.hongjolim.mfmanager.model.Transaction;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;
import com.hongjolim.mfmanager.tools.DateFormatConverter;

import java.math.BigDecimal;

public class ShowingIncomeDetailActivity extends AppCompatActivity implements ChoosingInCategory.CategorySelected,
ChoosingInToFragment.FromCallBack{

    private TextView date, income_category, to;
    private EditText amount, description;
    private DataSource mDataSource;
    private Uri uri;
    private Transaction oldTransaction;
    private String income_filter;
    private InCategory oldInCategory, newCategory;
    private Account oldAccount, newAccount;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showing_income_detail);

        mDataSource = new DataSource(this);
        mDataSource.open();

        date = findViewById(R.id.income_date);
        description = findViewById(R.id.income_desc);
        amount = findViewById(R.id.income_amount);
        to = findViewById(R.id.income_to);
        income_category = findViewById(R.id.income_category);

        date.setOnClickListener(listener);
        to.setOnClickListener(listener);
        income_category.setOnClickListener(listener);

        uri = getIntent().getParcelableExtra(ShowingIncomeActivity.INCOME_DETAIL);
        if(uri!=null) {
            income_filter = TransactionTable.COL1+"="+uri.getLastPathSegment();
            setDetail();
        }
    }

    private void setDetail() {

        //get the transaction data from the database using transaction id from showing expense account activity
        oldTransaction = mDataSource.getTransaction(Integer.parseInt(uri.getLastPathSegment()));

        date.setText(DateFormatConverter.convertDateToCustom(oldTransaction.getDate()));
        amount.setText(String.valueOf(oldTransaction.getAmount()));
        description.setText(oldTransaction.getDescription());

        oldInCategory = mDataSource.getInCategory(oldTransaction.getCategory_id());
        //to prevent null pointer exception when the category is not changed;
        newCategory = oldInCategory;
        income_category.setText(oldInCategory.getName());

        oldAccount = mDataSource.getAccount(oldTransaction.getAccount_id());
        //to prevent null pointer exception when the account is not changed
        newAccount = oldAccount;
        to.setText(oldAccount.getName());
    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.income_date:
                    setDatePickerDialog();
                    break;
                case R.id.income_category:
                    showChooseCategoryFragment();
                    break;
                case R.id.income_to:
                    ChoosingInToFragment choosingInToFragment = new ChoosingInToFragment();
                    choosingInToFragment.show(getFragmentManager(), "CHOOSING_TO");
                    break;
            }
        }
    };

    private void showChooseCategoryFragment() {

        DialogFragment chooseCategoryFragment = new ChoosingExCategory();
        chooseCategoryFragment.show(getFragmentManager(), "CHOOSE_EX_CATEGORY");
    }

    @Override
    public void onCategorySelected(InCategory inCategory){
        newCategory = inCategory;
        income_category.setText(inCategory.getName());
    }

    @Override
    public void toSelected(Account account){
        newAccount = account;
        to.setText(account.getName());
    }

    private void setDatePickerDialog(){
        String[] dateString = oldTransaction.getDate().split("-");
        int day = Integer.parseInt(dateString[2]);
        int month = Integer.parseInt(dateString[1]);
        int year = Integer.parseInt(dateString[0]);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ShowingIncomeDetailActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int thisYear, int monthOfYear, int day) {
                        monthOfYear = monthOfYear+1;
                        //set date using customized class
                        date.setText(DateFormatConverter.
                                convertDateToCustom(thisYear+"-"+monthOfYear+"-"+day));
                    }
                }, year, month-1, day);
        datePickerDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.detail_income_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.income_save:
                updateIncome();
                return true;
            case R.id.income_delete:
                deleteIncome();
                return true;
        }
        return false;
    }

    private void updateIncome(){

        String newDate = date.getText().toString();
        String newDescription = description.getText().toString();
        String newCategoryName = income_category.getText().toString();
        String newAccountName = to.getText().toString();

        double newAmount;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currencyCode = prefs.getString("CURRENCY", "Canada");

        if(amount.getText().toString().trim().length()==0){
            newAmount = BigDecimalCalculator.roundValue(0.0, currencyCode);
        }else{
            try {
                newAmount = BigDecimalCalculator.roundValue(Double.parseDouble(amount.getText().toString()), currencyCode);
            }catch(NumberFormatException e){
                newAmount = BigDecimalCalculator.roundValue(0.0, currencyCode);
            }
        }

        if(newAmount==BigDecimalCalculator.roundValue(0.0, currencyCode)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please enter amount").setPositiveButton("OK", null).create().show();
            amount.setFocusable(true);
            return;
        }

        //if there is no change made, just finish the activity
        if(newDate.equals(DateFormatConverter.convertDateToCustom(oldTransaction.getDate()))
                &&newDescription.equals(oldTransaction.getDescription())&&
                newCategoryName.equals(oldInCategory.getName())&&
                newAmount==Double.parseDouble(oldTransaction.getAmount())&&
                newAccountName.equals(oldAccount.getName())) {
            setResult(RESULT_CANCELED);
            finish();
            return;

        }

        ContentValues values = new ContentValues();

        values.put(TransactionTable.COL2, DateFormatConverter.convertDateToISO(newDate));
        values.put(TransactionTable.COL3, String.valueOf(newAmount));
        values.put(TransactionTable.COL4, newDescription);

        values.put(TransactionTable.COL5, newCategory.get_id());
        values.put(TransactionTable.COL6, newAccount.getId());

        getContentResolver().update(DataProvider.TRANSACTION_URI, values, income_filter, null);

        deductFromAccount(oldAccount, Double.parseDouble(oldTransaction.getAmount()));

        /**
         * if the old account is equal to the new account,
         * get the new data from the database
         * because the current balance of the account has been changed
         */
        if(oldAccount.getId()==newAccount.getId()){
            newAccount = mDataSource.getAccount(newAccount.getId());
        }

        //add the amount into the original account
        putItBackToAccount(newAmount);

        setResult(RESULT_OK);
        Toast.makeText(this, "Income updated", Toast.LENGTH_SHORT).show();
        finish();

    }

    private void putItBackToAccount(double newAmount) {
        String filter = AccountsTable.COL1+"="+newAccount.getId();

        ContentValues values = new ContentValues();
        //get the current balance in the newly chosen accounts

        BigDecimal bigNewNumber = BigDecimalCalculator.add(newAccount.getCurrent_balance(),
                String.valueOf(newAmount));

        values.put(AccountsTable.COL5, bigNewNumber.toString());

        getContentResolver().update(DataProvider.ACCOUNTS_URI, values, filter, null);
    }

    private void deductFromAccount(Account oldAccount, double oldAmount) {
        String filter = AccountsTable.COL1+"="+oldAccount.getId();

        ContentValues values = new ContentValues();
        //get the current balance in the newly chosen accounts

        BigDecimal bigNewNumber = BigDecimalCalculator.subtract(oldAccount.getCurrent_balance(),
                String.valueOf(oldAmount));
        values.put(AccountsTable.COL5, bigNewNumber.toString());

        getContentResolver().update(DataProvider.ACCOUNTS_URI, values, filter, null);
    }


    //delete income
    private void deleteIncome(){
        getContentResolver().delete(DataProvider.TRANSACTION_URI, income_filter, null);
        Toast.makeText(this, "Income deleted", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
        //add the old amount to the current 'from account(or cash)' balance
        deductFromAccount(oldAccount, Double.parseDouble(oldTransaction.getAmount()));

    }

    @Override
    public void onPause(){
        super.onPause();
        mDataSource.close();
    }

    @Override
    public void onResume(){
        super.onResume();
        mDataSource.close();
    }
}
