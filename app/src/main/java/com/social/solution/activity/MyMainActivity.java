/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.social.solution.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.social.solution.others.MyTwitterApiClient;
import com.social.solution.unused.MyApplication;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.tweetui.TweetUi;

import io.fabric.sdk.android.Fabric;

//import com.crashlytics.android.Crashlytics;


public class MyMainActivity extends AppCompatActivity {

    MyTwitterApiClient twitterApiClient;
    String username                  = null;

    TwitterAuthConfig   authConfig     = null;
    TwitterSession      currentSession = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode){
            case 0:
                setResult(0);
                finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("HELLO PRANJAL");
        String TWITTER_KEY = "i8lsarVzM1RLdQli7JvGibJya";
        String TWITTER_SECRET = "ivA141Pewjx3VYfKOUBMIRJZZnNhPQNW9gVdM1nlXrnsNmir29";

        authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        Fabric.with(this, new TweetUi());
        //Fabric.with(this, new TweetComposer(), new Crashlytics());

        MyApplication appState = ((MyApplication) getApplicationContext());
        currentSession = Twitter.getSessionManager().getActiveSession();

//        // Enable Local Datastore.
//        Parse.enableLocalDatastore(this);
//        Parse.initialize(this, "RO9EX10OE04h3ZEnFzuiddMpRgADGjt5K40eLfWH", "SHhX4ewjms9uTrYPb67M3kcJYvMMYH9aK6aXur24");
//
//
//
//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("foo", "bar");
//        testObject.saveInBackground();


        System.out.println("zooweemama");
        if (currentSession == null) {
            System.out.println("NULL POINTER EXCEPTION");
            Intent intent = new Intent(MyMainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, 1);
            //appState.startActivity(intent);
        } else {
            System.out.println("Pranjal testing");
            Intent intent = new Intent(MyMainActivity.this, ViewPagerTabListViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, 1);
            //appState.startActivity(intent);
        }

    }
}