package com.example.hongjo.myfinancialmanager;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDataSource = new DataSource(this);

        emailEdt = findViewById(R.id.email);
        passwordEdt = findViewById(R.id.password);

        Button signIn = findViewById(R.id.sign_in_button);
        Button createAccount = findViewById(R.id.create_account_button);

        signIn.setOnClickListener(listener);
        createAccount.setOnClickListener(listener);

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
        builder.setTitle("Sign In Error");
        //if there is no user in the database
        if(user==null){
            builder.setMessage("There is no user In database");
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
                builder.setMessage("Wrong Password");
                builder.setCancelable(false).setPositiveButton("OK", null).create().show();
            }
        }else{
            //if there is no user by the email in the database
            builder.setMessage("No Email found in the Database");
            builder.setCancelable(false).setPositiveButton("OK", null).create().show();
        }

    }

    //this method is to create account
    private void register(){

        user = mDataSource.getUser();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Register Error");

        if(user!=null){
            builder.setMessage("User Data Already Exists");
            builder.setPositiveButton("OK", null).create().show();
            return;
        }

        email = emailEdt.getText().toString();
        password = passwordEdt.getText().toString();

        if(!email.contains("@")){
            builder.setMessage("Please enter valid Email")
            .setPositiveButton("OK", null).create().show();
        }else if(password.trim().length()<8){
            builder.setMessage("Please use more than 8 letters for password")
            .setPositiveButton("OK", null).create().show();
        }else{
            insertUser(email, password);
            builder.setTitle("Account Created");
            builder.setMessage("Please sign in").setPositiveButton("OK", null).create().show();
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

