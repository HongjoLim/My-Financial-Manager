package com.example.hongjo.myfinancialmanager;

import android.app.Activity;
import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.hongjo.myfinancialmanager.adapter.ExCategoryAdapter;
import com.example.hongjo.myfinancialmanager.database.DataProvider;
import com.example.hongjo.myfinancialmanager.database.ExCategoryTable;
import com.example.hongjo.myfinancialmanager.model.ExCategory;

/**
 * Name: HONGJO
 * Date: Mar 23, 2018.
 * Purpose: This class shows list of expense categories that user can choose from
 */

public class ChoosingExCategory extends DialogFragment {

    private Cursor cursor;

    private CategorySelected activity;

    public ChoosingExCategory() {
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (CategorySelected) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_choose_category, null);
        ListView ex_category_lv = v.findViewById(R.id.category_choose_listView);
        cursor = getActivity().getContentResolver().query(DataProvider.EX_CATEGORY_URI,
                null, null, null, null);
        ExCategoryAdapter adapter = new ExCategoryAdapter(getActivity(), cursor, 0);
        ex_category_lv.setAdapter(adapter);

        ex_category_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {

                ExCategory exCategory = new ExCategory();
                exCategory.set_id(cursor.getInt(cursor.getColumnIndex(ExCategoryTable.COL1)));
                exCategory.setName(cursor.getString(cursor.getColumnIndex(ExCategoryTable.COL2)));
                exCategory.setAmount(cursor.getString(cursor.getColumnIndex(ExCategoryTable.COL3)));

                activity.onCategorySelected(exCategory);
                dismiss();
            }
        });

        return v;
    }

    public interface CategorySelected{
        void onCategorySelected(ExCategory exCategory);
    }

}
