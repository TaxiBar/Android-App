package com.example.gjen.taxibar;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CommentWriteActivity extends AppCompatActivity {
    WebView webView;
    String plateNum = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_write);
        getBundle();
        webView = (WebView)findViewById(R.id.webview2);
        webView.setWebViewClient(mWebViewClient);
        webView.loadUrl("http://140.134.26.71:9990/taxibar/GiveComment.jsp?plateNumber=" + plateNum);
    }

    private void getBundle(){
        Bundle bundle = this.getIntent().getExtras();
        plateNum = bundle.getString("PlateNum");
        Log.d("abc", "plateNum : " + plateNum);
    }

    WebViewClient mWebViewClient = new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    };
}
