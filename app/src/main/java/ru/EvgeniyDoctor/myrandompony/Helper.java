package ru.EvgeniyDoctor.myrandompony;

// some common things

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import net.grandcentrix.tray.AppPreferences;

import static androidx.core.content.ContextCompat.startForegroundService;



public class Helper {
    static final String tag = "edoctor"; // tag for logs
    static final String ACTION_NEXT_BUTTON = "ACTION_NEXT_BUTTON"; // действие для Intent, указывающее, что запуск состоялся при нажатии на кнопку "Дальше"



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
                if (settings.getBoolean(context.getResources().getString(R.string.wifi_only), true)) { // если нужен только wifi // wifi only
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



    // включение или отключение кнопок // enabling or disablin buttons
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //d("Helper - startService - startForegroundService");
            startForegroundService(
                context,
                new Intent(
                    context, ServiceRefresh.class
                )
            );
        }
        else { // normal version of android
            //d("Helper - startService - startService");
            context.startService(new Intent(context, ServiceRefresh.class));
        }
    }
    //--------------------------------------------------------------------------------------------------



    // for Next button and Autostart
    public static void startService (Context context, Intent intent){
        // если нажата кнопка "Дальше", незачем запускать ForegroundService, потому что активити уже открыто
        // if the "Next" button is pressed, there is no need to start ForegroundService, because the activity already opened
        if (intent.getAction() != null && intent.getAction().equals(ACTION_NEXT_BUTTON)) {
            //d("Helper - startService + intent - Next btn - startService");
            context.startService(intent);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //d("Helper - startService + intent - startForegroundService");
            context.startForegroundService(intent);
        }
        else { // normal version of android
            //d("Helper - startService + intent - startService");
            context.startService(intent);
        }
    }
    //--------------------------------------------------------------------------------------------------



    // make the first char Big
    public static String ucfirst (String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    //-----------------------------------------------------------------------------------------------



    public static void d (String text){
        Log.d(tag, text);
    }
    public static void d (int text){
        Log.d(tag, "" + text);
    }
    //-----------------------------------------------------------------------------------------------
}
