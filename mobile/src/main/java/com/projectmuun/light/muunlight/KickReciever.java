package com.projectmuun.light.muunlight;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Micheal on 1/3/2016.
 */
public class KickReciever extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        StaticWakeLock.lockOn(context);
        MonitorActivity.log("KickReceiver started, starting monitor activity.");
        //start activity
        Intent i = new Intent(context, MonitorActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("monitor", true);
        System.out.println("kicked");
        context.startActivity(i);

    }
}
