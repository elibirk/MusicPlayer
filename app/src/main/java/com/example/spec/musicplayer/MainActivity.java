package com.example.spec.musicplayer;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button btn_start;
    Button btn_stop;
    Button btn_pause;
    DownloadManager dm;
    static ImageView iv;
    static TextView title;
    static TextView artist;

    static Uri uri;
    boolean isBound = false;

    Play myService;

    MyBroadcastReceiver myReceiver;
    final int NOTIFICATION_ID = 1;

    String url = "http://www.primetechconsult.com/CIS472/secretsong_mario.mp3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //basic button functions
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartMusic();
            }
        });


        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StopMusic();
            }
        });

        btn_pause = (Button) findViewById(R.id.btn_pause);
        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PauseMusic();
            }
        });

        iv = (ImageView) findViewById(R.id.cover);
        title = (TextView) findViewById(R.id.title);
        artist = (TextView) findViewById(R.id.artist);

        //if the file already exists, act as if it was downloaded
        final File file = new File(Environment.getExternalStorageDirectory() + "/"+Environment.DIRECTORY_DOWNLOADS+ "/");
        File[] list=file.listFiles();
        if (list==null) {
            //do nothing
        } else {
            for(int i=0;i < list.length;i++) {
                //if we already have the file, set the image & text
                if(list[i].getName().equals("secretsong_mario.mp3")) {
                    iv.setImageResource(R.drawable.albumcover);
                    String[] separated = list[i].getName().split("\\_|\\.");
                    Log.d("title", separated[0]);
                    title.setText(separated[0]);
                    Log.d("title", separated[1]);
                    artist.setText(separated[1]);
                }
            }
        }//end else

        //connect to the play class
        Intent serviceIntent = new Intent(getApplicationContext(), Play.class);
        bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE);

    }//end onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }//end onCreateMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //decide which option item is selected
        if (id == R.id.download) {//if user chooses to download, then download
            dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "secretsong_mario.mp3");
            dm.enqueue(request);
            myReceiver = new MyBroadcastReceiver();
            registerReceiver(myReceiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            uri = dm.getUriForDownloadedFile(id);
            Intent serviceIntent = new Intent(getApplicationContext(), Download.class);
            startService(serviceIntent);
        }

        if (id == R.id.exit){//if user wants to exit, then call finish
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    //start stop pause intents
    public void StartMusic(){
        //create an intent and start the service
        Intent serviceIntent = new Intent(getApplicationContext(), Play.class);
        startService(serviceIntent);
        bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE);
    }

    public void StopMusic(){
        //basically destroy the service since it isn't doing anything
        myService.StopMusic();
        if (isBound) {
            unbindService(myConnection);
            isBound = false;
        }
        Intent intent = new Intent(MainActivity.this,
                Play.class);
        stopService(intent);
    }

    public void PauseMusic(){
        myService.PauseMusic();
    }

    @Override
    public void onStop(){
        super.onStop();
        //unregister receiver
        if(myReceiver != null){
            unregisterReceiver(myReceiver);
            myReceiver = null;
        }//release receiver
        if(isBound) {
            unbindService(myConnection);
            isBound = false;
        }
    }

    //warning if the file doesn't exist
    public static void DNE(){
        title.setText("File does not exist.\nTry downloading.");
    }

    //broadcast receiver to handle download
    class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

            builder.setContentTitle("Download complete");
            builder.setContentText("Enjoy!");
            builder.setSmallIcon(android.R.drawable.stat_sys_download_done);


            //create Intent
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_ONE_SHOT);

            builder.setContentIntent(pendingIntent);

            //create Notification object
            Notification notification = builder.build();

            notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);


            final File file = new File(Environment.getExternalStorageDirectory() + "/"+Environment.DIRECTORY_DOWNLOADS+ "/");
            File[] list=file.listFiles();
            if (list!=null) {
                for(int i=0;i < list.length;i++) {
                    //if we already have the file,set the image & text
                    if(list[i].getName().equals("secretsong_mario.mp3")) {
                        iv.setImageResource(R.drawable.albumcover);
                        String[] separated = list[i].getName().split("\\_|\\.");
                        Log.d("title", separated[0]);
                        title.setText(separated[0]);
                        Log.d("title", separated[1]);
                        artist.setText(separated[1]);
                    }
                }
            }//end if


            iv.setImageResource(R.drawable.albumcover);
        }
    }

    //service connection to bind them
    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //It has bound to MyBoundService, caset the IBinder and get MyBoundService instance
            Play.LocalBinder binder = (Play.LocalBinder) iBinder;

            myService = binder.getService();

            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

}
