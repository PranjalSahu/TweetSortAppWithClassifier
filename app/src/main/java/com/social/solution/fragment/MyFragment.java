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

package com.social.solution.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.mopub.nativeads.MoPubAdAdapter;
import com.mopub.nativeads.MoPubNativeAdPositioning;
import com.mopub.nativeads.MoPubNativeAdRenderer;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.ViewBinder;
import com.social.solution.HelperFunctions;
import com.social.solution.others.MyAdapter;
import com.social.solution.unused.MyApplication;
import com.social.solution.unused.MySQLiteHelper;
import com.social.solution.R;
import com.social.solution.others.TweetBank;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.AccountService;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.TweetUi;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class MyFragment extends BaseFragment {

    public static final String ARG_INITIAL_POSITION = "ARG_INITIAL_POSITION";

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY    = "i8lsarVzM1RLdQli7JvGibJya";
    private static final String TWITTER_SECRET = "ivA141Pewjx3VYfKOUBMIRJZZnNhPQNW9gVdM1nlXrnsNmir29";

    MyApplication appState;

    TwitterSession ts = null;

    Long lastDisplayTweetId  = Long.MAX_VALUE;
    Long firstDisplayTweetId = Long.MIN_VALUE;

    public List<Tweet> loadingTweets;
    public List<Tweet> tweetlist;
    public List<Tweet> tempTweetList;      // for storing in sorted order

    public twitter4j.Twitter twitter1;
    public TwitterFactory      twitterFactory;

    LinearLayout linlaHeaderProgress;
    public MyAdapter tweetadapter;
    MoPubAdAdapter mAdAdapter;

    long lastTimeStamp;
    int currentState = 0;

    Long              firsttweetid   = null;
    Long              lasttweetid    = null;
    boolean           loading        = false;
    boolean           downloading = false;
    SharedPreferences prefs          = null;
    ProgressBar headerProgress       = null;
    LinearLayout myLayout;

    MySQLiteHelper sqlitehelper      = null;
    private SQLiteDatabase WriteAbleDB;
    private SQLiteDatabase ReadAbleDB;

    String custkey      = "FacGCa1kekg6t68N9n1r46GAI";
    String custsecret   = "aQSljFzqIKuVu4H4sr9OQhvtEVW4sn1qRMHtJezZMiMKeOFlWo";
    String accesstoken  = "163158983-PcgEMJBfxFQBSK2JHcnKYfZhGTyPio6jt23z3FBh";
    String accesssecret = "BIf9DohxN21Y3jF1m3LP3JAgR2gA673Ywwe20QjVFyCnZ";

    public ObservableListView listView;
    Context baseContext;

    SwipeRefreshLayout mSwipeLayout;
    Activity storedActivity;

    private RequestParameters mRequestParameters;
    private static final String MY_AD_UNIT_ID = "d05480af91a04d7c841c5f9bb7621032";

    boolean filterTweets;
    View footer;

    AbsListView.OnScrollListener listenerObject = null;

    int position= 0;

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) storedActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ViewBinder viewBinder = new ViewBinder.Builder(R.layout.mynativead)
                .mainImageId(R.id.tw__full_ad_image)
                .iconImageId(R.id.tw__tweet_author_avatar_pran)
                .titleId(R.id.tw__tweet_author_full_name_pran)
                .textId(R.id.tw__tweet_text_pran)
                        //.addExtra("sponsoredText", R.id.sponsored_text)
                        //.addExtra("sponsoredImage", R.id.sponsored_image)
                .build();


        MoPubNativeAdPositioning.MoPubServerPositioning adPositioning =
                MoPubNativeAdPositioning.serverPositioning();
        MoPubNativeAdRenderer adRenderer = new MoPubNativeAdRenderer(viewBinder);

        footer          = (View)activity.getLayoutInflater().inflate(R.layout.listview_footer_row, null);

        tweetadapter    = new MyAdapter(activity);
        mAdAdapter      = new MoPubAdAdapter(activity, tweetadapter, adPositioning);
        mAdAdapter.registerAdRenderer(adRenderer);

        storedActivity = activity;

        LoadFirst();
    }

    public class LoadStatuses extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            System.out.println("inside loadrecent status");
            try {
                List<twitter4j.Status> statuses = null;
                String user;
                try {
                    statuses = HelperFunctions.twitter.getUserTimeline("ladygaga"/*HelperFunctions.currentSession.getUserName()*/);
                    //twitter4j.Status.READ.
                } catch (twitter4j.TwitterException e) {
                    e.printStackTrace();
                }
                //System.out.println("Showing @" + user + "'s user timeline.");
                for (twitter4j.Status status : statuses) {
                    //status.
                    System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText()+" vanitweet "+status.toString());
                }
            } catch (TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to get timeline: " + te.getMessage());
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            mSwipeLayout.setRefreshing(false);
        }
    }


    public void setAppState( Context baseContext, StatusesService statusesService,
            AccountService accountService,
            FavoriteService favoriteService) {

        this.baseContext      = baseContext;

        if(baseContext == null)
            System.out.println("PRANJALITISNULLBASEa");

    }

    void setmydata(ListView listView, View headerView){
        listView.addHeaderView(headerView);
    }

    protected void setDummyDataWithHeader(ListView listView, View headerView) {
        listView.addHeaderView(headerView);
        setDummyData(listView); // testing git
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tweet_list, container, false);

        lastTimeStamp = System.currentTimeMillis();

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeLayout.setProgressViewOffset(false, 150, 200);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                long currentTimeStamp = System.currentTimeMillis();
                if((currentTimeStamp - lastTimeStamp)/1000 >10) {
                    System.out.println("load recent pranjal");
                    LoadRecentTweets();
                }

            }
        });

        Activity parentActivity = getActivity();
        Fabric.with(getActivity(), new TweetUi());

        Bundle bd = getArguments();
        if(bd != null){
            filterTweets = bd.getBoolean("filter");
            position     = bd.getInt("position");
        }
        else{
            filterTweets = false;
            position     = 0;
        }

        //Toast.makeText(this, "Filter : "+filterTweets+" Position : "+position, Toast.LENGTH_SHORT).show();
        System.out.println("Filter : " + filterTweets + " Position : " + position);

        linlaHeaderProgress = (LinearLayout) view.findViewById(R.id.linlaHeaderProgress);
        listView            = (ObservableListView) view.findViewById(R.id.mylist);


        final EnumSet<RequestParameters.NativeAdAsset> desiredAssets = EnumSet.of(
                RequestParameters.NativeAdAsset.TITLE,
                RequestParameters.NativeAdAsset.TEXT,
                RequestParameters.NativeAdAsset.ICON_IMAGE,
                RequestParameters.NativeAdAsset.MAIN_IMAGE,
                RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT);

        mRequestParameters = new RequestParameters.Builder()
                //.location(location)
                .keywords("food")
                .desiredAssets(desiredAssets)
                .build();


        setmydata(listView, inflater.inflate(R.layout.padding, listView, false));

        listView.setAdapter(tweetadapter);

        linlaHeaderProgress.setBackgroundColor(-1);
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            // Scroll to the specified position after layout
            Bundle args = getArguments();
            if (args != null && args.containsKey(ARG_INITIAL_POSITION)) {
                final int initialPosition = args.getInt(ARG_INITIAL_POSITION, 0);
                ScrollUtils.addOnGlobalLayoutListener(listView, new Runnable() {
                    @Override
                    public void run() {
                        // scrollTo() doesn't work, should use setSelection()
                        listView.setSelection(initialPosition);
                    }
                });
            }

            // TouchInterceptionViewGroup should be a parent view other than ViewPager.
            // This is a workaround for the issue #117:
            // https://github.com/ksoichiro/Android-ObservableScrollView/issues/117
            listView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.root));

            listView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

        ConfigurationBuilder config =
                new ConfigurationBuilder()
                        .setOAuthConsumerKey(custkey)
                        .setOAuthConsumerSecret(custsecret)
                        .setOAuthAccessToken(accesstoken)
                        .setOAuthAccessTokenSecret(accesssecret);

        twitter1         = new TwitterFactory(config.build()).getInstance();
        tweetlist        = new ArrayList<Tweet>();
        loadingTweets    = new ArrayList<Tweet>();

        lastDisplayTweetId  = Long.MAX_VALUE;
        firstDisplayTweetId = Long.MIN_VALUE;

        return view;
    }

    void LoadFirst(){
        TweetBank.lasttweetid  = null;
        TweetBank.firsttweetid = null;

        lasttweetid  = TweetBank.lasttweetid;
        firsttweetid = TweetBank.firsttweetid;

        tweetlist    = new ArrayList<Tweet>();
        LoadOldTweetsFirst();
        return;
    }

    boolean checkToLoad(){
        int sizeOfDb = TweetBank.sqlitehelper.getSizeOfDB(TweetBank.WriteAbleDB);
        System.out.println("SIZE DIFFERENCE IS " + sizeOfDb + " " + tweetlist.size());
        if(sizeOfDb - tweetlist.size() < 50)
            return true;
        else
            return false;
    }

    void refreshAdapter(){
        tweetadapter.notifyDataSetChanged();
        tempTweetList = new ArrayList<Tweet>(tweetlist);
        mAdAdapter.loadAds(MY_AD_UNIT_ID, mRequestParameters);
        footer.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 0));
        loadingTweets.clear();
        System.out.println("Size of tweelist is " + tweetlist.size());
        linlaHeaderProgress.setVisibility(View.GONE);//linlaHeaderProgress.getvisi
        loading = false;
    }


    public void mySetOnScrollListener(final Activity activity){

        if(listenerObject == null) {
            listenerObject = new AbsListView.OnScrollListener() {
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {

                    int visibleThreshold = 2;
                    long currentTimeStamp = System.currentTimeMillis();
                    //System.out.println("firstVisibleItem "+firstVisibleItem+" visibleItemCount "+visibleItemCount+" totalItemCount "+totalItemCount+" (totalItemCount - visibleItemCount) "+(totalItemCount - visibleItemCount)+" (firstVisibleItem + visibleThreshold) "+(firstVisibleItem + visibleThreshold));
                    if ((currentTimeStamp - lastTimeStamp)/1000 >10 && loading == false && totalItemCount > 5 && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                        LoadOldTweets();
                        //footer.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 70));
                    }
                }
            };
        }

        listView.setOnScrollListener(listenerObject);
    }

    void LoadRecentTweets(){

        System.out.println("inside loadrecent status A");

        //new LoadStatuses().execute("0", "1");
        lastTimeStamp = System.currentTimeMillis();
        displayTweetsRecent();
        downloading = false;

        /*
        HelperFunctions.statusesService.homeTimeline(50, TweetBank.firsttweetid, null, false, true, false, true,
                new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> result) {
                        List<Tweet> ls = result.data;
                        if (ls.size() > 0) {
                            for (int i = 0; i < ls.size(); ++i) {
                                Tweet t = ls.get(i);
                                TweetBank.insertTweet(t);
                            }
                        }
                        lastTimeStamp = System.currentTimeMillis();

                        displayTweetsRecent();
                        downloading = false;
                        mSwipeLayout.setRefreshing(false);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        exception.printStackTrace();
                        lastTimeStamp = System.currentTimeMillis();

                        displayTweetsRecent();
                    }
                }
        );*/
    }

    public void displayTweetsRecent(){
        List<Tweet> filteredTweets = getFilteredRecent();
        tweetlist.addAll(0, filteredTweets);
        tempTweetList.addAll(0, filteredTweets);
        //tweetadapter.setTweets(tweetlist);
        tweetadapter.notifyDataSetChanged();
        mSwipeLayout.setRefreshing(false);
    }

    public List<Tweet> getFilteredRecent(){
        List<Tweet> temp       = new ArrayList<Tweet>();
        List<Tweet> filterTemp = new ArrayList<Tweet>();

        firstDisplayTweetId = Long.MIN_VALUE;

        for(Tweet t:tweetlist){
            if(firstDisplayTweetId < t.id)
                firstDisplayTweetId = t.id;
        }
        temp   =   TweetBank.getNewThan(firstDisplayTweetId);

        for(Tweet t: temp) {
            //if ((!filterTweets || HelperFunctions.genericFilterFunction(t, position)))
            if ((HelperFunctions.genericFilterFunction(t, position)))
                    filterTemp.add(t);
        }
        return filterTemp;
    }


    public List<Tweet> getFiltered(){
        List<Tweet> temp       = new ArrayList<Tweet>();
        List<Tweet> filterTemp = new ArrayList<Tweet>();

        lastDisplayTweetId  = Long.MAX_VALUE;

        for(Tweet t:tweetlist) {
            if (lastDisplayTweetId > t.id)
                lastDisplayTweetId = t.id;
        }
        temp   =   TweetBank.getOlderThan(lastDisplayTweetId);

        for(Tweet t: temp) {
            //if ((!filterTweets || HelperFunctions.genericFilterFunction(t, position)))
            if ((HelperFunctions.genericFilterFunction(t, position)))
                filterTemp.add(t);
        }

        HelperFunctions.sortTweets(3,  filterTemp, null);
        return filterTemp;
    }

    public void displayTweets(){
        List<Tweet> filteredTweets = getFiltered();
        tweetlist.addAll(filteredTweets);
        tempTweetList.addAll(filteredTweets);
        tweetadapter.notifyDataSetChanged();
        mySetOnScrollListener(storedActivity);
    }
    public void displayTweetsFirst(){
        tweetlist.addAll(getFiltered());

        tweetadapter.setTweets(tweetlist);
        tempTweetList = new ArrayList<Tweet>(tweetlist);

        tweetadapter.notifyDataSetChanged();
        mAdAdapter.loadAds(MY_AD_UNIT_ID, mRequestParameters);

        mySetOnScrollListener(storedActivity);

        listView.addFooterView(footer);
        listView.removeFooterView(footer);

        listView.setAdapter(mAdAdapter);
        linlaHeaderProgress.setVisibility(View.GONE);
    }

    //#FFAC33 golden color
    //#77B255 green button

    public void LoadOldTweetsFirst() {
        downloading = true;
        loading     = true;

        Handler handlerTimer = new Handler();
        handlerTimer.postDelayed(new Runnable() {
            public void run() {
                HelperFunctions.statusesService.homeTimeline(150, null, TweetBank.lasttweetid, false, true, false, true,
                        new Callback<List<Tweet>>() {
                            @Override
                            public void success(Result<List<Tweet>> result) {
                                List<Tweet> ls = result.data;
                                if (ls.size() > 0) {
                                    for (int i = 0; i < ls.size(); ++i) {
                                        Tweet t = ls.get(i);
                                        TweetBank.insertTweet(t);
                                    }
                                }

                                lastTimeStamp = System.currentTimeMillis();

                                //lastDisplayTweetId  = TweetBank.lasttweetid;
                                //firstDisplayTweetId = TweetBank.firsttweetid;

                                displayTweetsFirst();
                                downloading = false;
                                loading = false;
                            }

                            @Override
                            public void failure(TwitterException exception) {
                                exception.printStackTrace();
                                System.out.println("EXCEPTION FAILED TWITTER");
                                lastTimeStamp = System.currentTimeMillis();
                                displayTweetsFirst();
                                // TODO make this toast when the internet connection is not present
                                //Toast.makeText(storedActivity, "Check Network connectivity", Toast.LENGTH_LONG).show();
                                linlaHeaderProgress.setVisibility(View.GONE);
                                listView.removeFooterView(footer);
                                loading = false;
                                downloading = false;
                            }
                        }
                );
            }
        }, 2000);



    }

    public void LoadOldTweets() {
        downloading = true;
        loading     = true;
        listView.addFooterView(footer);

        System.out.println("LOADING LOADING LOADING LOADING");

        HelperFunctions.statusesService.homeTimeline(150, null, TweetBank.lasttweetid, false, true, false, true,
                new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> result) {
                        List<Tweet> ls = result.data;
                        if (ls.size() > 0) {
                            for (int i = 0; i < ls.size(); ++i) {
                                Tweet t = ls.get(i);
                                TweetBank.insertTweet(t);
                            }
                        }
                        lastTimeStamp = System.currentTimeMillis();
                        displayTweets();
                        listView.removeFooterView(footer);
                        loading = false;
                        downloading = false;
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        exception.printStackTrace();
                        System.out.println("EXCEPTION FAILED TWITTER");
                        lastTimeStamp = System.currentTimeMillis();
                        displayTweets();
                        listView.removeFooterView(footer);
                        loading = false;
                        downloading = false;
                    }

                }
        );
    }
}
