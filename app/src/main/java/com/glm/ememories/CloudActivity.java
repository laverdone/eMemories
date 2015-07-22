/*
package com.glm.ememories;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.glm.ememories.R;
import com.glm.utilities.SynchHelper;

*/
/**
 * Created by gianluca on 23/06/14.
 *//*

public class CloudActivity extends Activity {

    private Button mLogin;
    private Button mCancel;
    private EditText mUserName;
    private EditText mPassword;
    private CheckBox mNewUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloudactivity);

        mLogin      = (Button) findViewById(R.id.cmbOk);
        mCancel     = (Button) findViewById(R.id.cmdCancel);
        mNewUser    = (CheckBox) findViewById(R.id.chkNewUser);
        mUserName   = (EditText) findViewById(R.id.txtUserName);
        mPassword   = (EditText) findViewById(R.id.txtPassword);

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloudActivity.this.onBackPressed();
            }
        });
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginToCloud mLoginAsych = new LoginToCloud();
                mLoginAsych.execute();
            }
        });
    }


    //Task per Login
    private class LoginToCloud extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog oWaitForPage;
        private boolean isLoginorSignIn=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            oWaitForPage = ProgressDialog.show(CloudActivity.this,getString(R.string.app_name),getString(R.string.wait),true,true,null);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(oWaitForPage!=null) oWaitForPage.dismiss();
            if(isLoginorSignIn) CloudActivity.this.onBackPressed();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            SynchHelper mCloud = new SynchHelper(CloudActivity.this);
            if(mNewUser.isChecked()){
                isLoginorSignIn=mCloud.userSignUp(mLogin.getText().toString(),
                        mPassword.getText().toString());
            }else{
                isLoginorSignIn=mCloud.userLogIn(mLogin.getText().toString(),
                        mPassword.getText().toString());
            }

            return isLoginorSignIn;
        }
    }
}*/
