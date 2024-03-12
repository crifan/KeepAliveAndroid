package com.crifan.keepalive;

import android.content.Context;
import android.content.Intent;

public class KeepAliveConfigs {

    final Config PERSISTENT_CONFIG;
    final Config DAEMON_ASSISTANT_CONFIG;
    boolean ignoreOptimization = false;
    int rebootIntervalMs = 5000; // 10s
    int rebootMaxTimes = 3;
    boolean limitReboot = false;

    static OnBootReceivedListener bootReceivedListener;

    public KeepAliveConfigs(Config persistentConfig, Config daemonAssistantConfig) {
        this.PERSISTENT_CONFIG = persistentConfig;
        this.DAEMON_ASSISTANT_CONFIG = daemonAssistantConfig;
    }

    public KeepAliveConfigs(Config persistentConfig) {
        this.PERSISTENT_CONFIG = persistentConfig;
//        String daemonProcessName = "com.crifan.keepaliveandroid:daemon";
        String daemonProcessName = "com.crifan.keepaliveandroid.daemon";
//        String daemonProcessName = "com.crifan.keepaliveandroid:dae";
        this.DAEMON_ASSISTANT_CONFIG = new Config(daemonProcessName, KeepAliveService.class.getCanonicalName());
    }

    public KeepAliveConfigs ignoreBatteryOptimization() {
        ignoreOptimization = true;
        return this;
    }

    public KeepAliveConfigs rebootThreshold(int rebootIntervalMs, int rebootMaxTimes) {
        limitReboot = true;
        this.rebootIntervalMs = rebootIntervalMs;
        this.rebootMaxTimes = rebootMaxTimes;
        return this;
    }

    public KeepAliveConfigs setOnBootReceivedListener(OnBootReceivedListener listener) {
        bootReceivedListener = listener;
        return this;
    }

    public static class Config {

        final String processName;
        final String serviceName;

        public Config(String processName, String serviceName) {
            this.processName = processName;
            this.serviceName = serviceName;
        }
    }

    public interface OnBootReceivedListener {
        void onReceive(Context context, Intent intent);
    }
}
