package com.example.administrator.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016-05-28.
 */
public class DataReceiveService extends IntentService {

    DataInputStream dis;
    DataOutputStream dos;
    ServerSocket serverSocket;
    Socket socket = null;
    int action;

    public DataReceiveService() {
        super("DataSendService");
        Log.i("TAG", "데이터 리시브 서비스 생성");
    }

    public void onDestroy() {
        stopSelf();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver resultReceiver = (ResultReceiver) intent.getExtras().get(Constants.RESULT_RECEIVER);
        boolean serviceEnabled = true;

        Bundle result = new Bundle();
        try {
            serverSocket = new ServerSocket(Constants.DATA_SEND_PORT);
        } catch (IOException e) {
            Log.i("TAG", "서버 소켓 생성중 오류");
        }

        while(serviceEnabled) {

            Log.i("TAG", "서버로 부터 접속대기중");
            try {
                socket = serverSocket.accept();

                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                Log.i("TAG", "서버로부터 접속 대기중 오류");
            }

            try {


                action = dis.readInt();

                switch (action) {

                    case Constants.SEND_MUSIC:
                        File musicFile = new File("/storage/emulated/0/Download/a.mp3");

                        byte[] buffer = new byte[4096 * 64];
                        int bytesRead;

                        FileOutputStream fos = new FileOutputStream(musicFile);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);

                        while (true) {
                            bytesRead = dis.read(buffer, 0, buffer.length);
                            if (bytesRead == -1) {
                                break;
                            }
                            bos.write(buffer, 0, bytesRead);
                            bos.flush();
                        }

                        bos.close();
                        fos.close();

                        break;

                    case Constants.SEND_STATE:

                        result.putBoolean(Constants.STATE, dis.readBoolean());
                        break;

                    case Constants.SEND_POSITION:
                        result.putInt(Constants.POSITION, dis.readInt());
                        break;

                }

            } catch (IOException e) {
                Log.i("TAG", "데이터 수신중 에러");
            }
            resultReceiver.send(action, result);

            result.clear();

        }
        try {
            dis.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {

        }
        stopSelf();

    }

}
