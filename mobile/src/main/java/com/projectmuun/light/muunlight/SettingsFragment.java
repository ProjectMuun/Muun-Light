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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Micheal Roslikov.
 */
public class SettingsFragment extends Fragment{
    SeekBar marginSeek;
    CheckBox alarmTurnOffCheckBox;
    Spinner spinner;

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
        marginSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) myFragmentView.findViewById(R.id.margin_seek_txt)).setText("" + progress);
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
                        updateSettings(-2l, -1, true);
                        break;
                    case 1:
                        updateSettings(-2l, 7*24*60*60*1000l, true);
                        break;
                    case 2:
                        updateSettings(-2l, AlarmManager.INTERVAL_DAY, true);
                        break;
                    case 3:
                        updateSettings(-2l, AlarmManager.INTERVAL_HALF_DAY, true);
                        break;
                    case 4:
                        updateSettings(-2l, AlarmManager.INTERVAL_HOUR, true);
                        break;
                    case 5:
                        updateSettings(-2l, AlarmManager.INTERVAL_HALF_HOUR, true);
                        break;


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


  
        return myFragmentView;
    }
    //Save the settings
    void updateSettings(long margin, long interval, boolean turnOffAlarmAfterStandUp) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
        if (margin != -2l)
            editor.putLong("Interval", interval);
        if (margin != -2l)
            editor.putLong("WakeMargin", margin);

        editor.putBoolean("TurnOffAlarmAutomatically", turnOffAlarmAfterStandUp);
        editor.apply();

        Toast.makeText(getActivity().getApplicationContext(), "Settings Saved", Toast.LENGTH_SHORT).show();
    }
}
