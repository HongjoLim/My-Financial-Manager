package com.hongjolim.mfmanager;

import android.app.Activity;
import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hongjolim.mfmanager.adapter.InCategoryAdapter;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.InCategoryTable;
import com.hongjolim.mfmanager.model.InCategory;

/**
 * Name: HONGJO
 * Date: Mar 23, 2018.
 */

public class ChoosingInCategory extends DialogFragment {

    private Cursor cursor;
    private CategorySelected activity;

    public ChoosingInCategory() {
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (CategorySelected) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_choose_category, null);

        TextView fragment_name = v.findViewById(R.id.fragment_name);
        ListView in_category_lv = v.findViewById(R.id.category_choose_listView);
        cursor = getActivity().getContentResolver().query(DataProvider.IN_CATEGORY_URI,
                null, null, null, null);
        InCategoryAdapter adapter = new InCategoryAdapter(getActivity(), cursor, 0);
        fragment_name.setText(R.string.income_category);
        in_category_lv.setAdapter(adapter);

        in_category_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {

                InCategory inCategory = new InCategory();

                inCategory.set_id(cursor.getInt(cursor.getColumnIndex(InCategoryTable.COL1)));
                inCategory.setName(cursor.getString(cursor.getColumnIndex(InCategoryTable.COL2)));
                inCategory.setAmount(cursor.getString(cursor.getColumnIndex(InCategoryTable.COL3)));
                activity.onCategorySelected(inCategory);
                dismiss();
            }
        });

        return v;
    }

    public interface CategorySelected{
        void onCategorySelected(InCategory inCategory);
    }

}
