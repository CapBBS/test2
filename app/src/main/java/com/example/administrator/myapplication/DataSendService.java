package com.example.administrator.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016-05-26.
 */
public class DataSendService extends IntentService {

    DataInputStream dis;
    DataOutputStream dos;

    Socket socket = null;

    public DataSendService() {
        super("DataSendService");
        Log.i("TAG", "데이터 샌드 서비스 생성");
    }

    public void onDestroy() {
        stopSelf();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        boolean isConnected = false;
        ArrayList<String> clientsAddressList;

        ResultReceiver resultReceiver = (ResultReceiver) intent.getExtras().get(Constants.RESULT_RECEIVER);
        int action = intent.getExtras().getInt(Constants.ACTION);
        clientsAddressList = intent.getExtras().getStringArrayList(Constants.ADDRESS_LIST);
        Bundle result = new Bundle();

        for(String ip : clientsAddressList) {

            while( !isConnected) {
                try {
                    socket = new Socket(InetAddress.getByName(ip), Constants.DATA_SEND_PORT);
                } catch (UnknownHostException e) {

                } catch (IOException e) {

                }
                isConnected = socket.isConnected();
            }

            try {
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                Log.i("TAG" , "스트림 생성중 에러");
            }

            try {

                dos.writeInt(action);

                switch (action) {

                    case Constants.SEND_MUSIC:

                        FileInputStream fis = new FileInputStream((File)intent.getExtras().get(Constants.MUSIC));
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        byte[] buffer = new byte[4096 * 64];
                        int bytesRead;
                        while(true) {
                            bytesRead = bis.read(buffer, 0, buffer.length);
                            if (bytesRead == -1) {
                                break;
                            }
                            dos.write(buffer, 0, bytesRead);
                            dos.flush();
                        }

                        bis.close();
                        fis.close();

                        break;

                    case Constants.SEND_STATE:

                        dos.writeBoolean(intent.getExtras().getBoolean(Constants.STATE));
                        break;

                    case Constants.SEND_POSITION:
                        int pos = intent.getExtras().getInt(Constants.POSITION);
                        Log.i("TAG", pos+"");
                        dos.writeInt(pos);
                        break;

                }
            } catch (IOException e) {
                Log.i("TAG", "데이터 전송중 에러");
            }

            resultReceiver.send(action, null);

            try {
                dos.close();
                socket.close();
            } catch (IOException e) {
                Log.i("TAG", "소켓 닫는중 에러");
            }



            result.clear();
            isConnected = false;
        }

    }

}
