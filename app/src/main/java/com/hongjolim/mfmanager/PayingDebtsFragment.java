package com.hongjolim.mfmanager;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hongjolim.mfmanager.adapter.DebtsAdapter;
import com.hongjolim.mfmanager.database.AccountsTable;
import com.hongjolim.mfmanager.database.CreditTable;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.model.Account;
import com.hongjolim.mfmanager.model.CreditCard;
import com.hongjolim.mfmanager.model.Transaction;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;

import java.util.ArrayList;

/**
 * Name: Hongjo Lim
 * Date: Apr 18th, 2018
 */
public class PayingDebtsFragment extends DialogFragment implements DebtsAdapter.OnChecked, LoaderManager.LoaderCallbacks<Cursor>{

    private DebtsAdapter adapter;
    private CreditCard card;
    private DataSource mDataSource;
    private int cardId;

    private TextView debtsTotal;
    private TextView billsTotal;

    private ArrayList<Transaction> transactions = new ArrayList<>();

    public PayingDebtsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paying_debts, null);

        ListView expense_lv = view.findViewById(R.id.debts_listView);
        Button payBills = view.findViewById(R.id.pay_bills_button);
        debtsTotal = view.findViewById(R.id.debts_total);
        billsTotal = view.findViewById(R.id.bills_total);

        cardId = getArguments().getInt("CardId");

        mDataSource = new DataSource(getActivity());
        setTotalDebts(cardId);

        String[] selectionArgs = {String.valueOf(cardId)};

        Cursor cursor = getActivity().getContentResolver().query(DataProvider.TRANSACTION_URI,
                TransactionTable.ALL_COLS, TransactionTable.COL6+" IS NULL AND "+TransactionTable.COL7+"=?",
                selectionArgs, TransactionTable.COL2+" DESC, "+TransactionTable.COL1+" DESC");
        adapter = new DebtsAdapter(getActivity(), cursor, 0, cardId, this);

        expense_lv.setAdapter(adapter);

        setListenerOnPayBillsButton(payBills, transactions);

        getLoaderManager().initLoader(0, null, this);

        return view;
    }

    private void setTotalDebts(int cardId){
        card = mDataSource.getCreditCard(cardId);
        debtsTotal.setText(String.format("Total: %s", card.getAmount()));
    }

    private void setTotalBills(ArrayList<Transaction> transactions){
        billsTotal.setText(String.format("Total: %s", getTotalAmount(transactions)));
    }

    private void reLoad(){
        getLoaderManager().restartLoader(0, null, this);
        transactions.clear();
        setTotalDebts(cardId);
        setTotalBills(transactions);
    }

    private void setListenerOnPayBillsButton(Button payBills, final ArrayList<Transaction> transactions){

        final Account account = mDataSource.getAccount(card.getAccount_id());

        payBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String totalAmount = getTotalAmount(transactions);

                if (transactions.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Paying Credit Bills");
                    builder.setMessage(String.format("Total amount %s will be paid from %s ", totalAmount,
                            account.getName())).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            deductDebts(totalAmount);

                            deductFromAccount(totalAmount);

                            for (int j = 0; j < transactions.size(); j++) {
                                String[] stringArgs = {String.valueOf(transactions.get(j).getId())};

                                ContentValues values = new ContentValues();

                                //put associated account id of the card into the transaction table
                                //after this, the specific expense is not going to be on the Paying bills list
                                values.put(TransactionTable.COL6, card.getAccount_id());
                                getActivity().getContentResolver().update(DataProvider.TRANSACTION_URI,
                                        values, TransactionTable.COL1 + "=?", stringArgs);
                            }

                            reLoad();
                            dialogInterface.dismiss();
                        }
                    }).setNegativeButton("Cancel", null).create().show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Paying bills").setMessage("No Payment Selected").setNegativeButton("Cancel", null)
                    .create().show();
                }
            }
        });
    }

    private String getTotalAmount(ArrayList<Transaction> transactions){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String currencyCode = prefs.getString("CURRENCY", "Canada");

        String totalAmount = String.valueOf(String.valueOf(BigDecimalCalculator.roundValue(0.0, currencyCode)));
        for(int i = 0; i<transactions.size(); i++){
            totalAmount = BigDecimalCalculator.add(totalAmount, transactions.get(i).getAmount()).toString();
        }
        return totalAmount;
    }

    private void deductDebts(String totalAmount){

        String newBalance = BigDecimalCalculator.subtract(card.getAmount(), totalAmount).toString();

        ContentValues values = new ContentValues();
        values.put(CreditTable.COL5, newBalance);

        String[] stringArgs = {String.valueOf(card.getId())};
        getActivity().getContentResolver().update(DataProvider.CREDIT_URI, values,
                CreditTable.COL1+"=?", stringArgs);
    }

    private void deductFromAccount(String totalAmount){
        Account account = mDataSource.getAccount(card.getAccount_id());
        String newBalance = BigDecimalCalculator.subtract(account.getCurrent_balance(), totalAmount).toString();

        ContentValues values = new ContentValues();
        values.put(AccountsTable.COL5, newBalance);

        String[] stringArgs = {String.valueOf(account.getId())};
        getActivity().getContentResolver().update(DataProvider.ACCOUNTS_URI, values,
                AccountsTable.COL1+"=?", stringArgs);
    }

    @Override
    public void onItemChecked(Transaction transaction) {
        if(transaction.isChecked()) {
            transactions.add(transaction);
        }else{
            transactions.remove(transaction);
        }
        setTotalBills(transactions);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] selectionArgs = {String.valueOf(cardId)};

        return new CursorLoader(getContext(), DataProvider.TRANSACTION_URI,
                null, TransactionTable.COL6+" IS NULL AND "+TransactionTable.COL7+"=?",
                selectionArgs, TransactionTable.COL2+" DESC, "+TransactionTable.COL1+" DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
