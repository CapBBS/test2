package com.example.administrator.myapplication;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    //연결과 매니저
    public WifiManager wifiMgr;
    public WifiP2pManager manager;
    public WifiP2pManager.Channel channel;
    private SectionsPagerAdapter mSectionsPagerAdapter;


    //프래그먼트
    Musicplay musicplay = new Musicplay();
    Musiclist musiclist = new Musiclist();
    Connection connection = new Connection();
    Setting setting = new Setting();

    boolean finishFlag = false; // 뒤로가기 버튼 클릭시
    NotificationManager nmanager;

    private ViewPager mViewPager;
    TabLayout tabLayout;
    int[] tabIcons = {
            R.drawable.musicplayer,
            R.drawable.musiclist,
            R.drawable.musicshare,
            R.drawable.musicsettings
    };


    ArrayList<String> clientAddressList;

    //연결시 전달 변수
    File sendToMusic;

    @Override
    public void onBackPressed() {
        if(musicplay.music==null){
            nmanager.cancel(1);
        }else{
            if(!musicplay.music.isPlaying())
                nmanager.cancel(1);
        }

        if (finishFlag) {
            if(musicplay.music==null){
                super.onBackPressed();
            }else{
                if(musicplay.music.isPlaying()) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.MAIN");
                    intent.addCategory("android.intent.category.HOME");
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                            | Intent.FLAG_ACTIVITY_FORWARD_RESULT
                            | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(intent);
                }
                else{
                    super.onBackPressed();
                }
            }
        }
        else {
            Toast.makeText(getBaseContext(), "back키를 한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show();
            finishFlag = true;
        }
    }

    @Override
    protected void onResume() {
        finishFlag = false;
        super.onResume();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if(musicplay.music==null){
            nmanager.cancel(1);
        }else{
            if(!musicplay.music.isPlaying())
                nmanager.cancel(1);
        }
    }

    public void startClientService() {
        Intent clientServiceIntent = new Intent(this, ClientService.class);
        clientServiceIntent.putExtra(Constants.RESULT_RECEIVER, new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                Log.i("TAG", resultData.getString(Constants.ADDRESS));
            }
        });

        startService(clientServiceIntent);

    }

    public void startServerService(){
        Intent serverServiceIntent = new Intent(this,ServerService.class);
        serverServiceIntent.putExtra(Constants.RESULT_RECEIVER, new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, final Bundle resultData) {

                if(resultCode == Constants.CLIENT_ADDRESS_SEND )
                {
                    clientAddressList.add(resultData.getString(Constants.ADDRESS));
                }

            }
        });


        startService(serverServiceIntent);


    }

    public void startDataSendService(int action) {
        Intent intent = new Intent(this, DataSendService.class);
        intent.putExtra(Constants.RESULT_RECEIVER, new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                switch (resultCode) {
                    case Constants.SEND_MUSIC:
                        startDataSendService(Constants.SEND_POSITION);
                        break;
                }
            }
        });
        intent.putExtra(Constants.ADDRESS_LIST, clientAddressList);
        intent.putExtra(Constants.ACTION, action);

        switch (action) {

            case Constants.SEND_MUSIC :
                intent.putExtra(Constants.MUSIC, sendToMusic);
                break;

            case Constants.SEND_STATE :
                intent.putExtra(Constants.STATE, musicplay.music.isPlaying());
                break;

            case Constants.SEND_POSITION :
                int currentPos = musicplay.music.getCurrentPosition();
                intent.putExtra(Constants.POSITION, currentPos);

                musicplay.music.seekTo(currentPos);
                break;

        }

        startService(intent);
    }

    public void startDataReceiveService() {
        Intent intent = new Intent(this, DataReceiveService.class);
        intent.putExtra(Constants.RESULT_RECEIVER, new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, final Bundle resultData) {
                switch (resultCode) {

                    case Constants.SEND_MUSIC:
                        musicplay.receiveMusicStart();
                        break;

                    case Constants.SEND_STATE:
                        if(resultData.getBoolean(Constants.STATE)) {
                            musicplay.music.start();
                        } else {
                            musicplay.music.pause();
                        }
                        break;

                    case Constants.SEND_POSITION:
                        int pos = resultData.getInt(Constants.POSITION);
                        musicplay.music.seekTo(pos);
                        Log.i("TAG", pos+"");
                        break;
                }
            }
        });

        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        nmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setTabIcons();
        createNotification();
        clientAddressList = new ArrayList<>();

    }

    public void setNetworkToReadyState(WifiP2pInfo info) {
        Log.i("TAG", "네트워크 정보가 저장됨");
        if (info.isGroupOwner) {
            Log.i("TAG", "그룹 오너임");

            startServerService();

        } else {
            Log.i("TAG", "그룹 오너가 아님");

            startDataReceiveService();

        }

    }
    /**
     * A placeholder fragment containing a simple view.
     */

    public void setTabIcons() {
        for(int i = 0; i < tabIcons.length; i++ ) {
            tabLayout.getTabAt(i).setIcon(tabIcons[i]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nmanager.cancel(1);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0)
                return musicplay;
            else if(position==1)
                return musiclist;
            else if(position==2)
                return connection;
            else {
                return setting;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }


    }

    // 파일리스트에서 선택한 정보를 뮤직플레이로 넘기는 부분
    public void set_media_state(String file_path, List mlist, int currentmusicpos){
        musicplay.setMusic(file_path,mlist,currentmusicpos);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(connection.wifiBroadcastReceiver);
        } catch (IllegalArgumentException e) {

        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void createNotification() {


        Intent intentMain = new Intent(Intent.ACTION_MAIN);
        intentMain.addCategory(Intent.CATEGORY_LAUNCHER);
        intentMain.setComponent(new ComponentName(this,MainActivity.class));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,intentMain,0);


        Notification.Builder mBuilder = new Notification.Builder(this);
        mBuilder.setSmallIcon(android.R.drawable.ic_media_play);
        mBuilder.setTicker("Notification.Builder");
        mBuilder.setContentTitle("BBS");
        mBuilder.setContentText("앱이 실행중입니다.");
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setOngoing(true);
        mBuilder.setAutoCancel(false);


        nmanager.notify(1, mBuilder.build());

    }

}
