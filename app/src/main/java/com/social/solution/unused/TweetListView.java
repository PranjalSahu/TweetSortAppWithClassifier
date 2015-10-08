package com.social.solution.unused;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mopub.nativeads.MoPubAdAdapter;
import com.mopub.nativeads.MoPubNativeAdPositioning;
import com.mopub.nativeads.MoPubNativeAdRenderer;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.RequestParameters.NativeAdAsset;
import com.mopub.nativeads.ViewBinder;
import com.social.solution.R;
import com.social.solution.others.MyAdapter;
import com.social.solution.others.MyTwitterApiClient;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.AccountService;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pranjal on 29/04/15.
 */

public class TweetListView {
    View          view;
    ProgressBar   headerProgress   = null;
    LinearLayout  linlaHeaderProgress;
    ListView      lv;

    MoPubAdAdapter mAdAdapter;
    MyAdapter tweetadapter;

    private static final String MY_AD_UNIT_ID = "d05480af91a04d7c841c5f9bb7621032";
    //private static final String MY_AD_UNIT_ID = "76a3fefaced247959582d2d2df6f4757";


    Long lasttweetid         = null;

    String username          = null;
    twitter4j.Twitter        twitter1;
    twitter4j.TwitterFactory twitterFactory;
    MyTwitterApiClient twitterApiClient;
    StatusesService statusesService;
    AccountService accountService;
    FavoriteService favoriteService;
    TwitterAuthConfig authConfig     = null;
    TwitterSession currentSession = null;
    boolean                  loading        = false;
    View footer;

    private RequestParameters mRequestParameters;
    List<Tweet> tweetlist;

    AbsListView.OnScrollListener listenerObject = null;

    TweetListView(FragmentActivity fa, ViewGroup container){
        view                = fa.getLayoutInflater().inflate(R.layout.tweet_list, container, false);
        headerProgress      = (ProgressBar)  view.findViewById(R.id.pbHeaderProgress);
        linlaHeaderProgress = (LinearLayout) view.findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setBackgroundColor(-1);
        linlaHeaderProgress.setVisibility(View.VISIBLE);


        lv        = (ListView)view.findViewById(R.id.mylist);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        footer    = (View)fa.getLayoutInflater().inflate(R.layout.listview_footer_row, null);

        MyApplication appState = ((MyApplication)fa.getApplicationContext());

        accountService   = appState.accountService;
        favoriteService  = appState.favoriteService;
        statusesService  = appState.statusesService;
        twitterApiClient = appState.twitterApiClient;
        authConfig       = appState.authConfig;
        currentSession   = appState.currentSession;
        username         = appState.username;

        tweetadapter     = new MyAdapter(fa.getBaseContext());
        tweetlist        = new ArrayList<Tweet>();

        // Set up a ViewBinder and MoPubNativeAdRenderer as above.
//        ViewBinder viewBinder = new ViewBinder.Builder(R.layout.native_ad_layout)
//                .mainImageId(R.id.native_ad_main_image)
//                .iconImageId(R.id.native_ad_icon_image)
//                .titleId(R.id.native_ad_title)
//                .textId(R.id.native_ad_text)
//                //.addExtra("sponsoredText", R.id.sponsored_text)
//                //.addExtra("sponsoredImage", R.id.sponsored_image)
//                .build();

                //ViewBinder viewBinder = new ViewBinder.Builder(com.twitter.sdk.android.tweetui.R.layout.tw__tweet_compact)
        ViewBinder viewBinder = new ViewBinder.Builder(com.twitter.sdk.android.tweetui.R.layout.tw__tweet_compact)
                    .mainImageId(R.id.tw__tweet_media)
                    .iconImageId(R.id.tw__tweet_author_avatar)
                    .titleId(R.id.tw__tweet_author_screen_name)
                    .textId(R.id.tw__tweet_text)
                    //.addExtra("sponsoredText", R.id.sponsored_text)
                    //.addExtra("sponsoredImage", R.id.sponsored_image)
                    .build();


        // Set up the positioning behavior your ads should have.
        MoPubNativeAdPositioning.MoPubServerPositioning adPositioning =
                MoPubNativeAdPositioning.serverPositioning();
        MoPubNativeAdRenderer adRenderer = new MoPubNativeAdRenderer(viewBinder);

        final EnumSet<NativeAdAsset> desiredAssets = EnumSet.of(
                NativeAdAsset.TITLE,
                NativeAdAsset.TEXT,
                NativeAdAsset.ICON_IMAGE,
                NativeAdAsset.MAIN_IMAGE,
                NativeAdAsset.CALL_TO_ACTION_TEXT);

        mRequestParameters = new RequestParameters.Builder()
                //.location(location)
                .keywords("food")
                .desiredAssets(desiredAssets)
                .build();

//        myRequestParameters = RequestParameters.Builder()
//                .keyWords("news")
//                .build();

        //mAdAdapter.loadAds(MY_AD_UNIT_ID, myRequestParameters)

        // Set up the MoPubAdAdapter
        mAdAdapter = new MoPubAdAdapter(fa, tweetadapter, adPositioning);
        mAdAdapter.registerAdRenderer(adRenderer);
        //mAdAdapter.loadAds(MY_AD_UNIT_ID);

        LoadTweets();
    }

    public View getView(){
        return view;
    }



    public void mySetOnScrollListener(){

        if(listenerObject == null) {
            listenerObject = new AbsListView.OnScrollListener() {
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {

                    System.out.println("pranjal tweet footer scroll");
                    int visibleThreshold = 0;
                    if (loading == false && totalItemCount > 5 && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                        mytweets();
                        //loading = true;
                        //lv.addFooterView(footer);
                        //System.out.println("Footer View Added");
                    }
                }
            };
        }

        lv.setOnScrollListener(listenerObject);
        //com.twitter.sdk.android.tweetui.CompactTweetView.
    }

    void mytweets() {

        linlaHeaderProgress.setVisibility(View.VISIBLE);

        statusesService.homeTimeline(10, null, lasttweetid, false, true, false, true,
                new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> result) {
                        List<Tweet> ls = result.data;

                        for (int i = 1; i < ls.size(); ++i) {
                            Tweet t = ls.get(i);
                            tweetlist.add(t);
                            lasttweetid = t.getId();
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
                    }
                }
        );
    }

    public void LoadTweets() {
        statusesService.homeTimeline(20, null, lasttweetid, false, true, false, true,
                new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> result) {
                        List<Tweet> ls = result.data;
                        for (int i=1;i<ls.size();++i) {
                            Tweet t = ls.get(i);
                            tweetlist.add(t);
                            lasttweetid = t.getId();
                        }
                        tweetadapter.setTweets(tweetlist);

                        tweetadapter.notifyDataSetChanged();
                        mAdAdapter.loadAds(MY_AD_UNIT_ID, mRequestParameters);

                        lv.setAdapter(mAdAdapter);
                        //lv.setAdapter(tweetadapter);

                        linlaHeaderProgress.setVisibility(View.GONE);

                        System.out.println("TWEETS LOADED " + lasttweetid);
                        loading = false;
                        //setProgressBarIndeterminateVisibility(false);
                        //lv.removeFooterView(footer);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        exception.printStackTrace();
                        System.out.println("EXCEPTION FAILED TWITTER");
                        //LoadTweets();
                    }
                }
        );
    }

}