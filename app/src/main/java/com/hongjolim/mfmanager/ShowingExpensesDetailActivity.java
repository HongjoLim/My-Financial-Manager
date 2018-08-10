package com.hongjolim.mfmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.SharedPreferences;
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
import com.hongjolim.mfmanager.database.CreditTable;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.model.Account;
import com.hongjolim.mfmanager.model.CreditCard;
import com.hongjolim.mfmanager.model.ExCategory;
import com.hongjolim.mfmanager.model.Transaction;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;
import com.hongjolim.mfmanager.tools.DateFormatConverter;

import java.math.BigDecimal;

public class ShowingExpensesDetailActivity extends AppCompatActivity implements ChoosingExCategory.CategorySelected,
ChoosingExFromFragment.FromCallBack{

    private TextView date, expense_category, from;
    private EditText amount, description;
    private DataSource mDataSource;
    private Uri uri;
    private Transaction oldTransaction;
    private ExCategory oldCategory, newCategory;
    private Account oldAccount, newAccount;

    private boolean paidByCredit;
    private CreditCard oldCard, newCard;
    private String expense_filter="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showing_expenses_detail);

        mDataSource = new DataSource(this);
        mDataSource.open();

        //find the view on the screen
        date = findViewById(R.id.expense_date);
        description = findViewById(R.id.expense_desc);
        amount = findViewById(R.id.expense_amount);
        from = findViewById(R.id.expense_from);
        expense_category = findViewById(R.id.expense_category);

        //sets the listener on the views
        date.setOnClickListener(listener);
        from.setOnClickListener(listener);
        expense_category.setOnClickListener(listener);

        uri = getIntent().getParcelableExtra(ShowingExpensesActivity.EXPENSE_DETAIL);
        if(uri!=null) {
            expense_filter = TransactionTable.COL1+"="+uri.getLastPathSegment();
            setDetail();
        }
    }

    private void setDetail() {

        //get the transaction data from the database using transaction id from showing expense account activity
        oldTransaction = mDataSource.getTransaction(Integer.parseInt(uri.getLastPathSegment()));

        //if the transaction data has no account id, it is paid by credit card, otherwise, paid by account
        if(oldTransaction.getAccount_id()>0){
            paidByCredit = false;
            oldAccount = mDataSource.getAccount(oldTransaction.getAccount_id());
            //to prevent null pointer exception when the account is not changed;
            newAccount = oldAccount;
            from.setText(oldAccount.getName());
        }else{
            paidByCredit = true;
            oldCard = mDataSource.getCreditCard(oldTransaction.getCredit_id());
            //to prevent null pointer exception when the card is not changed;
            newCard = oldCard;
            from.setText(oldCard.getName());
        }

        date.setText(DateFormatConverter.convertDateToCustom(oldTransaction.getDate()));
        amount.setText(oldTransaction.getAmount());
        description.setText(oldTransaction.getDescription());

        oldCategory = mDataSource.getExCategory(oldTransaction.getCategory_id());
        //to prevent null pointer exception when the category is not changed;
        newCategory = oldCategory;
        expense_category.setText(oldCategory.getName());

    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.expense_date:
                    setDatePickerDialog();
                    break;
                case R.id.expense_category:
                    showChooseCategoryFragment();
                    break;
                case R.id.expense_from:
                    ChoosingExFromFragment choosingExFromFragment = new ChoosingExFromFragment();
                    choosingExFromFragment.show(getFragmentManager(), "CHOOSING_FROM");
                    break;
            }
        }
    };

    private void showChooseCategoryFragment() {

        DialogFragment chooseCategoryFragment = new ChoosingExCategory();
        chooseCategoryFragment.show(getFragmentManager(), "CHOOSE_EX_CATEGORY");
    }

    @Override
    public void onCategorySelected(ExCategory exCategory){
        newCategory = exCategory;
        expense_category.setText(exCategory.getName());
    }

    private void setDatePickerDialog(){
        String[] dateString = oldTransaction.getDate().split("-");
        int day = Integer.parseInt(dateString[2]);
        int month = Integer.parseInt(dateString[1]);
        int year = Integer.parseInt(dateString[0]);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ShowingExpensesDetailActivity.this,
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

        getMenuInflater().inflate(R.menu.detail_expense_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.expense_save:
                updateExpense();
                return true;
            case R.id.expense_delete:
                deleteExpense();
                return true;
        }
        return false;
    }

    private void updateExpense(){

        //get the revised data
        String newDate = date.getText().toString();
        String newDescription = description.getText().toString();
        String newCategoryName = expense_category.getText().toString();
        String newAccountName = from.getText().toString();

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
        if(!paidByCredit) {
            if (newDate.equals(DateFormatConverter.convertDateToCustom(oldTransaction.getDate())) &&
                    newDescription.equals(oldTransaction.getDescription()) &&
                    newCategoryName.equals(oldCategory.getName()) &&
                    newAmount == Double.parseDouble(oldTransaction.getAmount()) &&
                    newAccountName.equals(oldAccount.getName())) {

                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }else{
            if (newDate.equals(DateFormatConverter.convertDateToCustom(oldTransaction.getDate())) &&
                    newDescription.equals(oldTransaction.getDescription()) &&
                    newCategoryName.equals(oldCategory.getName()) &&
                    newAmount == Double.parseDouble(oldTransaction.getAmount()) &&
                    newAccountName.equals(oldCard.getName())) {

                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }

        //put updated values into the expense table in the database
        ContentValues values = new ContentValues();
        values.put(TransactionTable.COL2, DateFormatConverter.convertDateToISO(newDate));
        values.put(TransactionTable.COL3, String.valueOf(newAmount));
        values.put(TransactionTable.COL4, newDescription);

        values.put(TransactionTable.COL5, newCategory.get_id());

        if(newAccount!=null) {
            values.put(TransactionTable.COL6, newAccount.getId());
            values.putNull(TransactionTable.COL7);
        }else{
            values.put(TransactionTable.COL7, newCard.getId());
            values.putNull(TransactionTable.COL6);
        }

        getContentResolver().update(DataProvider.TRANSACTION_URI, values, expense_filter, null);

        if(!paidByCredit) {
            //add the amount into the original account
            putItBackToAccount();

            if(newAccount!=null) {
                if (oldAccount.getId() == (newAccount.getId())) {
                    /**
                     *  if old account is equal to new Account, get new data from the database
                     *  because the old transaction has been added to it, and its current balance has been changed
                     **/
                    newAccount = mDataSource.getAccount(newAccount.getId());
                }
            }
        }else{

            subtractDebtBackFromCard();

            /**
             * if old card is equal to new card, get new data from the database
             * because the old transaction has been added to it, and its current balance has been changed
             **/
            if(newCard!=null) {
                if (oldCard.getId() == newCard.getId()) {
                    newCard = mDataSource.getCreditCard(newCard.getId());
                }
            }
        }

        if(newAccount!=null) {
            //Now, deduct the new Amount from the new account
            deductFromAccount(newAmount);
        }else{
            addDebtToCard(newAmount);
        }

        setResult(RESULT_OK);
        Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
        finish();

    }

    private void putItBackToAccount() {
        String filter = AccountsTable.COL1+"="+oldAccount.getId();

        ContentValues values = new ContentValues();
        BigDecimal bigNewAmount = BigDecimalCalculator.add(oldAccount.getCurrent_balance(), oldTransaction.getAmount());
        values.put(AccountsTable.COL5, bigNewAmount.toString());

        getContentResolver().update(DataProvider.ACCOUNTS_URI, values, filter, null);
    }

    private void subtractDebtBackFromCard(){
        String filter = CreditTable.COL1+"="+oldCard.getId();

        ContentValues values = new ContentValues();
        BigDecimal bigNewAmount = BigDecimalCalculator.subtract(oldCard.getAmount(), oldTransaction.getAmount());
        values.put(CreditTable.COL5, bigNewAmount.toString());

        getContentResolver().update(DataProvider.CREDIT_URI, values, filter, null);
    }

    private void deductFromAccount(double newAmount) {
        String filter = AccountsTable.COL1+"="+newAccount.getId();

        ContentValues values = new ContentValues();
        BigDecimal bigNewAmount = BigDecimalCalculator.subtract(newAccount.getCurrent_balance(), String.valueOf(newAmount));
        values.put(AccountsTable.COL5, bigNewAmount.toString());

        getContentResolver().update(DataProvider.ACCOUNTS_URI, values, filter, null);
    }

    private void addDebtToCard(double newAmount){
        String filter = CreditTable.COL1+"="+newCard.getId();

        ContentValues values = new ContentValues();
        BigDecimal bigNewAmount = BigDecimalCalculator.add(newCard.getAmount(), String.valueOf(newAmount));
        values.put(CreditTable.COL5, bigNewAmount.toString());

        getContentResolver().update(DataProvider.CREDIT_URI, values, filter, null);
    }

    private void deleteExpense(){
        //delete the record from the expense table
        getContentResolver().delete(DataProvider.TRANSACTION_URI, expense_filter, null);
        Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();

        if(!paidByCredit) {
            //add the old amount to the current 'from account(or cash)' balance
            putItBackToAccount();
        }else{
            subtractDebtBackFromCard();
        }

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

    @Override
    public void fromAccountSelected(Account account) {
        this.newAccount = account;
        this.newCard = null;
        //to prevent null pointer exception
        if(account!=null){
            from.setText(account.getName());
        }else{
            from.setText("");
        }
    }

    @Override
    public void fromCreditSelected(CreditCard card) {
        this.newCard = card;
        this.newAccount=null;
        //to prevent null pointer exception
        if(card!=null){
            from.setText(card.getName());
        }else{
            from.setText("");
        }
    }
}
