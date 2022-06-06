package ru.EvgeniyDoctor.myrandompony;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

import java.util.Arrays;
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
        int providers = ImageProviders.PROVIDERS_DEFAULT; // checkboxes in settings
        if (settings.contains(Pref.IMAGE_SOURCES)){
            providers = settings.getInt(Pref.IMAGE_SOURCES, ImageProviders.PROVIDERS_DEFAULT);
        }

        DownloadResult code = DownloadResult.NOT_CONNECTED;
        switch (getRandomProvider(providers)) {
            case ImageProviders.DERPIBOORU:
                code = new Derpibooru(context, settings).load();
                break;
            case ImageProviders.MLWP:
                code = new MLWP(context, settings).load();
                break;
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
        else { // change wallpaper
            return DownloadResult.SUCCESS_CHANGE_WALLPAPER;
        }
    }
    //----------------------------------------------------------------------------------------------



    private int getRandomProvider(int providers){
        boolean[] enabledProviders = ImageProviders.toBitsArray(providers);
        final Random random = new Random();

        int r;
        do {
            r = random.nextInt(ImageProviders.TOTAL_PROVIDERS);
        }
        while (!enabledProviders[r]); // until enabled provider will be found

        return r + 1; // +1 bcos ImageProvider final vars has +1 value. E.g., Derpibooru = 0 pos in array, but has value = 1 for Settings checkboxes.
    }
    //----------------------------------------------------------------------------------------------
}
