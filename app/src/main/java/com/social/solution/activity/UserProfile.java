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

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.AbsListView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.mopub.volley.RequestQueue;
import com.mopub.volley.toolbox.ImageLoader;
import com.mopub.volley.toolbox.Volley;
import com.nineoldandroids.view.ViewHelper;
import com.social.solution.HelperFunctions;
import com.social.solution.R;
import com.social.solution.others.SquareImageView;

import twitter4j.TwitterException;

public class UserProfile extends BaseActivity implements ObservableScrollViewCallbacks {

    private View myTextView;
    private View mToolbarView;
    private View mListBackgroundView;
    private ObservableListView mListView;
    private int mParallaxImageHeight;
    String profileImageUrl = null;
    SquareImageView picture;
    protected RequestQueue mRequestQueue;
    protected ImageLoader imageLoader;
    Activity activityReference = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parallaxtoolbarlistviewa) ;

        //setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        activityReference = this;

        mRequestQueue = Volley.newRequestQueue(this);
        imageLoader  = new com.mopub.volley.toolbox.ImageLoader(mRequestQueue, new com.mopub.volley.toolbox.ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });


        picture    = (SquareImageView)findViewById(R.id.userimage);
        new LoadProfileImage().execute("0", "1");

        myTextView = findViewById(R.id.myflexibleview);
//        mToolbarView = findViewById(R.id.toolbar);
//        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(0, getResources().getColor(R.color.primary)));

        mParallaxImageHeight = getResources().getDimensionPixelSize(R.dimen.parallax_image_height);

        mListView = (ObservableListView) findViewById(R.id.list);
        mListView.setScrollViewCallbacks(this);
        // Set padding view for ListView. This is the flexible space.
        View paddingView = new View(this);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                mParallaxImageHeight);
        paddingView.setLayoutParams(lp);

        // This is required to disable header's list selector effect
        paddingView.setClickable(true);

        mListView.addHeaderView(paddingView);
        setDummyData(mListView);

        // mListBackgroundView makes ListView's background except header view.
        mListBackgroundView = findViewById(R.id.list_background);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onScrollChanged(mListView.getCurrentScrollY(), false, false);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        int baseColor = getResources().getColor(R.color.primary);
        float alpha = Math.min(1, (float) scrollY / mParallaxImageHeight);
        //mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, baseColor));
        ViewHelper.setTranslationY(myTextView, -scrollY / 2);

        // Translate list background
        ViewHelper.setTranslationY(mListBackgroundView, Math.max(0, -scrollY + mParallaxImageHeight));
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    public class LoadProfileImage extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String username = HelperFunctions.currentSession.getUserName();
                profileImageUrl = HelperFunctions.twitter.showUser(username).getOriginalProfileImageURL();
                //HelperFunctions.twitter.showUser(username).getBiggerProfileImageURL();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            picture.setImageUrl(profileImageUrl, imageLoader);
        }
    }
}
