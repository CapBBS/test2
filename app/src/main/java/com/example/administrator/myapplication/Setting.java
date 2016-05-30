package com.example.administrator.myapplication;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import javax.crypto.Mac;


/**
 * Created by Administrator on 2016-05-27.
 */
public class Setting extends Fragment {

    WifiManager wifiManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
// Inflate the layout for this fragment
        MainActivity activity = ((MainActivity)getActivity());
        wifiManager = activity.wifiMgr;
        View view = inflater.inflate(R.layout.setting, container, false);
        Button btnwifi = (Button)view.findViewById(R.id.btnwifi);
        btnwifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                    Toast.makeText(getActivity(), "와이파이가 켜집니다.", Toast.LENGTH_LONG).show();
                } else {
                    wifiManager.setWifiEnabled(false);
                    Toast.makeText(getActivity(), "와이파이가 꺼집니다.", Toast.LENGTH_LONG).show();

                }

            }
        });
        return view;
    }

}