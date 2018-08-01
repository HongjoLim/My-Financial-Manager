package com.hongjolim.mfmanager;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.model.User;

//this app only accepts 1 user account because it is run in local Database
public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
ShowingSecurityQuestionFragment.FromCallBack{

    //key for shared preference
    public static final String ENABLE_LOGIN = "ENABLE_LOGIN";

    private DataSource mDataSource;

    private User user;

    private EditText emailEdt, passwordEdt;

    private String email, password;

    private TextView forgotPassword;

    /**
     * This variable is used as the key, when passing a security question string
     * as an argument to the ShowingSecurityQuestionFragment.java class
     */
    public static final String SECURITY_QUESTION = "security_question";

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
        Button register = findViewById(R.id.register);

        signIn.setOnClickListener(listener);
        register.setOnClickListener(listener);
        forgotPassword.setOnClickListener(listener);

        //init the LoaderManager
        getLoaderManager().initLoader(0, null, this);

    }

    View.OnClickListener listener = new View.OnClickListener(){

        @Override
        public void onClick(View view){

            switch(view.getId()){
                case R.id.sign_in_button:
                    checkValidity();
                    break;
                case R.id.register:
                    //if user data already exists, show this alert dialog
                    if(user!=null){
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setTitle(R.string.register_error);
                        builder.setMessage(R.string.sign_up_error_user_already_in_database);
                        builder.setPositiveButton("OK", null).create().show();
                        return;
                    }
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.forgot_password:
                    //if the user forgets the password, send it to the user's email
                    //TO DO: let the user answer his/her security question
                    if(user!=null) {
                        promptSecurityQuestion();
                    }else{
                        Toast.makeText(LoginActivity.this, "Please create a new account", Toast.LENGTH_SHORT).show();
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

    /**
     * This method is to show the security question and get the answer to it.
     * It is called when the user forgets the password or id
     */

    private void promptSecurityQuestion(){

        ShowingSecurityQuestionFragment fragment = new ShowingSecurityQuestionFragment();

        int index = user.getSecurityQNum();
        String securityQuestion = getResources().getStringArray(R.array.security_question)[index];

        Bundle bundle = new Bundle();
        bundle.putString(SECURITY_QUESTION, securityQuestion);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "PROMPT_SECURITY_QUESTION");

    }

    private void reLoad(){
        getLoaderManager().restartLoader(0, null, this);
        user = mDataSource.getUser();
    }

    @Override
    public void onResume(){
        super.onResume();
        mDataSource.open();
        reLoad();
    }

    @Override
    public void onPause(){
        super.onPause();
        mDataSource.close();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, DataProvider.USER_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

    @Override
    public void securityQuestionAnswered(String securityAnswer) {

        if(securityAnswer.toLowerCase().equals(user.getSecuirtyAnswer().toLowerCase())){
            Toast.makeText(this, "Your password is: "+user.getPassword() , Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "Wrong security answer!", Toast.LENGTH_SHORT).show();
        }
    }
}

