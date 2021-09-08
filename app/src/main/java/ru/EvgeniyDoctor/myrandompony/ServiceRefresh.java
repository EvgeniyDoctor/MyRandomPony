package ru.EvgeniyDoctor.myrandompony;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

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
    private int typeRefreshFrequency;
    private Timer timer;
    private Calendar calendar;
    private static AppPreferences settings = null;
    private NotificationCompat.Builder notificationBuilder = null;
    private NotificationManager manager = null;
    private final int notificationId = 1;
    private LoadNewWallpaper loadNewWallpaper = null;



    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("d.MM.y HH:mm:ss"); // задание формата для получения часов

    

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
        if (settings == null) {
            settings = new AppPreferences(getApplicationContext());
        }
        if (loadNewWallpaper == null) {
            loadNewWallpaper = new LoadNewWallpaper(
                getApplicationContext(),
                settings,
                true // need to change the background
            );
        }
        return Service.START_STICKY;
    }
    //----------------------------------------------------------------------------------------------



    @Override
    public void onCreate() {
        super.onCreate();

        if (settings == null) {
            settings = new AppPreferences(getApplicationContext());
        }
        if (loadNewWallpaper == null) {
            loadNewWallpaper = new LoadNewWallpaper(
                getApplicationContext(),
                settings,
                true // need to change the background
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService();
        }

        typeRefreshFrequency = settings.getInt(Pref.REFRESH_FREQUENCY, 2);

        checkTime();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            updateNotification(typeRefreshFrequency);
        }
    }
    //----------------------------------------------------------------------------------------------



    // res - https://stackoverflow.com/questions/47531742/startforeground-fail-after-upgrade-to-android-8-1
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForegroundService(){
        //Helper.d("startForegroundService started");
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
            .setSmallIcon(R.drawable.ic_stat_name)
            //.setContentTitle("App is running")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build();
        startForeground(notificationId, notification);
    }
    //-----------------------------------------------------------------------------------------------



    @Override
    public void onDestroy() {
        Helper.d("Service - onDestroy");
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }

        System.gc();
    }
    //----------------------------------------------------------------------------------------------



    public void checkTime() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // checking if the network is available
                if (!Helper.checkInternetConnection(ServiceRefresh.this, settings.getBoolean(Pref.WIFI_ONLY, true))) {
                    Helper.d("No network connection or not WIFI, return");
                    return;
                }

                calendar = Calendar.getInstance();
                Helper.d(dateFormat.format(calendar.getTime()));

                switch (typeRefreshFrequency) {
                    case 1:
                        Helper.d("case 1");
                        int day = calendar.get(Calendar.DATE);

                        Helper.d("current day = " + day);
                        try {
                            Helper.d("saved day = " + settings.getInt(Pref.REFRESH_FREQUENCY_CURR_DAY));
                        }
                        catch (ItemNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (day != settings.getInt(Pref.REFRESH_FREQUENCY_CURR_DAY, 0)) {
                            Helper.d("true, change began");

                            settings.put(Pref.REFRESH_FREQUENCY_CURR_DAY, day); // текущее число
                            startLoad();
                        }
                        Helper.d("-----------------------------------------");
                        break;

                    case 2:
                        Helper.d("case 2");
                        int week = calendar.get(Calendar.WEEK_OF_YEAR);

                        Helper.d("current week = " + week);
                        try {
                            Helper.d("saved week = " + settings.getInt(Pref.REFRESH_FREQUENCY_CURR_WEEK));
                        }
                        catch (ItemNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (week != settings.getInt(Pref.REFRESH_FREQUENCY_CURR_WEEK, 0)) {
                            Helper.d("true, change began");

                            settings.put(Pref.REFRESH_FREQUENCY_CURR_WEEK, week); // номер текущей недели
                            startLoad();
                        }
                        Helper.d("-----------------------------------------");
                        break;

                    case 3:
                        Helper.d("case 3");
                        int month = calendar.get(Calendar.MONTH);

                        Helper.d("current month = " + month);
                        try {
                            Helper.d("saved month = " + settings.getInt(Pref.REFRESH_FREQUENCY_CURR_MONTH));
                        }
                        catch (ItemNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (month != settings.getInt(Pref.REFRESH_FREQUENCY_CURR_MONTH, 0)) { // текущий месяц != сохранённому
                            Helper.d("true, change began");

                            settings.put(Pref.REFRESH_FREQUENCY_CURR_MONTH, month); // текущий месяц
                            startLoad();
                        }
                        Helper.d("-----------------------------------------");
                        break;
                } //switch
            } // run
        }, 0, 60000); // 60000 - once a minute
    }
    //----------------------------------------------------------------------------------------------



    private void updateNotification (int frequency){
        if (notificationBuilder == null || manager == null) {
            Helper.d("ServiceRefresh - updateNotification - notificationBuilder || manager == null");
            return;
        }

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
            default:
                text = getResources().getString(R.string.settings_radio_2);
        }

        text = Helper.ucfirst(text);
        notificationBuilder.setContentText(text); // 61 char max
        manager.notify(notificationId, notificationBuilder.build());
    }
    //-----------------------------------------------------------------------------------------------



    private void startLoad() {
        LoadNewWallpaper.Codes code = loadNewWallpaper.load();
        if (code == LoadNewWallpaper.Codes.CHANGE_WALLPAPER) { // res. - http://stackoverflow.com/questions/20053919/programmatically-set-android-phones-background
            Helper.d("setting new bg!");

            // установка фона
            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            try {
                myWallpaperManager.setBitmap(openBackground()); // setting the background
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Helper.d("ServiceRefresh - startLoad - else: " + code);
        }
    }
    //-----------------------------------------------------------------------------------------------



    // opening the background
    private Bitmap openBackground() {
        File background = new File( // open bg.jpeg
            new ContextWrapper(getApplicationContext()).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
            Pref.FILE_NAME
        );
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
