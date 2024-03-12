package com.crifan.keepalive;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class KeepAliveProcessImpl implements IKeepAliveProcess {

    private static final String TAG = "KeepAliveProcessImpl";

    private final static String INDICATOR_DIR_NAME = "indicators";
    private final static String INDICATOR_PERSISTENT_FILENAME = "indicator_p";
    private final static String INDICATOR_DAEMON_ASSISTANT_FILENAME = "indicator_d";
    private final static String OBSERVER_PERSISTENT_FILENAME = "observer_p";
    private final static String OBSERVER_DAEMON_ASSISTANT_FILENAME = "observer_d";

    private IBinder mRemote;
    private Parcel mServiceData;

    private int mPid = Process.myPid();

    private static int serviceTransactCode;
    private static int broadcastTransactCode;
    private static int instrumentationTransactCode;

//    static {
//        switch (Build.VERSION.SDK_INT) {
//            case 26:
//            case 27:
//                serviceTransactCode = 26;
//                break;
//            case 28:
//                serviceTransactCode = 30;
//                break;
//            case 29:
//                serviceTransactCode = 24;
//                break;
//            default:
//                serviceTransactCode = 34;
//                break;
//        }
//    }

    @Override
    public boolean onInit(Context context, KeepAliveConfigs configs) {
        if (configs.ignoreOptimization) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!isIgnoringBatteryOptimizations(context)) {
                    requestIgnoreBatteryOptimizations(context);
                }
            }
        }

        return initIndicatorFiles(context);
    }

    @Override
    public void onPersistentCreate(final Context context, final KeepAliveConfigs configs) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        initAmsBinder();
        initServiceParcel(context, configs.DAEMON_ASSISTANT_CONFIG.serviceName);
        startServiceByAmsBinder();

        Thread t = new Thread() {
            public void run() {
                File indicatorDir = context.getDir(INDICATOR_DIR_NAME, Context.MODE_PRIVATE);

                String indicatorSelfPath = new File(indicatorDir, INDICATOR_PERSISTENT_FILENAME).getAbsolutePath();
                String indicatorDaemonPath = new File(indicatorDir, INDICATOR_DAEMON_ASSISTANT_FILENAME).getAbsolutePath();
                String observerSelfPath = new File(indicatorDir, OBSERVER_PERSISTENT_FILENAME).getAbsolutePath();
                String observerDaemonPath = new File(indicatorDir, OBSERVER_DAEMON_ASSISTANT_FILENAME).getAbsolutePath();
                Log.i(TAG, "onPersistentCreate: indicatorSelfPath=" + indicatorSelfPath);

                new NativeKeepAlive().doDaemon(
                        indicatorSelfPath,
                        indicatorDaemonPath,
                        observerSelfPath,
                        observerDaemonPath,
                        context.getPackageName(),
                        configs.PERSISTENT_CONFIG.serviceName,
                        Build.VERSION.SDK_INT
                        /*serviceTransactCode, getNativePtr(mServiceData)*/);
            }
        };
        t.start();
    }

    @Override
    public void onDaemonAssistantCreate(final Context context, final KeepAliveConfigs configs) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        initAmsBinder();
        initServiceParcel(context, configs.PERSISTENT_CONFIG.serviceName);
        startServiceByAmsBinder();

        Thread t = new Thread() {
            public void run() {
                File indicatorDir = context.getDir(INDICATOR_DIR_NAME, Context.MODE_PRIVATE);

                String indicatorSelfPath = new File(indicatorDir, INDICATOR_DAEMON_ASSISTANT_FILENAME).getAbsolutePath();
                String indicatorDaemonPath = new File(indicatorDir, INDICATOR_PERSISTENT_FILENAME).getAbsolutePath();
                String observerSelfPath = new File(indicatorDir, OBSERVER_DAEMON_ASSISTANT_FILENAME).getAbsolutePath();
                String observerDaemonPath = new File(indicatorDir, OBSERVER_PERSISTENT_FILENAME).getAbsolutePath();
                String packageName = context.getPackageName();
                String serviceName = configs.PERSISTENT_CONFIG.serviceName;
                Integer sdkVersion = Build.VERSION.SDK_INT;
                Log.i(TAG, "onDaemonAssistantCreate: indicatorSelfPath=" + indicatorSelfPath);

                new NativeKeepAlive().doDaemon(
                        indicatorSelfPath,
                        indicatorDaemonPath,
                        observerSelfPath,
                        observerDaemonPath,
                        packageName,
                        serviceName,
                        sdkVersion
                        /*serviceTransactCode, getNativePtr(mServiceData)*/);
            }
        };
        t.start();
    }

    @Override
    public void onDaemonDead() {
        Log.i(TAG, "on daemon dead!");
        if (startServiceByAmsBinder()) {
            int pid = Process.myPid();
            Log.i(TAG, "mPid: " + mPid + " current pid: " + pid);
            Process.killProcess(mPid);
        }
    }

    public final int getTransactCode(String oldStr, String newStr) {
        try {
            Class class0 = Class.forName("android.app.IActivityManager$Stub");
            Field field0 = class0.getDeclaredField(oldStr);
            field0.setAccessible(true);
            int oldStubInt = field0.getInt(class0);
            Log.i(TAG, "getTransactCode: oldStubInt=" + oldStubInt + " for oldStr=" + oldStr);
            return oldStubInt;
        }
        catch(Exception ex_1) {
            try {
                Class class1 = Class.forName("android.app.IActivityManager");
                Field field1 = class1.getDeclaredField(newStr);
                field1.setAccessible(true);
                int newManagerInt = field1.getInt(class1);
                Log.i(TAG, "getTransactCode: newManagerInt=" + newManagerInt + " for newStr=" + newStr);
                return newManagerInt;
            }
            catch(Exception ex_2) {
                return -1;
            }
        }
    }

    private void initCode(){
        serviceTransactCode = this.getTransactCode("TRANSACTION_startService", "START_SERVICE_TRANSACTION");
        Log.i(TAG, "initCode: serviceTransactCode=" + serviceTransactCode);
        broadcastTransactCode = this.getTransactCode("TRANSACTION_broadcastIntent", "BROADCAST_INTENT_TRANSACTION");
        Log.i(TAG, "initCode: broadcastTransactCode=" + broadcastTransactCode);
        instrumentationTransactCode = this.getTransactCode("TRANSACTION_startInstrumentation", "START_INSTRUMENTATION_TRANSACTION");
        Log.i(TAG, "initCode: instrumentationTransactCode=" + instrumentationTransactCode);
    }

    private void initAmsBinder() {
//        Class<?> activityManagerNative;
        try {
//            activityManagerNative = Class.forName("android.app.ActivityManagerNative");
//            Object amn = activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);
//            if (amn == null) {
//                Log.i(TAG, "Failed to get ActivityManagerNative.getDefault");
//                return;
//            } else {
//                Log.i(TAG, "Got ActivityManagerNative.getDefault");
//            }
//            Field mRemoteField = amn.getClass().getDeclaredField("mRemote");
//            mRemoteField.setAccessible(true);
//            mRemote = (IBinder) mRemoteField.get(amn);

            Class clsString = String.class;
            // IBinder b = ServiceManager.getService("activity");
            this.mRemote = (IBinder)Class.forName("android.os.ServiceManager").getMethod("getService", clsString).invoke(null, "activity");
            Log.i(TAG, "this.mRemote=" + this.mRemote);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // when processName dead, we should save time to restart and kill self, don`t take a waste of time to recycle
    private void initServiceParcel(Context context, String serviceName) {
        initCode();

        Intent intent = new Intent();
        ComponentName component = new ComponentName(context.getPackageName(), serviceName);
        intent.setComponent(component);

        Parcel parcel = Parcel.obtain();
        intent.writeToParcel(parcel, 0);

        mServiceData = Parcel.obtain();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Android 8.1 frameworks/base/core/java/android/app/IActivityManager.aidl
             * ComponentName startService(in IApplicationThread caller, in Intent service,
             *    in String resolvedType, boolean requireForeground, in String callingPackage, int userId);
             *
             * frameworks/base/services/core/java/com/android/server/am/ActiveServices.java
             * if (fgRequired) {
             *     final int mode = mAm.mAppOpsService.checkOperation(
             *             AppOpsManager.OP_START_FOREGROUND, r.appInfo.uid, r.packageName);
             *     switch (mode) {
             *         case AppOpsManager.MODE_ALLOWED:
             *         case AppOpsManager.MODE_DEFAULT: // All okay.
             *             break;
             *         case AppOpsManager.MODE_IGNORED:
             *             // Not allowed, fall back to normal start service, failing siliently if background check restricts that.
             *             fgRequired = false;
             *             forceSilentAbort = true;
             *             break;
             *         default:
             *             return new ComponentName("!!", "foreground not allowed as per app op");
             *     }
             * }
             * requireForeground 要求启动service之后，调用service.startForeground()显示一个通知，不然会崩溃
             */
            mServiceData.writeInterfaceToken("android.app.IActivityManager");
            mServiceData.writeStrongBinder(null);
            mServiceData.writeInt(1);
            intent.writeToParcel(mServiceData, 0);
            mServiceData.writeString(null); // resolvedType
//            mServiceData.writeInt(context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.O ? 1 : 0);
            mServiceData.writeInt(0);
            mServiceData.writeString(context.getPackageName()); // callingPackage
            mServiceData.writeInt(0); // userId
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // http://aospxref.com/android-7.1.2_r36/xref/frameworks/base/core/java/android/app/ActivityManagerNative.java
            /* ActivityManagerNative#START_SERVICE_TRANSACTION
             *  case START_SERVICE_TRANSACTION: {
             *             data.enforceInterface(IActivityManager.descriptor);
             *             IBinder b = data.readStrongBinder();
             *             IApplicationThread app = ApplicationThreadNative.asInterface(b);
             *             Intent service = Intent.CREATOR.createFromParcel(data);
             *             String resolvedType = data.readString();
             *             String callingPackage = data.readString();
             *             int userId = data.readInt();
             *             ComponentName cn = startService(app, service, resolvedType, callingPackage, userId);
             *             reply.writeNoException();
             *             ComponentName.writeToParcel(cn, reply);
             *             return true;
             *         }
             */
            mServiceData.writeInterfaceToken("android.app.IActivityManager");
            mServiceData.writeStrongBinder(null);
            intent.writeToParcel(mServiceData, 0);
            mServiceData.writeString(null);  // resolvedType
            mServiceData.writeString(context.getPackageName()); // callingPackage
            mServiceData.writeInt(0); // userId
        } else {
            /* Android4.4 ActivityManagerNative#START_SERVICE_TRANSACTION
             * case START_SERVICE_TRANSACTION: {
             *             data.enforceInterface(IActivityManager.descriptor);
             *             IBinder b = data.readStrongBinder();
             *             IApplicationThread app = ApplicationThreadNative.asInterface(b);
             *             Intent service = Intent.CREATOR.createFromParcel(data);
             *             String resolvedType = data.readString();
             *             int userId = data.readInt();
             *             ComponentName cn = startService(app, service, resolvedType, userId);
             *             reply.writeNoException();
             *             ComponentName.writeToParcel(cn, reply);
             *             return true;
             *         }
             */
            mServiceData.writeInterfaceToken("android.app.IActivityManager");
            mServiceData.writeStrongBinder(null);
            intent.writeToParcel(mServiceData, 0);
            mServiceData.writeString(null);  // resolvedType
            mServiceData.writeInt(0); // userId
        }
    }

    private boolean startServiceByAmsBinder() {
        try {
            if (mRemote == null || mServiceData == null) {
                Log.e("Daemon", "REMOTE IS NULL or PARCEL IS NULL !!!");
                return false;
            }
            Log.i("Daemon", "before transact: serviceTransactCode=" + serviceTransactCode);
            mRemote.transact(serviceTransactCode, mServiceData, null, 1); // flag=FLAG_ONEWAY=1
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean initIndicatorFiles(Context context) {
        File dirFile = context.getDir(INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        try {
            createNewFile(dirFile, INDICATOR_PERSISTENT_FILENAME);
            createNewFile(dirFile, INDICATOR_DAEMON_ASSISTANT_FILENAME);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createNewFile(File dirFile, String fileName) throws IOException {
        File file = new File(dirFile, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    private static long getNativePtr(Parcel parcel) {
        try {
            Field ptrField = parcel.getClass().getDeclaredField("mNativePtr");
            ptrField.setAccessible(true);
            // android19的mNativePtr是int类型，高版本是long类型
            return (long) ptrField.get(parcel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static boolean isIgnoringBatteryOptimizations(Context context) {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return isIgnoring;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static void requestIgnoreBatteryOptimizations(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
