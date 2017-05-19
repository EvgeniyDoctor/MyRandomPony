package ru.EvgeniyDoctor.myrandompony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutostartMyRandomPony extends BroadcastReceiver {
    public AutostartMyRandomPony() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // запуск сервиса при загрузке
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            context.startService(new Intent(context, Service_Refresh.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
