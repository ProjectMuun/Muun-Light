package com.projectmuun.light.muunlight;

/**
 * Created by Micheal on 1/9/2016.
 */
import android.content.Context;
import android.os.PowerManager;

public class StaticWakeLock {
    private static PowerManager.WakeLock wl = null;

    public static void lockOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //Object flags;
        if (wl == null)
            wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MUUN_ALARM");
        wl.acquire();
    }

    public static void lockOff(Context context) {
//		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        try {
            if (wl != null)
                wl.release();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}