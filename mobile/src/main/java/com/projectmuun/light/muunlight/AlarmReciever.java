package com.projectmuun.light.muunlight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;

/**
 * Created by Micheal on 12/26/2015.
 */
public class AlarmReciever extends WakefulBroadcastReceiver{

    AudioManager audioManager;
    MediaPlayer mp;
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Alarm Started");



        //*
        if (MainActivity.MonitoringSleep && MainActivity.AlarmSetByUser) {
            StaticWakeLock.lockOn(context);
            //start activity
            Intent i = new Intent(context, MonitorActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("monitor", false);
            System.out.println("backup alarm launch request");
            context.startActivity(i);

        }
        //*/
    }



}
