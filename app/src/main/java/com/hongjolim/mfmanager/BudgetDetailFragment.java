package com.hongjolim.mfmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.ExCategoryTable;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.model.ExCategory;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class BudgetDetailFragment extends DialogFragment {

    private EditText category_name;
    private EditText category_amount;
    private String filter;

    private DataSource mDataSource;

    private DataChangedListener mListener;
    private String oldName;
    private String oldAmount;
    private Long exCategory_id;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        mDataSource = new DataSource(activity);
        mListener = (DataChangedListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View v = inflater.inflate(R.layout.fragment_category_detail, container, false);
        TextView title = v.findViewById(R.id.category_dialog_title);
        ImageView delete = v.findViewById(R.id.category_delete);
        ImageView edit = v.findViewById(R.id.category_edit);
        Button cancel = v.findViewById(R.id.category_detail_dialog_cancel);

        edit.setColorFilter(R.color.colorPrimaryDark);
        delete.setColorFilter(R.color.colorPrimaryDark);

        cancel.setOnClickListener(listener);
        delete.setOnClickListener(listener);
        edit.setOnClickListener(listener);

        title.setText(R.string.edit_category);

        exCategory_id = getArguments().getLong("EX_ID");

        filter = ExCategoryTable.COL1+"="+exCategory_id;

        Uri uri = Uri.parse(DataProvider.EX_CATEGORY_URI+"/"+exCategory_id);
        Cursor cursor = getActivity().getContentResolver().query(uri, ExCategoryTable.ALL_COLS,
                filter, null, null);

        try {
            while (cursor.moveToNext()) {
                oldName = cursor.getString(cursor.getColumnIndex(ExCategoryTable.COL2));
                oldAmount = cursor.getString(cursor.getColumnIndex(ExCategoryTable.COL3));
            }
        }finally{
            if(cursor!=null&&!cursor.isClosed()){
                cursor.close();
            }
        }

        category_name = v.findViewById(R.id.category_dialog_name);
        category_amount = v.findViewById(R.id.category_dialog_amount);

        category_name.setText(oldName);
        category_amount.setText(String.valueOf(oldAmount));

        return v;
    }

    View.OnClickListener listener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            int id = v.getId();
            switch(id){
                case R.id.category_detail_dialog_cancel:
                    dismiss();
                    break;

                case R.id.category_delete:
                    if(exCategory_id==1){
                        AlertDialog.Builder delete_builder = new AlertDialog.Builder(getActivity());
                        delete_builder.setMessage("You cannot delete default category").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
                        return;
                    }

                    promptChooseAnotherCategory();

                    break;
                case R.id.category_edit:
                    String newName = category_name.getText().toString();

                    if(newName.isEmpty()||newName.trim().length()==0){
                        AlertDialog.Builder edit_builder = new AlertDialog.Builder(getActivity());
                        edit_builder.setMessage("Category name cannot be empty").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
                        return;
                    }

                    double newAmount;

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String currencyCode = prefs.getString("CURRENCY", "Canada");

                    if(category_amount.getText().toString().trim().length()==0) {
                        newAmount = BigDecimalCalculator.roundValue(0.0, currencyCode);
                    }
                    else{
                        try {
                            newAmount = BigDecimalCalculator.roundValue(Double.parseDouble
                                    (category_amount.getText().toString()), currencyCode);
                        }catch(Exception e){
                            newAmount = BigDecimalCalculator.roundValue(0.0, currencyCode);
                        }
                    }

                    if(oldName.equals(newName)&&Double.parseDouble(oldAmount)==newAmount) {
                        dismiss();
                    }else{
                        updateDetail(newName, newAmount);
                    }
                    break;
            }
        }
    };

    private void promptChooseAnotherCategory(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getActivity().getLayoutInflater().inflate(R.layout.delete_alert_dialog, null);

        builder.setView(view);
        builder.setTitle("Delete Category");
        builder.setMessage("Please select one");

        final Spinner categoryChooseSpinner = view.findViewById(R.id.delete_spinner);

        categoryChooseSpinner.setVisibility(GONE);

        //get All the ExCategory Objects from the DataSource class
        ArrayList<ExCategory> exCategories = mDataSource.getAllExCategories();

        //remove the ExCategory that is same as the one the user wants to delete
        //find it with its 'unique id(Primary Key in the table)' in the "ExCategory Table"
        for(int i = 0; i<exCategories.size(); i++){
            if(exCategories.get(i).get_id()==exCategory_id){
                exCategories.remove(exCategories.get(i));
            }
        }

        //declare this ArrayList as final, so that it can be passed into inner class
        final ArrayList<ExCategory> finalExCategories = exCategories;

        //get All the names of the ExCategory EXCEPT FOR this current ExCategory
        final String[] exCategoryNames = new String[exCategories.size()];
        for(int i = 0; i<exCategories.size(); i++){
            exCategoryNames[i] = exCategories.get(i).getName();
        }

        //initialize the adapter that sets the data to the spinner on the Dialog
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, exCategoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryChooseSpinner.setAdapter(adapter);

        final RadioGroup radioGroup = view.findViewById(R.id.radio_group);

        //set OnCheckedChangeListener to the Radio Group
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.radio_delete:
                        categoryChooseSpinner.setVisibility(GONE);
                        break;
                    case R.id.radio_move:
                        categoryChooseSpinner.setVisibility(VISIBLE);
                        break;
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch(radioGroup.getCheckedRadioButtonId()){

                    case R.id.radio_delete:
                        deleteBelongingTransactions();
                        break;
                    case R.id.radio_move:
                        //find the expense category id that matches with the id of expense chosen in spinner
                        ExCategory exCategory = finalExCategories.get(categoryChooseSpinner.getSelectedItemPosition());
                        moveBelongingTransactions(exCategory.get_id());
                        break;
                    default:
                        dismiss();
                }
                deleteCategory();
            }
        });

        builder.create().show();
    }

    private void deleteBelongingTransactions() {

        //TransactionTable.TRANS_TYPE1 means it is expense
        String selection = TransactionTable.COL5+"=? AND "+TransactionTable.COL8+"="+String.valueOf(TransactionTable.TRANS_TYPE1);
        String[] selectionArgs = {String.valueOf(exCategory_id)};

        getActivity().getContentResolver().delete(DataProvider.TRANSACTION_URI,
                selection, selectionArgs);
    }

    private void moveBelongingTransactions(int newCategoryId){

        String selection = TransactionTable.COL5+"=? AND "+TransactionTable.COL8+"=?";
        String[] selectionArgs ={String.valueOf(exCategory_id), String.valueOf(TransactionTable.TRANS_TYPE1)};

        ContentValues values = new ContentValues();

        values.put(TransactionTable.COL5, newCategoryId);

        getActivity().getContentResolver().update(DataProvider.TRANSACTION_URI, values, selection, selectionArgs);

    }

    private void updateDetail(String newName, double newAmount){

        ContentValues values = new ContentValues();
        values.put(ExCategoryTable.COL2, newName);
        values.put(ExCategoryTable.COL3, String.valueOf(newAmount));

        getActivity().getContentResolver().update(DataProvider.EX_CATEGORY_URI, values, filter, null);
        mListener.onDataChanged();
        Toast.makeText(getActivity(), "Category detail changed", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void deleteCategory(){
        getActivity().getContentResolver().delete(DataProvider.EX_CATEGORY_URI, filter, null);
        Toast.makeText(getActivity(), "Category deleted", Toast.LENGTH_SHORT).show();
        //callback method
        mListener.onDataChanged();
        dismiss();
    }

    public interface DataChangedListener{
        void onDataChanged();
    }
}
