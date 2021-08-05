package ru.EvgeniyDoctor.myrandompony;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.core.ItemNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;



public class ServiceRefresh extends Service {
    private int
            typeRefreshFrequency;
    private Timer
            timer;
    private Calendar
            calendar;
    static AppPreferences
            settings;
    NotificationCompat.Builder notificationBuilder;
    NotificationManager manager;
    int notificationid = 1;

    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH"); // задание формата для получения часов

    

    public ServiceRefresh() {
    }
    //----------------------------------------------------------------------------------------------



    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    //----------------------------------------------------------------------------------------------



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
    //----------------------------------------------------------------------------------------------



    @Override
    public void onCreate() {
        //Log.d(Helper.tag, "onCreate");
        super.onCreate();

        settings = new AppPreferences(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService();
        }

        // завершение работы, если сервис был запущен при автостарте
        if (!settings.contains(getResources().getString(R.string.enabled_pony_wallpapers)) || !settings.getBoolean(getResources().getString(R.string.enabled_pony_wallpapers), false)) {
            //Log.d(Helper.tag, "onCreate - stopSelf");
            stopSelf();
        }

        typeRefreshFrequency = settings.getInt(getResources().getString(R.string.refresh_frequency), 2);
        registerReceiver(receiver, new IntentFilter(IntentServiceLoadNewWallpaper.NOTIFICATION_LOAD_NEW_WALLPAPER));

        check_time();
    }
    //----------------------------------------------------------------------------------------------



    // res - https://stackoverflow.com/questions/47531742/startforeground-fail-after-upgrade-to-android-8-1
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForegroundService(){
        //Log.d(Helper.tag, "startForegroundService started");
        String NOTIFICATION_CHANNEL_ID = "ru.EvgeniyDoctor.myrandompony";
        String channelName = "My Background Service";

        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null; // I was so assertive
        manager.createNotificationChannel(channel);

        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            //.setContentTitle("App is running")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build();
        startForeground(notificationid, notification);
    }
    //-----------------------------------------------------------------------------------------------



    @Override
    public void onDestroy() {
        //Log.d(Helper.tag, "Service - onDestroy");
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }

        unregisterReceiver(receiver);
        System.gc();
    }
    //----------------------------------------------------------------------------------------------



    public void check_time() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // проверка, доступна ли сеть
                if (!Helper.checkInternetConnection(ServiceRefresh.this, settings.getBoolean(getResources().getString(R.string.wifi_only), true))) {
                    Log.d (Helper.tag, "No network connection or not WIFI, return");
                    return;
                }

                calendar = Calendar.getInstance();
                Log.d (Helper.tag, dateFormat.format(calendar.getTime()) + ":" + calendar.get(Calendar.MINUTE));

                switch (typeRefreshFrequency) {
                    case 1:
                        Log.d (Helper.tag, "case 1");
                        int day = calendar.get(Calendar.DATE);

                        Log.d (Helper.tag, "current day = " + day);
                        try {
                            Log.d (Helper.tag, "saved day = " + settings.getInt(getResources().getString(R.string.refresh_frequency_curr_day)));
                        }
                        catch (ItemNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (day != settings.getInt(getResources().getString(R.string.refresh_frequency_curr_day), 0)) {
                            Log.d(Helper.tag, "true, change began");

                            settings.put(getResources().getString(R.string.refresh_frequency_curr_day), day); // текущее число
                            startLoad();
                        }
                        Log.d (Helper.tag, "-----------------------------------------");

                        updateNotification(typeRefreshFrequency);

                        break;

                    case 2:
                        Log.d (Helper.tag, "case 2");
                        int week = calendar.get(Calendar.WEEK_OF_YEAR);

                        Log.d (Helper.tag, "current week = " + week);
                        try {
                            Log.d (Helper.tag, "saved week = " + settings.getInt(getResources().getString(R.string.refresh_frequency_curr_week)));
                        }
                        catch (ItemNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (week != settings.getInt(getResources().getString(R.string.refresh_frequency_curr_week), 0)) {
                            Log.d(Helper.tag, "true, change began");

                            settings.put(getResources().getString(R.string.refresh_frequency_curr_week), week); // номер текущей недели
                            startLoad();
                        }
                        Log.d (Helper.tag, "-----------------------------------------");

                        updateNotification(typeRefreshFrequency);

                        break;

                    case 3:
                        Log.d (Helper.tag, "case 3");
                        int month = calendar.get(Calendar.MONTH);

                        Log.d (Helper.tag, "current month = " + month);
                        try {
                            Log.d (Helper.tag, "saved month = " + settings.getInt(getResources().getString(R.string.refresh_frequency_curr_month)));
                        }
                        catch (ItemNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (month != settings.getInt(getResources().getString(R.string.refresh_frequency_curr_month), 0)) { // текущий месяц != сохранённому
                            Log.d(Helper.tag, "true, change began");

                            settings.put(getResources().getString(R.string.refresh_frequency_curr_month), month); // текущий месяц
                            startLoad();
                        }
                        Log.d (Helper.tag, "-----------------------------------------");

                        updateNotification(typeRefreshFrequency);

                        break;
                } //switch
            } // run
        }, 0, 60000); // 60000 - раз в минуту
    }
    //----------------------------------------------------------------------------------------------



    private void updateNotification (int frequency){
        String text = "";

        switch (frequency) {
            case 1:
                text = getResources().getString(R.string.settings_radio_1);
                break;
            case 2:
                text = getResources().getString(R.string.settings_radio_2);
                break;
            case 3:
                text = getResources().getString(R.string.settings_radio_3);
                break;
        }

        text = Helper.ucfirst(text);
        notificationBuilder.setContentText(text); // 61 char max
        manager.notify(notificationid, notificationBuilder.build());

    }
    //-----------------------------------------------------------------------------------------------



    private void startLoad() {
        Intent intent = new Intent(ServiceRefresh.this, IntentServiceLoadNewWallpaper.class);
        intent.putExtra(IntentServiceLoadNewWallpaper.FILENAME, getResources().getString(R.string.file_name));
        intent.putExtra(IntentServiceLoadNewWallpaper.URL_STRING, ""); // путь уже содержит id и .jpeg
        intent.putExtra(IntentServiceLoadNewWallpaper.need_change_bg, "1");
        startService(intent);
        System.gc();
    }
    //-----------------------------------------------------------------------------------------------



    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        IntentServiceLoadNewWallpaper.Codes res = (IntentServiceLoadNewWallpaper.Codes)
                intent.getSerializableExtra(IntentServiceLoadNewWallpaper.RESULT);

        if (res == IntentServiceLoadNewWallpaper.Codes.CHANGE_WALLPAPER) {// res. - http://stackoverflow.com/questions/20053919/programmatically-set-android-phones-background
            // установка фона
            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            try {
                myWallpaperManager.setBitmap(open_background()); // установка фона
                System.gc();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        }
    };
    //----------------------------------------------------------------------------------------------



    // открытие фона
    private Bitmap open_background() {
        File background = new File( // open bg.jpeg
                new ContextWrapper(
                        getApplicationContext()).getDir(getResources().getString(R.string.save_path), MODE_PRIVATE), getResources().getString(R.string.file_name));
        FileInputStream f = null;

        try {
            f = new FileInputStream(background);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (f != null) {
            return BitmapFactory.decodeStream(f);
        }

        return null;
    }
    //----------------------------------------------------------------------------------------------
}
