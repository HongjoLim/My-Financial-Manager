package com.example.hongjo.myfinancialmanager;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hongjo.myfinancialmanager.database.DataProvider;
import com.example.hongjo.myfinancialmanager.database.DataSource;
import com.example.hongjo.myfinancialmanager.database.UserTable;
import com.example.hongjo.myfinancialmanager.model.User;

//this app only accepts 1 user account because it is run in local Database
public class LoginActivity extends AppCompatActivity{

    //key for shared preference
    public static final String ENABLE_LOGIN = "ENABLE_LOGIN";

    private DataSource mDataSource;

    private User user;

    private EditText emailEdt, passwordEdt;

    private String email, password;

    private TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDataSource = new DataSource(this);

        /**
         * Find user data from the database,
         * if the system cannot find the user data,
         * the exception would be handled in DataSource class.
         * But just to be save, handle the exception here as well
         * */
        try {
            user = mDataSource.getUser();
        }catch(Exception e){
            Toast.makeText(this, R.string.login_no_user, Toast.LENGTH_SHORT).show();
        }

        emailEdt = findViewById(R.id.email);
        passwordEdt = findViewById(R.id.password);

        forgotPassword = findViewById(R.id.forgot_password);

        Button signIn = findViewById(R.id.sign_in_button);
        Button createAccount = findViewById(R.id.create_account_button);

        signIn.setOnClickListener(listener);
        createAccount.setOnClickListener(listener);
        forgotPassword.setOnClickListener(listener);

    }

    View.OnClickListener listener = new View.OnClickListener(){

        @Override
        public void onClick(View view){

            switch(view.getId()){
                case R.id.sign_in_button:
                    checkValidity();
                    break;
                case R.id.create_account_button:
                    register();
                    break;
                case R.id.forgot_password:
                    //if the user forgets the password, send it to the user's email
                    //TO DO: send the user email with his/her message
                    if(user!=null) {
                        Toast.makeText(LoginActivity.this, user.getPassword(), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    //if the user wants to sign in, this method is called
    private void checkValidity(){
        email = emailEdt.getText().toString();
        password = passwordEdt.getText().toString();

        //get the user object and store it into the instance variable
        user = mDataSource.getUser();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sign_in_error);

        if(email.isEmpty()||password.isEmpty()){
            builder.setMessage(R.string.sign_in_error_prompt_email_password);
            builder.setCancelable(false).setPositiveButton("OK", null).create().show();
            return;
        }

        //if there is no user in the database
        if(user==null){
            builder.setMessage(R.string.sign_in_error_no_user_in_database);
            builder.setCancelable(false).setPositiveButton("OK", null).create().show();
            return;
        }

        if(user.getEmail().equals(email)){
            //there is the matching email and the password data
            if(user.getPassword().equals(password)){
                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }else{
                //there is the matching email in the database but the password is different
                builder.setMessage(R.string.sign_in_error_no_password);
                builder.setCancelable(false).setPositiveButton("OK", null).create().show();
            }
        }else{
            //if there is no user who has the same email in the database
            builder.setMessage(R.string.sign_in_error_no_email_in_database);
            builder.setCancelable(false).setPositiveButton("OK", null).create().show();
        }

    }

    //this method is to create account
    private void register(){

        user = mDataSource.getUser();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.register_error);

        if(user!=null){
            builder.setMessage(R.string.sign_up_error_user_already_in_database);
            builder.setPositiveButton("OK", null).create().show();
            return;
        }

        email = emailEdt.getText().toString();
        password = passwordEdt.getText().toString();

        if(!email.contains("@")){
            builder.setMessage(R.string.register_error_invalid_email)
            .setPositiveButton("OK", null).create().show();
        }else if(password.trim().length()<8){
            builder.setMessage(R.string.register_error_short_password)
            .setPositiveButton("OK", null).create().show();
        }else{
            insertUser(email, password);
            builder.setTitle(R.string.register_ok_account_created);
            builder.setMessage(R.string.promt_sign_in).setPositiveButton("OK", null).create().show();
            emailEdt.setText(email);
            passwordEdt.setText("");
        }
    }

    private void insertUser(String email, String password){

        ContentValues values = new ContentValues();

        values.put(UserTable.COL1, email);
        values.put(UserTable.COL2, password);

        getContentResolver().insert(DataProvider.USER_URI, values);
    }

}

