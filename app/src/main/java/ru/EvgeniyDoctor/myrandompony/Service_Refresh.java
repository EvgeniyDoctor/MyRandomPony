package ru.EvgeniyDoctor.myrandompony;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

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

public class Service_Refresh extends Service {

    private int
            type_refresh_frequency;
    private static final String
        tag = "pony";
    private Timer
            timer;
    private Calendar
            calendar;
    static AppPreferences
            settings;
    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH"); // задание формата для получения часов




    public Service_Refresh() {
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
        super.onCreate();

        settings = new AppPreferences(getApplicationContext());

        // завершение работы, если сервис был запущен при автостарте
        if (!settings.contains(getResources().getString(R.string.enabled_pony_wallpapers)) || !settings.getBoolean(getResources().getString(R.string.enabled_pony_wallpapers), false)) {
            stopSelf();
        }

        type_refresh_frequency = settings.getInt(getResources().getString(R.string.refresh_frequency), 2);
        registerReceiver(receiver, new IntentFilter(IntentService_LoadNewWallpaper.NOTIFICATION));

        check_time();
    }
    //----------------------------------------------------------------------------------------------



    @Override
    public void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }

        unregisterReceiver(receiver);
        System.gc();
    }
    //----------------------------------------------------------------------------------------------



    // проверка, доступна ли сеть
    private boolean check_internet_connection(boolean need_type) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        }

        if (need_type) { // если важен тип подключения
            if (netInfo.isConnectedOrConnecting()) {
                if (settings.getBoolean(getResources().getString(R.string.wifi_only), true)) { // если нужен только wifi
                    return netInfo.getTypeName().equals("WIFI");
                }
            }
        }

        return netInfo.isConnectedOrConnecting();
    }
    //----------------------------------------------------------------------------------------------



    public void check_time() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // проверка, доступна ли сеть
                if (! check_internet_connection(settings.getBoolean(getResources().getString(R.string.wifi_only), true))) {
                    Log.e (tag, "No network connection or not WIFI, return");
                    return;
                }

                calendar = Calendar.getInstance();
                Log.e (tag, dateFormat.format(calendar.getTime()) + ":" + calendar.get(Calendar.MINUTE));

                switch (type_refresh_frequency) {
                    case 1:
                        Log.e (tag, "case 1");
                        int day = calendar.get(Calendar.DATE);

                        Log.e (tag, "current day = " + day);
                        try {
                            Log.e (tag, "saved day = " + settings.getInt(getResources().getString(R.string.refresh_frequency_curr_day)));
                        }
                        catch (ItemNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (day != settings.getInt(getResources().getString(R.string.refresh_frequency_curr_day), 0)) {
                            Log.e(tag, "true, change began");

                            settings.put(getResources().getString(R.string.refresh_frequency_curr_day), day); // текущее число

                            Intent intent = new Intent(Service_Refresh.this, IntentService_LoadNewWallpaper.class);
                            intent.putExtra(IntentService_LoadNewWallpaper.FILENAME, getResources().getString(R.string.file_name));
                            intent.putExtra(IntentService_LoadNewWallpaper.URL_STRING, ""); // путь уже содержит id и .jpeg
                            intent.putExtra(IntentService_LoadNewWallpaper.need_change_bg, "1");
                            startService(intent);
                            System.gc();
                        }
                        Log.e (tag, "-----------------------------------------");
                        break;

                    case 2:
                        Log.e (tag, "case 2");
                        int week = calendar.get(Calendar.WEEK_OF_YEAR);

                        Log.e (tag, "current week = " + week);
                        try {
                            Log.e (tag, "saved week = " + settings.getInt(getResources().getString(R.string.refresh_frequency_curr_week)));
                        }
                        catch (ItemNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (week != settings.getInt(getResources().getString(R.string.refresh_frequency_curr_week), 0)) {
                            Log.e(tag, "true, change began");

                            settings.put(getResources().getString(R.string.refresh_frequency_curr_week), week); // номер текущей недели

                            Intent intent = new Intent(Service_Refresh.this, IntentService_LoadNewWallpaper.class);
                            intent.putExtra(IntentService_LoadNewWallpaper.FILENAME, getResources().getString(R.string.file_name));
                            intent.putExtra(IntentService_LoadNewWallpaper.URL_STRING, ""); // путь уже содержит id и .jpeg
                            intent.putExtra(IntentService_LoadNewWallpaper.need_change_bg, "1");
                            startService(intent);
                            System.gc();
                        }
                        Log.e (tag, "-----------------------------------------");
                        break;

                    case 3:
                        Log.e (tag, "case 3");
                        int month = calendar.get(Calendar.MONTH);

                        Log.e (tag, "current month = " + month);
                        try {
                            Log.e (tag, "saved month = " + settings.getInt(getResources().getString(R.string.refresh_frequency_curr_month)));
                        }
                        catch (ItemNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (month != settings.getInt(getResources().getString(R.string.refresh_frequency_curr_month), 0)) { // текущий месяц != сохранённому
                            Log.e(tag, "true, change began");

                            settings.put(getResources().getString(R.string.refresh_frequency_curr_month), month); // текущий месяц

                            Intent intent = new Intent(Service_Refresh.this, IntentService_LoadNewWallpaper.class);
                            intent.putExtra(IntentService_LoadNewWallpaper.FILENAME, getResources().getString(R.string.file_name));
                            intent.putExtra(IntentService_LoadNewWallpaper.URL_STRING, ""); // путь уже содержит id и .jpeg
                            intent.putExtra(IntentService_LoadNewWallpaper.need_change_bg, "1");
                            startService(intent);
                            System.gc();
                        }
                        Log.e (tag, "-----------------------------------------");
                        break;
                } //switch
            } // run
        }, 0, 60000); // 60000 - раз в минуту
    }
    //----------------------------------------------------------------------------------------------



    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.getInt(IntentService_LoadNewWallpaper.RESULT) == 0) { // resultcode
                    // res. - http://stackoverflow.com/questions/20053919/programmatically-set-android-phones-background
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
