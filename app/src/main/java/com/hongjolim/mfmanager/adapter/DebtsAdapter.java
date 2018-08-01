package com.hongjolim.mfmanager.adapter;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.hongjolim.mfmanager.R;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.model.ExCategory;
import com.hongjolim.mfmanager.model.Transaction;
import com.hongjolim.mfmanager.tools.CurrencyFormatter;
import com.hongjolim.mfmanager.tools.DateFormatConverter;

public class DebtsAdapter extends CursorAdapter {

    private DataSource mDataSource;
    private int cardId;
    private Transaction transaction;
    private OnChecked fragment;
    private Context mContext;

    public DebtsAdapter(Context context, Cursor cursor, int flag, int cardId, Fragment fragment){
        super(context, cursor, flag);
        this.mContext = context;
        this.cardId = cardId;
        this.fragment = (OnChecked) fragment;
        mDataSource = new DataSource(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.debts_item, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView date = view.findViewById(R.id.debts_list_date);
        TextView amount = view.findViewById(R.id.debts_list_amount);
        TextView desc = view.findViewById(R.id.debts_list_desc);
        TextView category = view.findViewById(R.id.debts_list_category);

        CheckBox checkBox = view.findViewById(R.id.checkBox);

        String exDate = DateFormatConverter.
                convertDateToCustom(cursor.getString(cursor.getColumnIndex(TransactionTable.COL2)));
        String exAmount = cursor.getString(cursor.getColumnIndex(TransactionTable.COL3));
        String exDesc = cursor.getString(cursor.getColumnIndex(TransactionTable.COL4));

        int exCategory_id = cursor.getInt(cursor.getColumnIndex(TransactionTable.COL5));

        //declare variable to store the id of the card

        transaction = mDataSource.getTransaction(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL1)));

        date.setText(exDate);
        amount.setText(CurrencyFormatter.format(mContext, exAmount));
        desc.setText(exDesc);

        ExCategory exCategory = mDataSource.getExCategory(exCategory_id);

        category.setText(exCategory.getName());

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                transaction.setChecked(b);

                fragment.onItemChecked(transaction);

            }
        });

    }


    public interface OnChecked{
        void onItemChecked(Transaction transaction);
    }
}
