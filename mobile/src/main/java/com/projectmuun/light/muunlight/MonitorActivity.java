package com.projectmuun.light.muunlight;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Micheal Roslikov.
 */
public class MonitorActivity extends Activity {

    static final float NOISE = 0.5f;
    static final long TIME_INTERVAL = 400l;

    //Ranges (exclusive)
    static final int REM_MAX = 5 * 1000;
    static final int SEMI_REM_MIN = 4 * 1000;
    static final int SEMI_REM_MAX = 30 * 1000;
    static final int NON_REM_MIN = 29 * 1000;

    public float lastX;
    public float lastY;
    public float lastZ;

    public float deltaX;
    public float deltaY;
    public float deltaZ;

    public long lastRecording = 0l;
    SensorManager sensorManager;
    AudioManager audioManager;
    Button btn;
    Ringtone r;
    boolean monitorSleep = false;

    boolean playingRing = false;

    FileOutputStream log;


    SensorEventListener sel = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {


            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                deltaX = Math.abs(event.values[0] - lastX);
                deltaY = Math.abs(event.values[1] - lastY);
                deltaZ = Math.abs(event.values[2] - lastZ);

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];

                //System.out.println("Accelerometer (less)");

                long timeDelta = System.currentTimeMillis() - lastRecording;
                if ((deltaX > NOISE || deltaY > NOISE || deltaZ > NOISE) && (timeDelta) > TIME_INTERVAL) {
                    System.out.println("Accelerometer (more)");
                    lastRecording = System.currentTimeMillis();
                    if (timeDelta < REM_MAX && MainActivity.AlarmSetByUser) {
                        System.out.println("REM!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        log("Detected REM, turning on alarm.");
                        ring();
                    }
                } else {
                    if (playingRing && timeDelta > 2000) {
                        //TurnOffAlarmAutomatically();
                        System.out.println("Turning off automatically");
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    Sensor Accelerometer;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.alarm_went_off);
        //StaticWakeLock.lockOn(this);

        monitorSleep = getIntent().getExtras().getBoolean("monitor", monitorSleep);




            System.out.println("Activity Started");
            btn = (Button) findViewById(R.id.alarmOffBTN);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    turnOffAlarm();
                }
            });

        try {
            //if (isExternalStorageWritable())
            //{
                //*
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Sleep");

                if (!dir.exists()) {
                    dir.mkdir();
                }
                //*/

                File file = new File(dir.getAbsolutePath() + File.separator + "log_human_readable.txt");

                //FileWriter fw = new FileWriter(file);
                log = new FileOutputStream(file);
            //}
        } catch (Exception e) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+e.getMessage());
        }

            if (monitorSleep) {
            MainActivity.MonitoringSleep = true;
            //Sensor Stuff
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(sel, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
            log("Monitoring of sleep has been initiated.");
        } else {
                log("Initiating back up alarm.");
            System.out.println("Backup alarm launched");

            btn.setTextColor(Color.RED);
            ring();
        }
    }

    private void turnOffAlarm() {
        MainActivity.MonitoringSleep = false;
        if (AlarmSetFragment.instance() != null) {
            AlarmSetFragment.instance().disarmAlarmAndUncheck();
        }
        log("Turned off alarm.");
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (monitorSleep)
            sensorManager.unregisterListener(sel, Accelerometer);
        if (r != null)
            r.stop();
        StaticWakeLock.lockOff(this);
        log("Activity Destroyed. Is back up alarm?:" + !monitorSleep);
    }


    private void TurnOffAlarmAutomatically() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("TurnOffAlarmAutomatically", true)) {
            turnOffAlarm();
        }
    }

    private void ring() {
        if (!playingRing) {
            //MainActivity.setBrightness(255, this.getApplicationContext());
            playingRing = true;
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            r = RingtoneManager.getRingtone(this, ringtoneUri);
            r.play();
            System.out.println("What did the alarm say?\nRing-inginginginging");
            log("What did the alarm say?\nRing-inginginginging \t Is back Up?:"+!monitorSleep);
        } else {
            log("Request denied, alarm already in operation.");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //The back up alarm turned on, we wanna end this one
        System.out.println("Stopped Monitor Activity \nIs Backup alarm: " + !monitorSleep);

        log("Stopped Monitor Activity Is Backup alarm: "+!monitorSleep);
        //turnOffAlarm();
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("Paused Monitor Activity \nIs Backup alarm: " + !monitorSleep);
        log("Paused Monitor Activity Is Backup alarm: "+!monitorSleep);
    }

    public void log(String message) {
        try {
            log.write((returnDate() + ": " + message + "\n").getBytes());
            //log.newLine();
            log.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Utility functions

    public static String returnDate () {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"/"+Calendar.getInstance().get(Calendar.MONTH)+"/"+Calendar.getInstance().get(Calendar.YEAR)    +     "\t"+Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
