package com.projectmuun.light.muunlight;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by Micheal on 5/4/2016.
 */
public class NapFragment extends Fragment {

    ToggleButton nap_btn;
    AlarmManager alarmMgr;
    PendingIntent nap_kick_intent;

    //Parent View
    private View myFragmentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initialize Parent View
        myFragmentView = inflater.inflate(R.layout.nap_fragment, container, false);

        nap_btn = (ToggleButton) myFragmentView.findViewById(R.id.nap_toggle_btn);
        nap_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    engageNap();
                } else {
                    disengageNap();
                }
            }
        });
        alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        return myFragmentView;
    }

    public void engageNap () {
        nap_kick_intent = PendingIntent.getBroadcast(getActivity(), new Random().nextInt(), new Intent(getActivity(), KickReciever.class), PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 15);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), nap_kick_intent);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), nap_kick_intent);
    }
    public void disengageNap () {
        if (nap_kick_intent != null)
            alarmMgr.cancel(nap_kick_intent);

    }



}
