package com.divine.base.utils.appbadge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.divine.base.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Author: Divine
 * CreateDate: 2021/8/5
 * Describe:
 */
public class OtherBadge {
    public static int notificationId = 0;

    public static boolean setNotificationBadge(int count, Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService
                (Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 8.0之后添加角标需要NotificationChannel
            NotificationChannel channel = new NotificationChannel("badge", "badge",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(context, "badge")
                .setContentTitle("应用提醒")
                .setContentText("您有" + count + "条未读消息")
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_base))
                .setAutoCancel(true)
                .setChannelId("badge")
                .setNumber(count)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL).build();
        try {
            Field field = notification.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notification);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
            method.invoke(extraNotification, count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        notificationManager.notify(notificationId++, notification);
        return true;
    }
}
