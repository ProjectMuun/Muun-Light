package com.projectmuun.light.muunlight;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;

/**
 * Created by Micheal on 1/3/2016.
 */
public class MonitorActivity extends Activity implements SensorEventListener{

    static final float NOISE = 0.5f;
    static final long TIME_INTERVAL = 500l;

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
    Ringtone r;

    boolean playingRing = false;

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

                System.out.println("Accelerometer (less)");

                long timeDelta = System.currentTimeMillis() - lastRecording;
                if ((deltaX > NOISE || deltaY > NOISE || deltaZ > NOISE) && (timeDelta) > TIME_INTERVAL) {
                    System.out.println("Accelerometer (more)");
                    lastRecording = System.currentTimeMillis();
                    if (timeDelta < REM_MAX && MainActivity.AlarmSetByUser) {
                        System.out.println("REM!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        ring();
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    Sensor TemperatureSensor;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.alarm_went_off);
        //StaticWakeLock.lockOn(this);

        MainActivity.MonitoringSleep = true;
        System.out.println("Activity Started");
        (findViewById(R.id.alarmOffBTN)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.MonitoringSleep = false;
                if (MainActivity.instance() != null) {
                    MainActivity.instance().disarmAlarm();
                }
                ((Switch) findViewById(R.id.alarmToggle)).setChecked(false);
                finish();
            }
        });

        //Sensor Stuff
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            TemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(sel, TemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        //End sensor stuff
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(sel, TemperatureSensor);
        if (r != null)
            r.stop();
    }

    private void ring() {
        if (!playingRing) {
            MainActivity.setBrightness(255, this.getApplicationContext());
            playingRing = true;
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            r = RingtoneManager.getRingtone(this, ringtoneUri);
            r.play();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        StaticWakeLock.lockOff(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*
        System.out.println("Sensor Event");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            deltaX = Math.abs(event.values[0] - lastX);
            deltaY = Math.abs(event.values[1] - lastY);
            deltaZ = Math.abs(event.values[2] - lastZ);

            lastX = event.values[0];
            lastY = event.values[1];
            lastZ = event.values[2];

            System.out.println("Accelerometer (less)");

            long timeDelta = System.currentTimeMillis() - lastRecording;
            if ((deltaX > NOISE || deltaY > NOISE || deltaZ > NOISE) && (timeDelta) > TIME_INTERVAL) {
                System.out.println("Accelerometer (more)");
                if (timeDelta < REM_MAX) {
                    System.out.println("REM!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                    Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    Ringtone r = RingtoneManager.getRingtone(this, ringtoneUri);
                    r.play();
                }
            }
        } else {
            finish();
        }
        */
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
