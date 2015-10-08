package com.social.solution.unused;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.social.solution.R;
import com.social.solution.others.MyAdapter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.ArrayList;
import java.util.List;


public class ShowUserProfile extends Activity {
    LinearLayout      linlaHeaderProgress;
    LinearLayout      tweetLayout;
    MyAdapter tweetadapter;
    Long              lasttweetid    = null;
    List<Tweet>         tweetlist;

    ListView lv;
    StatusesService statusesService;
    FavoriteService favoriteService;


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tweetlist        = new ArrayList<Tweet>();

        statusesService = ((MyApplication)getApplicationContext()).statusesService;
        favoriteService = ((MyApplication)getApplicationContext()).favoriteService;

        tweetadapter     = new MyAdapter(this);

        setContentView(R.layout.tweet_list);
        lv = (ListView)findViewById(R.id.mylist);
        //tweetadapter     = new MyAdapter(this, statusesService, favoriteService);

        //headerProgress   = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setBackgroundColor(-1);

        Intent intent = getIntent();
        //int tweetId = intent.getIntExtra("userid", 1);
        //System.out.println("pranjalLONG ID is " + tweetId);

        linlaHeaderProgress.setVisibility(View.VISIBLE);

        statusesService.userTimeline(null, "@ladygaga", 10, null, null, false, true, false, false,
                new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> result) {
                        List<Tweet> ls = result.data;

                        System.out.println("pranjalsahu tweetsize is "+ls.size());
                        for (int i = 0; i < ls.size(); ++i) {
                            Tweet t = ls.get(i);
                            System.out.println("Tweet is ladygaga " + t.id);
                            tweetlist.add(t);
                            //lasttweetid = t.getId();
                        }

                        tweetadapter.setTweets(tweetlist);
                        linlaHeaderProgress.setVisibility(View.GONE);

                        lv.setAdapter(tweetadapter);
                        tweetadapter.notifyDataSetChanged();
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        exception.printStackTrace();
                        linlaHeaderProgress.setVisibility(View.GONE);
                        System.out.println("EXCEPTION FAILED TWITTER");
                        System.out.println("Pranjal loading tweets offline");
                    }
                }
        );

    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // your code
            return true;
        }
        System.out.println("pranjalbackispressed");
        return super.onKeyDown(keyCode, event);
    }*/
}
