package com.hongjolim.mfmanager;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hongjolim.mfmanager.adapter.ExpenseAdapter;
import com.hongjolim.mfmanager.database.AccountsTable;
import com.hongjolim.mfmanager.database.CreditTable;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.model.Account;
import com.hongjolim.mfmanager.model.CreditCard;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;

import java.util.ArrayList;

import static com.hongjolim.mfmanager.AddingCreditActivity.SELECT_DATE;
import static com.hongjolim.mfmanager.AddingCreditActivity.SELECT_DUE_VALUE;
import static com.hongjolim.mfmanager.AddingCreditActivity.SELECT_END_VALUE;
import static com.hongjolim.mfmanager.AddingCreditActivity.SELECT_START_VALUE;

public class ShowingCreditDetailActivity extends AppCompatActivity implements
        ChoosingExFromFragment.FromCallBack, SelectDateFragment.FromCallBack {

    //constant to put into the bundle for ChoosingExFromFragment
    public final static String FROM_CREDIT_KEY = "FROM_CREDIT";
    public final static int FROM_CREDIT_VALUE = 1;

    private EditText nameEdt;
    private TextView account_name_tv, date_select, cycle_start, cycle_end;
    private Account oldAccount, newAccount;
    private CreditCard card;
    private DataSource mDataSource;
    private int creditId;

    private String oldName;
    private int oldDate, oldCycleStart, oldCycleEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_credit);

        mDataSource = new DataSource(this);

        nameEdt = findViewById(R.id.credit_name);
        account_name_tv = findViewById(R.id.credit_account);
        cycle_start = findViewById(R.id.credit_cycle_start);
        cycle_end = findViewById(R.id.credit_cycle_end);
        date_select = findViewById(R.id.credit_date_select);

        setDetail();

        setUpAccountListener();

        setUpDateSelect();

        setUpHistory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.credits_detail_acitivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            //this case is when the user clicks income switch button on the menu
            case R.id.credit_menuItem_edit:
                updateCard();
                return true;
            case R.id.credit_menuItem_delete:
                deleteCard();
                return true;
            case R.id.credit_menuItem_pay:
                PayingDebtsFragment fragment = new PayingDebtsFragment();
                Bundle b = new Bundle();
                b.putInt("CardId", card.getId());
                fragment.setArguments(b);
                fragment.show(getFragmentManager(), "PAYING_DEBTS");
                return true;
        }

        return false;
    }

    private void setDetail() {

        Uri uri = getIntent().getParcelableExtra(ShowingCreditActivity.CREDIT_DETAIL);
        creditId = Integer.parseInt(uri.getLastPathSegment());

        if (uri!=null) {
            ArrayList<CreditCard> cards = mDataSource.getAllCreditCards();
            card = null;
            for (int i = 0; i < cards.size(); i++) {
                if (creditId == cards.get(i).getId()) {
                    card = cards.get(i);
                }
            }

            oldName = card.getName();
            oldDate = card.getPayDate();
            oldCycleStart = card.getCycleStart();
            oldCycleEnd = card.getCycleEnd();

            //get all the account list from data source
            ArrayList<Account> accounts = mDataSource.getAllAccounts();

            //declare variable to store the account that matches the criteria
            oldAccount = null;

            //search the account that matches the criteria
            for (int i = 0; i < accounts.size(); i++) {
                if (card.getAccount_id() == accounts.get(i).getId()) {
                    oldAccount = accounts.get(i);
                }
            }

            String oldAccountName = oldAccount.getName();

            nameEdt.setText(oldName);
            account_name_tv.setText(oldAccountName);
            date_select.setText(String.valueOf(oldDate));
            cycle_start.setText(String.valueOf(oldCycleStart));
            cycle_end.setText(String.valueOf(oldCycleEnd));

        }
    }

    private void updateCard() {
        String newName = nameEdt.getText().toString();
        int newPayDate = Integer.parseInt(date_select.getText().toString());
        int newCycleStart = Integer.parseInt(cycle_start.getText().toString());
        int newCycleEnd = Integer.parseInt(cycle_end.getText().toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //if the new name is null or is empty string then alert the user
        if (newName.trim().length() < 1) {
            builder.setMessage("Please enter the name");
            builder.setPositiveButton("OK", null).setCancelable(false).create().show();
            return;
        }

        //if there is no change, then finish the activity
        if (newName.equals(oldName) && newPayDate==oldDate && oldAccount.equals(newAccount)&&
                oldCycleStart==newCycleStart&&oldCycleEnd==newCycleEnd) {
            setResult(RESULT_CANCELED);
            finish();
        }

        //if the account is not changed, then assign old Account to new Account object
        if (newAccount == null) {
            newAccount = oldAccount;
        }

        ContentValues values = new ContentValues();

        values.put(CreditTable.COL2, newName);
        values.put(CreditTable.COL3, newPayDate);
        values.put(CreditTable.COL4, newAccount.getId());
        values.put(CreditTable.COL6, newCycleStart);
        values.put(CreditTable.COL7, newCycleEnd);

        String selection = CreditTable.COL1 + "=?";
        String[] selectionArgs = {String.valueOf(card.getId())};

        getContentResolver().update(DataProvider.CREDIT_URI, values,
                selection, selectionArgs);

        setResult(RESULT_OK);
        finish();

    }

    private void deleteCard() {

        final String selection = CreditTable.COL1 + "=?";
        final String[] selectionArgs = {String.valueOf(card.getId())};

        if(Double.parseDouble(card.getAmount())>0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Unpaid balance will be withdrawn from the associated account");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Account account = mDataSource.getAccount(card.getAccount_id());
                    String newBal = (String.valueOf(BigDecimalCalculator.subtract(account.getCurrent_balance(),
                            card.getAmount())));

                    ContentValues values1 = new ContentValues();
                    values1.put(AccountsTable.COL5, newBal);

                    String[] selectionArgs2 = {String.valueOf(account.getId())};

                    //subtract the balance of the credit card the balance of the account
                    getContentResolver().update(DataProvider.ACCOUNTS_URI, values1, AccountsTable.COL1 + "=?",
                            selectionArgs2);

                    //update data in Transaction table -> put null for credit id and put account id
                    ContentValues values2 = new ContentValues();
                    values2.put(TransactionTable.COL6, card.getAccount_id());
                    values2.putNull(TransactionTable.COL7);

                    getContentResolver().update(DataProvider.TRANSACTION_URI, values2,
                            TransactionTable.COL7 + "=?", selectionArgs);
                }
            }).setNegativeButton("Cancel", null).setCancelable(false).create().show();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete this card?").setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //delete the data of the credit card in credit card table
                getContentResolver().delete(DataProvider.CREDIT_URI,
                        selection, selectionArgs);
                setResult(RESULT_OK);
                finish();
            }
        }).setNegativeButton("Cancel", null).show();

    }

    //to handle the event when the user touches the account name text view
    private void setUpAccountListener() {
        //add the text view the on click listener
        account_name_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bundle to send to choosingExFromFragment to differentiate this activity is calling it
                //so that, when the user is shown with the fragment, the credit cards list is not visible
                //because this AddingCreditActivity is calling it
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

    private void setUpHistory(){
        ListView creditHistory = findViewById(R.id.credit_history);

        //get cursor to find transactions paid by this credit card
        Cursor cursor = getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                TransactionTable.COL7+"=?", new String[] {String.valueOf(creditId)},
                TransactionTable.COL2+" DESC, "+TransactionTable.COL1+" DESC");

        ExpenseAdapter adapter = new ExpenseAdapter(this, cursor, 0);

        creditHistory.setAdapter(adapter);

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

    @Override
    public void fromAccountSelected(Account account) {
        newAccount = account;
        account_name_tv.setText(newAccount.getName());
    }

    //this method is not needed in this activity
    @Override
    public void fromCreditSelected(CreditCard card) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataSource.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataSource.open();
    }

    //callback method
    @Override
    public void payDaySelected(int date) {
        date_select.setText(String.valueOf(date));
    }

    @Override
    public void cycleStartSelected(int start) {
        cycle_start.setText(String.valueOf(start));
    }

    @Override
    public void cycleEndSelected(int end) {
        cycle_end.setText(String.valueOf(end));
    }
}
