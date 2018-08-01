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
import android.widget.TextView;
import android.widget.Toast;

import com.hongjolim.mfmanager.adapter.InCategoryAdapter;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.InCategoryTable;

public class IncomeCategoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        EarningPlansAddFragment.DataEnterListener, InCategoryDetailFragment.DataChangedListener{

    private InCategoryAdapter adapter;
    private EarningPlansAddFragment addFragment;
    private DataSource mDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this class shares the layout with BudgetActivity since the contents are very similar to each other
        setContentView(R.layout.activity_budget);

        mDataSource = new DataSource(this);
        mDataSource.open();

        ListView in_category_lv = findViewById(R.id.category_choose_listView);

        TextView title = findViewById(R.id.category_activity_title);
        title.setText(R.string.earning_plans);

        adapter = new InCategoryAdapter(this, null, 0);

        in_category_lv.setAdapter(adapter);

        in_category_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Bundle b = new Bundle();
                b.putLong("IN_ID", id);

                InCategoryDetailFragment detailFragment = new InCategoryDetailFragment();
                detailFragment.setArguments(b);
                detailFragment.show(getFragmentManager(), "DETAIL_FRAGMENT");
            }
        });

        FloatingActionButton fab = findViewById(R.id.budget_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFragment = new EarningPlansAddFragment();
                addFragment.setCancelable(false);
                addFragment.show(getFragmentManager(),"DIALOG_FRAGMENT");
            }
        });

        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, DataProvider.IN_CATEGORY_URI,
                null, null, null, InCategoryTable.COL1);
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