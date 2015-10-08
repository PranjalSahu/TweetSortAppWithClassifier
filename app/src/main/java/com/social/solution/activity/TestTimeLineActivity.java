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

package com.social.solution.activity;

import android.app.ListActivity;
import android.os.Bundle;

import com.social.solution.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;


public class TestTimeLineActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline);

        final UserTimeline userTimeline = new UserTimeline.Builder()
                .screenName("fabric")
                .build();
        //TweetTimelineListAdapter.

        //TweetTimelineListAdapter.
        //TweetTimelineListAdapter tt =new TweetTimelineListAdapter();
        final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter(this, userTimeline);
                //.setTimeline(userTimeline)
                //.build();

        //adapter.set
        final Callback<TimelineResult<Tweet>> callback = new Callback<TimelineResult<Tweet>>() {
            @Override
            public void success(Result<TimelineResult<Tweet>> result) {

            }

            @Override
            public void failure(TwitterException e) {

            }
        };

        //userTimeline.next(null, callback);
        //adapter.
        //userTimeline.
        setListAdapter(adapter);
    }
}