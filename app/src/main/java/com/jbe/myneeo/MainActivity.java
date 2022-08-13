package com.jbe.myneeo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    TextView tvSettings;
    String brainIpForUrl;
    private WebView webView;

    private Menu menu;

    Boolean settingsExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSettings = findViewById(R.id.resultsettings);
        webView = findViewById(R.id.webView);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new MyWebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        //webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setUserAgentString("Custom_UA");

        // until proved otherwise
        settingsExist = false;

        //loading settings if they exist
        loadSettings();

        // no settings ? load the dialog
        if (!settingsExist) {
            dialogSettings();
        }

        // Settings ? Great, apply them to the webView
        else {
            //just for test purposes
            //webView.loadUrl(String.valueOf(tvSettings.getText()));

            // this loads the saved settings and sends to the brain UI
            webView.loadUrl(brainIpForUrl);
        }

    } // end onCreate



    @Override
    public void onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack();
        }
        else{
           super.onBackPressed();
        }

    }

    private void dialogSettings(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText et_settings = new EditText(this);
        et_settings.setHint("192.168.X.X");
        alert.setTitle("Enter Your Brain IP");
        alert.setMessage("\n");

        alert.setView(et_settings);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String brainIp = et_settings.getText().toString();

                tvSettings.setText(brainIp);
                setSettings(brainIp);

                // just for test purposes
                //webView.loadUrl(brainIp);

                // this sends to the brain UI
                webView.loadUrl("http://"+ brainIp + ":3200/eui/");
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Nothing to do
            }
        });

        alert.show();

    } // end dialogsettings

    private void loadSettings(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String brainSettings = prefs.getString("My_settings", "");
        setSettings(brainSettings);
        if (brainSettings.length() == 0){
            settingsExist = false;
        }
        else {
            settingsExist = true;
        }
        brainIpForUrl = "http://"+ brainSettings + ":3200/eui";
        // just to check...
        tvSettings.setText(brainSettings);
    } // end loadSettings

    private void setSettings(String sets){
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_settings", sets);
        editor.apply();
    } // end setSettings

// Settings menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menuSettings){
            dialogSettings();
        }


        return super.onOptionsItemSelected(item);
    }

} // end MainActivity