package com.jbe.n33o;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.net.InetAddress;

public class ServiceDiscovery extends AppCompatActivity {

    String host, hostName;

    Dialog customDialogIp;
    ImageView saveIp, closepopupIp;
    EditText et_manualIp;

    ImageView iv_autoDiscovery, iv_backToMain, iv_brainfound, iv_errorBrain, iv_neeoBrain;
    TextView tv_brainSearch,tv_result;
    Button bt_startSearch, bt_saveDiscovery;

    Boolean brainFound, brainNotFoundAfterDiscovery, searchStarted;

    Utils mUtils;

    View decorview;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_discovery);

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
        searchStarted = false;

        mUtils = new Utils(ServiceDiscovery.this);

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
        tv_result = findViewById(R.id.tv_result);

        bt_startSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(brainNotFoundAfterDiscovery){
                    // do something
                }
                else{
                    searchStarted = true;
                    mUtils.initializeNsd();
                    tv_brainSearch.setText("Searching...");
                    bt_startSearch.setVisibility(View.INVISIBLE);
                    bt_saveDiscovery.setVisibility(View.VISIBLE);
                    iv_neeoBrain.setVisibility(View.INVISIBLE);
                    iv_errorBrain.setVisibility(View.INVISIBLE);
                    iv_autoDiscovery.setVisibility(View.VISIBLE);
                }
            }
        });

        bt_saveDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUtils.tearDown();
                Intent intent = new Intent (ServiceDiscovery.this, MainActivity.class);
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("My_settings", host);
                editor.putString("Brain_Name", hostName);
                editor.apply();
                startActivity(intent);

            }
        });

        iv_backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUtils.tearDown();
                Intent intent = new Intent (ServiceDiscovery.this, MainActivity.class);
                startActivity(intent);
            }
        });







    } // End of onCreate


    // START OF FINDING THE BRAIN USING NETWORK SERVICES DISCOVERY
    class Utils {

        Context mContext;

        NsdManager mNsdManager;
        NsdManager.ResolveListener mResolveListener;
        NsdManager.DiscoveryListener mDiscoveryListener;

        public static final String SERVICE_TYPE = "_neeo._tcp.";

        public static final String TAG = "NsdHelper";
        public String mServiceName = "Plug_Service";

        NsdServiceInfo mService;


        public Utils(Context context) {
            mContext = context;
            mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        }

        public void initializeNsd() {
            Log.d(TAG, "Got this far...");
            initializeResolveListener();
            initializeDiscoveryListener();
            discoverServices();
        }

        public void initializeDiscoveryListener() {
            mDiscoveryListener = new NsdManager.DiscoveryListener() {

                @Override
                public void onDiscoveryStarted(String regType) {
                    Log.d(TAG, "Service discovery started");
                }

                @Override
                public void onServiceFound(NsdServiceInfo service) {
                    Log.d(TAG, "Service discovery success" + service);
                    if (!service.getServiceType().equals(SERVICE_TYPE)) {
                        Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                    }
                    else {
                        Log.d(TAG, "J'ai ça sur le réseau sous _neeo._tcp : " + service.getServiceName());
                        mNsdManager.resolveService(service, mResolveListener);
                    }

                }

                @Override
                public void onServiceLost(NsdServiceInfo service) {
                    Log.e(TAG, "service lost" + service);
                    if (mService == service) {
                        mService = null;
                    }
                }

                @Override
                public void onDiscoveryStopped(String serviceType) {
                    Log.i(TAG, "Discovery stopped: " + serviceType);
                }

                @Override
                public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    mNsdManager.stopServiceDiscovery(this);
                }

                @Override
                public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    mNsdManager.stopServiceDiscovery(this);
                }
            };
        }

        public void initializeResolveListener() {
            mResolveListener = new NsdManager.ResolveListener() {

                @Override
                public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    Log.e(TAG, "Resolve failed" + errorCode);
                }

                @Override
                public void onServiceResolved(NsdServiceInfo serviceInfo) {
                    Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                    if (serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                        Log.d(TAG, "Same Service Type.");
                        return;
                    }
                    mService = serviceInfo;
                    int port = mService.getPort();
                    InetAddress hostAddress = mService.getHost();
                    host = hostAddress.getHostAddress();
                    hostName = mService.getServiceName();
                    Log.d(TAG, "Port from resolve : " + port);
                    Log.d(TAG, "Host from resolve : " + host);
                    new Handler(Looper.getMainLooper()).post(new Runnable(){
                        @Override
                        public void run() {
                            tv_brainSearch.setVisibility(View.INVISIBLE);
                            iv_autoDiscovery.setVisibility(View.INVISIBLE);
                            iv_brainfound.setVisibility(View.VISIBLE);
                            bt_saveDiscovery.setVisibility(View.VISIBLE);
                            bt_saveDiscovery.isClickable();
                            bt_saveDiscovery.setText("Save Settings");

                            tv_result.setVisibility(View.VISIBLE);
                            tv_result.setText("BRAIN FOUND \n" + "Name : " + hostName + "\n" + "IP : " + host);
                        }
                    });

                }

            };
        }
        public void discoverServices() {
            Log.d(TAG, "Got to discoverServices");
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }

        // NsdHelper's tearDown method
        public void tearDown() {
            if(!searchStarted){
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            }
            else{
                // do nothing to avoid crashing the app, because the mDiscoveryListener
                // was never started in the first place...
            }

        }



    }  // End of Utils Class
// END OF FINDING THE BRAIN BY NETWORK SERVICES DISCOVERY





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
                Intent intent = new Intent (ServiceDiscovery.this, MainActivity.class);
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






} // End of ServiceDiscovery