package com.example.administrator.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2016-05-17.
 */
public class ServerService extends IntentService {

    ServerSocket serverSocket;

    DataInputStream dis;

    Socket socket = null;


    public ServerService() {
        super("ServerService");
        Log.i("TAG", "서버 서비스 생성");
    }

    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean serviceEnabled = true;
        ResultReceiver serverResult = (ResultReceiver) intent.getExtras().get(Constants.RESULT_RECEIVER);

        try {
            serverSocket = new ServerSocket(Constants.CONNECT_PORT);
        } catch (IOException e) {
            Log.i("TAG", "서버소켓 생성 오류");
        }

        Bundle result = new Bundle();

        while (serviceEnabled) {

            try {

                Log.i("TAG", "클라이언트 접속 대기중");
                socket = serverSocket.accept();

                String ip = socket.getInetAddress().toString().replaceAll("/", "");

                dis = new DataInputStream(socket.getInputStream());
                Log.i("TAG", ip);

                Log.i("TAG",dis.readUTF());

                result.putString(Constants.ADDRESS, ip);
                serverResult.send(Constants.CLIENT_ADDRESS_SEND, result);

                result.clear();

                socket.close();
            } catch (IOException e) {
                Log.i ("TAG", "클라이언트 접속 대기중 오류");
                serviceEnabled = false;
            }
        }

        stopSelf();

    }

}
