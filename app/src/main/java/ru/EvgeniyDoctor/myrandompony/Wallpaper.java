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



// needed type of the image
enum Image {
    Original,
    Edited,
}



public class Wallpaper {
    private static AppPreferences settings; // res. - https://github.com/grandcentrix/tray
    private final Image image;
    private final Context context;



    public Wallpaper(Context context, AppPreferences settings, Image image){
        Wallpaper.settings = settings;
        this.image = image;
        this.context = context;
    }
    //----------------------------------------------------------------------------------------------



    public void set(){
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
        int screen = 0; // both screens

        if (settings.contains(Pref.SCREEN_IMAGE)) {
            screen = settings.getInt(Pref.SCREEN_IMAGE, 0);
        }

        switch (screen) {
            case 0: // both
                try {
                    myWallpaperManager.setBitmap(load());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 1: // homescreen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0
                    try {
                        myWallpaperManager.setBitmap(load(), null, true, WallpaperManager.FLAG_SYSTEM);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2: // lockscreen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0
                    try {
                        myWallpaperManager.setBitmap(load(), null, true, WallpaperManager.FLAG_LOCK);
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
    public Bitmap load() {
        // res. - http://www.vogella.com/tutorials/AndroidApplicationOptimization/article.html#handling-bitmaps
        // https://habrahabr.ru/post/161027/

        File background;
        FileInputStream fileInputStream = null;

        if (image == Image.Edited) { // called in Main
            background = new File(
                new ContextWrapper(context).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
                Pref.IMAGE_EDITED
            );

            // если существует bg_edited.jpeg, то он и будет открыт, иначе - откроется исходное изо
            // if there is bg_edited.jpeg, then it will be opened, otherwise - the original image will open
            if (!background.exists()) {
                background = new File( // bg.jpeg
                    new ContextWrapper(context).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
                    Pref.IMAGE_ORIGINAL
                );
            }
        }
        else { // Image.Original, called in ServiceRefresh only
            background = new File( // open bg.jpeg
                new ContextWrapper(context).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
                Pref.IMAGE_ORIGINAL
            );
        }

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



    public boolean exists(Image image){
        File file;

        if (image == Image.Original) {
            file = new File(
                new ContextWrapper(context).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
                Pref.IMAGE_ORIGINAL
            );
        }
        else {
            file = new File(
                new ContextWrapper(context).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
                Pref.IMAGE_EDITED
            );
        }

        return file.exists();
    }
    //----------------------------------------------------------------------------------------------
}
