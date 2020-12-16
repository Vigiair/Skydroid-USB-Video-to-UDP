package org.freedesktop.gstreamer.tutorials.tutorial_2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.widget.Toast;

import com.shenyaocn.android.UartVideo.VideoClient;
import com.shenyaocn.android.UartVideo.usb.DeviceFilter;
import com.shenyaocn.android.UartVideo.usb.USBMonitor;

import org.freedesktop.gstreamer.GStreamer;

import java.util.List;

public class DataFlairService extends Service{

    //GStreamer
    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private long native_custom_data;      // Native code will use this to keep private data
    public native void receiveVideoData(byte[] h264);

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING

    String ACTION_STOP_SERVICE= "STOP";

    //VideoClient
    private VideoClient video;
    private USBMonitor mUSBMonitor;
    private UsbDevice uartDevice;

    private static final String NOTIFICATION_CHANNEL_ID ="notification_channel_id";
    private static final String NOTIFICATION_Service_CHANNEL_ID = "service_channel";

    //VideoClient
    private boolean deviceHasConnected(UsbDevice device) {
        return device != null && device.equals(uartDevice);
    }
    //VideoClient
    private static boolean deviceIsUartVideoDevice(UsbDevice device) {
        return device != null && (device.getVendorId() == 4292 && device.getProductId() == 60000);
    }
    //VideoClient
    private synchronized void disconnected() {
        video.stopPlayback();
        uartDevice = null;
    }
    //VideoClient
    final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        // USB device attach
        // USB设备插入
        @Override
        public void onAttach(final UsbDevice device) {

            if(deviceHasConnected(device) || uartDevice != null)
                return;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (device == null) {
                            final List<UsbDevice> devices = mUSBMonitor.getDeviceList();
                            if (devices.size() == 1) {
                                mUSBMonitor.requestPermission(devices.get(0));
                            }
                        } else {
                            mUSBMonitor.requestPermission(device);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        // USB device detach
        // USB设备物理断开
        @Override
        public void onDettach(UsbDevice device) {
            if(!deviceIsUartVideoDevice(device)) {
                return;
            }

            if(!deviceHasConnected(device)) {
                return;
            }

            disconnected();
        }

        // Open video device
        // boolean VideoClient.openUsbSerial(UsbDevice device)
        // device: Usb serial device that has obtained permission. Refer to the following sample code for more info
        // return value, success on true, failure on false

        // USB device has obtained permission
        // USB设备获得权限
        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            if(!deviceIsUartVideoDevice(device)) {
                return;
            }
            if(deviceHasConnected(device)) {
                return;
            }

            synchronized (this) {
                if(deviceIsUartVideoDevice(device)) {
                    if(video.openUsbSerial(device))
                        uartDevice = device;
                }
            }
        }
        // USB device disconnected
        // USB设备关闭连接
        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
            if(!deviceIsUartVideoDevice(device)) {
                return;
            }

            if(!deviceHasConnected(device)) {
                return;
            }

            ForegroundServiceLauncher goingOnForever = ForegroundServiceLauncher.getInstance();
            goingOnForever.stopService(getBaseContext());

            disconnected();
        }
        // USB device obtained permission failed
        // USB设备权限获取失败
        @Override
        public void onCancel() {

        }
    };

    //private MediaPlayer player;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "Stream starting...", Toast.LENGTH_LONG).show();

        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            Log.d("ForegroundService","called to cancel service");
            stopSelf();
        }

        Intent stopSelf = new Intent(this, DataFlairService.class);
        stopSelf.setAction(ACTION_STOP_SERVICE);

        PendingIntent pStopSelf = PendingIntent
                .getService(this, 0, stopSelf
                        ,PendingIntent.FLAG_CANCEL_CURRENT);

        int icon = R.drawable.gstreamer_logo_2;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            icon = R.drawable.gstreamer_logo_2;
        }

        Intent notificationIntent = new Intent(this, Tutorial2.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setContentIntent(pendingIntent)
                .setContentTitle("Service")
                .setContentText("Running...")
                .addAction(R.drawable.gstreamer_logo_2, "STOP STREAM", pStopSelf);
        Notification notification=builder.build();

        if(Build.VERSION.SDK_INT>=26) {

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_Service_CHANNEL_ID, "Sync Service", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Service Name");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            notification = new Notification.Builder(this,NOTIFICATION_Service_CHANNEL_ID)
                    .setContentTitle("Streaming...")
                    //.setContentText("Running...")
                    .setSmallIcon(icon)
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.gstreamer_logo_2, "STOP STREAM", pStopSelf)
                    .build();
        }

        startForeground(121, notification);

        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            //finish();
            return START_NOT_STICKY;
        }

        //if (savedInstanceState != null) {
        //    is_playing_desired = savedInstanceState.getBoolean("playing");
        Log.i ("GStreamer", "Activity created. Saved state is playing:" + is_playing_desired);
        //} else {
        //    is_playing_desired = false;
        //    Log.i ("GStreamer", "Activity created. There is no saved state, playing: false");
        //}
        nativeInit();

        //VideoClient
        video = new VideoClient(this);

        // Set resolution VideoClient.setVideoSize(VideoClient.Sizes size)
        // size resolution enumeration
        //        Size_Smooth
        //        Size_SD
        //        Size_HD
        //        Size_FHD
        video.setVideoSize(VideoClient.Sizes.Size_HD);

        // Set brightness/contrast/hue/saturation
        // VideoClient.setCSC(int brightness, int contrast, int hue, int saturation)
        // range 0 - 100
        video.setCSC(50, 80, 50, 80);

        // Set the exposure
        // VideoClient.setExposureTime(int time, boolean auto)
        // time: exposure time range 0 - 16384
        // auto: enable auto exposure, set true to ignore the time parameter
        video.setExposureTime(0, true);

        //VideoClient
        video.setClientListener(new VideoClient.ClientListener() {

            @Override
            public void onH264Received(byte[] h264) {
                // Raw H.264 data callback

                receiveVideoData(h264);
            }

            @Override
            public void onGPSReceived(byte[] bytes) {
                // GPS data callback
            }

            @Override
            public void onDataReceived(byte[] bytes) {
                // Transmission data callback
            }

            @Override
            public void onDebugReceived(byte[] bytes) {
                // Debug data callback
            }

            @Override
            public void onError(String error) {
                // Error data callback
            }
        });

        //VideoClient
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
        mUSBMonitor.setDeviceFilter(filters);
        mUSBMonitor.register();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nativeFinalize();
        //player.stop();
        disconnected();

        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        Toast.makeText(this, "Stream stopped", Toast.LENGTH_LONG).show();
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i ("GStreamer", "Gst initialized. Calling native play");
        // Restore previous playing state
        //if (is_playing_desired) {
            nativePlay();
        //} else {
        //    nativePause();
        //}
    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {}

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("tutorial-2");
        nativeClassInit();
    }
}
