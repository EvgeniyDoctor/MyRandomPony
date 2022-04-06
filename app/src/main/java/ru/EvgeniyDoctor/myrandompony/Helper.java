package ru.EvgeniyDoctor.myrandompony;

// some common things

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import net.grandcentrix.tray.AppPreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static androidx.core.content.ContextCompat.startForegroundService;



public class Helper {
    static final String tag = "edoctor"; // tag for logs



    // checking if the network is available
    public static boolean checkInternetConnection(Context context, boolean need_type) {
        AppPreferences settings = new AppPreferences(context);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        }

        if (need_type) { // if the connection type is important
            if (netInfo.isConnectedOrConnecting()) {
                if (settings.getBoolean(Pref.WIFI_ONLY, true)) { // если нужен только wifi // wifi only
                    return netInfo.getTypeName().equals("WIFI");
                }
            }
        }

        return netInfo.isConnectedOrConnecting();
    }
    //----------------------------------------------------------------------------------------------



    // из-за того, что некоторые строки заданы в ресурсах многострочно, они выводятся с пробелами в начале строки. Этот метод это исправляет
    // due to the fact that some lines are set multiline in resources, they are output with spaces at the beginning of the line. This method fixes it
    public static String removeSpacesFromStringStart (String text){
        String[] arr;
        int i;

        arr = text.split("\\n\\n");
        for (i=0; i<arr.length; ++i) {
            arr[i] = arr[i].trim();
        }
        text = TextUtils.join("\n\n", arr);

        arr = text.split("\\n");
        for (i=0; i<arr.length; ++i) {
            arr[i] = arr[i].trim();
        }
        text = TextUtils.join("\n", arr);

        return text;
    }
    //-----------------------------------------------------------------------------------------------



    // включение или отключение кнопок // enabling or disabling buttons
    public static void toggleViewState (Context context, View view, boolean state){
        if (state) {
            view.setEnabled(true);
            view.setBackgroundColor(Themes.getThemeColorById(context, R.attr.colorButton));
        }
        else {
            view.setEnabled(false);
            view.setBackgroundColor(Themes.getThemeColorById(context, R.attr.colorButtonSemitransparent));
        }
    }
    //-----------------------------------------------------------------------------------------------



    // старт сервиса в зависимости от платформы // start of the service depending on the platform
    public static void startService (Context context){
        d("Helper - startService - startForegroundService");
        ContextCompat.startForegroundService(context, new Intent(context, ServiceRefresh.class)); // res: https://stackoverflow.com/questions/64932667/android-workmanager-start-delay-after-killing-app
    }
    //--------------------------------------------------------------------------------------------------



    // for Autostart
    public static void startService (Context context, Intent intent){
        d("Helper - startService + intent - startForegroundService");
        ContextCompat.startForegroundService(context, intent);
    }
    //--------------------------------------------------------------------------------------------------



    // make the first char Big
    public static String ucfirst (String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    //-----------------------------------------------------------------------------------------------



    // debug log
    public static <T> void d (T text) {
        try {
            Log.d(tag, text + "");
        }
        catch (Exception e) {
            Log.d(tag, "Helper d error:");
            e.printStackTrace();
        }
    }
    //-----------------------------------------------------------------------------------------------



    // timestamp
    public static String now(){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return s.format(new Date());
    }
    //-----------------------------------------------------------------------------------------------



    // log to file in device memory, not SD card
    public static <T> void f (T text) {
        // <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        String root = Environment.getExternalStorageDirectory().toString();
        File dir = new File(root + "/My Random Pony");
        File file = new File(dir, "log.txt");

        if (!dir.exists()) {
            dir.mkdir();
        }

        try {
            FileOutputStream out = new FileOutputStream(file, true); // true - append
            out.write((text + "\n").getBytes());
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    //-----------------------------------------------------------------------------------------------



    // log to file in private app folder
    public static <T> void f (Context context, T text) {
        File log = new File(
            new ContextWrapper(context).getDir(Pref.SAVE_PATH, Context.MODE_APPEND),
            "log.txt"
        );

        FileWriter writer = null;
        try {
            writer = new FileWriter(log, true);
            writer.write(text + "\n");
        }
        catch (IOException e) {
            // ignore
        }
        finally {
            try {
                if (writer != null)
                    writer.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
    }
    //-----------------------------------------------------------------------------------------------
}
