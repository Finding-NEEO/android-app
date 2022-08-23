package com.jbe.n33o;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;


public class MainActivity extends AppCompatActivity {

    TextView tvSettings, tv_popupStartup;
    String brainIpForUrl;
    private WebView webView;

    Dialog customDialog, customDialogIp;
    ImageView applogopopup, discoverypopup, manualippopup, closepopup, saveIp, closepopupIp;
    EditText et_manualIp;

    View decorview;

    private Menu menu;

    Boolean settingsExist, navBarVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // showing the N33O logo in the menu bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.titlelogo);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        decorview = getWindow().getDecorView();
        // Hiding nav and settings bar
        decorview.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        customDialog = new Dialog(this);
        customDialogIp = new Dialog(this);


        tvSettings = findViewById(R.id.resultsettings);
        webView = findViewById(R.id.webView);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new MyWebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setUserAgentString("Custom_UA");

        // until proved otherwise
        settingsExist = false;
        navBarVisible = false;

        //loading settings if they exist
        loadSettings();

        // no settings ? load the dialog
        if (!settingsExist) {
            firstDialogSettings();
        }
        // Settings ? Great, apply them to the webView
        else {
            // this loads the saved settings and sends to the brain UI
            webView.loadUrl(brainIpForUrl);
            }
    } // end onCreate


    private void firstDialogSettings() {
        // defining popup images
        customDialog.setContentView(R.layout.custompopup);
        applogopopup = customDialog.findViewById(R.id.iv_logopopup);
        discoverypopup = customDialog.findViewById(R.id.iv_discovery);
        Glide.with(this).load(R.drawable.discovery).into(discoverypopup);
        manualippopup = customDialog.findViewById(R.id.iv_manual);
        Glide.with(this).load(R.drawable.manualip).into(manualippopup);
        closepopup = customDialog.findViewById(R.id.iv_closepopup);
        tv_popupStartup = customDialog.findViewById(R.id.tv_popupStartup);

        if(settingsExist){
            tv_popupStartup.setText("What do you want to do ?");
        }

        closepopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog.dismiss();
            }
        });

        manualippopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSettings();
                customDialog.dismiss();
            }
        });

        discoverypopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DiscoveryActivity.class);
                startActivity(intent);

            }
        });

        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.show();

    } // end firstDialogSettings

    private void dialogSettings(){
        customDialogIp.setContentView(R.layout.custompopupip);
        saveIp = customDialogIp.findViewById(R.id.iv_saveIp);
        Glide.with(this).load(R.drawable.saveipgif).into(saveIp);
        closepopupIp = customDialogIp.findViewById(R.id.iv_closepopupIp);
        et_manualIp = customDialogIp.findViewById(R.id.et_manualIp);

        closepopupIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialogIp.dismiss();
            }
        });

        if(settingsExist){
            SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
            String brainSettings = prefs.getString("My_settings", "");
            et_manualIp.setText(brainSettings);
        }
        else {
           et_manualIp.setHint("192.168.X.X");
        }

        saveIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String brainIp = et_manualIp.getText().toString();
                setSettings(brainIp);
                webView.loadUrl("http://" + brainIp + ":3200/eui/");
                customDialogIp.dismiss();
            }
        });

        customDialogIp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialogIp.show();

    } // end dialogsettings

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack();
        }
        else{
           super.onBackPressed();
        }

    }

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
            firstDialogSettings();
        }

        if (id == R.id.menuFullscreen){
            decorview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }


        return super.onOptionsItemSelected(item);
    }

} // end MainActivity