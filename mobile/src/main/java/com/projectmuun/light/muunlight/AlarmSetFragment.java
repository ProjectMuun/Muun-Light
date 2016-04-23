package com.projectmuun.light.muunlight;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by Micheal on 4/22/2016.
 */
public class AlarmSetFragment extends Fragment {

    //How early we will start monitoring
    public static final long EARLY_WAKE_MARGIN_DEFUALT = 15 * 60 * 1000L; //Milliseconds
    public static long EARLY_WAKE_MARGIN = EARLY_WAKE_MARGIN_DEFUALT;

    //Alarm stuff
    private AlarmManager alarmMgr;
    private PendingIntent kickIntent;
    private PendingIntent alarmIntent;
    //Wake up time
    public static int hours = 0;
    public static int minutes = 0;
    //Views
    private TextView hoursTxT;
    private TextView minutesTxT;
    private TextView AmPmTxT;
    public Switch alarmSwitch;
    //Singleton variable
    static AlarmSetFragment fragment;
    //Track keeping variables
    static boolean AlarmSetByUser = false;
    static boolean TimePickerOn = false;
    public static boolean MonitoringSleep = false;
    //Notifications
    NotificationManager mNotificationManager;
    public static int FEEDBACK_NOTIFICATION_ID = 3573;

    //Singleton function, return current instance
    static AlarmSetFragment instance() {
        /*
        if (activity == null)
            activity = new MainActivity();
        */
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        
        //Get services
        alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        //Initiate Views
        hoursTxT = (TextView) getActivity().findViewById(R.id.hours);
        minutesTxT = (TextView) getActivity().findViewById(R.id.minutes);
        AmPmTxT = (TextView) getActivity().findViewById(R.id.ampm);

        //initiate time currently picked
        hours = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("Hour", Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        minutes = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("Minute", Calendar.getInstance().get(Calendar.MINUTE));
        //Update the time shown
        updateAlarmTime(hours, minutes);
        AmPmTxT.setText(hours > 12 ? "PM" : "AM");

        //On click for the time shown (same as fab)
        (getActivity().findViewById(R.id.time)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        //Alarm Switch stuff
        alarmSwitch = ((Switch) getActivity().findViewById(R.id.alarmToggle));
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setAlarm();
                } else {
                    disarmAlarm();
                }
            }
        });

        //Initiate reference for singleton
        fragment = this;

        //If high enough version, set notification bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getActivity().getWindow();

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color

            window.setStatusBarColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        }

        //Create the intents to be launched by the notification buttons
        Intent intent1 = new Intent(getActivity(), FeedbackReceiverPositive.class);
        Intent intent2 = new Intent(getActivity(), FeedbackReceiverNeutral.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(), 97426, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent nIntent = PendingIntent.getBroadcast(getActivity(), 93706, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
        //Build "heads up" notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity())
                .setContentTitle("How did you sleep today?")
                        //.setContentText("")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(R.mipmap.smile, "Great!", pIntent)
                .addAction(R.mipmap.neutral, "Meh.", nIntent);
        //Make sure there is no vibration
        if (Build.VERSION.SDK_INT >= 21) mBuilder.setVibrate(new long[0]);
        //Show notification
        mNotificationManager.notify(FEEDBACK_NOTIFICATION_ID, mBuilder.build());
        
        
        
        // Inflate the layout for getActivity() fragment
        return inflater.inflate(R.layout.settings_dialog, container, false);
    }



    //Shows the time picker
    private void showTimePicker() {
        if (!AlarmSetByUser && !TimePickerOn) {
            TimePickerOn = true;
            TimePickerDialog tp = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    updateAlarmTime(hourOfDay, minute);
                    TimePickerOn = false;
                }
            }, hours, minutes, false);
            tp.show();
        }
    }

    //Saves and displays alarm time
    private void updateAlarmTime(int hour, int minute) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putInt("Hour", hour);
        editor.putInt("Minute", minute);
        editor.apply();

        hours = hour;
        minutes = minute;

        hoursTxT.setText(Integer.toString(hours > 12 ? hours - 12 : hours));
        minutesTxT.setText(minutes < 10 ? "0" + minutes : Integer.toString(minutes));
        AmPmTxT.setText(hours > 12 ? "PM" : "AM");
    }

    //Sets the alarm
    public void setAlarm() {

        AlarmSetByUser = true;
        setTimeEnabled(false);

        Intent intent = new Intent(getActivity(), AlarmReciever.class);
        Intent intent1 = new Intent(getActivity(), KickReciever.class);
        alarmIntent = PendingIntent.getBroadcast(getActivity(), new Random().nextInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        kickIntent = PendingIntent.getBroadcast(getActivity(), new Random().nextInt(), intent1, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            if (calendar.get(Calendar.DAY_OF_YEAR) > 364) {
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
            } else {
                calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
            }
        }
        long interval = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong("Interval", -1L);
        EARLY_WAKE_MARGIN = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong("WakeMargin", EARLY_WAKE_MARGIN_DEFUALT);
        if (interval == -1L) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //In the case SDK version is higher, so that you can set the Exact time
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - EARLY_WAKE_MARGIN, kickIntent);
            } else {
                //in the case it isn't higher
                alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - EARLY_WAKE_MARGIN, kickIntent);
            }
        } else {
            //In the case of the User specified the Interval
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()-EARLY_WAKE_MARGIN, interval, kickIntent);
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, alarmIntent);
        }

        System.out.println("Alarm set\n" + hours + ":" + minutes + "\nAlarm in " + ((calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000 / 60) + " minutes");
        Toast.makeText(getActivity(), "Alarm in " + ((calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000 / 60 / 60) + " hours", Toast.LENGTH_SHORT).show();

    }
    public void setAlarm(boolean checkSwitch) {
        //setAlarm();
        if (checkSwitch)
            alarmSwitch.setChecked(true);
        else
            setAlarm();
    }

    public void disarmAlarm() {
        MonitoringSleep = false;
        AlarmSetByUser = false;
        setTimeEnabled(true);
        alarmMgr.cancel(alarmIntent);
        alarmMgr.cancel(kickIntent);
        //alarmIntent.cancel();

        Toast.makeText(getActivity(), "Alarm Canceled", Toast.LENGTH_LONG).show();

    }
    public void disarmAlarm(boolean unCheck) {
        //disarmAlarm();
        //
        if (unCheck)
            alarmSwitch.setChecked(false);
        else
            setAlarm();
    }

    public void setTimeEnabled(boolean enabled) {
        if (enabled) {
            //((TextView) findViewById(R.id.hours)).setTextColor(Color.WHITE);
            //((TextView) findViewById(R.id.minutes)).setTextColor(Color.WHITE);
            //setBrightness(255, getActivity().getApplicationContext());
            ((FloatingActionButton) getActivity().findViewById(R.id.fab)).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        } else {
            //((TextView) findViewById(R.id.hours)).setTextColor(Color.GRAY);
            //((TextView) findViewById(R.id.minutes)).setTextColor(Color.GRAY);
            //setBrightness(100, getActivity().getApplicationContext());
            ((FloatingActionButton) getActivity().findViewById(R.id.fab)).setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

        }
    }


    public static void setBrightness(int brightness, Context context){

        //constrain the value of brightness
        if(brightness < 0)
            brightness = 0;
        else if(brightness > 255)
            brightness = 255;


        ContentResolver cResolver = context.getContentResolver();
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);

    }

}