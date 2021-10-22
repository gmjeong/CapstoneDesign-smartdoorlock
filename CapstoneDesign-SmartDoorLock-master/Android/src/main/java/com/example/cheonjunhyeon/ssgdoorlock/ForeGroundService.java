package com.example.cheonjunhyeon.ssgdoorlock;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class ForeGroundService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(this);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
    public static void startForeground(Service service){
        if(service != null){
            try{
                Notification notification = getNotification(service);
                if(notification != null){
                    service.startForeground(1220,notification);
                }
            }catch (Exception e){

            }
        }
    }
    public static Notification getNotification(Context paramContext){
        int smallIcon = R.mipmap.ic_launcher;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //smallIcon = R.mipmap.l_launcher;
        }
        Notification notification =  new NotificationCompat.Builder(paramContext)
                .setSmallIcon(smallIcon)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setAutoCancel(true)
                .setWhen(0)
                .setTicker("").build();
        notification.flags = 16;
        return  notification;
    }
}