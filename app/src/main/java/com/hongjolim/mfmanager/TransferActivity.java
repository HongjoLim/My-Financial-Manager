package com.hongjolim.mfmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.model.Account;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;
import com.hongjolim.mfmanager.tools.DateFormatConverter;

import java.math.BigDecimal;
import java.util.Calendar;

public class TransferActivity extends AppCompatActivity implements ChoosingInToFragment.FromCallBack {

    private int mode;
    private int year, month, todayDate;
    private TextView date, from, to;
    private EditText amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        setTitle("Transfer");

        date = findViewById(R.id.transfer_date);
        from = findViewById(R.id.transfer_from);
        to = findViewById(R.id.transfer_to);

        setTodayDate();
        amount = findViewById(R.id.transfer_amount);

        //set the views onClickListener
        date.setOnClickListener(listener);
        from.setOnClickListener(listener);
        to.setOnClickListener(listener);

    }

    private void setTodayDate(){
        Calendar mCurrentDate = Calendar.getInstance();
        year = mCurrentDate.get(Calendar.YEAR);
        month = mCurrentDate.get(Calendar.MONTH)+1;
        todayDate = mCurrentDate.get(Calendar.DAY_OF_MONTH);

        //set the date as the format(ex: Jan dd yyyy) using customized class
        date.setText(DateFormatConverter.convertDateToCustom(year+"-"+month+"-"+todayDate));
    }

    //declare the onClickListener
    View.OnClickListener listener = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            switch(view.getId()){
                case R.id.transfer_date:
                    DatePickerDialog datePickerDialog = new DatePickerDialog(TransferActivity.this,
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
                case R.id.transfer_from:
                    mode=1;
                    ChoosingInToFragment fromFragment = new ChoosingInToFragment();
                    fromFragment.show(getFragmentManager(), "CHOOSE_FROM");
                    break;
                case R.id.transfer_to:
                    mode=2;
                    ChoosingInToFragment toFragment = new ChoosingInToFragment();
                    toFragment.show(getFragmentManager(),"CHOOSE_TO");
                    break;
            }
        }
    };

    public void toSelected(Account account){
        if(mode==1){
            from.setText(account.getName());
        }else
            to.setText(account.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.transfer_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()) {
            case R.id.transfer_save:
                saveTransfer();
                return true;
        }
        return false;
    }

    private void saveTransfer() {
        String toAccount = to.getText().toString();
        String fromAccount = from.getText().toString();

        double transfer_amount;
        BigDecimal bigNewBalance;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currencyCode = prefs.getString("CURRENCY", "Canada");

        if (amount.getText().toString().trim().length() == 0){
            transfer_amount = BigDecimalCalculator.roundValue(0.0, currencyCode);
        }
        else {
            try {
                transfer_amount = BigDecimalCalculator.roundValue(Double.parseDouble(amount.getText().toString()), currencyCode);
            } catch (NumberFormatException e) {
                transfer_amount = BigDecimalCalculator.roundValue(0.0, currencyCode);
            }
        }

        if(transfer_amount==0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please enter amount").
                    setPositiveButton("OK", new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
            return;
        }


        if(fromAccount.isEmpty()||toAccount.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please choose both accounts").
                    setPositiveButton("OK", new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
            return;
        }

        if(fromAccount.equals(toAccount)){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please choose different accounts").
                    setPositiveButton("OK", new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
            return;
        }

        //create where clause to find the same account in the account table in the SQL database
        //created where clause would be [name= '(account_name)' - don't forget to put '' around the account name]
        String filter1 = AccountsTable.COL2 + "= '" + fromAccount + "'";

        //get cursor at the exact location
        Cursor cursor = getContentResolver().query(DataProvider.ACCOUNTS_URI, AccountsTable.ALL_COLS,
                filter1, null, null);

        String fromAccountBalance = "";
        int fromAccount_id = 0;

        try {
            while (cursor.moveToNext()) {
                fromAccount_id = cursor.getInt(cursor.getColumnIndex(AccountsTable.COL1));
                fromAccountBalance = cursor.getString(cursor.getColumnIndex(AccountsTable.COL5));
            }
        }finally{
            if(cursor !=null&&!cursor.isClosed()){
                cursor.close();
            }
        }

        bigNewBalance = BigDecimalCalculator.subtract(fromAccountBalance, String.valueOf(transfer_amount));

        ContentValues values1 = new ContentValues();
        values1.put(AccountsTable.COL5, bigNewBalance.toString());

        getContentResolver().update(DataProvider.ACCOUNTS_URI, values1, filter1, null);


        String filter2 = AccountsTable.COL2 + "= '" + toAccount + "'";

        cursor = getContentResolver().query(DataProvider.ACCOUNTS_URI, AccountsTable.ALL_COLS,
                filter2, null, null);

        int toAccount_id = 0;
        String toAccountBalance = "";

        try {
            while (cursor.moveToNext()) {
                toAccount_id = cursor.getInt(cursor.getColumnIndex(AccountsTable.COL1));
                toAccountBalance = cursor.getString(cursor.getColumnIndex(AccountsTable.COL5));
            }
        }finally{
            if(cursor !=null&&!cursor.isClosed()){
                cursor.close();
            }
        }

        bigNewBalance = BigDecimalCalculator.add(toAccountBalance, String.valueOf(transfer_amount));

        ContentValues values2 = new ContentValues();
        values2.put(AccountsTable.COL5, bigNewBalance.toString());

        getContentResolver().update(DataProvider.ACCOUNTS_URI, values2, filter2, null);

        alertTransferDialogShow(fromAccount, toAccount, transfer_amount);

        String todayDate = date.getText().toString();
        saveTransaction(todayDate, fromAccount_id, fromAccount, toAccount_id, toAccount, transfer_amount);
    }

    private void alertTransferDialogShow(String fromAccount, String toAccount, double transfer_amount){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(transfer_amount+" will be transferred from "+fromAccount+ " to "+toAccount);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
    }

    private void saveTransaction(String todayDate, int fromAccount_id, String fromAccount,
                                 int toAccount_id, String toAccount, double transfer_amount){

        //insert as an income for 'toAccount'
        ContentValues values1 = new ContentValues();

        //convert today's date to the ISO format using customized class
        values1.put(TransactionTable.COL2, DateFormatConverter.convertDateToISO(todayDate));
        values1.put(TransactionTable.COL3, transfer_amount);
        values1.put(TransactionTable.COL4, "transfer from "+fromAccount);
        values1.put(TransactionTable.COL6, toAccount_id);
        //store data as transfer_plus
        values1.put(TransactionTable.COL8, TransactionTable.TRANS_TYPE4);

        getContentResolver().insert(DataProvider.TRANSACTION_URI, values1);


        //insert as an expense for 'fromAccount'
        ContentValues values2 = new ContentValues();

        //convert today's date to the ISO format using customized class
        values2.put(TransactionTable.COL2, DateFormatConverter.convertDateToISO(todayDate));
        values2.put(TransactionTable.COL3, transfer_amount);
        values2.put(TransactionTable.COL4, "transfer to "+toAccount);
        values2.put(TransactionTable.COL6, fromAccount_id);
        //store record as transfer_minus
        values2.put(TransactionTable.COL8, TransactionTable.TRANS_TYPE3);

        getContentResolver().insert(DataProvider.TRANSACTION_URI, values2);

    }
}
