package com.jbe.myneeo;

import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {



    @Override
    public void onPageFinished(WebView view, String url) {

        super.onPageFinished(view, url);

        String script = "(function() { return 'JavaScript executed successfully.'; })();";

        view.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d("output", value);
            }
        });
    }

}
