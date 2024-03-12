package com.crifan.keepaliveandroid;

import android.app.Service;
import android.content.Intent;
import android.util.Log;

import com.crifan.keepalive.KeepAliveService;

public class Service1 extends KeepAliveService {
    private static final String TAG = "KeepAliveService1";
    int x = 0;
    private boolean exit = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service1 started");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!exit) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // int x = 8/0;
                    x++;
//                    Log.d("Service1", x + "");
                }
            }
        }).start();

        Integer superStartRet = super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "superStartRet=" + superStartRet);
        return Service.START_STICKY;
    }
}
