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

/**
 * Name: HONGJO LIM
 * Date: Jun 22nd, 2018
 * Purpose: This is a fragment to choose a security question from, when the user tries to register
 */
public class ChoosingSecurityQuestionFragment extends DialogFragment {

    private ChoosingSecurityQuestionFragment.FromCallBack activity;
    private ListView securityQuestionList;

    private String[] securityQuestions;

    public ChoosingSecurityQuestionFragment() {

    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (ChoosingSecurityQuestionFragment.FromCallBack) activity;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_choosing_security_question, container, false);
        securityQuestionList = view.findViewById(R.id.security_question_listView);

        securityQuestions = getActivity().getResources().getStringArray(R.array.security_question);
        securityQuestionList.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1,
                securityQuestions));

        securityQuestionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
               activity.securityQuestionSelected((int) id, securityQuestions[(int) id]);
                dismiss();
            }
        });

        return view;
    }

    public interface FromCallBack{

        void securityQuestionSelected(int securityQuestionIndex, String securityQuestion);
    }

}
