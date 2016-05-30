package com.example.administrator.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Created by Administrator on 2016-05-03.
 */
public class WifiBroadcastReceiver extends BroadcastReceiver{

        private WifiP2pManager wifiP2pManager;
        private Channel channel;
        private Connection activity;
        private MainActivity mActivity;

        public WifiBroadcastReceiver(WifiP2pManager manager, Channel channel, Connection activity,MainActivity mActivity) {
            super();
            this.channel = channel;
            this.wifiP2pManager = manager;
            this.activity = activity;
            this.mActivity = mActivity;
        }

        @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    activity.wifiP2pDeviceList = peers;
                    activity.displayPeerButtons();
                }
            });
        }else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);

            if(networkState.isConnected())
            {
                //set client state so that all needed fields to make a transfer are ready
                //activity.setTransferStatus(true);
                //연결이 실행될때 MainActivity에서 실행할 메소드
                Log.i("TAG","와이파이연결정보를 넘김");
                mActivity.setNetworkToReadyState(wifiInfo);

                if(!wifiInfo.isGroupOwner)
                    mActivity.startClientService();
                //activity.sendIPaddress(wifiInfo);

                /*
                wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup group) {
                        try {
                            Method[] methods = WifiP2pManager.class.getMethods();
                            for (int i = 0; i < methods.length; i++) {
                                if (methods[i].getName().equals("deletePersistentGroup")) {
                                    for (int netid = 0; netid < 32; netid++) {
                                        methods[i].invoke(wifiP2pManager, channel, netid, new WifiP2pManager.ActionListener() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onFailure(int reason) {
                                                Log.i("TAG", "그룹 정보 삭제 실패");
                                            }
                                        });
                                    }
                                }
                            }

                        } catch (InvocationTargetException e) {
                            Log.i("TAG", "인보케이션타겟");
                        } catch (IllegalAccessException e) {
                            Log.i("TAG", "일리갈엑세스");
                        }
                    }
                });
*/


            }
            else
            {
                //set variables to disable file transfer and reset client back to original state

                //연결이 해제될때 MainActivity에서 실행할 메소드
                wifiP2pManager.cancelConnect(channel, null);

            }
            //activity.setClientStatus(networkState.isConnected());

            // Respond to new connection or disconnections
        }
    }
}

