package com.social.solution.unused;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.social.solution.R;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.LoadCallback;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;


public class ShowTweet extends Activity {
    LinearLayout linlaHeaderProgress;
    LinearLayout tweetLayout;
    //ListView tweetLayout;

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

        Intent intent = getIntent();
        Long tweetId = intent.getLongExtra("tweetid", 1);

        setContentView(R.layout.my_tweet_layout);

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress1);
        tweetLayout         = (LinearLayout) findViewById(R.id.tweetlinearlayout);

        linlaHeaderProgress.setBackgroundColor(-1);

        //linlaHeaderProgress.setVisibility(View.VISIBLE);

        //final FrameLayout lv1 = new FrameLayout(this, null);
        //final LinearLayout lv1 = new LinearLayout(this, null);

        //FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        //                                                            FrameLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                    LinearLayout.LayoutParams.WRAP_CONTENT);

        //lv1.setLayoutParams(lp);
        //setContentView(R.layout.my_tweet_layout);

        //setContentView(lv1);

        linlaHeaderProgress.setVisibility(View.VISIBLE);
        TweetUtils.loadTweet(tweetId, new LoadCallback<Tweet>() {
            @Override
            public void success(Tweet tweet) {
                System.out.println("Tweet Loaded");
                tweetLayout.addView(new TweetView(ShowTweet.this, tweet), 0);
                linlaHeaderProgress.setVisibility(View.GONE);
                //linlaHeaderProgress.setVisibility(View.GONE);
                //tv.setTweet(tweet);
            }

            @Override
            public void failure(TwitterException exception) {
                //Toast.makeText(...).show();
            }
        });
    }
}
