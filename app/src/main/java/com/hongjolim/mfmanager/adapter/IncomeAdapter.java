package com.hongjolim.mfmanager.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.hongjolim.mfmanager.R;
import com.hongjolim.mfmanager.database.AccountsTable;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.InCategoryTable;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.tools.CurrencyFormatter;
import com.hongjolim.mfmanager.tools.DateFormatConverter;

public class IncomeAdapter extends CursorAdapter {

    private Context mContext;

    public IncomeAdapter(Context context, Cursor cursor, int flag){
        super(context, cursor, flag);
        this.mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.income_item, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView date = view.findViewById(R.id.income_list_date);
        TextView amount = view.findViewById(R.id.income_list_amount);
        TextView to = view.findViewById(R.id.income_list_to);
        TextView desc = view.findViewById(R.id.income_list_desc);
        TextView category = view.findViewById(R.id.income_list_category);

        String inDate = DateFormatConverter.
                convertDateToCustom(cursor.getString(cursor.getColumnIndex(TransactionTable.COL2)));
        String inAmount = cursor.getString(cursor.getColumnIndex(TransactionTable.COL3));
        String inDesc = cursor.getString(cursor.getColumnIndex(TransactionTable.COL4));
        int inCategory_id = cursor.getInt(cursor.getColumnIndex(TransactionTable.COL5));
        int inTo_id = cursor.getInt(cursor.getColumnIndex(TransactionTable.COL6));

        date.setText(inDate);
        amount.setText(CurrencyFormatter.format(mContext, "+"+inAmount));
        desc.setText(inDesc);

        cursor = context.getContentResolver().query(DataProvider.IN_CATEGORY_URI, InCategoryTable.ALL_COLS,
                InCategoryTable.COL1+"="+inCategory_id, null, null);

        try {
            while (cursor.moveToNext()) {
                String inCategory = cursor.getString(cursor.getColumnIndex(InCategoryTable.COL2));
                category.setText(inCategory);
            }
        }finally{
            if(cursor!=null&&!cursor.isClosed()){
                cursor.close();
            }
        }

        cursor = context.getContentResolver().query(DataProvider.ACCOUNTS_URI, AccountsTable.ALL_COLS,
                AccountsTable.COL1+"="+inTo_id, null, null);

        try {
            while (cursor.moveToNext()) {
                String inTo = cursor.getString(cursor.getColumnIndex(AccountsTable.COL2));
                to.setText(inTo);
            }
        }
        finally{
            if(cursor!=null&&!cursor.isClosed()){
                cursor.close();
            }
        }
    }
}
