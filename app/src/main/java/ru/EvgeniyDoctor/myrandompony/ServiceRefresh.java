package ru.EvgeniyDoctor.myrandompony;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;



public class ServiceRefresh extends Service {
    private int typeRefreshFrequency;
    private Timer timer;
    private Calendar calendar;
    private static AppPreferences settings = null;
    private final int notificationId = 1;
    private LoadNewWallpaper loadNewWallpaper = null;
    private final String NOTIFICATION_CHANNEL_ID = "ru.EvgeniyDoctor.myrandompony";



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

        typeRefreshFrequency = settings.getInt(Pref.REFRESH_FREQUENCY, 2);

        Helper.d("typeRefreshFrequency: " + typeRefreshFrequency);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService();
            //updateNotification();
        }
        else { // 7 and lower
            startForegroundServiceOld();
        }

        checkTime();
    }
    //----------------------------------------------------------------------------------------------



    // res - https://stackoverflow.com/questions/47531742/startforeground-fail-after-upgrade-to-android-8-1
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForegroundService(){
        //Helper.d("startForegroundService started");
        String channelName = "My Background Service";

        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null; // I was so assertive
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_stat_name)
            //.setContentTitle(getResources().getString(R.string.app_name))
            .setContentText(getNotificationText())
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build();
        startForeground(notificationId, notification);
    }
    //-----------------------------------------------------------------------------------------------



    // 7 and lower
    private void startForegroundServiceOld(){
        Intent notificationIntent = new Intent(this, Main.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // api 24, android 7
            notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentText(getNotificationText())
                .setContentIntent(pendingIntent)
                .build();
        }
        else { // android 6 and lower
            notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getNotificationText())
                .setContentIntent(pendingIntent)
                .build();
        }

        startForeground(notificationId, notification);
    }
    //-----------------------------------------------------------------------------------------------



    private String getNotificationText(){
        String text;
        switch (typeRefreshFrequency) {
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
        return text;
    }
    //-----------------------------------------------------------------------------------------------



    public void checkTime() {
        int time;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            time = 60000; // 60000 - once a minute
        }
        else {
            time = 60000 * 20;
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                calendar = Calendar.getInstance();
                Helper.d(dateFormat.format(calendar.getTime()));

                // checking if the network is available
                if (!Helper.checkInternetConnection(ServiceRefresh.this, settings.getBoolean(Pref.WIFI_ONLY, true))) {
                    Helper.d("No network connection or not WIFI, return");
                    return;
                }

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
                            startLoad(day);
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
                            startLoad(week);
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
                            startLoad(month);
                        }
                        Helper.d("-----------------------------------------");
                        break;
                } //switch
            } // run
        }, 0, time);
    }
    //----------------------------------------------------------------------------------------------



    private void startLoad (int unit) {
        LoadNewWallpaper.Codes code = loadNewWallpaper.load();
        if (code == LoadNewWallpaper.Codes.SUCCESS_CHANGE_WALLPAPER) { // res. - http://stackoverflow.com/questions/20053919/programmatically-set-android-phones-background
            Helper.d("setting new bg!");

            saveNew(unit);

            // установка фона
            new ChangeWallpaper(settings).setWallpaper(getApplicationContext());
        }
        else {
            Helper.d("ServiceRefresh - startLoad - else: " + code);
        }
    }
    //-----------------------------------------------------------------------------------------------



    // save current day, week or month after successful download
    private void saveNew (int unit) {
        switch (typeRefreshFrequency) {
            case 1:
                settings.put(Pref.REFRESH_FREQUENCY_CURR_DAY, unit);
                break;
            case 2:
                settings.put(Pref.REFRESH_FREQUENCY_CURR_WEEK, unit);
                break;
            case 3:
                settings.put(Pref.REFRESH_FREQUENCY_CURR_MONTH, unit);
                break;
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
}
