package ru.EvgeniyDoctor.myrandompony;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;


// class for load new image. Working for BackgroundService, ForegroundService and "Next" button



public class LoadNewWallpaper {
    private final Context context;
    private final AppPreferences settings;
    private final boolean needChangeBg;



    // constructor
    LoadNewWallpaper(Context context, AppPreferences settings, boolean needChangeBg) {
        this.context = context;
        this.settings = settings;
        this.needChangeBg = needChangeBg;
    }
    //-----------------------------------------------------------------------------------------------



    // load new image
    public DownloadResult load() {
        // todo
        // 1. random - mlwp or derpi
        // 2. load

        DownloadResult code = new MLWP(context, settings).load();

        switch (code) {
            case NOT_CONNECTED:
            case NOT_JSON:
            case ERROR_JSON:
            case ERROR_IMAGE:
            case ERROR_SAVE:
            case UNKNOWN:
                Helper.d("LoadNewWallpaper load - bad code: " + code);
                return DownloadResult.NOT_CONNECTED;
        }

        // send result
        if (!needChangeBg) { // не нужно менять фон
            return DownloadResult.SUCCESS;
        }
        else { // нужно изменить фон // change wallpaper
            return DownloadResult.SUCCESS_CHANGE_WALLPAPER;
        }
    }
    //----------------------------------------------------------------------------------------------
}
