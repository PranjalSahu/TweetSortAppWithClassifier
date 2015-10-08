package com.social.solution.others;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

import retrofit.http.GET;
import retrofit.http.Query;


/**
 * Created by pranjal on 16/05/15.
 */
public class MyTwitterApiClient extends TwitterApiClient {
    public MyTwitterApiClient(TwitterSession session) {
        super(session);
    }

    public CustomService getCustomService() {
        return getService(CustomService.class);
    }

}

interface CustomService {

      @GET("/1.1/followers/ids.json")
      void show(@Query("screen_name") String var, Callback<Object> cb);


    //@GET("/1.1/users/show.json")
    //void show(@Query("screen_name") String var, Callback<User> cb);

    //@GET("/1.1/followers/list.json")
    //void show(@Query("screen_name")  String var, @Query("skip_status") Boolean var1, @Query("include_user_entities") Boolean var2, @Query("count") Integer var3, Callback<User> cb);
}