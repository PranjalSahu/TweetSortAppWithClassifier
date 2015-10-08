package com.social.solution.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.social.solution.unused.MyApplication;
import com.social.solution.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;


public class LoginActivity extends Activity {

    TwitterLoginButton loginButton;
    TwitterSession currentSession = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {

            @Override
            public void success(Result<TwitterSession> result) {
                currentSession = result.data;
                MyApplication appState = ((MyApplication)getApplicationContext());
                appState.currentSession = currentSession;

                Intent intent = new Intent(LoginActivity.this, ViewPagerTabListViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 1);
            }

            @Override
            public void failure(TwitterException exception) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);


        if(resultCode == 0) {
            setResult(0);
            finish();
        }

    }

}
