package com.projectmuun.light.muunlight;

import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Micheal Roslikov.
 */
public class SettingsFragment extends Fragment{

    public static final String WAKE_MARGIN_ID = "wake_margin";
    public static final long WAKE_MARGIN_DEFAULT = 15L  * 60 * 1000;
    public static final int WAKE_MARGIN_SEEKBAR_POSTION_DEFUALT = 15;
    public static final String INTERVAL_ID = "interval";
    public static final long INTERVAL_DEFAULT = -1L;
    public static final String INTERVAL_SPINNER_POSITION_ID = "interval_spinner_position";
    public static final int INTERVAL_SPINNER_POSITION_DEFAULT = 0;
    public static final String ALARM_TURN_OFF_AUTOMATICALLY_ID = "turn_off_alarm_automatically";
    public static final boolean ALARM_TURN_OFF_AUTOMATICALLY_DEFAULT = false;

    SeekBar marginSeek;
    CheckBox alarmTurnOffCheckBox;
    Spinner spinner;

    long wake_margin = WAKE_MARGIN_DEFAULT;

    long interval = INTERVAL_DEFAULT;
    boolean alarm_turn_off_auto = ALARM_TURN_OFF_AUTOMATICALLY_DEFAULT;
    int spinner_position = INTERVAL_SPINNER_POSITION_DEFAULT;

    //Parent View
    private View myFragmentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initialize Parent View
        myFragmentView = inflater.inflate(R.layout.settings_fragment, container, false);

        marginSeek = (SeekBar) myFragmentView.findViewById(R.id.margin_seek);
        alarmTurnOffCheckBox = (CheckBox) myFragmentView.findViewById(R.id.AlarmTurnOffCheckBox);

        alarmTurnOffCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                alarm_turn_off_auto = isChecked;
            }
        });

        marginSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) myFragmentView.findViewById(R.id.margin_seek_txt)).setText("" + progress);
                wake_margin = progress * 60 * 1000;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        spinner = (Spinner) myFragmentView.findViewById(R.id.interval_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.interval_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        interval = -1L;
                        break;
                    case 1:
                        interval = 7 * 24 * 60 * 60 * 1000l;
                        break;
                    case 2:
                        interval = AlarmManager.INTERVAL_DAY;
                        break;
                    case 3:
                        interval = AlarmManager.INTERVAL_HALF_DAY;
                        break;
                    case 4:
                        interval = AlarmManager.INTERVAL_HOUR;
                        break;


                }
                spinner_position = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        (myFragmentView.findViewById(R.id.save_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
        (myFragmentView.findViewById(R.id.reset_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreSettings();
            }
        });

//        loadSettings();


  
        return myFragmentView;
    }
    //Save the settings
    public void saveSettings() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();

        editor.putLong(INTERVAL_ID, interval);
        editor.putLong(WAKE_MARGIN_ID, wake_margin);

        editor.putInt(INTERVAL_SPINNER_POSITION_ID, spinner_position);

        editor.putBoolean(ALARM_TURN_OFF_AUTOMATICALLY_ID, alarm_turn_off_auto);
        editor.apply();

        Toast.makeText(getActivity().getApplicationContext(), "Settings Saved", Toast.LENGTH_SHORT).show();
    }
    public void restoreSettings() {
        marginSeek.setProgress(WAKE_MARGIN_SEEKBAR_POSTION_DEFUALT);
        wake_margin = WAKE_MARGIN_SEEKBAR_POSTION_DEFUALT;

        spinner.setSelection(INTERVAL_SPINNER_POSITION_DEFAULT,true);
        spinner_position = INTERVAL_SPINNER_POSITION_DEFAULT;

        alarmTurnOffCheckBox.setChecked(ALARM_TURN_OFF_AUTOMATICALLY_DEFAULT);
        alarm_turn_off_auto = ALARM_TURN_OFF_AUTOMATICALLY_DEFAULT;

        saveSettings();

    }
    //Retrieve the settings
    public void loadSettings(){
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getActivity());

        marginSeek.setProgress(s.getInt(WAKE_MARGIN_ID, 15));
        spinner.setSelection(s.getInt(INTERVAL_SPINNER_POSITION_ID, INTERVAL_SPINNER_POSITION_DEFAULT), true);
        alarmTurnOffCheckBox.setChecked(s.getBoolean(ALARM_TURN_OFF_AUTOMATICALLY_ID,ALARM_TURN_OFF_AUTOMATICALLY_DEFAULT));

    }
}
