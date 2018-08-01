package com.hongjolim.mfmanager.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.hongjolim.mfmanager.tools.BigDecimalCalculator;
import com.hongjolim.mfmanager.R;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.InCategoryTable;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.tools.CurrencyFormatter;

import java.math.BigDecimal;

public class InCategoryAdapter extends CursorAdapter {

    private Context mContext;
    public InCategoryAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.budget_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView earned = view.findViewById(R.id.sofar);
        earned.setText(R.string.earned);

        int id = cursor.getInt(cursor.getColumnIndex(InCategoryTable.COL1));
        String name = cursor.getString(cursor.getColumnIndex(InCategoryTable.COL2));
        String amount = cursor.getString(cursor.getColumnIndex(InCategoryTable.COL3));

        String selection = TransactionTable.COL2+"<= date('now', 'start of month', '+1 month', '-1 day') AND "+
                TransactionTable.COL2 + ">= date('now', 'start of month') AND "
                +TransactionTable.COL5+"=? AND "+TransactionTable.COL8+"=?";
        String[] selectionArgs = {String.valueOf(id),
                String.valueOf(TransactionTable.TRANS_TYPE2)};
        cursor = context.getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                selection, selectionArgs, TransactionTable.COL1);

        BigDecimal totalEarnedAmount = new BigDecimal("0.0");

        try{

            while(cursor.moveToNext()){
                totalEarnedAmount = BigDecimalCalculator.add(totalEarnedAmount.toString(),
                        cursor.getString(cursor.getColumnIndex(TransactionTable.COL3)));
            }

        }finally{
            if(cursor!=null&&!cursor.isClosed()){
                cursor.close();
            }
        }

        TextView cate_name = view.findViewById(R.id.category_list_name);
        TextView cate_amount = view.findViewById(R.id.category_list_amount);
        TextView cate_balance = view.findViewById(R.id.category_list_balance);

        cate_name.setText(name);
        cate_amount.setText(CurrencyFormatter.format(mContext, amount));
        cate_balance.setText(CurrencyFormatter.format(mContext, totalEarnedAmount.toString()));

    }
}
