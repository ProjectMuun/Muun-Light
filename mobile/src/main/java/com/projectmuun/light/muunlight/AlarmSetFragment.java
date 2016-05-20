package com.projectmuun.light.muunlight;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
 * Created by Micheal Roslikov.
 */
public class AlarmSetFragment extends Fragment implements View.OnClickListener {

    //Parent View
    private View myFragmentView;
    //private View globalView;

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


    //Singleton function, return current instance
    static AlarmSetFragment instance() {
        /*
        if (activity == null)
            activity = new MainActivity();
        */
        MonitorActivity.log("AlarmSetFragment instance has been requested Is null:"+(fragment==null));
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Initialize Parent View
        myFragmentView = inflater.inflate(R.layout.alarm_set_fragment, container, false);
        //globalView = inflater.inflate(R.layout.activity_main, container, false);
        //Get services
        alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);


        //Initiate Views
        hoursTxT = (TextView) myFragmentView.findViewById(R.id.hours);
        minutesTxT = (TextView) myFragmentView.findViewById(R.id.minutes);
        AmPmTxT = (TextView) myFragmentView.findViewById(R.id.ampm);

        //initiate time currently picked
        hours = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("Hour", Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        minutes = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("Minute", Calendar.getInstance().get(Calendar.MINUTE));
        //Update the time shown
        updateAlarmTime(hours, minutes);
        AmPmTxT.setText(hours > 12 ? "PM" : "AM");



        //Alarm Switch stuff
        alarmSwitch = ((Switch) myFragmentView.findViewById(R.id.alarmToggle));
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


        return myFragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // interact with UI here, not in onCreateView
        super.onActivityCreated(savedInstanceState);

        //On click for the time shown (same as fab)
        (myFragmentView.findViewById(R.id.time)).setOnClickListener(this);
        //floating action button (fab) onclick
        (myFragmentView.findViewById(R.id.fab)).setOnClickListener(this);
    }



        //Shows the time picker
    private void showTimePicker() {
        if (/*!AlarmSetByUser &&*/ !TimePickerOn) {
            TimePickerOn = true;
            TimePickerDialog tp = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    updateAlarmTime(hourOfDay, minute);
                    TimePickerOn = false;
                }
            }, hours, minutes, false);
            tp.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    System.out.println("tp dismissed");
                    TimePickerOn = false;
                }
            });
            tp.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    System.out.println("tp shown");
                }
            });
            tp.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    System.out.println("");
                }
            });
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

        //Also, if the alarm is already on, it turns off and gets turned back on again
        if (AlarmSetByUser) {
            disarmAlarm();
            setAlarm();
        }
    }

    //Sets the alarm
    public void setAlarm() {

        AlarmSetByUser = true;
        setTimeEnabled(false);

        //make the intents for the kick that launches REM detection and back up alarm
        Intent intent = new Intent(getActivity(), AlarmReciever.class);
        Intent intent1 = new Intent(getActivity(), KickReciever.class);
        alarmIntent = PendingIntent.getBroadcast(getActivity(), new Random().nextInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        kickIntent = PendingIntent.getBroadcast(getActivity(), new Random().nextInt(), intent1, PendingIntent.FLAG_CANCEL_CURRENT);
        //using calendar class to find the given time in a special millisecond format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        //Make the day for the alarm the next day if that time has passed
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            //If end of year, make sure it doesn't become day 366
            if (calendar.get(Calendar.DAY_OF_YEAR) > 364) {
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
            } else {
                calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
            }
        }
        //retrieve the settings used to set alarms that the user has set
        long interval = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong("Interval", -1L);
        EARLY_WAKE_MARGIN = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong("WakeMargin", EARLY_WAKE_MARGIN_DEFUALT);
        //Checks if alarm interval has been set
        if (interval == -1L) {
            //If it has not, then go along with the whole alarm setting business
            //Btw, it is really weird how the functions needed to achieve the same thing has been inconsistent among versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT  < Build.VERSION_CODES.M) {
                //In the case SDK version is between KITKAT and M, so that you can set the Exact time
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - EARLY_WAKE_MARGIN, kickIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //In the case SDK version is higher or equal to M, so you can set exact time and allow wake up in power save mode
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - EARLY_WAKE_MARGIN, kickIntent);
            } else {
                //in the case SDK is lower than KITKAT, so it wakes the phone at exact time no matter what
                alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - EARLY_WAKE_MARGIN, kickIntent);
            }
        } else {
            //In the case of the User specified the Interval, I should probably remove this feature as problems
            // will occur with newer versions
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()-EARLY_WAKE_MARGIN, interval, kickIntent);
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, alarmIntent);
        }

        //Nice little confirmation to the user
        System.out.println("Alarm set\n" + hours + ":" + minutes + "\nAlarm in " + ((calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000 / 60) + " minutes");
        Toast.makeText(getActivity(), "Alarm in " + ((calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000 / 60 / 60) + " hours", Toast.LENGTH_SHORT).show();
        MonitorActivity.log("Alarm Set for "+(hours>12?hours-12:hours)+":"+(minutes < 10 ? "0" + minutes : Integer.toString(minutes)));
    }

    //Disarm the alarm
    public void disarmAlarm() {
        MonitoringSleep = false;
        AlarmSetByUser = false;
        setTimeEnabled(true);
        alarmMgr.cancel(alarmIntent);
        alarmMgr.cancel(kickIntent);

        Toast.makeText(getActivity(), "Alarm Canceled", Toast.LENGTH_LONG).show();
        MonitorActivity.log("Alarm Canceled");
    }
    //This function is called in a different thread. It unchecks the switch and that calls the listener that calls the disarm alarm
    public void disarmAlarmAndUncheck() {
            alarmSwitch.setChecked(false);
    }

    //this function is called to change the ui according if the alarm is currently on
    public void setTimeEnabled(boolean enabled) {
        if (enabled) {
            //((TextView) findViewById(R.id.hours)).setTextColor(Color.WHITE);
            //((TextView) findViewById(R.id.minutes)).setTextColor(Color.WHITE);
            //setBrightness(255, getActivity().getApplicationContext());
            //((FloatingActionButton) myFragmentView.findViewById(R.id.fab)).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        } else {
            //((TextView) findViewById(R.id.hours)).setTextColor(Color.GRAY);
            //((TextView) findViewById(R.id.minutes)).setTextColor(Color.GRAY);
            //setBrightness(100, getActivity().getApplicationContext());
            //((FloatingActionButton) myFragmentView.findViewById(R.id.fab)).setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

        }
    }


    @Override
    public void onClick(View v) {
        //System.out.println("showing timepicker? maybe not.");
        showTimePicker();
    }
}
