package com.social.solution;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.google.gson.Gson;
import com.social.solution.fragment.MyFragment;
import com.social.solution.naivebayes.classifiers.NaiveBayes;
import com.social.solution.naivebayes.dataobjects.NaiveBayesKnowledgeBase;
import com.social.solution.others.Keys;
import com.social.solution.others.MyAdapter;
import com.social.solution.others.MyTwitterApiClient;
import com.social.solution.others.TweetBank;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.AccountService;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.TweetUi;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by pranjal on 29/04/15.
 */
public class HelperFunctions {

    public static boolean animate = true;                  // if 0 then don't animate else animate

    public static ArrayList<String> TITLES = new ArrayList<String>();
    public static ArrayList<User> friends  = new ArrayList<User>();
    public static ArrayList<String> users  = new ArrayList<String>();
    public static ArrayList<ArrayList<String>> filterList = new ArrayList<ArrayList<String>>();

    public static TwitterAuthConfig authConfig = null;
    public static TwitterSession currentSession = null;
    public static twitter4j.Twitter twitter = null;
    public static TwitterStream twitterStream = null;
    public static MyTwitterApiClient twitterApiClient = null;
    public static StatusesService statusesService = null;
    public static AccountService accountService = null;
    public static FavoriteService favoriteService = null;
    public static SearchService searchService = null;
    public static NaiveBayes nb;

    SearchService ss;


    public static Gson gson = new Gson();

    private static final UserStreamListener listener = new UserStreamListener() {
        @Override
        public void onStatus(Status status) {
            //TweetBank.insertTweet(t);
            //System.out.println("onStatus @" + status.getUser().getScreenName() + " - " + status.getText());
            String statusJson = TwitterObjectFactory.getRawJSON(status);
            //System.out.println("rawjson "+statusJson);
            Tweet updatedTweet  = HelperFunctions.gson.fromJson(statusJson, Tweet.class);
            //System.out.println("rawjson updateTweet is "+updatedTweet.text);
            TweetBank.insertTweet(updatedTweet);
        }

        @Override
        public void onFriendList(long[] friendIds) {

        }

        @Override
        public void onFavorite(User source, User target, Status favoritedStatus) {
            //System.out.println("PRANJALUSERNAMEIS favorite some tweet");

        }

        @Override
        public void onUnfavorite(User source, User target, Status unfavoritedStatus) {

        }

        @Override
        public void onFollow(User source, User followedUser) {
            //System.out.println("PRANJALUSERNAMEIS followed someone");

        }

        @Override
        public void onUnfollow(User source, User unfollowedUser) {

        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) {

        }

        @Override
        public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {

        }

        @Override
        public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {

        }

        @Override
        public void onUserListSubscription(User subscriber, User listOwner, UserList list) {

        }

        @Override
        public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {

        }

        @Override
        public void onUserListCreation(User listOwner, UserList list) {

        }

        @Override
        public void onUserListUpdate(User listOwner, UserList list) {

        }

        @Override
        public void onUserListDeletion(User listOwner, UserList list) {

        }

        @Override
        public void onUserProfileUpdate(User updatedUser) {

        }

        @Override
        public void onBlock(User source, User blockedUser) {

        }

        @Override
        public void onUnblock(User source, User unblockedUser) {

        }

        @Override
        public void onException(Exception ex) {
            //System.out.println("PRANJALUSERNAMEIS exception");
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
        }

        @Override
        public void onDeletionNotice(long directMessageId, long userId) {
            //System.out.println("Got a direct message deletion notice id:" + directMessageId);
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            //System.out.println("Got a track limitation notice:" + numberOfLimitedStatuses);
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
            //System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
        }

        @Override
        public void onStallWarning(StallWarning warning) {
            //System.out.println("Got stall warning:" + warning);
        }
    };

    public static int getRelativeLeft(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }

    public static int getRelativeTop(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }

    public static void checkAndInit(Context context){

        if(HelperFunctions.authConfig == null) {
            HelperFunctions.authConfig = new TwitterAuthConfig(Keys.TWITTER_KEY, Keys.TWITTER_SECRET);
            Fabric.with(context, new Twitter(HelperFunctions.authConfig));
            Fabric.with(context, new TweetUi());
        }

        if(HelperFunctions.currentSession == null) {
            //System.out.println("com.social.solution currentSession is NULL");
            HelperFunctions.currentSession = Twitter.getSessionManager().getActiveSession();
        }

        ConfigurationBuilder config = new ConfigurationBuilder();
        config.setJSONStoreEnabled(true);
        config.setOAuthConsumerKey(Keys.TWITTER_KEY);
        config.setOAuthConsumerSecret(Keys.TWITTER_SECRET);
        config.setOAuthAccessToken(HelperFunctions.currentSession.getAuthToken().token);
        config.setOAuthAccessTokenSecret(HelperFunctions.currentSession.getAuthToken().secret);
        Configuration cf        = config.build();

        if(HelperFunctions.twitter == null) {
            //System.out.println("com.social.solution HelperFunctions.twitter is NULL");
            HelperFunctions.twitter = new TwitterFactory(cf).getInstance();
        }


        if(HelperFunctions.twitterStream == null) {
            //System.out.println("com.social.solution HelperFunctions.twitterStream is NULL");
            HelperFunctions.twitterStream = new TwitterStreamFactory(cf).getInstance();
            HelperFunctions.twitterStream.addListener(listener);
            HelperFunctions.twitterStream.user();
        }

        if (HelperFunctions.twitterApiClient == null) {
            //System.out.println("com.social.solution HelperFunctions.twitterApiClient is NULL");
            HelperFunctions.twitterApiClient = new MyTwitterApiClient(HelperFunctions.currentSession);
            HelperFunctions.accountService   = HelperFunctions.twitterApiClient.getAccountService();
            HelperFunctions.statusesService  = HelperFunctions.twitterApiClient.getStatusesService();
            HelperFunctions.favoriteService  = HelperFunctions.twitterApiClient.getFavoriteService();
            HelperFunctions.searchService    = HelperFunctions.twitterApiClient.getSearchService();
        }

        if(nb == null){
            NaiveBayesKnowledgeBase knowledgeBase = null;
            try {
                FileInputStream fis   = new FileInputStream("mybean.ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                knowledgeBase         = (NaiveBayesKnowledgeBase) ois.readObject();
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            nb = new NaiveBayes(knowledgeBase);
        }
    }


    public static boolean checkit(Tweet t){
        return t.user.verified;
    }

    public static MyFragment getFragment(int position, Bundle b){
//            if(fragments.get(position) != null) {
//                System.out.println("pranjalsahu returning old fragment");
//                return fragments.get(position);
//            }
//            else {
        MyFragment myfg = new MyFragment();
        myfg.setArguments(b);
        return myfg;
        //}
    }

    public static boolean genericFilterFunction(Tweet t, int position){
        if(position == 0 || position == 2)
            return true;
        if(position == 1)                       // for verified tweets
            return t.user.verified;

        //System.out.println("YOYO "+t.user.name);
        boolean flag = false;
        ArrayList<String> userList = filterList.get(position);
        for(int i=0;i<userList.size();++i){
            if(t.user.name.contains(userList.get(i))) {
                //System.out.println("PRANJAL match username : "+t.user.name+" userlist : "+userList.get(i));
                return true;
            }
        }
        return flag;
    }


    public static List<Tweet> getFilteredList(List<Tweet> tList){
        List<Tweet> resultList = new ArrayList<Tweet>();
        for(int i=0;i< tList.size();++i){
            if(checkit(tList.get(i)))
                resultList.add(tList.get(i));
        }
        return resultList;
    }

    //helper method to disable subviews
    public static void disableViewAndSubViews(ViewGroup layout) {

//        layout.setEnabled(false);
//        layout.setClickable(false);
//        layout.setLongClickable(false);
//
//        for (int i = 0; i < layout.getChildCount(); i++) {
//            View child = layout.getChildAt(i);
//
//            if (child instanceof ViewGroup) {
//                disableViewAndSubViews((ViewGroup) child);
//            } else {
//
//                if(child instanceof TextView) {
//                    TextView tmp = ((TextView) child);
//                    return;
//                }
//                if(child instanceof ImageView){
//                    ImageView tmp = ((ImageView) child);
//                    //System.out.println("pranjaldisable : checking ImageView " + child.getId());
//                    //return;
//                }
//                child.setEnabled(false);
//                child.setClickable(false);
//                child.setLongClickable(false);
//            }
//        }
    }

    public static void sortTweets(int type,  List<Tweet> tweetlist, MyAdapter tweetadapter){
        if(type == 1)
            Collections.sort(tweetlist, comparatorTweetCount);
        else if(type == 2)
            Collections.sort(tweetlist, comparatorFavoriteCount);
        else if(type == 3)
            Collections.sort(tweetlist, comparatorId);

        if(tweetadapter != null)
            tweetadapter.notifyDataSetChanged();
        //lv.setSelectionAfterHeaderView();
        //lv.smoothScrollToPosition(0);

    }

    public static void sortTweets(int type,  List<Tweet> tweetlist, MyAdapter tweetadapter, ObservableListView lv){
        if(tweetlist == null || tweetlist.size() <= 0)
            return;

        if(type == 1)
            Collections.sort(tweetlist, comparatorTweetCount);
        else if(type == 2)
            Collections.sort(tweetlist, comparatorFavoriteCount);
    }

    public static Comparator<Tweet> comparatorId = new Comparator<Tweet>() {
        @Override
        public int compare(Tweet lhs, Tweet rhs) {
            if(lhs.id > rhs.id)
                return -1;
            else if(lhs.id == rhs.id)
                return 0;
            else
                return 1;
        }
    };

    public static Comparator<Tweet> comparatorTweetCount = new Comparator<Tweet>() {
        @Override
        public int compare(Tweet lhs, Tweet rhs) {
            if(lhs.retweetCount > rhs.retweetCount)
                return -1;
            else if(lhs.retweetCount == rhs.retweetCount)
                return 0;
            else
                return 1;
        }
    };

    public static Comparator<Tweet> comparatorFavoriteCount = new Comparator<Tweet>() {
        @Override
        public int compare(Tweet lhs, Tweet rhs) {
            if(lhs.favoriteCount > rhs.favoriteCount)
                return -1;
            else if(lhs.favoriteCount == rhs.favoriteCount)
                return 0;
            else
                return 1;
        }
    };


    public static Bitmap loadBitmapFromView(View v) {
        Bitmap bitmap;
        v.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public static Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static void showImage(Context context, Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        context.startActivity(intent);
    }

}
