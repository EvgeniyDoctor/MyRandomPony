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

import net.grandcentrix.tray.AppPreferences;

public class Settings extends AppCompatActivity {
    private static AppPreferences settings; // res. - https://github.com/grandcentrix/tray
    private CheckBox
        checkBoxMobileOnly,
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
        textScreenImage                     = findViewById(R.id.screen_image);
        textScreenSize                      = findViewById(R.id.screen_size);
        LinearLayout layout_root_set_screen     = findViewById(R.id.layout_root_set_screen);

        layout_wifi_only.setOnClickListener(click);
        layout_set_screen.setOnClickListener(click);
        layout_screen_size.setOnClickListener(click);
        layout_themes.setOnClickListener(click);

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
        AlertDialog.Builder builder;

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.layout_wifi_only:
                    if (checkBoxWifiOnly.isChecked()) {
                        checkBoxWifiOnly.setChecked(false);
                    }
                    else { // чекбокс был ВЫключен при нажатии // checkbox was unchecked when you clicked
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
