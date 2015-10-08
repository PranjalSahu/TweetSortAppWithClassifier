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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.mopub.volley.RequestQueue;
import com.mopub.volley.toolbox.ImageLoader;
import com.mopub.volley.toolbox.Volley;
import com.social.solution.HelperFunctions;
import com.social.solution.R;
import com.social.solution.unused.ShowImage;
import com.social.solution.others.SquareImageView;
import com.social.solution.others.TweetBank;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

public class MyImageFragment extends BaseFragment {
    //List<String> imageUrls;
    List<Tweet> imageTweets;
    ImageAdapter imageAdapter;
    ImageAdapterHorizontal imageAdapterHorizontal;

    Activity storedActivity;
    LayoutInflater mInflater;

    boolean loading        = false;
    private RequestQueue mRequestQueue;
    private ImageLoader  mImageLoader;

    long lastTimeStamp;

    View storedView;
    Activity parentActivity;

    SwipeRefreshLayout mSwipeLayout;
    ObservableListView listView;

    public static final String ARG_INITIAL_POSITION = "ARG_INITIAL_POSITION";

    void setmydata(ListView listView, View headerView){
        listView.addHeaderView(headerView);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        storedActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View view  = inflater.inflate(R.layout.image_list, container, false);
        View view  = inflater.inflate(R.layout.image_list, container, false);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeLayout.setProgressViewOffset(false, 150, 200);

        storedView     = view;

        parentActivity = getActivity();
        mInflater      = LayoutInflater.from(parentActivity);

        //imageUrls = TweetBank.getAllImageUrls();
        imageTweets =  TweetBank.getAllNewsTweets();

        mRequestQueue = Volley.newRequestQueue(parentActivity);
        mImageLoader  = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });

        listView = (ObservableListView) view.findViewById(R.id.mylist);

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

        View newsrowslist         = (View)inflater.inflate(R.layout.newsrowslistlayout, listView, false);

        newsrowslist.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println("pranjal danger");
                return false;
            }
        });

        imageAdapter              = new ImageAdapter(parentActivity);
        imageAdapterHorizontal    = new ImageAdapterHorizontal(parentActivity);

        setmydata(listView, inflater.inflate(R.layout.padding, listView, false));

        listView.setAdapter(imageAdapterHorizontal);

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //System.out.println("hello pranjal how are you");
                return false;
            }
        });

        /*
        View horizontalView  = null;
        ViewGroup imageviews = null;
        int count            = 0;

        for (Tweet t : imageTweets) {
            if(count == 0) {
                //horizontalView = inflater.inflate(R.layout.myhorizontalscrollviewa, null);//, true);
                horizontalView = inflater.inflate(R.layout.myhorizontalscrollviewa, container, false);
                TextView    tv = new TextView(parentActivity);
                tv.setText("POLITICS");
                newsrowslist.addView(tv);
                newsrowslist.addView(horizontalView);
                imageviews     = (ViewGroup) horizontalView.findViewById(R.id.imageviews);
            }

            ++count;
            count = count%5;

            //final View v = mInflater.inflate(R.layout.new_grid_item, null);
            final View v = mInflater.inflate(R.layout.new_grid_item, container, false);
            final SquareImageView picture = (SquareImageView) v.findViewById(R.id.picture);
            final TextView name = (TextView) v.findViewById(R.id.picturetext);

            name.setTag(0);
            name.setTag(R.id.action0, name.getTop());

            name.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int flag = (int) v.getTag();
                    Layout t = name.getLayout();

                    int left = name.getLeft();
                    int right = name.getRight();
                    int bottom = name.getBottom();

                    if (flag == 0) {
                        //name.setMaxLines(5);
                        System.out.println("pranjal LAYOUT 0 width = " + t.getWidth() + " height " + t.getHeight());
                        int top = 0;
                        // l t r b
                        name.layout(left, top, right, picture.getBottom());
                        name.setBackgroundColor(Color.parseColor("#FF000000"));
                        name.setGravity(Gravity.NO_GRAVITY);
                        name.setTag(1);
                    } else {
                        //name.setMaxLines(2);
                        System.out.println("pranjal LAYOUT 1 width = " + t.getWidth() + " height " + t.getHeight());
                        int top = (int) name.getTag(R.id.action0);

                        System.out.println("new top position is " + top);

                        // l t r b
                        name.layout(left, picture.getBottom() - 40, right, picture.getBottom());
                        name.setGravity(Gravity.NO_GRAVITY);
                        name.setTag(0);
                    }
                    return false;
                }
            });

            picture.setImageUrl(t.entities.media.get(0).mediaUrl, mImageLoader);
            String temp  = t.text;
            temp = temp+"\n";
            String temp1 = temp.replaceAll("http.*?\\s", " ").replaceAll("http.*?\\n", "");
            System.out.println("STRING1 "+temp+ " STRING2 "+temp1);
            name.setText(Html.fromHtml("<b>@" + t.user.screenName + "</b><br>" + temp1));
            imageviews.addView(v);
        }*/
        return view;
    }


    public class ImageAdapter extends BaseAdapter {
        private Context localContext;
        private final LayoutInflater mInflater;

        ImageAdapter(Context ct){
            this.localContext = ct;
            mInflater = LayoutInflater.from(localContext);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int getCount() {
            //return 0; //imageTweets.size();
            return imageTweets.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //final SquareImageView imageView;
            View v;
            SquareImageView picture;
            TextView name;

            if (convertView == null) {
                //LayoutInflater inflater = LayoutInflater.from(storedActivity);
                //imageView  = new SquareImageView(storedActivity);

                v = mInflater.inflate(R.layout.new_grid_item, parent, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.picturetext,    v.findViewById(R.id.picturetext));

                //imageView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 400));
                //imageView = (NetworkImageView) storedView.inflate(parentActivity, R.id.networkimageview, null);
                // .inflate(, false);
            } else
                v = convertView;

            picture = (SquareImageView) v.getTag(R.id.picture);
            name    = (TextView) v.getTag(R.id.picturetext);

            //picture.setImageUrl(imageUrls.get(position), mImageLoader);
            picture.setImageUrl(imageTweets.get(position).entities.media.get(0).mediaUrl, mImageLoader);
            name.setText("@" + imageTweets.get(position).user.screenName);

            return v;
        }
    }



    public class ImageAdapterHorizontal extends BaseAdapter {
        private Context localContext;
        private final LayoutInflater mInflater;

        ImageAdapterHorizontal(Context ct){
            this.localContext = ct;
            mInflater = LayoutInflater.from(localContext);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int getCount() {
            return (imageTweets.size())/5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //if(convertView == null){
            LinearLayout lv1 = new LinearLayout(parentActivity, null);

            lv1.setOrientation(LinearLayout.VERTICAL);

            View horizontalView  = mInflater.inflate(R.layout.myhorizontalscrollviewa, parent, false);
            ViewGroup imageViews = (ViewGroup) horizontalView.findViewById(R.id.imageviews);

            TextView heading = new TextView(parentActivity);

            // ltrb
            heading.setPadding(8, 8, 0, 2);
            heading.setTextColor(Color.BLACK);
            heading.setTypeface(null, Typeface.BOLD);
            heading.setTextSize(20);

            lv1.addView(heading);
            lv1.addView(horizontalView);

            switch (position){
                case 0:                 heading.setText("POLITICS");
                    break;
                case 1 :                heading.setText("MUSIC");
                    break;
                case 2:                 heading.setText("SPORTS");
                    break;
                case 3:                 heading.setText("TECHNOLOGY");
                    break;
                case 4:                 heading.setText("CELEBRITIES");
                    break;
                case 5:                 heading.setText("BUSINESS");
                    break;
            }


            int size  = imageTweets.size();
            int start = position*5;
            int end   = start+5;

            while(start < size && start < end) {
                final Tweet t = imageTweets.get(start);

                final View v                  = mInflater.inflate(R.layout.new_grid_item, parent, false);

                v.setClickable(true);

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(ForegroundLayoutActivity.this, R.string.item_pressed, Toast.LENGTH_LONG).show();
                    }
                });

                v.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_UP) {
                            Intent it = new Intent();
                            //it.
                            Intent intent = new Intent(parentActivity, ShowImage.class);
                            intent.putExtra("tweetstring", HelperFunctions.gson.toJson(t));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        return false;
                    }
                });


                final SquareImageView picture = (SquareImageView) v.findViewById(R.id.picture);
                final TextView name           = (TextView) v.findViewById(R.id.picturetext);

                picture.setImageUrl(t.entities.media.get(0).mediaUrl, mImageLoader);
                //name.setText(Html.fromHtml("<b>@" + t.user.screenName + "</b><br>" + t.text));
                String temp  = t.text;
                temp = temp+"\n";
                String temp1 = temp.replaceAll("http.*?\\s", " ").replaceAll("http.*?\\n", "");
                //System.out.println("STRING1 "+temp+ " STRING2 "+temp1);

                name.setText(Html.fromHtml(temp1));
                imageViews.addView(v);
                ++start;
            }

            return lv1;
            //return horizontalView;
        }
    }
}