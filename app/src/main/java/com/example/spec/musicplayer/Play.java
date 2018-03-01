package com.example.spec.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;


public class Play extends Service {

    MediaPlayer mp;

    final int NOTIFICATION_ID = 1;

    public Play() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try
        {
            StartMusic();
        }
        catch(Exception e)
        {
            Log.d("Error", e.toString());
            MainActivity.DNE();//if we can't start, then assume file doesn't exist
        }

        return super.onStartCommand(intent, flags, startId);
    }//end onstart

    private final IBinder myBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    NotificationCompat.Builder builder;
    Notification notification;
    NotificationManager notificationManager;

    public void StartMusic(){
        if(mp != null){
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.i("RawMediaPlayer", "Music is over");
                    Toast.makeText(getApplicationContext(), "Music is over", Toast.LENGTH_LONG).show();
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
                    notificationManager.cancel(NOTIFICATION_ID);
                    stopSelf();
                }
            });
            mp.start();
        }else{
            mp = new MediaPlayer();
            final File file = new File(Environment.getExternalStorageDirectory() + "/"+Environment.DIRECTORY_DOWNLOADS+ "/secretsong_mario.mp3");
            Uri u = Uri.fromFile(file);
            Log.d("URI", u.toString());
            mp = MediaPlayer.create(this, u);


            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.i("RawMediaPlayer", "Music is over");
                    Toast.makeText(getApplicationContext(), "Music is over", Toast.LENGTH_LONG).show();
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancel(NOTIFICATION_ID);
                    stopSelf();
                }
            });
            mp.start();
        }//end else

        //notify of playing music/service working. Placed here so it doesn't show when item isn't available
        builder = new NotificationCompat.Builder(getApplicationContext());

        builder.setContentTitle("Music is playing");
        builder.setContentText("Enjoy!");
        builder.setSmallIcon(android.R.drawable.ic_media_play);
        builder.setOngoing(true);

        //create Intent
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        //create Notification object, mark it as ongoing
        notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE
        | Notification.FLAG_ONGOING_EVENT;

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }//end start

    public void StopMusic(){
        if( mp != null){
            mp.stop();
            mp.release();
            mp = null;
            notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
        }
        stopSelf();
    }//end stop

    public void PauseMusic(){//pause and remove notification
        if( mp != null){
            mp.pause();
        }
        notificationManager.cancel(NOTIFICATION_ID);
    }//end pause

    public class LocalBinder extends Binder{
        Play getService(){
            return Play.this;
        }
    }


    @Override public boolean onUnbind(Intent intent){
        return true;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mp != null){
            mp.release(); //release the media player resource
            mp = null;
        }

        notificationManager.cancel(NOTIFICATION_ID);
    }//end onDestroy

}//end class
