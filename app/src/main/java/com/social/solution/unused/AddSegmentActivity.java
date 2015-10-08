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

package com.social.solution.unused;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mopub.volley.RequestQueue;
import com.mopub.volley.toolbox.ImageLoader;
import com.mopub.volley.toolbox.Volley;
import com.social.solution.HelperFunctions;
import com.social.solution.R;
import com.social.solution.activity.BaseActivity;
import com.social.solution.others.SquareImageView;
import com.social.solution.others.UserItem;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;

//import com.crashlytics.android.Crashlytics;


public class AddSegmentActivity extends BaseActivity {

    HashMap<String, String> selectedUsers = new HashMap<String, String>();
    LinearLayout linlaHeaderProgress;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode){
            case 0:
                setResult(0);
                finish();
        }
    }

    private ListView lv;
    UserAdapter adapter;
    EditText inputSearch;

    ArrayList<HashMap<String, String>> productList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addsegmentlayout);

        HelperFunctions.TITLES.add(HelperFunctions.TITLES.size(), "ADDED");

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setBackgroundColor(-1);
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        //setSupportActionBar((Toolbar) findViewById(R.id.toolbara));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();

        lv = (ListView) findViewById(R.id.user_list_view);

        inputSearch = (EditText) findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                AddSegmentActivity.this.adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        adapter = new UserAdapter(this);
        lv.setAdapter(adapter);

        //new LoadUserLists().execute("0", "1");
    }

    public class LoadUserLists extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            ResponseList<UserList> lists = null;
            try {
                System.out.println("pranjalsahulist inside load list");
                lists = HelperFunctions.twitter.getUserLists(HelperFunctions.currentSession.getUserName());
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            for (UserList list : lists) {

                System.out.println("pranjalsahulist id:" + list.getId() + ", name:" + list.getName() + ", description:"
                        + list.getDescription() + ", slug:" + list.getSlug() + "");
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        ArrayList<String> mylist = new ArrayList<String>();

        Set<String> suser = selectedUsers.keySet();

        if(HelperFunctions.filterList.size() == 0){
            HelperFunctions.filterList.add(0, mylist);
            HelperFunctions.filterList.add(1, mylist);
            HelperFunctions.filterList.add(2, mylist);
        }
        System.out.println("testingpranjal size = \"" + HelperFunctions.filterList.size() + " ****************************************");
        for(String s:suser) {
            System.out.println("testingpranjal " + s);
            mylist.add(s);
        }
        HelperFunctions.filterList.add(HelperFunctions.filterList.size(), mylist);
        System.out.println("testingpranjal +new size is " + HelperFunctions.filterList.size() + " ****************************************");
    }

    public class UserAdapter extends BaseAdapter implements Filterable {
        private Context localContext;
        private final LayoutInflater mInflater;

        private RequestQueue mRequestQueue;
        private ImageLoader  mImageLoader;

        private ArrayList<UserItem> filteredfriends  = new ArrayList<UserItem>();
        private ArrayList<UserItem> originalfriends  = new ArrayList<UserItem>();

        @Override
        public Filter getFilter() {
            return new Filter() {
                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredfriends = (ArrayList<UserItem>) results.values;
                    notifyDataSetChanged();
                }

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String filterString   = constraint.toString().toLowerCase();

                    FilterResults results = new FilterResults();
                    final List<UserItem> list = originalfriends;

                    int count                   = list.size();
                    final ArrayList<UserItem> nlist = new ArrayList<UserItem>(count);

                    String filterableString;

                    for (int i = 0; i < count; i++) {
                        UserItem temp = list.get(i);
                        if(temp.user.getName().toLowerCase().contains(filterString)){
                            nlist.add(temp);
                            //System.out.println("Adding User " + temp.getName());
                        }
                    }

                    results.values = nlist;
                    results.count  = nlist.size();
                    return results;
                }
            };
        }

        UserAdapter(Context ct){
            this.localContext = ct;
            mInflater = LayoutInflater.from(localContext);

            mRequestQueue = Volley.newRequestQueue(ct);
            mImageLoader  = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
                private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
                public void putBitmap(String url, Bitmap bitmap) {
                    mCache.put(url, bitmap);
                }
                public Bitmap getBitmap(String url) {
                    return mCache.get(url);
                }
            });

            for(int i=0;i<HelperFunctions.friends.size();++i){
                UserItem ui = new UserItem();
                ui.user     = HelperFunctions.friends.get(i);
                originalfriends.add(i, ui);
                filteredfriends.add(i, ui);

            }
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int getCount() {
            return filteredfriends.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredfriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            SquareImageView picture;
            TextView name;
            TextView description;
            CheckBox checkbox = null;
            final UserItem useritem = (UserItem)this.getItem(position);

            if (convertView == null) {
                v = mInflater.inflate(R.layout.user_item_twitter, parent, false);
                v.setTag(R.id.userpicture,  v.findViewById(R.id.userpicture));
                v.setTag(R.id.twitterusername, v.findViewById(R.id.twitterusername));
                v.setTag(R.id.twitteruserdescription, v.findViewById(R.id.twitteruserdescription));
                v.setTag(R.id.checkBox1, v.findViewById(R.id.checkBox1));
            } else {
                v = convertView;
            }

            picture        = (SquareImageView) v.getTag(R.id.userpicture);
            name           = (TextView) v.getTag(R.id.twitterusername);
            description    = (TextView) v.getTag(R.id.twitteruserdescription);
            checkbox       = (CheckBox) v.getTag(R.id.checkBox1);

            checkbox = (CheckBox)v.findViewById(R.id.checkBox1);
            checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb       = (CheckBox) v;
                    useritem.selected = cb.isChecked();
                    if(cb.isChecked())
                        selectedUsers.put(useritem.user.getName(), "true");
                    else if(selectedUsers.get(useritem.user.getName()) != null){
                        selectedUsers.remove(useritem.user.getName());
                    }
                }
            });

            picture.setImageUrl(useritem.user.getBiggerProfileImageURL(), mImageLoader);
            name.setText(useritem.user.getName());
            description.setText(useritem.user.getDescription());
            checkbox.setChecked(useritem.selected);
            return v;
        }
    }
}
