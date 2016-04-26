package com.projectmuun.light.muunlight;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;


/**
 * Created by Micheal Roslikov on 1/3/2016.
 */
public class FeedbackReceiverNeutral extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("neutral receiver activated");
        //Send data to server
        //make notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Delete existing notification
        notificationManager.cancel(MainActivity.FEEDBACK_NOTIFICATION_ID);
        //Create new one
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("Thanks!")
                .setContentText("We appreciate your feedback.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                ;
        //Post the new notification
        notificationManager.notify(MainActivity.FEEDBACK_NOTIFICATION_ID+1,mBuilder.build());


    }
}