package com.hongjolim.mfmanager;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hongjolim.mfmanager.adapter.CreditAdapter;
import com.hongjolim.mfmanager.database.CreditTable;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;

public class ShowingCreditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ListView cardsListView;
    private DataSource mDataSource;
    private CreditAdapter creditAdapter;
    private static final int ADD_OK = 1;
    public static final String CREDIT_DETAIL = "detail";
    private static final int DETAIL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showing_credit);

        mDataSource = new DataSource(this);
        cardsListView = findViewById(R.id.credit_listView);
        FloatingActionButton addCards = findViewById(R.id.credit_add_button);

        setUpListView();

        addCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addCardIntent = new Intent(ShowingCreditActivity.this, AddingCreditActivity.class);
                startActivityForResult(addCardIntent, ADD_OK);
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    private void setUpListView() {

        creditAdapter = new CreditAdapter(this, null, 0);

        cardsListView.setAdapter(creditAdapter);

        cardsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Uri uri = Uri.parse(DataProvider.CREDIT_URI+"/"+id);
                Intent detailIntent = new Intent(ShowingCreditActivity.this,
                        ShowingCreditDetailActivity.class);
                detailIntent.putExtra(CREDIT_DETAIL, uri);
                startActivityForResult(detailIntent, DETAIL);
            }
        });

    }

    public void reLoad(){
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==ADD_OK&&resultCode==RESULT_OK){
            reLoad();
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
        mDataSource.open();
        reLoad();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, DataProvider.CREDIT_URI,
                null, null, null, CreditTable.COL1);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        creditAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        creditAdapter.swapCursor(null);
    }
}
