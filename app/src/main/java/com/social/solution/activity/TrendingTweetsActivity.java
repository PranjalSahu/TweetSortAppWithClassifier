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
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.social.solution.HelperFunctions;
import com.social.solution.others.MyAdapter;
import com.social.solution.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.List;


public class TrendingTweetsActivity extends BaseActivity {

    LinearLayout linlaHeaderProgress;
    ObservableListView listView;
    MyAdapter tweetadapter;
    List<Tweet> tweetlist;
    List<Tweet> tempTweetList;
    private View mHeaderView;
    String query;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode){
            case 0:
                setResult(0);
                finish();
        }
    }

    void setmydata(ListView listView, View headerView){
        listView.addHeaderView(headerView);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.trending_tweets);

        query               = getIntent().getStringExtra("query");
        System.out.println("Query is " + query);
        listView            = (ObservableListView) findViewById(R.id.mylist);

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setBackgroundColor(-1);
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(query);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tweetadapter    = new MyAdapter(this);
        listView        = (ObservableListView) findViewById(R.id.mylist);

        listView.setAdapter(tweetadapter);

        //setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mHeaderView = findViewById(R.id.header);
        ViewCompat.setElevation(mHeaderView, getResources().getDimension(R.dimen.toolbar_elevation));

        tweetlist = new ArrayList<Tweet>();
        LoadTrendingTweets();
    }

    public void displayTweets(){
        tweetadapter.setTweets(tweetlist);
        tweetadapter.notifyDataSetChanged();
        linlaHeaderProgress.setVisibility(View.GONE);
    }

    public void LoadTrendingTweets() {
        HelperFunctions.searchService.tweets(query, null, null, null, "mixed", 80, null, null, null, true,
                new Callback<Search>() {
                    @Override
                    public void success(Result<Search> result) {
                        List<Tweet> ls = result.data.tweets;
                        System.out.println("tweetsearch size = "+ls.size());
                        if (ls.size() > 0) {
                            tweetlist.addAll(ls);
                            for(Tweet t:ls){
                                System.out.println("tweetsearch count checking :" + t.retweetCount+ " "+t.favoriteCount);
                            }
                        }
                        displayTweets();
                    }

                    @Override
                    public void failure(TwitterException e) {

                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.mymenutrend, menu);
        System.out.println("pranjal menu has visible items " + menu.hasVisibleItems());
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.animation_entry_right, R.anim.animation_exit_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.originaltimeline){
            HelperFunctions.animate = false;
            tweetadapter.setTweets(tweetlist);
            tweetadapter.notifyDataSetChanged();
            listView.smoothScrollToPosition(0);
            HelperFunctions.animate = true;
            Toast.makeText(this, "Sorted By Time", Toast.LENGTH_LONG).show();
            return true;
        }
        else if(id == R.id.sortitemsbyfavorites){
            HelperFunctions.animate = false;
            MyAdapter mya           = tweetadapter;
            tempTweetList           = new ArrayList<>(tweetlist);
            HelperFunctions.sortTweets(2, tempTweetList, mya, listView);
            tweetadapter.setTweets(tempTweetList);
            tweetadapter.notifyDataSetChanged();
            listView.smoothScrollToPosition(0);
            Toast.makeText(this, "Sorted By Favorite Count", Toast.LENGTH_LONG).show();
            HelperFunctions.animate = true;
            return true;
        }
        else if(id == R.id.sortitemsbytweet){
            HelperFunctions.animate = false;
            MyAdapter mya           = tweetadapter;
            tempTweetList        = new ArrayList<Tweet>(tweetlist);
            HelperFunctions.sortTweets(1, tempTweetList, mya, listView);
            tweetadapter.setTweets(tempTweetList);
            tweetadapter.notifyDataSetChanged();
            listView.smoothScrollToPosition(0);
            Toast.makeText(this, "Sorted By Retweet Count", Toast.LENGTH_LONG).show();
            HelperFunctions.animate = true;
            return true;
        }
        else if(id == android.R.id.home) {
            System.out.println("pranjalsahu TrendingTweetsActivity");
            this.finish();
            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
