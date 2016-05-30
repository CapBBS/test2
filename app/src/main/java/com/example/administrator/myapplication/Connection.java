package com.example.administrator.myapplication;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-05-27.
 */
public class Connection extends Fragment {

    WifiManager wifiManager;
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    WifiBroadcastReceiver wifiBroadcastReceiver;

    protected WifiP2pDeviceList wifiP2pDeviceList;
    ArrayList<String> deviceNameList;
    ListView peerListView;
    Button btnFindPeer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
// Inflate the layout for this fragment
        final MainActivity activity = ((MainActivity)getActivity());
        wifiManager = activity.wifiMgr;
        manager = activity.manager;
        channel = activity.channel;

        IntentFilter wifiP2pIntentFilter = new IntentFilter();
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wifiBroadcastReceiver = new WifiBroadcastReceiver(manager, channel, this, activity);
        activity.registerReceiver(wifiBroadcastReceiver, wifiP2pIntentFilter);

        View view = inflater.inflate(R.layout.connection, container, false);


        btnFindPeer = (Button)view.findViewById(R.id.btnFindpeer);
        peerListView = (ListView) view.findViewById(R.id.peerlist);
        btnFindPeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!wifiManager.isWifiEnabled()) {
                    activity.tabLayout.getTabAt(3).select();
                    Toast.makeText(getActivity(), "와이파이를 켜주세요!", Toast.LENGTH_LONG).show();
                }

                manager.discoverPeers(channel, null);
                Log.i("TAG", "피어찾기를 시작함");
            }
        });

        return view;
    }

    protected void displayPeerButtons() {
        deviceNameList = new ArrayList<String>();
        for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
            deviceNameList.add(device.deviceName);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.musiclist_item, deviceNameList);
        peerListView.setAdapter(arrayAdapter);
        peerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                    if (device.deviceName.equals(deviceNameList.get(position))) {
                        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                        wifiP2pConfig.deviceAddress = device.deviceAddress;
                        wifiP2pConfig.groupOwnerIntent = 0;
                        manager.connect(channel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.i("TAG", "와이파이 다이렉트가 연결됨");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.i("TAG", "와이파이 다이렉트 연결에 실패함");
                            }
                        });

                    }
                }

            }
        });


    }

}