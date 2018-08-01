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

import com.hongjolim.mfmanager.adapter.IncomeAdapter;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;
import com.hongjolim.mfmanager.tools.CurrencyFormatter;

public class ShowingIncomeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private DataSource mDataSource;
    private static final int REQUEST_OK = 1;
    public static final String INCOME_DETAIL = "detail";

    private TextView total_tv;

    private IncomeAdapter adapter;

    public ShowingIncomeActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showing_income);

        mDataSource = new DataSource(this);
        mDataSource.open();

        ListView income_lv = findViewById(R.id.income_listView);
        adapter = new IncomeAdapter(this, null, 0);
        income_lv.setAdapter(adapter);

        total_tv = findViewById(R.id.income_total);

        setTotal();

        income_lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent incomeDetailIntent = new Intent(ShowingIncomeActivity.this,
                        ShowingIncomeDetailActivity.class);
                Uri uri = Uri.parse(DataProvider.TRANSACTION_URI+"/"+id);

                incomeDetailIntent.putExtra(INCOME_DETAIL, uri);
                startActivityForResult(incomeDetailIntent, REQUEST_OK);
            }
        });

        getLoaderManager().initLoader(0, null, this);

        FloatingActionButton fab = findViewById(R.id.income_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent incomeAddIntent = new Intent(ShowingIncomeActivity.this, AddingIncomeActivity.class);
                startActivityForResult(incomeAddIntent, REQUEST_OK);
            }
        });
    }

    private void setTotal(){

        //in Transaction table, 1 means expenditure, 2 means income
        String[] selectionArgs = {String.valueOf(TransactionTable.TRANS_TYPE2)};
        Cursor totalCursor = getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                TransactionTable.COL8+"=?",
                selectionArgs, TransactionTable.COL1);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currencyCode = prefs.getString("CURRENCY", "Canada");

        double total =  BigDecimalCalculator.roundValue(0.0, currencyCode);

        try {
            while (totalCursor.moveToNext()) {
                total += totalCursor.getDouble(totalCursor.getColumnIndex(TransactionTable.COL3));
            }
        }finally{
            if(totalCursor!=null&&!totalCursor.isClosed()){
                totalCursor.close();
            }
        }

        total_tv.setText(String.format("total  +%s", CurrencyFormatter.format(this, String.valueOf(total))));
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //in Transaction table, only the data whose COL7 (trans_type) is income are displayed

        String[] selectionArgs = {String.valueOf(TransactionTable.TRANS_TYPE2)};
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
