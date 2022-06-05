package ru.EvgeniyDoctor.myrandompony;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.grandcentrix.tray.AppPreferences;

public class Settings extends AppCompatActivity {
    private static AppPreferences settings; // res. - https://github.com/grandcentrix/tray
    private CheckBox
        checkBoxWifiOnly;
    private TextView
        textScreenImage,
        textScreenSize;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(Themes.loadTheme(settings));

        setContentView(R.layout.settings);

        RelativeLayout layout_wifi_only     = findViewById(R.id.layout_wifi_only);
        checkBoxWifiOnly                    = findViewById(R.id.only_wifi);
        RelativeLayout layout_set_screen    = findViewById(R.id.layout_set_screen);
        RelativeLayout layout_screen_size   = findViewById(R.id.layout_screen_size);
        RelativeLayout layout_themes        = findViewById(R.id.layout_themes);
        RelativeLayout layout_image_source  = findViewById(R.id.layout_image_source);
        textScreenImage                     = findViewById(R.id.screen_image);
        textScreenSize                      = findViewById(R.id.screen_size);
        RelativeLayout layout_derpibooru_safe_search     = findViewById(R.id.layout_derpibooru_tags);
        LinearLayout layout_root_set_screen     = findViewById(R.id.layout_root_set_screen);

        layout_wifi_only.setOnClickListener(click);
        layout_set_screen.setOnClickListener(click);
        layout_screen_size.setOnClickListener(click);
        layout_themes.setOnClickListener(click);
        layout_image_source.setOnClickListener(click);
        layout_image_source.setOnClickListener(click);
        layout_derpibooru_safe_search.setOnClickListener(click);

        // WiFi only
        if (settings.contains(Pref.WIFI_ONLY)) {
            checkBoxWifiOnly.setChecked(settings.getBoolean(Pref.WIFI_ONLY, true));
        }
        else {
            settings.put(Pref.WIFI_ONLY, true);
        }

        // change image on screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0
            if (settings.contains(Pref.SCREEN_IMAGE)) {
                setScreenImageText(settings.getInt(Pref.SCREEN_IMAGE, 0));
            }
            else {
                settings.put(Pref.SCREEN_IMAGE, 0);
                textScreenImage.setText(getResources().getString(R.string.screen_both));
            }
        }
        else {
            layout_root_set_screen.setVisibility(View.GONE); // hide, if Android version < 7.0
        }

        // screen size
        if (settings.contains(Pref.SCREEN_RESOLUTION)) {
            setScreenImageSize(settings.getInt(Pref.SCREEN_RESOLUTION, 0));
        }
        else {
            settings.put(Pref.SCREEN_RESOLUTION, 0);
            textScreenSize.setText(getResources().getString(R.string.screen_size_normal));
        }
    }
    //----------------------------------------------------------------------------------------------



    // creating a 3-dot menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        menu.setGroupVisible(R.id.menu_group_image, false); // hide Image group
        menu.setGroupVisible(R.id.menu_group_app, false);

        MenuCompat.setGroupDividerEnabled(menu, true); // for dividers
        return true;
    }
    //----------------------------------------------------------------------------------------------



    // 3-dot menu items
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // actions
        if (id == R.id.menu_item_action_help) { // help
            menuShowActivity(Help.class);
            return true;
        }
        else if (id == R.id.menu_item_action_about) { // about
            menuShowActivity(About.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //----------------------------------------------------------------------------------------------



    private void menuShowActivity(Class param){
        Intent intent = new Intent(this, param);
        startActivity(intent);
    }
    //----------------------------------------------------------------------------------------------



    View.OnClickListener click = new View.OnClickListener() {
        int dialogScreenImage;
        int dialogScreenSize;
        int dflt = 0; // default value for alert dialogs with radio buttons
        int defaultImageSource;
        int defaultDerpTags;
        AlertDialog.Builder builder;

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                // WiFi only
                case R.id.layout_wifi_only:
                    if (checkBoxWifiOnly.isChecked()) {
                        checkBoxWifiOnly.setChecked(false);
                    }
                    else { // checkbox was unchecked
                        checkBoxWifiOnly.setChecked(true);
                    }
                    settings.put(Pref.WIFI_ONLY, checkBoxWifiOnly.isChecked());
                    break;

                // themes
                case R.id.layout_themes:
                    Intent intent = new Intent(Settings.this, Themes.class);
                    startActivity(intent);
                    break;

                // change screen
                case R.id.layout_set_screen:
                    String[] vars = {
                        getResources().getString(R.string.screen_both),
                        getResources().getString(R.string.screen_homescreen),
                        getResources().getString(R.string.screen_lockscreen),
                    };

                    dflt = 0;
                    if (settings.contains(Pref.SCREEN_IMAGE)) {
                        dflt = settings.getInt(Pref.SCREEN_IMAGE, 0);
                    }

                    builder = new AlertDialog.Builder(Settings.this);
                    builder.setTitle(getResources().getString(R.string.screen_alert_title));
                    builder.setSingleChoiceItems(vars, dflt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogScreenImage = which;
                        }
                    });
                    builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            settings.put(Pref.SCREEN_IMAGE, dialogScreenImage);

                            // update ui
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setScreenImageText(dialogScreenImage);
                                }
                            });
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();

                    break;
                    
                // screen size
                case R.id.layout_screen_size:
                    String[] sizes = {
                        getResources().getString(R.string.screen_size_normal),
                        getResources().getString(R.string.screen_size_large),
                    };

                    dflt = 0;
                    if (settings.contains(Pref.SCREEN_RESOLUTION)) {
                        dflt = settings.getInt(Pref.SCREEN_RESOLUTION, 0);
                    }

                    builder = new AlertDialog.Builder(Settings.this);
                    builder.setTitle(getResources().getString(R.string.screen_size_alert_title));
                    builder.setSingleChoiceItems(sizes, dflt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogScreenSize = which;
                        }
                    });
                    builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            settings.put(Pref.SCREEN_RESOLUTION, dialogScreenSize);

                            // update ui
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setScreenImageSize(dialogScreenSize);
                                }
                            });

                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    break;

                case R.id.layout_image_source:
                    defaultImageSource = ImageProviders.PROVIDERS_DEFAULT; // 0b11
                    if(settings.contains(Pref.IMAGE_SOURCES)){
                        defaultImageSource = settings.getInt(Pref.IMAGE_SOURCES, ImageProviders.PROVIDERS_DEFAULT);
                    }

                    builder = new AlertDialog.Builder(Settings.this);
                    builder.setTitle(getResources().getString(R.string.image_source_alert_title));
                    builder.setMultiChoiceItems(
                        ImageProviders.PROVIDERS,
                        ImageProviders.toBitsArray(defaultImageSource), // bools
                        new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                            defaultImageSource ^= (1 << which); // xor. 0b01 - derpi; 0b10 - mlwp; 0b11 - all
                        }
                    });
                    builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (defaultImageSource <= 0) { // if no one checked
                                Toast.makeText(Settings.this, getResources().getString(R.string.image_source_no_one_checked), Toast.LENGTH_SHORT).show();
                                settings.put(Pref.IMAGE_SOURCES, ImageProviders.DERPIBOORU);
                            }
                            else {
                                settings.put(Pref.IMAGE_SOURCES, defaultImageSource);
                            }

                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    break;

                // derpibooru tags
                case R.id.layout_derpibooru_tags:
                    String[] tags = {"Wallpaper", "Safe"};
                    boolean[] b = {true, true};

                    defaultDerpTags = 0b11;
                    if(settings.contains(Pref.DERPIBOORU_TAGS)){
                        defaultDerpTags = settings.getInt(Pref.DERPIBOORU_TAGS, 0b11);
                    }

                    switch (defaultDerpTags) {
                        case 0:
                            b[0] = false;
                            b[1] = false;
                            break;
                        case Derpibooru.TAG_WALLPAPER:
                            b[0] = true;
                            b[1] = false;
                            break;
                        case Derpibooru.TAG_SAFE:
                            b[0] = false;
                            b[1] = true;
                            break;
                    }

                    builder = new AlertDialog.Builder(Settings.this);
                    builder.setTitle(getResources().getString(R.string.derpibooru_tags_alert_title));
                    builder.setMultiChoiceItems(
                            tags,
                            b,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                                    switch (which+1) {
                                        case Derpibooru.TAG_WALLPAPER:
                                            defaultDerpTags ^= (1 << Derpibooru.TAG_WALLPAPER-1);
                                            break;
                                        case Derpibooru.TAG_SAFE:
                                            defaultDerpTags ^= (1 << Derpibooru.TAG_SAFE-1);
                                            break;
                                    }
                                }
                            });
                    builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            settings.put(Pref.DERPIBOORU_TAGS, defaultDerpTags);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    break;
            }
        }
    };
    //----------------------------------------------------------------------------------------------



//    private void restartService(){
//        stopService(new Intent(Settings.this, ServiceRefresh.class));
//        Helper.startService(Settings.this);
//    }
    //-----------------------------------------------------------------------------------------------



    // set text for screen
    private void setScreenImageText(int screenImage){
        switch (screenImage){
            case 0: // both
                textScreenImage.setText(getResources().getString(R.string.screen_both));
                break;
            case 1: // homescreen
                textScreenImage.setText(getResources().getString(R.string.screen_homescreen));
                break;
            case 2: // lockscreen
                textScreenImage.setText(getResources().getString(R.string.screen_lockscreen));
                break;
        }
    }
    //----------------------------------------------------------------------------------------------



    private void setScreenImageSize(int dialogScreenSize) {
        switch (dialogScreenSize) {
            case 0: // normal
                textScreenSize.setText(getResources().getString(R.string.screen_size_normal));
                break;
            case 1: // large
                textScreenSize.setText(getResources().getString(R.string.screen_size_large));
                break;
        }
    }
    //----------------------------------------------------------------------------------------------
}
