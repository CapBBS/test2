package com.example.administrator.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2016-05-17.
 */
public class ClientService extends IntentService {

    InetAddress targetIP;

    DataOutputStream dos;

    public ClientService() {
        super("ClientService");
        Log.i("TAG", "클라이언트 서비스 시작");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        boolean isConnected = false;
        Socket socket = null;
        ResultReceiver clientResult = (ResultReceiver) intent.getExtras().get(Constants.RESULT_RECEIVER);

        try {
            targetIP = InetAddress.getByName(Constants.HOST_ADRRESS);
        } catch (IOException e) {
            Log.i("TAG", "클라이언트 연결 생성 오류");
        }

        while(!isConnected) {
            try {
                socket = new Socket(targetIP, Constants.CONNECT_PORT);
            } catch (IOException e) {
                Log.i("TAG", "서버 접속 시도 중 오류");
                break;
            }
                isConnected = socket.isConnected();
        }
        String ip = null;
        try {

            ip = socket.getLocalAddress().toString().replaceAll("/", "");
            dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF(ip);
        } catch (IOException e) {

        }
        Bundle result = new Bundle();

        result.putString(Constants.ADDRESS, ip);

        clientResult.send(Constants.CLIENT_ADDRESS_SEND, result);
        try {
            socket.close();
        } catch (IOException e) {
            Log.i("TAG","클라이언트 자원 정리중 오류");
        }

        Log.i("TAG", "클라이언트 서비스 종료");

        stopSelf();
    }
}



