package com.projectmuun.light.muunlight;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static long EARLY_WAKE_MARGIN = 15 * 60 * 1000L; //Milliseconds

    private AlarmManager alarmMgr;
    private PendingIntent kickIntent;
    private PendingIntent alarmIntent;
    public static int hours = 0;
    public static int minutes = 0;
    public static boolean MonitoringSleep = false;
    private TextView hoursTxT;
    private TextView minutesTxT;
    private TextView AmPmTxT;
    private ImageButton settingsBtn;
    static MainActivity activity;
    static boolean AlarmSetByUser = false;

    static MainActivity instance() {
        if (activity ==null)
            activity = new MainActivity();

        return activity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Muun Light");
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                settingsDialog();
                return false;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
                showTimePicker();
            }
        });



        alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        hoursTxT = (TextView)  findViewById(R.id.hours);
        minutesTxT = (TextView) findViewById(R.id.minutes);
        AmPmTxT = (TextView) findViewById(R.id.ampm);
        //settingsBtn = (ImageButton) findViewById(R.id.setting_btn);


        hours = PreferenceManager.getDefaultSharedPreferences(this).getInt("Hour", Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        minutes = PreferenceManager.getDefaultSharedPreferences(this).getInt("Minute",Calendar.getInstance().get(Calendar.MINUTE));
    /*
        hoursTxT.setText(Integer.toString(hours>12?hours-12:hours));
        minutesTxT.setText(minutes<10?"0"+minutes:Integer.toString(minutes));
        */
        updateAlarmTime(hours, minutes);
        AmPmTxT.setText(hours>12?"PM":"AM");

        ((LinearLayout) findViewById(R.id.time)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        ((Switch) findViewById(R.id.alarmToggle)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setAlarm();
                } else {
                    disarmAlarm();
                }
            }
        });
        /*
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsDialog();
            }
        });
        */

        activity = this;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        Window window = activity.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color

            window.setStatusBarColor(activity.getResources().getColor(R.color.colorPrimaryDark));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void settingsDialog() {
        final Dialog dialog = new Dialog(this, R.style.AppTheme);
        dialog.setContentView(R.layout.settings_dialog);
        dialog.setTitle("Settings");

        ((SeekBar) dialog.findViewById(R.id.margin_seek)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) dialog.findViewById(R.id.margin_seek_txt)).setText(""+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        dialog.show();
    }

    private void showTimePicker() {
        if (!AlarmSetByUser) {
            TimePickerDialog tp = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    updateAlarmTime(hourOfDay, minute);
                }
            }, hours, minutes, false);
            tp.show();
        }
    }
    private void updateAlarmTime(int hour, int minute) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt("Hour", hour);
        editor.putInt("Minute", minute);
        editor.apply();

        hours = hour;
        minutes = minute;

        hoursTxT.setText(Integer.toString(hours>12?hours-12:hours));
        minutesTxT.setText(minutes<10?"0"+minutes:Integer.toString(minutes));
        AmPmTxT.setText(hours > 12 ? "PM" : "AM");
    }


    public void setAlarm() {

        AlarmSetByUser = true;
        setTimeEnabled(false);

        Intent intent = new Intent(this, AlarmReciever.class);
        Intent intent1 = new Intent(this, KickReciever.class);
        alarmIntent = PendingIntent.getBroadcast(this, new Random().nextInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        kickIntent = PendingIntent.getBroadcast(this, new Random().nextInt(), intent1, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            if (calendar.get(Calendar.DAY_OF_YEAR) > 364) {
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
            }else {
                calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)+1);
            }
        }
        long interval = PreferenceManager.getDefaultSharedPreferences(this).getLong("Interval", -1L);
        //if (interval == -1L) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - EARLY_WAKE_MARGIN, kickIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - EARLY_WAKE_MARGIN, kickIntent);
        }
        //} else
        //    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()-EARLY_WAKE_MARGIN, interval, alarmIntent);

        System.out.println("Alarm set\n" + hours + ":" + minutes + "\nAlarm in " + ((calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000 / 60) + " minutes");
        Toast.makeText(MainActivity.this, "Alarm in " + ((calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000 / 60 / 60) + " hours", Toast.LENGTH_SHORT).show();
        ((Switch)findViewById(R.id.alarmToggle)).setChecked(true);
    }
    public void disarmAlarm() {
        MonitoringSleep= false;
        AlarmSetByUser = false;
        setTimeEnabled(true);
        alarmMgr.cancel(alarmIntent);
        //alarmIntent.cancel();
        ((Switch)findViewById(R.id.alarmToggle)).setChecked(false);
        Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_LONG).show();

    }

    public void setTimeEnabled(boolean enabled) {
        if (enabled) {
            ((TextView) findViewById(R.id.hours)).setTextColor(Color.WHITE);
            ((TextView) findViewById(R.id.minutes)).setTextColor(Color.WHITE);
            ((FloatingActionButton) findViewById(R.id.fab)).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        } else {
            ((TextView) findViewById(R.id.hours)).setTextColor(Color.GRAY);
            ((TextView) findViewById(R.id.minutes)).setTextColor(Color.GRAY);
            ((FloatingActionButton) findViewById(R.id.fab)).setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));;
        }
    }


}
