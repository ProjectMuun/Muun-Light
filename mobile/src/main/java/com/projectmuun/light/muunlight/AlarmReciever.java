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


        if (MainActivity.MonitoringSleep) {
            MainActivity.MonitoringSleep = false;
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            mp = MediaPlayer.create(context, R.raw.wakiewakie);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    restartAlarmSound();
                }
            });
            mp.start();
        }
    }

    private void restartAlarmSound() {
        if (MainActivity.MonitoringSleep && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("Restart", true)) {
            mp.stop();
            mp.start();
        } else {
            mp.stop();
            mp.release();
            mp=null;
        }
    }

}
