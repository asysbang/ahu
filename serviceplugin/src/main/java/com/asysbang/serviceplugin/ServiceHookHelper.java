package com.asysbang.serviceplugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ServiceHookHelper {

    private final String TAG = getClass().getSimpleName();

    public static final String EXTRA_INTENT_HOOK = "action_hook";
    private static final String EXTRA_INTENT_KEY = "originIntent";

    private ServiceHookHelper(){

    }

    public static ServiceHookHelper getInstance() {
        return InstanceHolder.sInt;
    }

    private static class InstanceHolder {
       private  static ServiceHookHelper sInt = new ServiceHookHelper();
    }

    private Context mContext;
    public void hookStartService(Context context) {
        mContext = context;
        int sdkInt = Build.VERSION.SDK_INT;
        Log.d(TAG , "====SDK_INT : "+sdkInt);
        Log.d(TAG , "====Build.VERSION_CODES.O : "+Build.VERSION_CODES.O);
        hookActivityManagerAndroidO();
        hookActivityThread();
        hookPackageManager();

    }

    private void hookActivityManagerAndroidO() {
        try {
            /// 获取IActivityManagerSingleton
            Class activityManagerClass = Class.forName("android.app.ActivityManager");
            Field singletonField = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
            singletonField.setAccessible(true);
            Object gDefault = singletonField.get(null);

            //　获取mIntance
            Class singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            Object mInstance = mInstanceField.get(gDefault);
            // 替换mIntance
            Object proxy = Proxy.newProxyInstance(
                    mInstance.getClass().getClassLoader(),
                    new Class[]{Class.forName("android.app.IActivityManager")},
                    new IActivityManagerHandler(mInstance));
            mInstanceField.set(gDefault, proxy);
        } catch (Exception e) {
            Log.e(TAG, "err", e);
        }
    }

    private class IActivityManagerHandler implements InvocationHandler {

        private Object mOrigin;

        IActivityManagerHandler(Object origin) {
            mOrigin = origin;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("startService".equals(method.getName())) {
                Log.e(TAG, "======IActivityManagerHandler");
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        index = i;
                        break;
                    }
                }
                Intent originIntent = (Intent) args[index];
                Log.e(TAG, "======>>> IActivityManagerHandler " + originIntent.toString());
                if (EXTRA_INTENT_HOOK.equals(originIntent.getStringExtra(EXTRA_INTENT_HOOK))) {
                    Intent proxyIntent = new Intent();
                    Log.e(TAG, "======FakeService");
                    ComponentName componentName = new ComponentName(mContext, FakeService.class);
                    proxyIntent.setComponent(componentName);
                    proxyIntent.putExtra(EXTRA_INTENT_KEY, originIntent);
                    args[index] = proxyIntent;
                }
            }
            return method.invoke(mOrigin, args);
        }
    }


//    private void handleCreateService(CreateServiceData data) {
//        // If we are getting ready to gc after going to the background, well
//        // we are back active so skip it.
//        unscheduleGcIdler();
//
//        LoadedApk packageInfo = getPackageInfoNoCheck(
//                data.info.applicationInfo, data.compatInfo);
//        Service service = null;
//        try {
//            java.lang.ClassLoader cl = packageInfo.getClassLoader();
//            service = (Service) cl.loadClass(data.info.name).newInstance();
//        } catch (Exception e) {
//            if (!mInstrumentation.onException(service, e)) {
//                throw new RuntimeException(
//                        "Unable to instantiate service " + data.info.name
//                                + ": " + e.toString(), e);
//            }
//        }
//
//        try {
//            if (localLOGV) Slog.v(TAG, "Creating service " + data.info.name);
//
//            ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
//            context.setOuterContext(service);
//
//            Application app = packageInfo.makeApplication(false, mInstrumentation);
//            service.attach(context, this, data.info.name, data.token, app,
//                    ActivityManager.getService());
//            service.onCreate();
//            mServices.put(data.token, service);
//            try {
//                ActivityManager.getService().serviceDoneExecuting(
//                        data.token, SERVICE_DONE_EXECUTING_ANON, 0, 0);
//            } catch (RemoteException e) {
//                throw e.rethrowFromSystemServer();
//            }
//        } catch (Exception e) {
//            if (!mInstrumentation.onException(service, e)) {
//                throw new RuntimeException(
//                        "Unable to create service " + data.info.name
//                                + ": " + e.toString(), e);
//            }
//        }
//    }

    //    private class H extends Handler {
//        public static final int LAUNCH_ACTIVITY         = 100;
//        public static final int PAUSE_ACTIVITY          = 101;
//        public static final int PAUSE_ACTIVITY_FINISHING= 102;
//        public static final int STOP_ACTIVITY_SHOW      = 103;
//        public static final int STOP_ACTIVITY_HIDE      = 104;
//        public static final int SHOW_WINDOW             = 105;
//        public static final int HIDE_WINDOW             = 106;
//        public static final int RESUME_ACTIVITY         = 107;
//        public static final int SEND_RESULT             = 108;
//        public static final int DESTROY_ACTIVITY        = 109;
//        public static final int BIND_APPLICATION        = 110;
//        public static final int EXIT_APPLICATION        = 111;
//        public static final int NEW_INTENT              = 112;
//        public static final int RECEIVER                = 113;
//        public static final int CREATE_SERVICE          = 114;
//        public static final int SERVICE_ARGS            = 115;
//        public static final int STOP_SERVICE            = 116;
//
//        public static final int CONFIGURATION_CHANGED   = 118;
//        public static final int CLEAN_UP_CONTEXT        = 119;
//        public static final int GC_WHEN_IDLE            = 120;
//        public static final int BIND_SERVICE            = 121;
//        public static final int UNBIND_SERVICE          = 122;
//        public static final int DUMP_SERVICE            = 123;
//        public static final int LOW_MEMORY              = 124;

    private void hookActivityThread() {
        try {
            //　获取ActivityThread实例
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Field threadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            threadField.setAccessible(true);
            Object sCurrentActivityThread = threadField.get(null);

            //　获取mH变量
            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            final Object mH = mHField.get(sCurrentActivityThread);

            //　设置mCallback变量
            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            Handler.Callback callback = new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == 115) {
                        try {
                            Log.e(TAG, "======>>> 115intent :" + msg.obj.getClass());
                            Field intentField = msg.obj.getClass().getDeclaredField("args");
                            intentField.setAccessible(true);
                            Intent intent = (Intent) intentField.get(msg.obj);
                            Log.e(TAG, "======>>> intent :" + intent);
                            Intent raw = intent.getParcelableExtra(EXTRA_INTENT_KEY);
                            //不要去替换其他消息
                            if (null == raw || raw.getAction() != null) {
                                return false;
                            }
                            Log.e(TAG, "======>>> raw :" + raw);
                            Log.e(TAG, "======>>> raw :" + raw.getStringExtra(EXTRA_INTENT_HOOK));
                            intent.setComponent(raw.getComponent());
                        } catch (Exception e) {
                            Log.e(TAG, "get intent err", e);
                        }
                    }else
                    if (msg.what == 114) {
                        try {
                            Log.e(TAG, "======>>> 114intent :" + msg.obj.getClass());
                            //这个地方intent是空，需要替换serviceinfo

                            Field infoField = msg.obj.getClass().getDeclaredField("info");
                            infoField.setAccessible(true);
                            ServiceInfo info = (ServiceInfo) infoField.get(msg.obj);
                            Log.e(TAG, "======>>> info :" + info);
                            Log.e(TAG, "======>>> infoapplicationInfo :" + info.applicationInfo);
                            Log.e(TAG, "======>>> infoname :" + info.name);
                            info.name = "com.asysbang.serviceplugin.MainService";

//                            Field intentField = msg.obj.getClass().getDeclaredField("intent");
//                            intentField.setAccessible(true);
//                            Intent intent = (Intent) intentField.get(msg.obj);
//                            Log.e(TAG, "======>>> intent :" + intent);
//                            Intent raw = intent.getParcelableExtra(EXTRA_INTENT_KEY);
//                            //不要去替换其他消息
//                            if (null == raw || raw.getAction() != null) {
//                                return false;
//                            }
//                            Log.e(TAG, "======>>> raw :" + raw);
//                            Log.e(TAG, "======>>> raw :" + raw.getStringExtra(EXTRA_INTENT_HOOK));
//                            intent.setComponent(raw.getComponent());
                        } catch (Exception e) {
                            Log.e(TAG, "get intent err", e);
                        }
                    }
                    return false;
                }
            };
            mCallbackField.set(mH, callback);
        } catch (Exception e) {
            Log.e(TAG, "err", e);
        }
    }

    private void hookPackageManager() {
        try {
            //要先获取一下,保证它初始化
            mContext.getPackageManager();
            Class activityThread = Class.forName("android.app.ActivityThread");
            Field pmField = activityThread.getDeclaredField("sPackageManager");
            pmField.setAccessible(true);
            final Object origin = pmField.get(null);
            Object handler = Proxy.newProxyInstance(activityThread.getClassLoader(),
                    new Class[]{Class.forName("android.content.pm.IPackageManager")},
                    new PackageManagerHandler(mContext, origin));
            pmField.set(null, handler);
        } catch (Exception e) {
            Log.e(TAG, "=====hook IPackageManager err", e);
        }
    }

    class PackageManagerHandler implements InvocationHandler {
        private Context mContext;
        private Object mOrigin;

        PackageManagerHandler(Context context, Object origin) {
            mContext = context;
            mOrigin = origin;
        }
        //=====?????????  MainActivity.class.getName  需要替换

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!method.getName().equals("getServiceInfo")) {
                return method.invoke(mOrigin, args);
            }
            //如果没有注册,并不会抛出异常,而是会直接返回null
            Object ret = method.invoke(mOrigin, args);
            if (ret == null) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof ComponentName) {
                        ComponentName componentName = (ComponentName) args[i];
                        componentName.getClassName();
                        args[i] = new ComponentName(mContext.getPackageName(), FakeService.class.getName());
                        Log.e(TAG, "======>>> PackageManagerHandler :" + componentName);
                        return method.invoke(mOrigin, args);
                    }
                }
            }
            return ret;
        }
    }


}
