package com.hongjolim.mfmanager;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.UserTable;
import com.hongjolim.mfmanager.model.User;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Name: HONGJO LIM
 * Date: Jun 22, 2018
 * Purpose: A separate activity to let the user create new account
 */

public class RegisterActivity extends AppCompatActivity implements ChoosingSecurityQuestionFragment.FromCallBack{

    private DataSource mDataSource;
    private User user;

    /**
     * boolean variable to distinguish cases whether..
     * there is no input or invalid input (false),
     * or there is valid input (true)
     */

    private boolean email_valid = false;
    private boolean password_valid = false;
    private boolean security_question_selected = false;
    private boolean security_answer_valid = false;

    private EditText emailEdt, passwordEdt, securityATv;

    //TextView to touch choose security question
    //(will be set up with adapter, to show question lists to choose from)
    private TextView securityQTv;

    //this needs to be class member to be controlled by call back method
    private TextView security_question_notselected;

    private Button create_account;

    //security question number. It will be assigned from call back
    private int securityQNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //find the views for view components
        emailEdt = findViewById(R.id.register_email);
        passwordEdt = findViewById(R.id.register_password);
        securityQTv = findViewById(R.id.security_question);
        securityATv = findViewById(R.id.security_answer);
        create_account = findViewById(R.id.create_account);
        security_question_notselected = findViewById(R.id.security_question_notselected_warning);

        //connect this activity to the data source
        mDataSource = new DataSource(this);
        mDataSource.open();

        //get user data if there is any (this must be null)
        user = mDataSource.getUser();

        securityQTv.setOnClickListener(listener);
        create_account.setOnClickListener(listener);

    }

    View.OnClickListener listener = new View.OnClickListener(){

        @Override
        public void onClick(View view){

            switch(view.getId()){

                case R.id.security_question:
                    ChoosingSecurityQuestionFragment fragment = new ChoosingSecurityQuestionFragment();
                    fragment.show(getFragmentManager(), "CHOOSE_SECURITY_QUESTION");
                    break;
                case R.id.create_account:
                    register();
                    break;
            }
        }
    };

    private void register() {

        user = mDataSource.getUser();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.register_error);

        if (user != null) {
            builder.setMessage(R.string.sign_up_error_user_already_in_database);
            builder.setPositiveButton("OK", null).create().show();
            return;
        }

        //variables to store user input
        String email = emailEdt.getText().toString();
        String password = passwordEdt.getText().toString();

        String securityAnswer = securityATv.getText().toString();

        checkInputValidation(email, password, securityAnswer);

        if (email_valid && password_valid && security_answer_valid) {

            insertUser(email, password, securityQNumber, securityAnswer);
            return;
        }
    }

    //this method is to show warnings (if the inputs for email or password are invalid)
    private void checkInputValidation(String email, String password, String securityAnswer){

        //if the email is invalid, show the warning in the text view
        TextView invalid_email_warning = findViewById(R.id.invalid_email_warning);

        //if the password is too short, show the warning in the text view
        TextView invalid_password_warning = findViewById(R.id.invalid_password_warning);

        TextView invalid_security_answer = findViewById(R.id.invalid_security_answer_warning);

        if(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            email_valid = true;
            invalid_email_warning.setVisibility(GONE);
        }else{
            email_valid = false;
            invalid_email_warning.setVisibility(VISIBLE);
        }

        //password should be at lease 8 letters
        if(password.trim().length()>=8){
            password_valid = true;
            invalid_password_warning.setVisibility(GONE);
        }else{
            password_valid = false;
            invalid_password_warning.setVisibility(VISIBLE);
        }

        //if security question is not selected, show warning
        if(security_question_selected){
            security_question_notselected.setVisibility(GONE);
        }else{
            security_question_notselected.setVisibility(VISIBLE);
        }

        if(!securityAnswer.trim().isEmpty()){
            security_answer_valid = true;
            invalid_security_answer.setVisibility(GONE);
        }else{
            security_answer_valid = false;
            invalid_security_answer.setVisibility(VISIBLE);
        }

    }

    private void insertUser(String email, String password, int securityQNumber, String securityAnswer){

        ContentValues values = new ContentValues();
        values.put(UserTable.COL1, email);
        values.put(UserTable.COL2, password);
        values.put(UserTable.COL3, securityQNumber);
        values.put(UserTable.COL4, securityAnswer);

        getContentResolver().insert(DataProvider.USER_URI, values);

        Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void securityQuestionSelected(int index, String question) {
        //show the edit text for the security answer
        securityATv.setVisibility(VISIBLE);

        //assign the securityQuestionNumber
        securityQNumber = index;
        security_question_selected = true;

        //if the warning is visible, make it gone
        security_question_notselected.setVisibility(GONE);
        securityQTv.setText(question);

    }
}
