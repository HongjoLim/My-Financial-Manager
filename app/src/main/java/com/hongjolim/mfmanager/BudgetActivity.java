package com.hongjolim.mfmanager;


import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hongjolim.mfmanager.adapter.ExCategoryAdapter;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.ExCategoryTable;

public class BudgetActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
CategoryAddFragment.DataEnterListener, BudgetDetailFragment.DataChangedListener{

    private ExCategoryAdapter adapter;
    private CategoryAddFragment addFragment;
    private DataSource mDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        mDataSource = new DataSource(this);
        mDataSource.open();

        ListView ex_category_lv = findViewById(R.id.category_choose_listView);

        adapter = new ExCategoryAdapter(this, null, 0);

        ex_category_lv.setAdapter(adapter);

        ex_category_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Bundle b = new Bundle();
                b.putLong("EX_ID", id);

                BudgetDetailFragment detailFragment = new BudgetDetailFragment();
                detailFragment.setArguments(b);
                detailFragment.show(getFragmentManager(), "DETAIL_FRAGMENT");

            }
        });

        FloatingActionButton fab = findViewById(R.id.budget_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFragment = new CategoryAddFragment();
                addFragment.setCancelable(false);
                addFragment.show(getFragmentManager(),"DIALOG_FRAGMENT");
            }
        });

        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, DataProvider.EX_CATEGORY_URI,
                null, null, null, ExCategoryTable.COL1);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onDataEnterComplete(){
        reload();
        Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDataChanged(){
        reload();
    }

    private void reload(){
        getLoaderManager().restartLoader(0, null, this);
    }
}
