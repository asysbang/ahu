package com.asysbang.serviceplugin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MainService extends Service {

    private final String TAG = getClass().getSimpleName();

    private static final int NOTIFICATION_ID = 0x1001;

    private static final String NOTIFICATION_CHANNEL_ID = "service_plugin";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"===oncreate");
        showNotification();
    }

    private void showNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,   "my_channel", NotificationManager.IMPORTANCE_DEFAULT);
        nm.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = builder.setWhen(System.currentTimeMillis())
                .setContentTitle("Title")
                .setContentText("This is a notification")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        nm.notify(NOTIFICATION_ID, notification);
    }
}
