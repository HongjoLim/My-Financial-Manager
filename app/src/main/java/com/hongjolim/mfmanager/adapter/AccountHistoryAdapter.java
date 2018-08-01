package com.hongjolim.mfmanager.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.hongjolim.mfmanager.R;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.tools.CurrencyFormatter;
import com.hongjolim.mfmanager.tools.DateFormatConverter;

public class AccountHistoryAdapter extends CursorAdapter {

    Context mContext;

    public AccountHistoryAdapter(Context context, Cursor cursor, int flag){
        super(context, cursor, flag);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.account_history_item, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView date = view.findViewById(R.id.account_history_list_date);
        TextView amount = view.findViewById(R.id.account_history_list_amount);
        TextView desc = view.findViewById(R.id.account_history_list_desc);

        String transDate = DateFormatConverter.
                convertDateToCustom(cursor.getString(cursor.getColumnIndex(TransactionTable.COL2)));
        String transAmount = cursor.getString(cursor.getColumnIndex(TransactionTable.COL3));
        String transDesc = cursor.getString(cursor.getColumnIndex(TransactionTable.COL4));
        int transType = cursor.getInt(cursor.getColumnIndex(TransactionTable.COL8));

        date.setText(transDate);
        desc.setText(transDesc);
        if(transType==TransactionTable.TRANS_TYPE1||transType==TransactionTable.TRANS_TYPE3||
                transType==TransactionTable.TRANS_TYPE5) {
            amount.setTextColor(Color.RED);
            amount.setText("-" + CurrencyFormatter.format(mContext, transAmount));
        }else{
            amount.setTextColor(context.getResources().getColor(R.color.colorAccent));
            amount.setText("+" +transAmount);
        }

    }
}
