package com.hongjolim.mfmanager;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Name: HONGJO LIM
 * Date: Jun 22, 2018
 * Purpose: This class is to show the security question when the user forgets the id or password
 */

public class ShowingSecurityQuestionFragment extends DialogFragment{

    private TextView securityQuestionTv;

    private EditText securityAnswerEdt;

    private Button submit;

    private String securityQuestion;

    private ShowingSecurityQuestionFragment.FromCallBack activity;

    public ShowingSecurityQuestionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = (ShowingSecurityQuestionFragment.FromCallBack)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_showing_security_question, container, false);

        securityQuestionTv = view.findViewById(R.id.security_question_textView);
        securityAnswerEdt = view.findViewById(R.id.security_answer_editText);
        submit = view.findViewById(R.id.security_question_submit);

        //get the security question from Login Activity
        securityQuestion = getArguments().getString(LoginActivity.SECURITY_QUESTION);

        Toast.makeText(getActivity(), securityQuestion, Toast.LENGTH_SHORT).show();

        //set the question on the edit text component
        securityQuestionTv.setText(securityQuestion);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String securityAnswer = securityAnswerEdt.getText().toString();
                activity.securityQuestionAnswered(securityAnswer);
                dismiss();
            }
        });

        return view;
    }

    public interface FromCallBack {
        void securityQuestionAnswered(String securityAnswer);
    }

}
