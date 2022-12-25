package com.nicobrailo.rcc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommandDispatcher extends Service implements Wget.Callback {
    private boolean serviceRunning = false;
    private ScheduledExecutorService executor;
    private static final int CONN_TIMEOUT_MS = 250;
    private Wget wget;

    public CommandDispatcher() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioService = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
        startForeground(1, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("START")) {
            Log.e("XXXXXX", "START" + intent.getAction());
            startBackgroundRun();
        } else {
            Log.e("XXXXXX", intent.getAction());
        }
        return START_STICKY;
    }

    private void startBackgroundRun() {
        if (serviceRunning) return;
        serviceRunning = true;
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::run, getRunPeriodInSeconds(), getRunPeriodInSeconds(), TimeUnit.SECONDS);
    }


    private Notification createNotification() {
        final String notificationChannelId = "RCC Service Notification Channel";
        createNotificationChannel(notificationChannelId);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return notificationBuilder(notificationChannelId)
                .setContentTitle("Device managed")
                .setContentText("Device managed by RCC")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker text RCC")
                .setPriority(Notification.PRIORITY_HIGH)
                .build();
    }
    private void createNotificationChannel(String notificationChannelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(notificationChannelId, notificationChannelId, NotificationManager.IMPORTANCE_HIGH);
            mgr.createNotificationChannel(channel);
        }
    }

    private Notification.Builder notificationBuilder(String notificationChannelId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return new Notification.Builder(this, notificationChannelId);
        } else {
            return new Notification.Builder(this);
        }
    }


    protected long getRunPeriodInSeconds() { return 1; }

    protected long run() {
        wget = new Wget(this);
        Log.e("XXXXXX", "RUNNING");
        if (wget.wget("http://192.168.1.50:4321/get_pending")) {
            Log.e("XXXXXX", "POSTED");
        } else {
            Log.e("XXXXXX", "STILL RUNNING PREV...");
        }
        return 0;
    }

    private AudioManager audioService;


    @Override
    public void onResponse(String result) {
        if (result.contains("volume_down")) {
            audioService.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        }
        if (result.contains("volume_up")) {
            audioService.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        }
        Log.e("XXXXXX", result);
    }

    @Override
    public void onConnectionError(String message) {
        Log.e("XXXXXX", "Failed to connect" + message);
    }

    @Override
    public void onHttpNotOkResponse() {
        Log.e("XXXXXX", "HTTP error code");
    }
}