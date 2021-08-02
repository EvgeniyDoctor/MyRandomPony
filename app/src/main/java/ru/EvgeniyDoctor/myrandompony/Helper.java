package ru.EvgeniyDoctor.myrandompony;

// some common things

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import static androidx.core.content.ContextCompat.startForegroundService;



public class Helper {
    static final String tag = "edoctor"; // tag for logs
    static final String ACTION_NEXT_BUTTON = "ACTION_NEXT_BUTTON"; // действие для Intent, указывающее, что запуск состоялся при нажатии на кнопку "Дальше"



    // старт сервиса в зависимости от платформы
    public static void startService (Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(Helper.tag, "Helper - startService - startForegroundService");
            startForegroundService(
                context,
                new Intent(
                    context, ServiceRefresh.class
                )
            );
        }
        else { // normal version of android
            Log.d(Helper.tag, "Helper - startService - startService");
            context.startService(new Intent(context, ServiceRefresh.class)); // no action
        }
    }
    //--------------------------------------------------------------------------------------------------



    // for Next button and Autostart
    public static void startService (Context context, Intent intent){
        if (intent.getAction() != null && intent.getAction().equals(ACTION_NEXT_BUTTON)) { // если нажата кнопка "Дальше", незачем запускать ForegroundService, потому что активити будет открыто
            Log.d(Helper.tag, "Helper - startService + intent - Next btn - startService");
            context.startService(intent);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(Helper.tag, "Helper - startService + intent - startForegroundService");
            context.startForegroundService(intent);
        }
        else { // normal version of android
            Log.d(Helper.tag, "Helper - startService + intent - startService");
            context.startService(intent);
        }
    }
    //--------------------------------------------------------------------------------------------------
}
