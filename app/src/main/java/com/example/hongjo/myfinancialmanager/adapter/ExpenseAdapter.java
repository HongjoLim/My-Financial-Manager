package com.example.hongjo.myfinancialmanager.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hongjo.myfinancialmanager.R;
import com.example.hongjo.myfinancialmanager.database.AccountsTable;
import com.example.hongjo.myfinancialmanager.database.DataProvider;
import com.example.hongjo.myfinancialmanager.database.DataSource;
import com.example.hongjo.myfinancialmanager.database.ExCategoryTable;
import com.example.hongjo.myfinancialmanager.database.TransactionTable;
import com.example.hongjo.myfinancialmanager.model.Account;
import com.example.hongjo.myfinancialmanager.model.CreditCard;
import com.example.hongjo.myfinancialmanager.model.ExCategory;
import com.example.hongjo.myfinancialmanager.model.Transaction;
import com.example.hongjo.myfinancialmanager.tools.DateFormatConverter;

import static android.view.View.VISIBLE;

public class ExpenseAdapter extends CursorAdapter {

    private DataSource mDataSource;

    public ExpenseAdapter(Context context, Cursor cursor, int flag){
        super(context, cursor, flag);
        mDataSource = new DataSource(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.expense_item, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView date = view.findViewById(R.id.expense_list_date);
        TextView amount = view.findViewById(R.id.expense_list_amount);
        TextView from = view.findViewById(R.id.expense_list_from);
        TextView desc = view.findViewById(R.id.expense_list_desc);
        TextView category = view.findViewById(R.id.expense_list_category);

        String exDate = DateFormatConverter.
                convertDateToCustom(cursor.getString(cursor.getColumnIndex(TransactionTable.COL2)));
        String exAmount = cursor.getString(cursor.getColumnIndex(TransactionTable.COL3));
        String exDesc = cursor.getString(cursor.getColumnIndex(TransactionTable.COL4));

        int exCategory_id = cursor.getInt(cursor.getColumnIndex(TransactionTable.COL5));

        //declare variable to store the id of the account
        int exFrom_id;
        //if it is not paid from account, the cursor will return -1
        if(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL6))>=1){
            exFrom_id = cursor.getInt(cursor.getColumnIndex(TransactionTable.COL6));
            Account account = mDataSource.getAccount(exFrom_id);
            from.setText(account.getName());
        }else{
            exFrom_id = cursor.getInt(cursor.getColumnIndex(TransactionTable.COL7));
            CreditCard card = mDataSource.getCreditCard(exFrom_id);
            from.setText(card.getName());
        }

        date.setText(exDate);
        amount.setText(String.format("- %s", exAmount));
        desc.setText(exDesc);

        ExCategory exCategory = mDataSource.getExCategory(exCategory_id);

        category.setText(exCategory.getName());

    }
}
