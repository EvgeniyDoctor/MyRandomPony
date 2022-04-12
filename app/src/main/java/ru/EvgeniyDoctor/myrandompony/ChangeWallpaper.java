package ru.EvgeniyDoctor.myrandompony;


import static android.content.Context.MODE_PRIVATE;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import net.grandcentrix.tray.AppPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;



public class ChangeWallpaper {
    private static AppPreferences settings; // res. - https://github.com/grandcentrix/tray



    public ChangeWallpaper(AppPreferences settings){
        ChangeWallpaper.settings = settings;
    }
    //----------------------------------------------------------------------------------------------



    public void setWallpaper(Context context){
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
        int screen = 0;

        if (settings.contains(Pref.SCREEN_IMAGE)) {
            screen = settings.getInt(Pref.SCREEN_IMAGE, 0);
        }

        switch (screen) {
            case 0: // both
                try {
                    myWallpaperManager.setBitmap(loadWallpaper(context));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 1: // homescreen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0
                    try {
                        myWallpaperManager.setBitmap(loadWallpaper(context), null, true, WallpaperManager.FLAG_SYSTEM);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2: // lockscreen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0
                    try {
                        myWallpaperManager.setBitmap(loadWallpaper(context), null, true, WallpaperManager.FLAG_LOCK);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
    //----------------------------------------------------------------------------------------------



    // open background
    public Bitmap loadWallpaper(Context context) {
        // res. - http://www.vogella.com/tutorials/AndroidApplicationOptimization/article.html#handling-bitmaps
        // https://habrahabr.ru/post/161027/

        File background = new File(
            new ContextWrapper(context).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
            Pref.FILE_NAME_EDITED
        );

        // если существует bg_edited.jpeg, то он и будет открыт, иначе - откроется исходное изо
        // if there is bg_edited.jpeg, then it will be opened, otherwise - the original image will open
        if (!background.exists()) {
            background = new File( // bg.jpeg
                new ContextWrapper(context).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
                Pref.FILE_NAME
            );
        }
        FileInputStream fileInputStream = null;

        if (background.exists()) {
            try {
                fileInputStream = new FileInputStream(background);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (fileInputStream != null) {
            return BitmapFactory.decodeStream(fileInputStream);
        }

        return null;
    }
    //----------------------------------------------------------------------------------------------



    /*
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

     */
    //----------------------------------------------------------------------------------------------
}
