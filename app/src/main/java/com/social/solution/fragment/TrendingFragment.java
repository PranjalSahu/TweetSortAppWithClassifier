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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.social.solution.HelperFunctions;
import com.social.solution.unused.MyApplication;
import com.social.solution.R;
import com.social.solution.activity.TrendingTweetsActivity;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.services.AccountService;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.TweetUi;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import twitter4j.ResponseList;
import twitter4j.Trend;
import twitter4j.Trends;

public class TrendingFragment extends BaseFragment {

    private View mHeaderView;
    private View mToolbarView;
    private int  mBaseTranslationY;

    public static final String ARG_INITIAL_POSITION = "ARG_INITIAL_POSITION";

    MyApplication appState;
    TwitterSession ts = null;
    LinearLayout linlaHeaderProgress;

    long lastTimeStamp;
    int currentState = 0;

    boolean           loading      = false;
    boolean           downloading = false;
    SharedPreferences prefs          = null;
    ProgressBar headerProgress       = null;
    LinearLayout myLayout;

    ObservableListView listView;
    Context baseContext;

    Activity storedActivity;
    Activity parentActivity;

    AbsListView.OnScrollListener listenerObject = null;

    ArrayList<String> trends;

    double latitude;
    double longitude;

    ArrayAdapter<String> adapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        storedActivity = activity;

        //LocationManager lm = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
        //Location location  = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //longitude          = location.getLongitude();
        //latitude           = location.getLatitude();

        LoadTrends();
    }

    public void LoadTrends() {
        downloading = true;
        loading     = true;
        new LoadTrends().execute("0", "1");
    }

    public void notifydata(ArrayList<String> trends){
        adapter.addAll(trends);
        adapter.notifyDataSetChanged();
    }

    public class LoadTrends extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            System.out.println("inside loadrecent status");
            int woeid = 1;

            try {
                ResponseList<twitter4j.Location> locations;
                trends = new ArrayList<String>();
                locations = HelperFunctions.twitter.getAvailableTrends();
                System.out.println("Showing available trends");
                for (twitter4j.Location location : locations) {
                    System.out.println(location.getName() + " (woeid:" + location.getWoeid() + ")");
                    if(location.getName().equalsIgnoreCase("india")) {
                        woeid = location.getWoeid();
                        break;
                    }
                }

                Trends newtrends = HelperFunctions.twitter.getPlaceTrends(woeid);
                Trend[] newtrend = newtrends.getTrends();

                for(Trend tr: newtrend){
                    //trends.add(tr.getName()+" Query: "+tr.getQuery()+" URL: "+tr.getURL());
                    trends.add(tr.getName());
                }
            } catch (twitter4j.TwitterException e) {
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
            notifydata(trends);
            linlaHeaderProgress.setVisibility(View.GONE);
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

        parentActivity = getActivity();
        Fabric.with(getActivity(), new TweetUi());

        Bundle bd = getArguments();

        linlaHeaderProgress = (LinearLayout) view.findViewById(R.id.linlaHeaderProgress);
        listView            = (ObservableListView) view.findViewById(R.id.mylist);

        setmydata(listView, inflater.inflate(R.layout.padding, listView, false));

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
                        listView.setSelection(initialPosition);
                    }
                });
            }
            listView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.root));
            listView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

        adapter = new ArrayAdapter<String>(parentActivity.getBaseContext(), R.layout.my_simple_list_item);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(parentActivity, TrendingTweetsActivity.class);
                intent.putExtra("query", trends.get(position-1));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                parentActivity.overridePendingTransition(R.anim.animation_entry_right, R.anim.animation_exit_left);

                //TextView tv = (TextView)view.findViewById(android.R.id.text1);
                //Toast.makeText(parentActivity.getBaseContext(), tv.getText()+" "+trends.get(position-1), Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}
