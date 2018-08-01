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

import com.hongjolim.mfmanager.adapter.AccountsAdapter;
import com.hongjolim.mfmanager.database.AccountsTable;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.model.Account;

public class ChoosingInToFragment extends DialogFragment {

    private Cursor cursor;

    public ChoosingInToFragment() {}

        ListView acc_list_tv;
        ChoosingInToFragment.FromCallBack activity;

        @Override
        public void onAttach(Activity activity){
            super.onAttach(activity);
            this.activity = (ChoosingInToFragment.FromCallBack) activity;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_choosing_in_to, container, false);
            acc_list_tv = view.findViewById(R.id.account_listView);

            setAccountAdapter();

            return view;
        }

    private void setAccountAdapter(){
        cursor = getActivity().getContentResolver().query(DataProvider.ACCOUNTS_URI,
                null, null, null, AccountsTable.COL2);
        AccountsAdapter adapter = new AccountsAdapter(getActivity(), cursor, 0);
        acc_list_tv.setAdapter(adapter);

        acc_list_tv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Account account = new Account();

                account.setId(cursor.getInt(cursor.getColumnIndex(AccountsTable.COL1)));
                account.setName(cursor.getString(cursor.getColumnIndex(AccountsTable.COL2)));
                account.setType(cursor.getString(cursor.getColumnIndex(AccountsTable.COL3)));
                account.setStarting_balance(cursor.getString(cursor.getColumnIndex(AccountsTable.COL4)));
                account.setCurrent_balance(cursor.getString(cursor.getColumnIndex(AccountsTable.COL5)));

                activity.toSelected(account);
                dismiss();
            }

        });
    }

    public interface FromCallBack{
        void toSelected(Account account);
    }

}
