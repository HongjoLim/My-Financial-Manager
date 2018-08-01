package com.hongjolim.mfmanager;


import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectDateFragment extends DialogFragment {

    private FromCallBack activity;
    private ListView dateList;
    private ArrayAdapter<String> adapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (FromCallBack) activity;

    }

    public SelectDateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_payment_date_fragment, null);

        dateList = view.findViewById(R.id.date_listView);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.dates));

        int value = getArguments().getInt(AddingCreditActivity.SELECT_DATE);

        switch(value){
            //if this fragment is called to get pay due date,
            case 1:
                sendDueDate();
                break;
            //if this fragment is called to get cycle start date
            case 2:
                sendStartDate();
                break;
            //if this fragment is called to get cycle start date
            case 3:
                sendEndDate();
                break;
        }

        return view;
    }

    private void sendDueDate(){
        dateList.setAdapter(adapter);

        dateList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {

                activity.payDaySelected((int)id+1);
                dismiss();
            }
        });
    }

    private void sendStartDate(){
        dateList.setAdapter(adapter);

        dateList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {

                activity.cycleStartSelected((int)id+1);
                dismiss();
            }
        });
    }

    private void sendEndDate(){
        dateList.setAdapter(adapter);

        dateList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {

                activity.cycleEndSelected((int)id+1);
                dismiss();
            }
        });
    }

    public interface FromCallBack{
        void payDaySelected(int date);
        void cycleStartSelected(int start);
        void cycleEndSelected(int end);
    }
}
