package org.freedesktop.gstreamer.tutorials.tutorial_2;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

public class ForegroundServiceLauncher
{
    private static volatile ForegroundServiceLauncher foregroundServiceLauncher;

    public static ForegroundServiceLauncher getInstance()
    {
        if (foregroundServiceLauncher == null)
        {
            synchronized (ForegroundServiceLauncher.class)
            {
                if (foregroundServiceLauncher == null)
                {
                    foregroundServiceLauncher = new ForegroundServiceLauncher();
                }
            }
        }
        return foregroundServiceLauncher;
    }

    private ForegroundServiceLauncher()
    {
        //Prevent form the reflection api.
        if (foregroundServiceLauncher != null)
        {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public synchronized void startService(Context context)
    {
        if (!isServiceRunning(context))
        {
            if (isOreoOrHigher())
            {
                context.startForegroundService(new Intent(context, DataFlairService.class));
            }
            else
            {
                context.startService(new Intent(context,DataFlairService.class));
            }
        }

    }

    public synchronized void stopService(Context context)
    {
        context.stopService(new Intent(context, DataFlairService.class));
    }

    private boolean isOreoOrHigher()
    {
        return true;
    }

    private boolean isServiceRunning(Context ctx) {

        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DataFlairService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
