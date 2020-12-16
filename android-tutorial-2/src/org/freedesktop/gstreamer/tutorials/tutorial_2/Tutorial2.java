package org.freedesktop.gstreamer.tutorials.tutorial_2;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.widget.EditText;
import android.widget.Toast;

import android.app.ActivityManager;
import android.content.Context;


import org.freedesktop.gstreamer.GStreamer;


public class Tutorial2 extends Activity {

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        startStream();
        finish();
    }

    public void startStream() {
        ForegroundServiceLauncher goingOnForever = ForegroundServiceLauncher.getInstance();
        goingOnForever.startService(this);
    }
    public void stopStream() {
        ForegroundServiceLauncher goingOnForever = ForegroundServiceLauncher.getInstance();
        goingOnForever.stopService(this);
    }

    public class NotificationReceiver extends BroadcastReceiver {

        public NotificationReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Update the notification
        }
    }


    protected void onDestroy() {
        super.onDestroy();
        //stopStream();
    }

}
