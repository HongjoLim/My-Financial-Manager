package com.hongjolim.mfmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.hongjolim.mfmanager.database.AccountsTable;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.model.Account;
import com.hongjolim.mfmanager.model.InCategory;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;
import com.hongjolim.mfmanager.tools.DateFormatConverter;

import java.math.BigDecimal;
import java.util.Calendar;

public class AddingIncomeActivity extends AppCompatActivity implements ChoosingInCategory.CategorySelected,
ChoosingInToFragment.FromCallBack{

    private static final int IS_FROM_ADDING_ACTIVITY = 1003;
    private boolean switchable;

    private TextView date, description, income_category, to;
    private EditText amount;
    private DataSource mDataSource;
    private int year, month, todayDate;

    private Account account;
    private InCategory inCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_income);

        mDataSource = new DataSource(this);
        mDataSource.open();

        if(getIntent().getExtras()!=null){
            switchable = true;
        }

        date = findViewById(R.id.income_date);
        description = findViewById(R.id.income_desc);
        amount = findViewById(R.id.income_amount);
        to = findViewById(R.id.income_to);
        income_category = findViewById(R.id.income_category);

        setTodayDate();

        setDefaultValues();

        date.setOnClickListener(listener);
        to.setOnClickListener(listener);
        income_category.setOnClickListener(listener);

    }

    //this method is to set today's date as default
    private void setTodayDate(){
        Calendar mCurrentDate = Calendar.getInstance();
        year = mCurrentDate.get(Calendar.YEAR);
        month = mCurrentDate.get(Calendar.MONTH)+1;
        todayDate = mCurrentDate.get(Calendar.DAY_OF_MONTH);

        //set the date as the format(ex: Jan dd yyyy) using customized class
        date.setText(DateFormatConverter.convertDateToCustom(year+"-"+month+"-"+todayDate));
    }

    private void setDefaultValues(){

        inCategory = mDataSource.getInCategory(1);

        income_category.setText(inCategory.getName());

        account = mDataSource.getAccount(1);

        to.setText(account.getName());
    }

    View.OnClickListener listener = new View.OnClickListener(){
        @Override
        public void onClick(View v){

            switch(v.getId()){
                case R.id.income_date:
                    DatePickerDialog datePickerDialog = new DatePickerDialog(AddingIncomeActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int thisYear, int monthOfYear, int day) {
                                    monthOfYear = monthOfYear+1;
                                    //set date using customized class
                                    date.setText(DateFormatConverter.
                                            convertDateToCustom(thisYear+"-"+monthOfYear+"-"+day));
                                }
                            }, year, month-1, todayDate);
                    datePickerDialog.show();
                    break;
                case R.id.income_to:
                    ChoosingInToFragment choosingInToFragment = new ChoosingInToFragment();
                    choosingInToFragment.show(getFragmentManager(), "CHOOSING_TO");
                    break;
                case R.id.income_category:
                    showChooseCategoryFragment();
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.adding_income_activity, menu);
        MenuItem switch_button = menu.findItem(R.id.expense_switch_button);
        if(!switchable) {
            switch_button.setVisible(false);
            return true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.expense_switch_button:
                Intent expenseSwitchIntent = new Intent(this, AddingExpenseActivity.class);
                expenseSwitchIntent.putExtra("FROM_ADDING_ACTIVITY", IS_FROM_ADDING_ACTIVITY);
                startActivity(expenseSwitchIntent);
                finish();
                return true;
            case R.id.income_save:
                saveIncome();
                return true;
        }
        return false;
    }

    @Override
    public void onCategorySelected(InCategory inCategory){
        this.inCategory = inCategory;
        income_category.setText(inCategory.getName());
    }

    @Override
    public void toSelected(Account account) {
        this.account = account;
        to.setText(account.getName());
    }

    private void saveIncome(){
        String inDate, inDesc;
        double inAmount;

        //convert the date format using customized class
        inDate = DateFormatConverter.convertDateToISO(date.getText().toString());

        inDesc = description.getText().toString();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currencyCode = prefs.getString("CURRENCY", "Canada");

        if(amount.getText().toString().trim().length()==0){
            inAmount = BigDecimalCalculator.roundValue(0.0, currencyCode);
        }else{
            try {
                inAmount = BigDecimalCalculator.roundValue(Double.parseDouble(amount.getText().toString()), currencyCode);
            }catch(NumberFormatException ne){
                inAmount = BigDecimalCalculator.roundValue(0.0, currencyCode);
            }
        }

        if(inAmount==0.0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please enter amount").setPositiveButton("OK", null).create().show();
            amount.setFocusable(true);
            return;
        }


        ContentValues values = new ContentValues();
        values.put(TransactionTable.COL2, inDate);
        values.put(TransactionTable.COL3, String.valueOf(inAmount));
        values.put(TransactionTable.COL4, inDesc);

        //type 'income' means it is income
        values.put(TransactionTable.COL8, TransactionTable.TRANS_TYPE2);

        values.put(TransactionTable.COL5, inCategory.get_id());

        values.put(TransactionTable.COL6, account.getId());

        getContentResolver().insert(DataProvider.TRANSACTION_URI, values);

        addToAccount(account, inAmount);

        //if this activity is originally called from main activity, calls the showingIncome Activity
        if(switchable){
            Intent intent = new Intent(this, ShowingIncomeActivity.class);
            startActivity(intent);
        }
        setResult(RESULT_OK);
        finish();

    }

    private void addToAccount(Account account, double inAmount) {
        //create where clause to find the same account in the account table in the SQL database
        //created where clause would be [name= '(account_name)' - don't forget to put '' around the account name]
        String filter = AccountsTable.COL1 + "=" + account.getId();

        ContentValues values = new ContentValues();

        BigDecimal newBalance = BigDecimalCalculator.add(account.getCurrent_balance(), String.valueOf(inAmount));
        values.put(AccountsTable.COL5, newBalance.toString());

        getContentResolver().update(DataProvider.ACCOUNTS_URI, values, filter, null);

    }

    private void showChooseCategoryFragment(){

        DialogFragment chooseCategoryFragment = new ChoosingInCategory();
        chooseCategoryFragment.show(getFragmentManager(), "CHOOSE_IN_CATEGORY");
    }

    @Override
    public void onResume(){
        super.onResume();
        mDataSource.open();
    }

    @Override
    public void onPause(){
        super.onPause();
        mDataSource.close();
    }
}
