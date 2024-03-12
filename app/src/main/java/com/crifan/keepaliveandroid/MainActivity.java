package com.crifan.keepaliveandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;

public class MainActivity extends AppCompatActivity {
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    IntentFilter intentFilter;
    MyReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for A resource failed to call close
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build());

//        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
//        this.registerReceiver(new NetworkMonitoringUtil(getApplicationContext()), intentFilter);
//        Context curCtx = getApplicationContext();
//        Context curCtx = getBaseContext();
//        this.registerReceiver(new NetworkMonitoringUtil(curCtx), intentFilter);
//        curCtx.registerReceiver(new NetworkMonitoringUtil(curCtx), intentFilter);

        intentFilter = new IntentFilter(CONNECTIVITY_ACTION);
        receiver = new MyReceiver();

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    void loadData(){
        // do sth
    }

    void updateUI(){
        // No internet connection, update the ui and warn the user
    }

    // Self explanatory method
    public boolean checkForInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String actionOfIntent = intent.getAction();
            boolean isConnected = checkForInternet();
            if(actionOfIntent.equals(CONNECTIVITY_ACTION)){
                if(isConnected){
                    loadData();
                }else{
                    updateUI();
                }
            }
        }
    }
}