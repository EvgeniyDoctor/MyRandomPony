package ru.EvgeniyDoctor.myrandompony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.grandcentrix.tray.AppPreferences;



public class AutostartMyRandomPony extends BroadcastReceiver {
    public AutostartMyRandomPony() {
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        AppPreferences settings = new AppPreferences(context);

        if (!settings.contains(Pref.ENABLED) || !settings.getBoolean(Pref.ENABLED, false)) {
            Helper.d("Autostart - stop service");
            return;
        }

        // запуск сервиса при загрузке
        Helper.d("Autostart - onReceive");

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Helper.d("Autostart - ACTION_BOOT_COMPLETED");

            Helper.startService(
                context,
                new Intent(context, ServiceRefresh.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );
        }
    }
    //--------------------------------------------------------------------------------------------------
}
