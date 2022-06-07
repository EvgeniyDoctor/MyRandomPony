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



@SuppressLint("DefaultLocale")
public class Settings extends AppCompatActivity {
    private static AppPreferences settings; // res. - https://github.com/grandcentrix/tray
    private CheckBox
        checkBoxWifiOnly;
    private TextView
        textImageSources,
        textDerpibooruTags,
        textScreenImage,
        textScreenSize;
    private final String dd = "%d / %d";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(Themes.loadTheme(settings));

        setContentView(R.layout.settings);

        checkBoxWifiOnly                    = findViewById(R.id.only_wifi);
        LinearLayout layout_root_set_screen = findViewById(R.id.layout_root_set_screen);
        RelativeLayout layout_wifi_only     = findViewById(R.id.layout_wifi_only);
        RelativeLayout layout_set_screen    = findViewById(R.id.layout_set_screen);
        RelativeLayout layout_screen_res    = findViewById(R.id.layout_screen_res);
        RelativeLayout layout_themes        = findViewById(R.id.layout_themes);
        RelativeLayout layout_image_sources = findViewById(R.id.layout_image_sources);
        RelativeLayout layout_derpibooru_safe_search = findViewById(R.id.layout_derpibooru_tags);
        textScreenImage                     = findViewById(R.id.screen_image);
        textImageSources                    = findViewById(R.id.image_sources);
        textScreenSize                      = findViewById(R.id.screen_size);
        textDerpibooruTags                  = findViewById(R.id.derpibooru_tags);

        setTitle(getResources().getString(R.string.settings));

        layout_wifi_only.setOnClickListener(click);
        layout_set_screen.setOnClickListener(click);
        layout_screen_res.setOnClickListener(click);
        layout_themes.setOnClickListener(click);
        layout_image_sources.setOnClickListener(click);
        layout_image_sources.setOnClickListener(click);
        layout_derpibooru_safe_search.setOnClickListener(click);

        // WiFi only
        if (settings.contains(Pref.WIFI_ONLY)) {
            checkBoxWifiOnly.setChecked(settings.getBoolean(Pref.WIFI_ONLY, Pref.WIFI_ONLY_DEFAULT));
        }
        else {
            settings.put(Pref.WIFI_ONLY, Pref.WIFI_ONLY_DEFAULT);
        }

        // change image on screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0
            if (settings.contains(Pref.SCREEN_IMAGE)) {
                setTextScreenImage(settings.getInt(Pref.SCREEN_IMAGE, Pref.SCREEN_IMAGE_DEFAULT));
            }
            else {
                settings.put(Pref.SCREEN_IMAGE, Pref.SCREEN_IMAGE_DEFAULT);
                textScreenImage.setText(getResources().getString(R.string.screen_both));
            }
        }
        else {
            layout_root_set_screen.setVisibility(View.GONE); // hide, if Android version < 7.0
        }

        // image sources
        if (settings.contains(Pref.IMAGE_SOURCES)) {
            setTextImageSources(settings.getInt(Pref.IMAGE_SOURCES, ImageProviders.PROVIDERS_DEFAULT));
        }
        else {
            settings.put(Pref.IMAGE_SOURCES, ImageProviders.PROVIDERS_DEFAULT);
            textImageSources.setText(String.format(dd, ImageProviders.TOTAL_PROVIDERS, ImageProviders.TOTAL_PROVIDERS));
        }

        // derp tags
        if (settings.contains(Pref.DERPIBOORU_TAGS)) {
            setTextDerpibooruTags(settings.getInt(Pref.DERPIBOORU_TAGS, Pref.DERPIBOORU_TAGS_DEFAULT));
        }
        else {
            settings.put(Pref.DERPIBOORU_TAGS, Pref.DERPIBOORU_TAGS_DEFAULT);
            textDerpibooruTags.setText(String.format(dd, 2, 2));
        }

        // screen res
        if (settings.contains(Pref.SCREEN_RESOLUTION)) {
            setTextScreenImageSize(settings.getInt(Pref.SCREEN_RESOLUTION, Pref.SCREEN_RESOLUTION_DEFAULT));
        }
        else {
            settings.put(Pref.SCREEN_RESOLUTION, Pref.SCREEN_RESOLUTION_DEFAULT);
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
                                    setTextScreenImage(dialogScreenImage);
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
                    
                // screen res
                case R.id.layout_screen_res:
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
                                    setTextScreenImageSize(dialogScreenSize);
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

                // image sources
                case R.id.layout_image_sources:
                    defaultImageSource = ImageProviders.PROVIDERS_DEFAULT; // 0b11
                    if (settings.contains(Pref.IMAGE_SOURCES)){
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

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setTextImageSources(defaultImageSource);
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

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setTextDerpibooruTags(defaultDerpTags);
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
            }
        }
    };
    //----------------------------------------------------------------------------------------------



    private void setTextDerpibooruTags(int i) {
        switch (i) {
            case Derpibooru.TAG_WALLPAPER | Derpibooru.TAG_SAFE:
                textDerpibooruTags.setText(String.format(dd, 2, 2));
                break;
            case Derpibooru.TAG_WALLPAPER:
            case Derpibooru.TAG_SAFE:
                textDerpibooruTags.setText(String.format(dd, 1, 2));
                break;
            default:
                textDerpibooruTags.setText(String.format(dd, 0, 2));
        }
    }
    //----------------------------------------------------------------------------------------------



    void setTextImageSources(int i){
        boolean[] b = ImageProviders.toBitsArray(i);
        int count = 0;
        for (boolean a : b) {
            if (a) {
                ++count;
            }
        }

        if (count == 0) {
            // cannot be 0
            textImageSources.setText(String.format(dd, 1, ImageProviders.TOTAL_PROVIDERS));
        }
        else if (count == ImageProviders.TOTAL_PROVIDERS){
            textImageSources.setText(String.format(dd, ImageProviders.TOTAL_PROVIDERS, ImageProviders.TOTAL_PROVIDERS));
        }
        else {
            textImageSources.setText(String.format(dd, count, ImageProviders.TOTAL_PROVIDERS));
        }
    }
    //----------------------------------------------------------------------------------------------



    // set text for screen
    private void setTextScreenImage (int screenImage){
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



    private void setTextScreenImageSize (int dialogScreenSize) {
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
