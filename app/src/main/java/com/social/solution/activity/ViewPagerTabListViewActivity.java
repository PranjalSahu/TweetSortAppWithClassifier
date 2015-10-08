package com.social.solution.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.melnykov.fab.FloatingActionButton;
import com.mopub.volley.RequestQueue;
import com.mopub.volley.toolbox.ImageLoader;
import com.mopub.volley.toolbox.Volley;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.social.solution.HelperFunctions;
import com.social.solution.R;
import com.social.solution.fragment.MyFragment;
import com.social.solution.fragment.MyImageFragment;
import com.social.solution.fragment.TrendingFragment;
import com.social.solution.others.Keys;
import com.social.solution.others.MyAdapter;
import com.social.solution.others.SlidingTabLayout;
import com.social.solution.others.SquareImageView;
import com.social.solution.others.TweetBank;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetui.TweetUi;

import java.util.ArrayList;
import java.util.Arrays;

import io.fabric.sdk.android.Fabric;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * SlidingTabLayout and SlidingTabStrip are from google/iosched:
 * https://github.com/google/iosched
 */
public class ViewPagerTabListViewActivity extends BaseActivity implements ObservableScrollViewCallbacks, OnShowcaseEventListener {

    boolean showIntro = true;

    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {
        ++currentShowCase;

        if(currentShowCase == 1) {
            ViewTarget target = new ViewTarget(R.id.sortitemsbytweet, this);
            sv  = new ShowcaseView.Builder(this, true)
                    .setTarget(target)
                    .setContentTitle("SORT TWEETS BY RETWEET COUNT")
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setShowcaseEventListener(this)
                    .build();
        }
        else if(currentShowCase == 2) {
            ViewTarget target = new ViewTarget(R.id.originaltimeline, this);
            sv  = new ShowcaseView.Builder(this, true)
                    .setTarget(target)
                    .setContentTitle("SHOW ORIGINAL TIMELINE")
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setShowcaseEventListener(this)
                    .build();


            SharedPreferences.Editor editor = getSharedPreferences("INTRO", MODE_PRIVATE).edit();
            editor.putString("introopen", "done");
            editor.commit();
        }
    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(0);
        finish();
    }

    ShowcaseView sv;

    SquareImageView picture;
    String profileImageUrl = null;

    private View mHeaderView;
    private View mToolbarView;
    private int  mBaseTranslationY;
    private ViewPager mPager;
    private NavigationAdapter mPagerAdapter;
    SlidingTabLayout slidingTabLayout;

    FloatingActionButton fab;
    public static Context baseContext = null;

    String username                  = null;

    protected RequestQueue mRequestQueue;
    protected ImageLoader imageLoader;

    Activity activityReference = null;

    int currentShowCase = 0;

    public class LoadProfileImage extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                profileImageUrl = HelperFunctions.twitter.showUser(username).getBiggerProfileImageURL();
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
            picture.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        Intent intent = new Intent(ViewPagerTabListViewActivity.this, UserProfile.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        activityReference.overridePendingTransition(R.anim.animation_entry_right, R.anim.animation_exit_left);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.originaltimeline){
            Fragment fgT = getCurrentFragment();
            if(fgT instanceof MyFragment) {
                HelperFunctions.animate = false;
                MyFragment fg           = (MyFragment) fgT;
                ObservableListView olv  = fg.listView;
                fg.tweetadapter.setTweets(fg.tweetlist);
                fg.tweetadapter.notifyDataSetChanged();
                olv.smoothScrollToPosition(0);
                HelperFunctions.animate = true;
                Toast.makeText(this, "Sorted By Time", Toast.LENGTH_LONG).show();
            }
            return true;
        }
        else if(id == R.id.sortitemsbyfavorites){
            Fragment fgT = getCurrentFragment();
            if(fgT instanceof MyFragment) {
                HelperFunctions.animate = false;
                MyFragment fg           = (MyFragment) fgT;
                ObservableListView olv  = fg.listView;
                MyAdapter mya           = fg.tweetadapter;
                fg.tempTweetList        = new ArrayList<Tweet>(fg.tweetlist);
                HelperFunctions.sortTweets(2, fg.tempTweetList, mya, olv);
                fg.tweetadapter.setTweets(fg.tempTweetList);
                fg.tweetadapter.notifyDataSetChanged();
                olv.smoothScrollToPosition(0);
                Toast.makeText(this, "Sorted By Favorite Count", Toast.LENGTH_LONG).show();
                HelperFunctions.animate = true;
            }
            return true;
        }
        else if(id == R.id.sortitemsbytweet){
            Fragment fgT = getCurrentFragment();
            if(fgT instanceof MyFragment) {
                HelperFunctions.animate = false;
                MyFragment fg           = (MyFragment) fgT;
                ObservableListView olv  = fg.listView;
                MyAdapter mya           = fg.tweetadapter;
                fg.tempTweetList        = new ArrayList<Tweet>(fg.tweetlist);
                HelperFunctions.sortTweets(1, fg.tempTweetList, mya, olv);
                fg.tweetadapter.setTweets(fg.tempTweetList);
                fg.tweetadapter.notifyDataSetChanged();
                olv.smoothScrollToPosition(0);
                Toast.makeText(this, "Sorted By Retweet Count", Toast.LENGTH_LONG).show();
                HelperFunctions.animate = true;
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mymenu, menu);

        if(showIntro) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final View temp = findViewById(R.id.sortitemsbyfavorites);
                    if (temp != null) {
                        ViewTarget target = new ViewTarget(temp);
                        sv = new ShowcaseView.Builder(activityReference, true)
                                .setTarget(target)
                                .setContentTitle("SORT TWEETS BY FAVORITE COUNT")
                                .setStyle(R.style.CustomShowcaseTheme2)
                                .setShowcaseEventListener((OnShowcaseEventListener) activityReference)
                                .build();
                    }
                }
            }, 5000);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public class LoadFriends extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {

            long nextCursor = -1;
            IDs friendIds   = null;
            do{
                ResponseList<User> followers = null;
                try {
                    friendIds = HelperFunctions.twitter.getFriendsIDs(nextCursor);
                    long arr[] = friendIds.getIDs();
                    int cur    = 0;

                    while(cur+100<arr.length) {
                        followers = HelperFunctions.twitter.lookupUsers(Arrays.copyOfRange(arr, cur, cur+100));
                        HelperFunctions.friends.addAll(followers);
                        for(User follower : followers) {
                            HelperFunctions.users.add(follower.getName());
                            //System.out.println("FRIEND " + follower.getId() + " " + follower.getScreenName() + " " + follower.getName());
                        }
                        cur = cur+100;
                    }
                    followers = HelperFunctions.twitter.lookupUsers(Arrays.copyOfRange(arr, cur, arr.length));
                    for(User follower : followers) {
                        HelperFunctions.friends.addAll(followers);
                        //System.out.println("FRIEND " + follower.getId() + " " + follower.getScreenName() + " " + follower.getName());
                    }
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }while((nextCursor = friendIds.getNextCursor()) != 0);

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpagertab);

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    int TWEET_COMPOSER_REQUEST_CODE = 100;
                    Intent intent = new TweetComposer.Builder(baseContext)
                            .text("What's on your mind")
                            .createIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent, TWEET_COMPOSER_REQUEST_CODE);
                }
                return false;
            }
        });


        SharedPreferences prefs = getSharedPreferences("INTRO", MODE_PRIVATE);
        String restoredText     = prefs.getString("introopen", null);
        if (restoredText != null)
            showIntro = false;

        activityReference = this;

        if(HelperFunctions.TITLES.size() == 0) {
            HelperFunctions.TITLES.add(0, "TimeLine");
            HelperFunctions.TITLES.add(1, "Verified");
            HelperFunctions.TITLES.add(2, "Trending");
        }

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

        TweetBank.init(this.getApplicationContext());
        //    TweetBank.sqlitehelper.clearDb(TweetBank.WriteAbleDB);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        baseContext = this.getApplication().getBaseContext();

        HelperFunctions.authConfig = new TwitterAuthConfig(Keys.TWITTER_KEY, Keys.TWITTER_SECRET);
        Fabric.with(this, new Twitter(HelperFunctions.authConfig));
        Fabric.with(this, new TweetUi());
        Fabric.with(this, new TweetComposer());

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        HelperFunctions.checkAndInit(this);

        username = HelperFunctions.currentSession.getUserName();

        picture = (SquareImageView) findViewById(R.id.userimage);

        new LoadProfileImage().execute("0", "1");

        mHeaderView = findViewById(R.id.header);
        ViewCompat.setElevation(mHeaderView, getResources().getDimension(R.dimen.toolbar_elevation));
        mToolbarView = findViewById(R.id.toolbar);

        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(3);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.underlinecolor));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mPager);

        // When the page is selected, other fragments' scrollY should be adjusted
        // according to the toolbar status(shown/hidden)
        slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                propagateToolbarState(toolbarIsShown());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        //ViewTarget target = new ViewTarget(R.id.sortitemsbyfavorites, this);
//        ViewTarget target = new ViewTarget(R.id.userimage, this);
//
//        sv = new ShowcaseView.Builder(this, true)
//                .setTarget(target)
//                .setContentTitle("SORT TWEETS BY FAVORITE COUNT")
//                .setContentText("PRANJAL TESTING TEXT")
//                .setStyle(R.style.CustomShowcaseTheme2)
//                .setShowcaseEventListener(this)
//                .hideOnTouchOutside()
//                .build();

        //ActionViewTarget target = new ActionViewTarget(this, ActionViewTarget.Type.TITLE);


        propagateToolbarState(toolbarIsShown());
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        //System.out.println("ViewPagerTabListViewActivity onScrollChanged dragging "+dragging+" firstScroll "+firstScroll+" scrollY "+scrollY);

        if (dragging) {
            int toolbarHeight = mToolbarView.getHeight();
            float currentHeaderTranslationY = ViewHelper.getTranslationY(mHeaderView);
            if (firstScroll) {
                if (-toolbarHeight < currentHeaderTranslationY) {
                    mBaseTranslationY = scrollY;
                }
            }
            float headerTranslationY = ScrollUtils.getFloat(-(scrollY - mBaseTranslationY), -toolbarHeight, 0);
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewHelper.setTranslationY(mHeaderView, headerTranslationY);
        }
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        //System.out.println("ViewPagerTabListViewActivity onUpOrCancelMotionEvent ScrollState "+scrollState);

        mBaseTranslationY = 0;

        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return;
        }
        View view = fragment.getView();
        if (view == null) {
            return;
        }

        int toolbarHeight = mToolbarView.getHeight();
        final ObservableListView listView = (ObservableListView) view.findViewById(R.id.mylist);
        if (listView == null) {
            return;
        }

        int scrollY = listView.getCurrentScrollY();
        if (scrollState == ScrollState.DOWN) {
            showToolbar();
            fab.show();
        } else if (scrollState == ScrollState.UP) {
            if (toolbarHeight <= scrollY) {
                hideToolbar();
                fab.hide();
            } else {
                fab.show();
                showToolbar();
            }
        } else {
            // Even if onScrollChanged occurs without scrollY changing, toolbar should be adjusted
            if (toolbarIsShown() || toolbarIsHidden()) {
                // Toolbar is completely moved, so just keep its state
                // and propagate it to other pages
                propagateToolbarState(toolbarIsShown());
            } else {
                // Toolbar is moving but doesn't know which to move:
                // you can change this to hideToolbar()
                showToolbar();
                fab.show();
            }
        }
    }

    private Fragment getCurrentFragment() {
        Fragment fg              = mPagerAdapter.getItemAt(mPager.getCurrentItem());
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if(fg instanceof MyImageFragment) {
            //MyFloatingActionButton fab = (MyFloatingActionButton) findViewById(R.id.fab);
            //fab.attachToListView(fg.);
            return (MyImageFragment) fg;
        }
        else if(fg instanceof TrendingFragment){
            return  (TrendingFragment) fg;
        }
        else{
            //fab.attachToListView(((MyFragment) fg).listView);
            //((MyFragment) fg).
            ((MyFragment) fg).mySetOnScrollListener(this);
            return (MyFragment) fg;
        }
    }

    private void propagateToolbarState(boolean isShown) {
        int toolbarHeight = mToolbarView.getHeight();

        // Set scrollY for the fragments that are not created yet
        mPagerAdapter.setScrollY(isShown ? 0 : toolbarHeight);

        // Set scrollY for the active fragments
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            // Skip current item
            if (i == mPager.getCurrentItem()) {
                continue;
            }

            // Skip destroyed or not created item
            Fragment f = mPagerAdapter.getItemAt(i);
            if (f == null ){//|| f instanceof MyImageFragment) {
                continue;
            }

            View view = f.getView();
            if (view == null) {
                continue;
            }
            ObservableListView listView;

            //if(f instanceof  MyFragment)
            listView = (ObservableListView) view.findViewById(R.id.mylist);
            //else
            //   listView = (ObservableListView) view.findViewById(R.id.newsobservableview);

            if (isShown) {
                // Scroll up
                if (0 < listView.getCurrentScrollY()) {
                    listView.setSelection(0);
                }
            } else {
                // Scroll down (to hide padding)
                if (listView.getCurrentScrollY() < toolbarHeight) {
                    listView.setSelection(1);
                }
            }
        }
    }

    private boolean toolbarIsShown() {
        return ViewHelper.getTranslationY(mHeaderView) == 0;
    }

    private boolean toolbarIsHidden() {
        return ViewHelper.getTranslationY(mHeaderView) == -mToolbarView.getHeight();
    }

    private void showToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        if (headerTranslationY != 0) {
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewPropertyAnimator.animate(mHeaderView).translationY(0).setDuration(200).start();
        }
        propagateToolbarState(true);
    }

    private void hideToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        int toolbarHeight = mToolbarView.getHeight();
        if (headerTranslationY != -toolbarHeight) {
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewPropertyAnimator.animate(mHeaderView).translationY(-toolbarHeight).setDuration(200).start();
        }
        propagateToolbarState(false);
    }

    //private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {
    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        private int mScrollY;

        public NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setScrollY(int scrollY) {
            mScrollY = scrollY;
        }

        @Override
        protected Fragment createItem(int position) {
            Bundle b     = new Bundle();
            b.putInt("position", position);

            if (position == 0)
                b.putBoolean("filter", false);
            else
                b.putBoolean("filter", true);

            if (0 < mScrollY) {
                b.putInt(MyFragment.ARG_INITIAL_POSITION, 1);
            }

            Fragment f = HelperFunctions.getFragment(position, b);

            if(position == 2)
                f = new TrendingFragment();
            return f;
        }

        @Override
        public int getCount() {
            return HelperFunctions.TITLES.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return HelperFunctions.TITLES.get(position);
        }
    }

}