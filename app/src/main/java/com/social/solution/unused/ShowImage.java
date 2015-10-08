package com.social.solution.unused;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.social.solution.HelperFunctions;
import com.social.solution.R;
import com.twitter.sdk.android.core.models.Tweet;


public class ShowImage extends Activity {
    LinearLayout linlaHeaderProgress;
    LinearLayout tweetLayout;
    //ListView tweetLayout;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_webview);

        Intent intent = getIntent();
        String tweetString = (String) intent.getCharSequenceExtra("tweetstring");
        //int tweetId = intent.getIntExtra("userid", 1);
        //System.out.println("pranjalLONG ID is " + tweetId);

        Tweet t1          = HelperFunctions.gson.fromJson(tweetString, Tweet.class);

        //Instantiating WebView instance
        WebView webView = (WebView) findViewById(R.id.webView);
//        String customHtml = "<html><body><h1>Hello, WebView</h1>" +
//                "<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>" +
//                "<p>This is a sample paragraph.</p>" +
//                "</body></html>";
//        webView.loadData(customHtml, "text/html", "UTF-8");


        webView.setWebViewClient(new MyWebViewClient());
        String url = null;

        if(t1.entities != null && t1.entities.urls != null && t1.entities.urls.size() > 0) {
            url = t1.entities.urls.get(0).url;
            System.out.println("YOYO HONEY SINGH");
        }
        else {
            url = "http://javatechig.com";
        }
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //activity.setTitle("Loading...");
                //activity.setProgress(progress * 100);

                //if (progress == 100)
                //    activity.setTitle(R.string.app_name);
            }
        });

        webView.loadUrl(url);



        /*final Activity activity = this;
        final Window w = activity.getWindow();

        final View content = activity.findViewById(android.R.id.content).getRootView();
        if (content.getWidth() > 0) {
            Bitmap image = BlurBuilder.blur(content);
            w.setBackgroundDrawable(new BitmapDrawable(activity.getResources(), image));
        } else {
            content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Bitmap image = BlurBuilder.blur(content);
                    w.setBackgroundDrawable(new BitmapDrawable(activity.getResources(), image));
                }
            });
        }
        */

//        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress1);
//        tweetLayout         = (LinearLayout) findViewById(R.id.tweetlinearlayout);
//
//        linlaHeaderProgress.setBackgroundColor(-1);
//        linlaHeaderProgress.setVisibility(View.VISIBLE);
//
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                                                                    LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

}
