package com.jbe.n33o;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DiscoveryActivity extends AppCompatActivity {

    Thread thread;
    String finalBrainIp;

    Dialog customDialogIp;
    ImageView saveIp, closepopupIp;
    EditText et_manualIp;

    ImageView iv_autoDiscovery, iv_backToMain, iv_brainfound, iv_errorBrain, iv_neeoBrain;
    TextView tv_brainSearch;
    Button bt_startSearch, bt_saveDiscovery;

    Boolean brainFound, brainNotFoundAfterDiscovery;

    View decorview;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        decorview = getWindow().getDecorView();
        // Hiding nav and settings bar
        decorview.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        brainFound = false;
        brainNotFoundAfterDiscovery = false;

        iv_neeoBrain = findViewById(R.id.iv_neeoBrain);
        iv_autoDiscovery = findViewById(R.id.iv_autoDiscovery);
        Glide.with(this).load(R.drawable.discovery).into(iv_autoDiscovery);
        iv_brainfound = findViewById(R.id.iv_brainFound);
        Glide.with(this).load(R.drawable.brainfoundgif).into(iv_brainfound);
        iv_errorBrain = findViewById(R.id.iv_errorBrain);
        Glide.with(this).load(R.drawable.errorbrain).into(iv_errorBrain);
        tv_brainSearch = findViewById(R.id.tv_brainSearch);
        bt_saveDiscovery = findViewById(R.id.bt_saveDiscovery);
        bt_startSearch = findViewById(R.id.bt_startSearch);
        iv_backToMain = findViewById(R.id.iv_backToMain);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startPingService(DiscoveryActivity.this);
                if(brainFound){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_brainSearch.setText("Brain was found at this IP : \n" + finalBrainIp);
                            bt_saveDiscovery.setText("Save to settings");
                            iv_autoDiscovery.setVisibility(View.INVISIBLE);
                            iv_brainfound.setVisibility(View.VISIBLE);
                            bt_saveDiscovery.setVisibility(View.VISIBLE);
                            thread.interrupt();
                        }
                    });
                }
                if(brainNotFoundAfterDiscovery){
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            tv_brainSearch.setText("The Brain could not be found");
                            iv_autoDiscovery.setVisibility(View.INVISIBLE);
                            iv_errorBrain.setVisibility(View.VISIBLE);
                            bt_startSearch.setVisibility(View.VISIBLE);
                            bt_startSearch.setText("Reload and try again");
                            thread.interrupt();
                        }
                    });

                }
            }
        });

        bt_saveDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (DiscoveryActivity.this, MainActivity.class);
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("My_settings", finalBrainIp);
                editor.apply();
                startActivity(intent);
            }
        });

        bt_startSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(brainNotFoundAfterDiscovery){
                    Intent intent = new Intent (DiscoveryActivity.this, DiscoveryActivity.class);
                    startActivity(intent);
                }
                else{
                    thread.start();
                    tv_brainSearch.setText("Searching...");
                    bt_startSearch.setVisibility(View.INVISIBLE);
                    iv_neeoBrain.setVisibility(View.INVISIBLE);
                    iv_errorBrain.setVisibility(View.INVISIBLE);
                    iv_autoDiscovery.setVisibility(View.VISIBLE);
                }
            }
        });

        iv_backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thread.interrupt();
                Intent intent = new Intent (DiscoveryActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    } // end onCreate



    public static boolean isPortOpen(final String ip, final int port, final int timeout) {

        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        }

        catch(ConnectException ce){
            ce.printStackTrace();
            return false;
        }

        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public  void startPingService(Context context)
    {
        try {

            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            String subnet = getSubnetAddress(mWifiManager.getDhcpInfo().gateway);

            // Looking at all the IPs
            for (int i=1;i<255;i++){

                String host = subnet + "." + i;

                if (InetAddress.getByName(host).isReachable(100)){
                    if(isPortOpen(String.valueOf(host), 3200, 100)){
                        finalBrainIp = String.valueOf(host);
                        brainFound = true;
                        break;
                    }
                    else{
                        //keep going
                    }

                }
                if (i == 254){
                    brainNotFoundAfterDiscovery = true;
                }
                else
                {
                    Log.e("DeviceDiscovery", "❌ Not Reachable Host: " + String.valueOf(host));
                }
            }


        }
        catch(Exception e){
            System.out.println(e);
        }

    }

    private String getSubnetAddress(int address)
    {
        String ipString = String.format(
                "%d.%d.%d",
                (address & 0xff),
                (address >> 8 & 0xff),
                (address >> 16 & 0xff));

        return ipString;
    }

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

        et_manualIp.setHint("192.168.X.X");

        saveIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String brainIp = et_manualIp.getText().toString();
                setSettings(brainIp);
                Intent intent = new Intent (DiscoveryActivity.this, MainActivity.class);
                startActivity(intent);
                customDialogIp.dismiss();
            }
        });

        customDialogIp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialogIp.show();

    } // end dialogsettings

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




} // end DiscoveryActivity
