package com.crifan.keepalive;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class KeepAliveService extends Service {
    private static final String TAG = "KeepAliveService";

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return new Binder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
//        return Service.START_NOT_STICKY;
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();
        bindDaemonService();
    }

    private IBinder binder;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = iBinder;
            try {
                iBinder.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        public void binderDied() {
            if (binder != null) {
                binder.unlinkToDeath(this, 0);
                binder = null;
            }
            bindDaemonService();
        }
    };

    protected void bindDaemonService() {
        Log.d(TAG, "bindDaemonService");

        KeepAliveConfigs configs;
        if (KeepAlive.client != null && KeepAlive.client.mConfigurations != null) {
            String processName = KeepAlive.getProcessName();
            configs = KeepAlive.client.mConfigurations;

            if (processName == null) {
                return;
            }
            if (processName.startsWith(configs.PERSISTENT_CONFIG.processName)) {
                Intent intent = new Intent();
                ComponentName component = new ComponentName(getPackageName(), configs.DAEMON_ASSISTANT_CONFIG.serviceName);
                intent.setComponent(component);
                bindService(intent, conn, BIND_AUTO_CREATE);
            } else if (processName.startsWith(configs.DAEMON_ASSISTANT_CONFIG.processName)) {
                Intent intent = new Intent();
                ComponentName component = new ComponentName(getPackageName(), configs.PERSISTENT_CONFIG.serviceName);
                intent.setComponent(component);
                bindService(intent, conn, BIND_AUTO_CREATE);
            }
        }
    }
}
