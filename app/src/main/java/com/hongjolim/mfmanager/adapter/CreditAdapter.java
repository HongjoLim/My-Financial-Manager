package com.hongjolim.mfmanager.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hongjolim.mfmanager.R;
import com.hongjolim.mfmanager.database.CreditTable;
import com.hongjolim.mfmanager.tools.CurrencyFormatter;

public class CreditAdapter extends CursorAdapter {

    public CreditAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.credit_item, null);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = view.findViewById(R.id.credit_item_name);
        TextView date = view.findViewById(R.id.credit_due_date);
        TextView amount = view.findViewById(R.id.credit_amount);
        TextView cycle = view.findViewById(R.id.credit_cycle);

        String credit_name = cursor.getString(cursor.getColumnIndex(CreditTable.COL2));
        int due = cursor.getInt(cursor.getColumnIndex(CreditTable.COL3));
        String credit_amount = cursor.getString(cursor.getColumnIndex(CreditTable.COL5));
        int cycleStart = cursor.getInt(cursor.getColumnIndex(CreditTable.COL6));
        int cycleEnd = cursor.getInt(cursor.getColumnIndex(CreditTable.COL7));

        name.setText(credit_name);
        date.setText(String.format("due: %s", due));
        amount.setText(String.format("bal: %s" , CurrencyFormatter.format(mContext, credit_amount)));
        cycle.setText(String.format("cycle: %d to %d", cycleStart, cycleEnd));
    }
}
