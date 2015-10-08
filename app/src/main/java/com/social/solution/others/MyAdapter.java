package com.social.solution.others;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.social.solution.HelperFunctions;
import com.social.solution.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.BaseTweetView;
import com.twitter.sdk.android.tweetui.TweetViewAdapter;

import java.util.List;

import twitter4j.JSONException;
import twitter4j.JSONObject;

/**
 * Created by pranjal on 29/04/15.
 */

public class MyAdapter extends TweetViewAdapter {

    LayoutInflater inflater         = null;

    private int mLastPosition = -1;

    int defaultColor = 0;

    public MyAdapter(Context context){
        super(context);
        inflater = LayoutInflater.from(context);

    }

    private void updateTweet(Tweet updateTweet){
        int pos = 0;
        //R.anim.
        List<Tweet> tl =  this.getTweets();

        for(Tweet t:tl){
            if(t.id == updateTweet.id)
                break;
            ++pos;
        }

        tl.set(pos, updateTweet);
        this.setTweets(tl);
        System.out.println("tweet updated " + updateTweet.id + " old: " + this.getTweetAtPosition(pos).favorited + " new: " + updateTweet.favorited);

        this.notifyDataSetChanged();

        System.out.println("tweet updated checking " + this.getTweetAtPosition(pos).id + " " + this.getTweetAtPosition(pos).favorited);
        return;
    }

    private void updateTweet(Tweet temp, int a){
        int pos = 0;
        List<Tweet> tl =  this.getTweets();

        for(Tweet t:tl){
            if(t.id == temp.id)
                break;
            ++pos;
        }

        tl.set(pos, temp);

        //System.out.println("pranjalsahuretweeted: "+temp.id);
        this.setTweets(tl);
        //this.notifyDataSetChanged();
        return;
    }

    private void updateTweet(long id){
        HelperFunctions.statusesService.lookup(Long.toString(id), true, false, false, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                updateTweet(result.data.get(0), 0);
            }

            @Override
            public void failure(TwitterException e) {

            }
        });
        return;
    }

    private void getFollowers(){

        //String encodedSearch = URLEncoder.encode(searchTerm, "UTF-8");

    }

    private Tweet getTweetAtPosition(int position){
        return this.getItem(position);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        Object rowView    = convertView;
        final Tweet tweet = this.getItem(position);

        System.out.println("getview "+tweet.idStr+" "+tweet.favorited);

        if(convertView == null) {
            rowView          = this.getTweetView(this.context, tweet);
            View buttonsRow  = inflater.inflate(R.layout.buttons_row, parent, false);

            final ImageButton iv2 = (ImageButton)buttonsRow.findViewById(R.id.retweetimagebutton);
            final ImageButton iv3 = (ImageButton)buttonsRow.findViewById(R.id.favoriteimagebutton);
            final TextView     t1 = (TextView)buttonsRow.findViewById(R.id.retweetcounttext);
            final TextView     t2 = (TextView)buttonsRow.findViewById(R.id.favoritecounttext);

            t1.setTag(tweet);
            t2.setTag(tweet);
            iv2.setTag(tweet);
            iv3.setTag(tweet);

            System.out.println("Retweet count: "+tweet.retweetCount+" Favorite count : "+tweet.favoriteCount);
            t1.setText(Integer.toString(tweet.retweetCount));
            t2.setText(Integer.toString(tweet.favoriteCount));

            iv2.setBackgroundColor(0);
            iv3.setBackgroundColor(0);

            defaultColor = t1.getTextColors().getDefaultColor();

            if(tweet.retweeted) {
                t1.setTextColor(Color.parseColor("#77B255"));
                iv2.setImageResource(R.drawable.retweet_on);
            }
            else{
                t1.setTextColor(defaultColor);
            }
            if(tweet.favorited) {
                t2.setTextColor(Color.parseColor("#FFAC33"));
                iv3.setImageResource(R.drawable.favorite_on);
            }
            else{
                t2.setTextColor(defaultColor);
            }

            iv2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Tweet tempTweet = (Tweet) v.getTag();
                    //Toast.makeText(context, "iv2 " + tempTweet.user.name, Toast.LENGTH_SHORT).show();
                }
            });


            iv3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tweet tempTweet = (Tweet) v.getTag();
                    //Toast.makeText(context, tempTweet.user.name, Toast.LENGTH_SHORT).show();

                    if (!tempTweet.favorited) {
                        //iv3.setImageResource(R.drawable.favorite_on);
                        HelperFunctions.favoriteService.create(tempTweet.id, false, new Callback<Tweet>() {
                            @Override
                            public void success(Result<Tweet> result) {
                                updateTweet(result.data);
                                iv3.setTag(result.data);
                                iv3.setImageResource(R.drawable.favorite_on);
                                //Toast.makeText(context, "Favorite Done "+result.data.favorited, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failure(TwitterException e) {
                                //Toast.makeText(context, "Favorite Not Done", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        HelperFunctions.favoriteService.destroy(tempTweet.id, false, new Callback<Tweet>() {
                            @Override
                            public void success(Result<Tweet> result) {
                                updateTweet(result.data);
                                iv3.setTag(result.data);
                                iv3.setImageResource(R.drawable.favorite);
                                //Toast.makeText(context, "UnFavorite Done", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failure(TwitterException e) {
                                //Toast.makeText(context, "UnFavorite Not Done", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });



            LinearLayout lv1 = (LinearLayout)inflater.inflate(R.layout.tweet_card_view, null);

            //lv1.setOrientation(LinearLayout.VERTICAL);

            ((LinearLayout)lv1.findViewById(R.id.card_view_linear)).addView((View) rowView, 0);
            ((LinearLayout)lv1.findViewById(R.id.card_view_linear)).addView((View) buttonsRow, 1);

            rowView = (View)lv1;

            t1.setEnabled(true);
            t2.setEnabled(true);

        } else {

            ((BaseTweetView)((LinearLayout)((LinearLayout) convertView).findViewById(R.id.card_view_linear))
                    .getChildAt(0))
                    .setTweet(tweet);

            ((BaseTweetView)((LinearLayout)((LinearLayout) convertView).findViewById(R.id.card_view_linear))
                    .getChildAt(0))
                    .setEnabled(true);

            ((BaseTweetView)((LinearLayout)((LinearLayout) convertView).findViewById(R.id.card_view_linear))
                    .getChildAt(0))
                    .setTag(tweet);

            View btnRow              = ((LinearLayout)((LinearLayout) rowView).findViewById(R.id.card_view_linear)).getChildAt(1);
            final ImageButton child1 = (ImageButton)btnRow.findViewById(R.id.retweetimagebutton);
            final ImageButton child2 = (ImageButton)btnRow.findViewById(R.id.favoriteimagebutton);
            final TextView t1        = (TextView)btnRow.findViewById(R.id.retweetcounttext);
            final TextView t2        = (TextView)btnRow.findViewById(R.id.favoritecounttext);

            child1.setEnabled(true);
            child2.setEnabled(true);
            t1.setEnabled(true);
            t2.setEnabled(true);

            child1.setTag(tweet);
            child2.setTag(tweet);
            t1.setTag(tweet);
            t2.setTag(tweet);

            System.out.println("Retweet count: " + Integer.toString(((Tweet) t1.getTag()).retweetCount) + " Favorite count : " + Integer.toString(((Tweet) t2.getTag()).favoriteCount));

            t1.setText(Integer.toString(((Tweet) t1.getTag()).retweetCount));
            t2.setText(Integer.toString(((Tweet) t2.getTag()).favoriteCount));

            if(tweet.retweeted) {
                t1.setTextColor(Color.parseColor("#77B255"));
                child1.setImageResource(R.drawable.retweet_on);
            }
            else {
                t1.setTextColor(defaultColor);
                child1.setImageResource(R.drawable.retweet);
            }

            if(tweet.favorited) {
                t2.setTextColor(Color.parseColor("#FFAC33"));
                child2.setImageResource(R.drawable.favorite_on);
            }
            else {
                t2.setTextColor(defaultColor);     // TODO : testing favorite bug
                child2.setImageResource(R.drawable.favorite);
            }


           child2.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   final Tweet tempTweet = (Tweet) v.getTag();
                   //Toast.makeText(context, tempTweet.user.name, Toast.LENGTH_SHORT).show();

                   if(!tempTweet.favorited)
                       child2.setImageResource(R.drawable.favorite_on);

                   if (!tempTweet.favorited) {
                       //child2.setImageResource(R.drawable.favorite_on);
                       HelperFunctions.favoriteService.create(tempTweet.id, false, new Callback<Tweet>() {
                           @Override
                           public void success(Result<Tweet> result) {
                               System.out.println("vani temp:" + HelperFunctions.gson.toJson(tempTweet));
                               System.out.println("vani result:" + HelperFunctions.gson.toJson(result.data));

                               String originalJson = HelperFunctions.gson.toJson(tempTweet);
                               String newJson = HelperFunctions.gson.toJson(result.data);
                               JSONObject jsonObjOriginal = null;
                               JSONObject jsonObjNew = null;
                               try {
                                   jsonObjOriginal = new JSONObject(originalJson);
                                   jsonObjNew = new JSONObject(newJson);
                                   jsonObjOriginal.put("favorite_count", jsonObjNew.get("favorite_count"));
                                   jsonObjOriginal.put("favorited", jsonObjNew.get("favorited"));
                                   jsonObjOriginal.put("retweet_count", jsonObjNew.get("retweet_count"));
                                   jsonObjOriginal.put("retweeted", jsonObjNew.get("retweeted"));
                               } catch (JSONException e) {
                                   e.printStackTrace();
                               }

                               Tweet updatedTweet = HelperFunctions.gson.fromJson(jsonObjOriginal.toString(), Tweet.class);

                               updateTweet(updatedTweet);
                               child2.setTag(updatedTweet);
                               child2.setImageResource(R.drawable.favorite_on);

                               t2.setTextColor(Color.parseColor("#FFAC33"));
                               //System.out.println("5pranjalupdate favoriteon " + result.data.id);
                               //Toast.makeText(context, "Favorite Done " + result.data.favorited, Toast.LENGTH_SHORT).show();
                           }

                           @Override
                           public void failure(TwitterException e) {
                               //Toast.makeText(context, "Favorite Not Done", Toast.LENGTH_SHORT).show();
                           }
                       });
                   } else {

                   }
               }
           });

            child1.setOnClickListener(new View.OnClickListener() {//myOnClickListener(tweet) {
                @Override
                public void onClick(View v) {
                    final Tweet tempTweet = (Tweet) v.getTag();
                    //Toast.makeText(context, "new child1 "+tempTweet.user.name +" retweeted = "+tempTweet.retweeted, Toast.LENGTH_SHORT).show();
                    if(!tempTweet.retweeted)
                        child1.setImageResource(R.drawable.retweet_on);

                    HelperFunctions.statusesService.retweet(tempTweet.id, false, new Callback<Tweet>() {
                        @Override
                        public void success(Result<Tweet> result) {
                            child1.setImageResource(R.drawable.retweet_on);
                            t1.setTextColor(Color.parseColor("#77B255"));

                            child1.setTag(result.data);
                            child2.setTag(result.data);
                            updateTweet(tempTweet.id);
                            //Toast.makeText(context, "Retweet done new" + result.data.retweeted, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failure(TwitterException e) {
                            //Toast.makeText(context, "Retweet Not done", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
        }

        if(HelperFunctions.animate) {
            TranslateAnimation animation = null;
            if (position > mLastPosition) {
                animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF,
                        0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);

                animation.setDuration(400);
                ((View) rowView).startAnimation(animation);
                mLastPosition = position;
            }
        }

//        Animation animation = AnimationUtils.loadAnimation(context, (position > mLastPosition) ? R.anim.up_from_bottom : R.anim.down_from_bottom);
//        ((View)rowView).startAnimation(animation);
//        mLastPosition = position;


        return (View)rowView;
    }
}
