package com.example.spec.musicplayer;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;



public class Download extends Service {
    public Download() {
    }

    @Override
    public void onCreate(){
        registerReceiver(myReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    MyBroadcastReceiver myReceiver;

    @Override
    public int onStartCommand(Intent intent, int flag, int id){
        super.onStartCommand(intent, flag, id);

        stopSelf();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy(){
        if(myReceiver != null) {
            unregisterReceiver(myReceiver);
        }
        myReceiver = null;
    }


    public class LocalBinder extends Binder{
         Download getService(){
            return Download.this;
        }
    }



    private final IBinder myBinder = new Download.LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }


class MyBroadcastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent){


    }
}
}
