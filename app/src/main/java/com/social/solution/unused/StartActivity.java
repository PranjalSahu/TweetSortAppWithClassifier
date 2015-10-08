package com.social.solution.unused;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.social.solution.others.MyTwitterApiClient;
import com.social.solution.R;
import com.social.solution.others.MyAdapter;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.AccountService;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetui.TweetUi;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import twitter4j.IDs;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class StartActivity extends FragmentActivity {
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY    = "i8lsarVzM1RLdQli7JvGibJya";
    private static final String TWITTER_SECRET = "ivA141Pewjx3VYfKOUBMIRJZZnNhPQNW9gVdM1nlXrnsNmir29";

    Menu optionsMenu;
    TwitterSession ts = null;
    View footer;
    TwitterLoginButton loginButton;
    String authToken  = null;
    String authSecret = null;
    ListView lv;
    LinearLayout        mLayout;
    List<Tweet>         tweetlist;

    String username = null;
    twitter4j.Twitter twitter1;
    TwitterFactory twitterFactory;
    MyTwitterApiClient twitterApiClient;
    StatusesService statusesService;
    AccountService accountService;
    FavoriteService favoriteService;
    TwitterAuthConfig authConfig     = null;
    TwitterSession currentSession = null;

    LinearLayout linlaHeaderProgress;
    MyAdapter tweetadapter;
    Long              lasttweetid    = null;
    boolean           loading        = false;
    SharedPreferences prefs          = null;
    ProgressBar    headerProgress    = null;
    //SQLiteDatabase myDB              = null;
    MySQLiteHelper sqlitehelper     = null;
    LinearLayout myLayout;

    private SQLiteDatabase WriteAbleDB;
    private SQLiteDatabase ReadAbleDB;




    public void setRefreshActionButtonState(boolean refreshing) {
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu
                    .findItem(R.id.sortbytweetcount);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }



    void initDB(){
        SQLiteDatabase mydatabase = openOrCreateDatabase(MySQLiteHelper.TWEETS_TABLE_NAME, MODE_PRIVATE, null);

        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                MySQLiteHelper.TWEETS_TABLE_NAME +
                "(" + MySQLiteHelper.COLUMN_ID + " LONG PRIMARY KEY, tweet BLOB);");

        return;
    }

    /*void getAllFollowers(){
        IDs ids = mTwitter.mTwitter.getFriendsIDs(-1);// ids
        // for (long id : ids.getIDs()) {
        do {
            for (long id : ids.getIDs()) {


                String ID = "followers ID #" + id;
                String[] firstname = ID.split("#");
                String first_Name = firstname[0];
                String Id = firstname[1];

                Log.i("split...........", first_Name + Id);

                String Name = mTwitter.mTwitter.showUser(id).getName();
                String screenname = mTwitter.mTwitter.showUser(id).getScreenName();


                //            Log.i("id.......", "followers ID #" + id);
                //          Log.i("Name..", mTwitter.mTwitter.showUser(id).getName());
                //          Log.i("Screen_Name...", mTwitter.mTwitter.showUser(id).getScreenName());
                //          Log.i("image...", mTwitter.mTwitter.showUser(id).getProfileImageURL());


            }
        } while (ids.hasNext());
    }*/

    //new LoadTwitterFeed().execute(Integer.toString(currentindex), Integer.toString(PAGE_SIZE));
    public class LoadTwitterFeed extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

            long cursor    = -1L;
            List<Long> ids = new ArrayList<Long>();
            //long[] ids    = null;
            int length    = 1;
            //long[] allIds = null;
            long[] tempids = null;

            StringBuilder sb = new StringBuilder();
            //sb.append();
            //sb.toString()
            //prefs.edit().putString("authToken", tat.token);


            while(cursor != 0 && length > 0) {
                System.out.println("in loop");
                try {
                    IDs temp    = twitter1.friendsFollowers().getFollowersIDs(username, cursor);
                    cursor      = temp.getNextCursor();

                    //ArrayUtils.append(allsIds, temp); //allsIds.
                    //ids. ids    = temp.getIDs();
                    tempids = temp.getIDs();
                    length = temp.getIDs().length;
                    //ids = twitter1.friendsFollowers().getFollowersIDs() frgetFollowersIDs(String screenName, long cursor)
                    //ids = twitter1.friendsFollowers().getFollowersIDs(-1L).getIDs();
                } catch (twitter4j.TwitterException e) {
                    System.out.println("twittersahu: failed");
                    e.printStackTrace();
                    return null;
                }

                if(tempids != null) {
                    for (long id : tempids) {
                        ids.add(id);
                        System.out.println("twittersahu: " + id);
                    }
                }
            }

            if(ids.size() > 0) {
                Collections.sort(ids);
                for (long tid : ids) {
                    sb.append(Long.toString(tid));
                    sb.append(" ");
                }

                String preFollowers = null;
                String curFollowers = sb.toString();


                if (prefs.contains("followers"))
                    preFollowers = prefs.getString("followers", null);

                System.out.println("prefollowersString: " + preFollowers);
                System.out.println("followersString: " + curFollowers);

                SharedPreferences.Editor ed = prefs.edit();
                ed.putString("followers", curFollowers);
                ed.commit();

                if (preFollowers != null) {
                    String[] preTokens = preFollowers.split(" ");
                    String[] curTokens = curFollowers.split(" ");
                    List<Long> preLong = new ArrayList<Long>();
                    List<Long> curLong = new ArrayList<Long>();

                    int count = 0;
                    for (String st : preTokens) {
                        boolean flag = true;
                        for (String st1 : curTokens) {
                            if (st.equals(st1)) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            ++count;
                            System.out.println("twitterunfollow: " + st);
                        }
                    }
                    //Toast.makeText(getApplicationContext(), "Unfollowed Numbers "+count, Toast.LENGTH_LONG);
                    System.out.println(" Unfollowed Numbers: " + count);
                }
                //else
                //    Toast.makeText(getApplicationContext(), "No previous Data", Toast.LENGTH_LONG);

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            //List<String> strList 		    = new ArrayList<String>();
            System.out.println("LOADING DONE");
            loading = false;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         String custkey      = "FacGCa1kekg6t68N9n1r46GAI";
         String custsecret   = "aQSljFzqIKuVu4H4sr9OQhvtEVW4sn1qRMHtJezZMiMKeOFlWo";
         String accesstoken  = "163158983-PcgEMJBfxFQBSK2JHcnKYfZhGTyPio6jt23z3FBh";
         String accesssecret = "BIf9DohxN21Y3jF1m3LP3JAgR2gA673Ywwe20QjVFyCnZ";

        ConfigurationBuilder config =
                new ConfigurationBuilder()
                        .setOAuthConsumerKey(custkey)
                        .setOAuthConsumerSecret(custsecret)
                        .setOAuthAccessToken(accesstoken)
                        .setOAuthAccessTokenSecret(accesssecret);

        twitter1 = new TwitterFactory(config.build()).getInstance();

        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        prefs = PreferenceManager.getDefaultSharedPreferences(StartActivity.this);

        System.out.println("reading it: " + prefs.getString("pranjal", "Not found"));

        authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        Fabric.with(this, new TweetUi());
        Fabric.with(this, new TweetComposer());
        //Fabric.with(this, new MoPub());

        MyApplication appState = ((MyApplication)getApplicationContext());

        tweetlist        = new ArrayList<Tweet>();

        android.app.ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        currentSession = appState.currentSession;  //Twitter.getSessionManager().getActiveSession();

        if (currentSession == null) {
            System.out.println("NULL POINTER EXCEPTION");
            //Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //appState.startActivity(intent);
        }



        //System.out.println("Current Session is NULL1");
        username = currentSession.getUserName();
        System.out.println("PRANJAL TWITTER USERNAME IS "+username);

//        try {
//            new LoadTwitterFeed().execute("0", "10");
//        }
//        catch (TwitterException te){
//            System.out.println("Twitter Exception "+te.getMessage());
//        }

        twitterApiClient = new MyTwitterApiClient(currentSession); //TwitterCore.getInstance().getApiClient(currentSession);
        accountService   = twitterApiClient.getAccountService();
        statusesService  = twitterApiClient.getStatusesService();
        favoriteService  = twitterApiClient.getFavoriteService();


        appState.accountService   = accountService;
        appState.favoriteService  = favoriteService;
        appState.statusesService  = statusesService;
        appState.twitterApiClient = twitterApiClient;
        appState.authConfig       = authConfig;
        appState.currentSession   = currentSession;
        appState.username         = username;


        sqlitehelper = new MySQLiteHelper(this.getApplicationContext());
        WriteAbleDB  = sqlitehelper.getWritableDatabase();
        ReadAbleDB   = sqlitehelper.getReadableDatabase();

        File f = getApplicationContext().getDatabasePath("tweets.db");
        long dbSize = f.length();

        System.out.println("pranjal size of db is : " + dbSize);

        sqlitehelper.clearDb(WriteAbleDB);

        setContentView(R.layout.activity_main_pager);

        if (savedInstanceState == null) {
            FragmentTransaction transaction   = getSupportFragmentManager().beginTransaction();
            SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();

            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }


        //twitterApiClient.getStatusesService().userTimeline();
        //footer           = (View)getLayoutInflater().inflate(R.layout.listview_footer_row, null);
        //setContentView(R.layout.tweet_list);

        //tweetadapter     = new MyAdapter(this, statusesService, favoriteService);

        //headerProgress   = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        //linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        //linlaHeaderProgress.setBackgroundColor(-1);

//        lv = (ListView)findViewById(R.id.mylist);
//
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                System.out.println("Pranjal Position clicked is " + position);
//            }
//        });
//
//        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//            }
//
//            public void onScroll(AbsListView view, int firstVisibleItem,
//                                 int visibleItemCount, int totalItemCount) {
//
//                int visibleThreshold = 0;
//                if (loading == false && totalItemCount > 5 && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
//                    loading = true;
//
//                    //lv.getRootView()
//                    lv.addFooterView(footer);
//                    System.out.println("Footer View Added");
//                    LoadTweets();
//                }
//            }
//
//        });



        //mytweets();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("Inside onActivityResult method pranjal");
        // Pass the activity result to the login button.
        if(data == null)
            System.out.println("onactivityresult data is null");
        if(loginButton == null)
            System.out.println("onactivityresult loginbutton is null");

        if(loginButton != null)
            loginButton.onActivityResult(requestCode, resultCode,
                data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        //return true;

        this.optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.airport_menu, menu);
        boolean checkme =  super.onCreateOptionsMenu(menu);
        System.out.println("YOYOYO checkme "+checkme);
        return checkme;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.sortbytweetcount) {
            //HelperFunctions.sortTweets(1);
            return true;
        }
        if(id == R.id.sortbyfavoritecount) {
            //HelperFunctions.sortTweets(2);
            return true;
        }
        if(id == R.id.composetweet){

//            Intent composeTweet = new TweetComposer.Builder(this.getApplicationContext())
//                                        .text("#hastag")
//                                        .createIntent();
//
//            System.out.println(" Composing Tweet Intent");
//            startActivityForResult(composeTweet, 0);
//            overridePendingTransition(R.anim.animation_entry_right, R.anim.stay_still);

            Intent intent = new Intent(StartActivity.this, ShowUserProfile.class);
            intent.putExtra("userid", 1001);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ((MyApplication)getApplicationContext()).startActivity(intent);
            overridePendingTransition(R.anim.animation_entry_right, R.anim.stay_still);

        }
        return super.onOptionsItemSelected(item);
    }
}
