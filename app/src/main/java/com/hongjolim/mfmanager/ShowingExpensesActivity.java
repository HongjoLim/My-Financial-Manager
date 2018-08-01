package com.hongjolim.mfmanager;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hongjolim.mfmanager.adapter.ExpenseAdapter;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;
import com.hongjolim.mfmanager.tools.CurrencyFormatter;

import java.math.BigDecimal;

public class ShowingExpensesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private DataSource ds;
    private static final int REQUEST_OK = 1;
    public static final String EXPENSE_DETAIL = "detail";

    private TextView total_tv;

    private ExpenseAdapter adapter;

    public ShowingExpensesActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showing_expenses);

        ds = new DataSource(this);
        ds.open();

        ListView expense_lv = findViewById(R.id.expense_listView);
        adapter = new ExpenseAdapter(this, null, 0);

        total_tv = findViewById(R.id.expense_total);
        setTotal();

        expense_lv.setAdapter(adapter);

        expense_lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent expenseDetailIntent = new Intent(ShowingExpensesActivity.this,
                        ShowingExpensesDetailActivity.class);
                Uri uri = Uri.parse(DataProvider.TRANSACTION_URI+"/"+id);

                expenseDetailIntent.putExtra(EXPENSE_DETAIL, uri);
                startActivityForResult(expenseDetailIntent, REQUEST_OK);
            }
        });

        getLoaderManager().initLoader(0, null, this);

        FloatingActionButton fab = findViewById(R.id.expense_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent expenseAddIntent = new Intent(ShowingExpensesActivity.this, AddingExpenseActivity.class);
                startActivityForResult(expenseAddIntent, REQUEST_OK);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUEST_OK&&resultCode==RESULT_OK){
            setTotal();
            reload();
        }
    }

    private void reload(){
        getLoaderManager().restartLoader(0, null, this);
    }

    private void setTotal(){

        //in Transaction table, TRANS_TYPE1 means expenditure, TRANS_TYPE2 means income
        String[] selectionArgs = {String.valueOf(TransactionTable.TRANS_TYPE1)};
        Cursor totalCursor = getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                TransactionTable.COL8+"=?", selectionArgs, TransactionTable.COL1);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currencyCode = prefs.getString("CURRENCY", "Canada");

        BigDecimal total = new BigDecimal(String.valueOf(BigDecimalCalculator.roundValue(0.0, currencyCode)));

        try {
            while(totalCursor.moveToNext()){
                total = BigDecimalCalculator.add(String.valueOf(total),
                        totalCursor.getString(totalCursor.getColumnIndex(TransactionTable.COL3)));
            }

            total_tv.setText(String.format("total  -%s", CurrencyFormatter.format(this, String.valueOf(total))));
        }finally {
            if (totalCursor != null && totalCursor.isClosed())
                totalCursor.close();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        ds.close();
    }

    @Override
    public void onResume(){
        super.onResume();
        ds.open();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //in Transaction table, only the data whose COL7 (trans_type) is TRANS_TYPE1 are displayed

        String[] selectionArgs = {String.valueOf(TransactionTable.TRANS_TYPE1)};

        return new CursorLoader(this, DataProvider.TRANSACTION_URI,
                null, TransactionTable.COL8+"=?",
                selectionArgs, TransactionTable.COL2+" DESC, "+TransactionTable.COL1+" DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
