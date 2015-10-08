/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.social.solution.unused;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.social.solution.others.MyAdapter;
import com.social.solution.R;
import com.social.solution.others.SlidingTabLayout;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.AccountService;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.List;


/**
 * A basic sample which shows how to use {com.example.android.common.view.SlidingTabLayout}
 * to display a custom {@link ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class SlidingTabsBasicFragment extends Fragment {

    static final String LOG_TAG = "SlidingTabsBasicFragment";

    public StatusesService statusesService;
    public AccountService accountService;
    public FavoriteService favoriteService;
    List<Tweet>       tweetlist;
    LinearLayout      linlaHeaderProgress;
    MyAdapter tweetadapter;
    Long              lasttweetid    = null;
    boolean           loading        = false;
    SharedPreferences prefs          = null;
    ProgressBar headerProgress       = null;
    //SQLiteDatabase myDB              = null;
    MySQLiteHelper sqlitehelper     = null;
    LinearLayout myLayout;
    ListView lv;

    View footer;

    View homeTimeLine               = null;
    View check1                     = null;
    View check2                     = null;

    TweetListView a;
    TweetListView b;
    TweetListView c;

    public TweetListView []viewHolder   = new TweetListView[3];
    public Parcelable []state           = new Parcelable[3];

    private SQLiteDatabase WriteAbleDB;
    private SQLiteDatabase ReadAbleDB;

    boolean []aBoolean = {false, false, false} ;

    public void LoadTweets() {
        statusesService.homeTimeline(20, null, lasttweetid, false, true, false, true,
                new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> result) {
                        List<Tweet> ls = result.data;
                        for (int i = 1; i < ls.size(); ++i) {
                            Tweet t = ls.get(i);
                            tweetlist.add(t);
                            sqlitehelper.insertTweet(WriteAbleDB, t);
                            lasttweetid = t.getId();

                        }
                        tweetadapter.setTweets(tweetlist);

                        tweetadapter.notifyDataSetChanged();
                        System.out.println("TWEETS LOADED " + lasttweetid);
                        loading = false;
                        System.out.println("SQLITE SIZE OF DB IS " + sqlitehelper.getSizeOfDB(ReadAbleDB));
                        //setProgressBarIndeterminateVisibility(false);
                        //lv.removeFooterView(footer);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        exception.printStackTrace();
                        System.out.println("EXCEPTION FAILED TWITTER");
                        LoadTweets();
                    }
                }
        );
    }


    public void loadTweetsOffline(){
        List<Tweet> tweetstemp = sqlitehelper.getTweetsFromDb(ReadAbleDB, 10);
        for(Tweet t:tweetstemp){
            tweetlist.add(t);
            System.out.println("pranjaltweet : "+t.idStr);
        }

        tweetadapter.setTweets(tweetlist);
        lv.setAdapter(tweetadapter);
        tweetadapter.notifyDataSetChanged();
        return;
    }

    void mytweets() {

        //System.out.println(currentSession.toString());
        //setRefreshActionButtonState(true);
        //setProgressBarIndeterminateVisibility(true);

        linlaHeaderProgress.setVisibility(View.VISIBLE);
        //headerProgress.setVisibility(View.VISIBLE);

        //lv.addFooterView(footer);

        statusesService.homeTimeline(10, null, lasttweetid, false, true, false, true,
                new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> result) {
                        List<Tweet> ls = result.data;
                        //Gson gson = new Gson();

                        for (int i = 1; i < ls.size(); ++i) {
                            Tweet t = ls.get(i);
                            sqlitehelper.insertTweet(WriteAbleDB, t);
                            tweetlist.add(t);
                            lasttweetid = t.getId();
                        }

                        System.out.println("SQLITE SIZE OF DB IS " + sqlitehelper.getSizeOfDB(ReadAbleDB));

                        //lasttweetid = ls.get(ls.size()-1).getId();
                        tweetadapter.setTweets(tweetlist);
                        linlaHeaderProgress.setVisibility(View.GONE);

                        lv.setAdapter(tweetadapter);
                        tweetadapter.notifyDataSetChanged();
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        exception.printStackTrace();
                        //headerProgress.setVisibility(View.INVISIBLE);
                        linlaHeaderProgress.setVisibility(View.GONE);
                        System.out.println("EXCEPTION FAILED TWITTER");

                        System.out.println("Pranjal loading tweets offline");
                        loadTweetsOffline();
                    }
                }
        );
    }

    /**
     * A {@link ViewPager} which will be used in conjunction with the {SlidingTabLayout} above.
     */
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample, container, false);
    }

    // BEGIN_INCLUDE (fragment_onviewcreated)
    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     *
     * We set the {@link ViewPager}'s adapter to be an instance of {@link SamplePagerAdapter}. The
     * {SlidingTabLayout} is then given the {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter());
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);

        //slidingTabs.setDistributeEvenly(true);

        mSlidingTabLayout.setViewPager(mViewPager);
        // END_INCLUDE (setup_slidingtablayout)
    }

    // END_INCLUDE (fragment_onviewcreated)

    /**
     * The {@link PagerAdapter} used to display pages in this sample.
     * The individual pages are simple and just display two lines of text. The important section of
     * this class is the {@link #getPageTitle(int)} method which controls what is displayed in the
     * {SlidingTabLayout}.
     */
    class SamplePagerAdapter extends PagerAdapter {

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 3;
        }

        /**
         * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
         * same object as the {@link View} added to the {@link ViewPager}.
         */
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        // BEGIN_INCLUDE (pageradapter_getpagetitle)
        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {SlidingTabLayout}.
         * <p>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return "Item " + (position + 1);
        }
        // END_INCLUDE (pageradapter_getpagetitle)

        /**
         * Instantiate the {@link View} which should be displayed at {@code position}. Here we
         * inflate a layout from the apps resources and then change the text view to signify the position.
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // Inflate a new layout from our resources
            //View view = getActivity().getLayoutInflater().inflate(R.layout.pager_item, container, false);

            View view = null;

            System.out.println("pranjal inside instantiate "+position);

            //footer    = (View)fa.getLayoutInflater().inflate(R.layout.listview_footer_row, null);

            if(position == 0){
                if(homeTimeLine == null){
                    System.out.println("pranjal instantiate item0 " + position);
                    a = new TweetListView(getActivity(), container);
                    if(a == null)
                        System.out.println("SLIDING TABS FRAGMENT a is "+null);
                    viewHolder[position]   = a;
                    homeTimeLine           = a.view;
                    view                   = homeTimeLine;
                }
                else {
                    viewHolder[position].mySetOnScrollListener();
                    view = homeTimeLine;
                    ((ListView)((view).findViewById(R.id.mylist))).onRestoreInstanceState(state[position]);
                }
            }
            else if(position == 1){
                if(check1 == null){
                    System.out.println("pranjal instantiate item1 " + position);
                    b = new TweetListView(getActivity(), container);
                    if(b == null)
                        System.out.println("SLIDING TABS FRAGMENT b is "+null);
                    viewHolder[position] = b;
                    check1               = b.view;
                    view                 = check1;
                }
                else {
                    viewHolder[position].mySetOnScrollListener();
                    view = check1;
                    ((ListView)((view).findViewById(R.id.mylist))).onRestoreInstanceState(state[position]);
                }
            }
            else if(position == 2){
                if(check2 == null){
                    System.out.println("pranjal instantiate item2 " + position);
                    c = new TweetListView(getActivity(), container);
                    if(c == null)
                        System.out.println("SLIDING TABS FRAGMENT c is "+null);
                    viewHolder[position] = c;
                    check2               = c.view;
                    view                 = check2;
                }
                else {
                    viewHolder[position].mySetOnScrollListener();
                    view = check2;
                    ((ListView)((view).findViewById(R.id.mylist))).onRestoreInstanceState(state[position]);
                }
            }

            System.out.println("adding to container "+position);
            container.addView(view);
            aBoolean[position] = true;
            return view;
        }

        /**
         * Destroy the item from the {@link ViewPager}. In our case this is simply removing the
         * {@link View}.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            System.out.println("pranjal inside destroy item "+position);
            state[position] = ((ListView)(((View)object).findViewById(R.id.mylist))).onSaveInstanceState();
            container.removeView((View) object);
        }

    }
}
