package com.crifan.keepalive;

import android.util.Log;

/* package */class NativeKeepAlive {
    private static final String TAG = "NativeKeepAlive";

    static {
        try {
            System.loadLibrary("keep_alive");
            Log.d(TAG, "Loaded library keep_alive");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public native void doDaemon(String indicatorSelfPath, String indicatorDaemonPath, String observerSelfPath, String observerDaemonPath, String packageName, String serviceName, int sdkVersion);

    public native void test(String packageName, String serviceName, int sdkVersion);

    public native void empty();

    public void onDaemonDead() {
        IKeepAliveProcess.Fetcher.fetchStrategy().onDaemonDead();
    }
}
