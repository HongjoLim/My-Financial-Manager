package com.hongjolim.mfmanager;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hongjolim.mfmanager.database.CreditTable;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.model.Account;
import com.hongjolim.mfmanager.model.CreditCard;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;

import static android.view.View.GONE;

public class AddingCreditActivity extends AppCompatActivity implements ChoosingExFromFragment.FromCallBack,
SelectDateFragment.FromCallBack{

    //constant to put into the bundle for ChoosingExFromFragment
    public final static String FROM_CREDIT_KEY = "FROM_CREDIT";
    public final static int FROM_CREDIT_VALUE = 1;

    //constants for tags that needed to be sent to 'SelectDateFragment'
    public final static String SELECT_DATE = "select due";
    public final static int SELECT_DUE_VALUE = 1;
    public final static int SELECT_START_VALUE = 2;
    public final static int SELECT_END_VALUE = 3;

    //declare view components
    private EditText nameEdt;
    private TextView account_name_tv;
    private TextView date_select;
    private TextView cycle_start;
    private TextView cycle_end;

    /*
    declare an instance of Account class
    this instance is used to store an account associated with a credit card (credit payment is made in this account)
     */
    private Account selectedAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_credit);

        //initialize view components
        nameEdt = findViewById(R.id.credit_name);
        account_name_tv = findViewById(R.id.credit_account);
        cycle_start = findViewById(R.id.credit_cycle_start);
        cycle_end = findViewById(R.id.credit_cycle_end);
        date_select = findViewById(R.id.credit_date_select);
        TextView historyTv = findViewById(R.id.credit_history_tv);
        historyTv.setVisibility(GONE);

        //set 25th of every month as default
        date_select.setText("25");

        //set up
        setUpAccountListener();

        setUpDateSelect();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.adding_credit_acitivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            //this case is when the user clicks income switch button on the menu
            case R.id.credit_menuItem_add:
                saveCard();
                return true;
        }

        return false;
    }

    //to handle the event when the user touches the account name text view
    private void setUpAccountListener(){

        //add the text view the on click listener
        account_name_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                'ChoosingExFromFragment' is used to choose account.
                In this activity, the user has to choose the account that credit card payment is withdrawn from
                Because 'ChoosingExFromFragment' is also used by 'AddingExpenseActivity',
                this Bundle is sent to the fragment to indicate that
                this activity is calling the fragment (not 'AddingExpenseActivity)
                So that, the credit card list will not be shown in the listview in the 'ChoosingExFromFragment'
                */
                Bundle b = new Bundle();
                b.putInt(FROM_CREDIT_KEY, FROM_CREDIT_VALUE);
                ChoosingExFromFragment choosingExFromFragment = new ChoosingExFromFragment();
                choosingExFromFragment.setArguments(b);
                choosingExFromFragment.show(getFragmentManager(), "CHOOSING_FROM");
            }
        });

    }

    private void setUpDateSelect(){

        date_select.setOnClickListener(listener);
        cycle_start.setOnClickListener(listener);
        cycle_end.setOnClickListener(listener);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Bundle b = new Bundle();
            switch(view.getId()){
                case R.id.credit_date_select:
                    SelectDateFragment fragment1 = new SelectDateFragment();
                    b.putInt(SELECT_DATE, SELECT_DUE_VALUE);
                    fragment1.setArguments(b);
                    fragment1.show(getFragmentManager(), "SELECT_PAY_DATE");
                    break;
                case R.id.credit_cycle_start:
                    SelectDateFragment fragment2 = new SelectDateFragment();
                    b.putInt(SELECT_DATE, SELECT_START_VALUE);
                    fragment2.setArguments(b);
                    fragment2.show(getFragmentManager(), "SELECT_CYCLE_START");
                    break;
                case R.id.credit_cycle_end:
                    SelectDateFragment fragment3 = new SelectDateFragment();
                    b.putInt(SELECT_DATE, SELECT_END_VALUE);
                    fragment3.setArguments(b);
                    fragment3.show(getFragmentManager(), "SELECT_CYCLE_END");
                    break;
            }
        }
    };

    public void saveCard(){

        String name = nameEdt.getText().toString();
        int payDate = Integer.parseInt(date_select.getText().toString());
        String cycleStart = cycle_start.getText().toString();
        String cycleEnd = cycle_end.getText().toString();

        String accountName = account_name_tv.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(name.trim().length()<1){
            builder.setMessage("Please enter the name");
            builder.setPositiveButton("OK", null).setCancelable(false).create().show();
            return;
        }

        if(accountName.isEmpty()){
            builder.setMessage("Please select associated account");
            builder.setPositiveButton("OK", null).setCancelable(false).create().show();
            return;
        }

        ContentValues values = new ContentValues();

        values.put(CreditTable.COL2, name);
        values.put(CreditTable.COL3, payDate);
        values.put(CreditTable.COL4, selectedAccount.getId());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currencyCode = prefs.getString("CURRENCY", "Canada");

        values.put(CreditTable.COL5, BigDecimalCalculator.roundValue(0.0, currencyCode));
        values.put(CreditTable.COL6, Integer.parseInt(cycleStart));
        values.put(CreditTable.COL7, Integer.parseInt(cycleEnd));

        getContentResolver().insert(DataProvider.CREDIT_URI, values);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void fromAccountSelected(Account account) {
        selectedAccount = account;
        account_name_tv.setText(selectedAccount.getName());
    }

    /*
    this method is not currently needed in this activity.
    but it has to be overridden because this activity implements ChoosingExFromFragment.FromCallBack interface
     */

    @Override
    public void fromCreditSelected(CreditCard card) {}

    //callback method to set the credit card payment date after the user selects it from fragment
    @Override
    public void payDaySelected(int date) {
        date_select.setText(String.valueOf(date));
    }

    //call back method to set the credit card cycle start date after the user selects it from fragment
    @Override
    public void cycleStartSelected(int start) {
        cycle_start.setText(String.valueOf(start));
    }

    //call back method to set the credit card cycle end date after the user selects it from fragment
    @Override
    public void cycleEndSelected(int end) {
        cycle_end.setText(String.valueOf(end));
    }
}
