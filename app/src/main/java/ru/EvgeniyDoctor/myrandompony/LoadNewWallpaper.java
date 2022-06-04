package ru.EvgeniyDoctor.myrandompony;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

import java.util.Random;


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
        // todo: settings or random

        DownloadResult code;
        final Random random = new Random();

        int r = random.nextInt(2);
        if (r == 0) {
            code = new Derpibooru(context, settings).load();
        }
        else {
            code = new MLWP(context, settings).load();
        }

        switch (code) {
            case NOT_CONNECTED:
            case NOT_JSON:
            case ERROR_JSON:
            case ERROR_IMAGE:
            case ERROR_SAVE:
            case UNKNOWN:
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
