package com.crifan.keepaliveandroid;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crifan.keepalive.KeepAlive;
import com.crifan.keepalive.KeepAliveConfigs;
import com.crifan.keepalive.KeepAliveConfigs.Config;

public class MainApplication extends Application {
    private static final String TAG = "MainApplication";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG, "attachBaseContext");
        String processName = getPackageName() + ":resident"; // com.crifan.keepaliveandroid:resident
        String serviceName = Service1.class.getCanonicalName(); // com.crifan.keepaliveandroid.Service1
        Config newCfg =  new KeepAliveConfigs.Config(processName, serviceName);
        KeepAliveConfigs configs = new KeepAliveConfigs(newCfg);

        configs.ignoreBatteryOptimization();
        // configs.rebootThreshold(10*1000, 3);
        configs.setOnBootReceivedListener(new KeepAliveConfigs.OnBootReceivedListener() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive boot");
                // 设置服务自启
                context.startService(new Intent(context, Service1.class));
            }
        });
        KeepAlive.init(base, configs);
    }
}