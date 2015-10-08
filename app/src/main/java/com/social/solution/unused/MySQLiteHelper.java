package com.social.solution.unused;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.social.solution.others.TweetBank;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.List;


public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TWEETS_TABLE_NAME   = "TweetsTable";
    public static final String COLUMN_ID           = "TweetId";
    public static final String COLUMN_DATA         = "TweetObject";

    private static final String DATABASE_NAME      = "tweets.db";
    private static final int DATABASE_VERSION      = 1;

    private static Gson gson = null;



    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TWEETS_TABLE_NAME + "(" + COLUMN_ID
            + " INTEGER PRIMARY KEY , " + COLUMN_DATA
            + " TEXT not null);";


    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        gson = new Gson();
    }

    public void clearDb(SQLiteDatabase db){
        Cursor cursor = db.rawQuery("select * from TweetsTable", null);
        cursor.moveToFirst();

        int i    = 0;
        int size = 100;

        int beforesize = getSizeOfDB(db);
        System.out.println("size of db before cleardb "+beforesize);

        if(beforesize < 200) {
            System.out.println("returning size is "+beforesize);
            return;
        }

        while (i< size) {
            String tweetString = cursor.getString(1);
            Tweet t1          = gson.fromJson(tweetString, Tweet.class);
            cursor.moveToNext();
            ++i;
        }

        int tempcount = 0;
        while(!cursor.isLast()){
            ++tempcount;
            String tweetString = cursor.getString(1);
            Tweet t1          = gson.fromJson(tweetString, Tweet.class);
            int delete = db.delete(TWEETS_TABLE_NAME, "TweetId = " + t1.id, null);
            cursor.moveToNext();
        }

        System.out.println("checking count "+tempcount);
        cursor.close();
        System.out.println("size of db after cleardb "+getSizeOfDB(db));
    }

    public void storeState(SQLiteDatabase db){
        for(Tweet t: TweetBank.tweetlist)
            insertTweet(db, t);
        clearDb(db);
        return;
    }

    public long insertTweet(SQLiteDatabase db, Tweet tweet) {
        String tweet_json = gson.toJson(tweet);

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_DATA, tweet_json);
        values.put(MySQLiteHelper.COLUMN_ID, tweet.id);

        //db.replace(TWEETS_TABLE_NAME, null, values);
        //db.()
        return db.insert(TWEETS_TABLE_NAME, null, values);
    }

    public List<Tweet> getTweetsFromDb(SQLiteDatabase db, int size){
        List<Tweet> tweets = new ArrayList<Tweet>();

        Cursor cursor = db.rawQuery("select * from TweetsTable", null);

        cursor.moveToFirst();
        int i=0;
        while (i< size && i < cursor.getCount()) {
            String tweetString = cursor.getString(1);
            Tweet t1          = gson.fromJson(tweetString, Tweet.class);
            tweets.add(t1);
            cursor.moveToNext();
            ++i;
        }
        cursor.close();
        return tweets;
    }

    public int getSizeOfDB(SQLiteDatabase db){
        int size      = 0;
        Cursor cursor = db.rawQuery("select * from TweetsTable", null);
        return cursor.getCount();
    }

    public Tweet readTweet(SQLiteDatabase db, long tweetid){
        return null;
        //Tweet t1          = gson.fromJson(tweet_json, Tweet.class);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*@Override
    public void onCreate(SQLiteDatabase db) {
        //db.execSQL(DATABASE_CREATE);
    }*/
}
