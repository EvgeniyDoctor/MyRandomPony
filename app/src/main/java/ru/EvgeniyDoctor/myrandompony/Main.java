package ru.EvgeniyDoctor.myrandompony;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;

import net.grandcentrix.tray.AppPreferences;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Calendar;



public class Main extends AppCompatActivity {
    private CheckBox
            checkBoxEnabled,
            checkBoxMobileOnly,
            checkBoxWifiOnly;
    private ImageView currentWallpaper = null;
    private TextView
            textviewDownloadUrl,
            textScreenImage,
            textFrequency;
    private ProgressDialog progressDialog = null;
    private Button
            btnCancel,
            btnEdit,
            btnNext;
    private static AppPreferences settings; // res. - https://github.com/grandcentrix/tray
    private ChangeWallpaper changeWallpaper;
    private AlertDialog alertDialog = null;
    /*
        alertDialog - переменная для показа диалоговых окон.
    Нужна, так как если делать напрямую - builder.show(); то если, например, во время показа диалога вёрстка экрана изменится
    ориентация экрана, это вызовет Activity ... has leaked.
    Обнуляется в onDestroy.
    res. - http://stackoverflow.com/questions/11051172/progressdialog-and-alertdialog-cause-leaked-window
     */



    // todo 05.08.2021: if press "Enabled" or radio buttons quickly too much times; then will be this error: Context.startForegroundService() did not then call Service.startForeground()
    // todo 07.04.2022: add derpibooru?
    // todo 10.04.2022: update help, help dialogs and readme



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(Themes.loadTheme(settings));

        setContentView(R.layout.main);

        btnCancel                       = findViewById(R.id.btn_cancel);
        btnEdit                         = findViewById(R.id.btn_edit);
        btnNext                         = findViewById(R.id.btn_next);
        FrameLayout layout_enable       = findViewById(R.id.layout_enable);
        FrameLayout layout_mobile_only  = findViewById(R.id.layout_mobile_only);
        FrameLayout layout_wifi_only    = findViewById(R.id.layout_wifi_only);
        checkBoxEnabled                 = findViewById(R.id.enable_checkbox);
        checkBoxMobileOnly              = findViewById(R.id.only_mobile);
        checkBoxWifiOnly                = findViewById(R.id.only_wifi);
        currentWallpaper                = findViewById(R.id.preview_wallpaper);
        textviewDownloadUrl             = findViewById(R.id.download_url);
        LinearLayout layout_root_set_screen     = findViewById(R.id.layout_root_set_screen);
        FrameLayout layout_set_screen   = findViewById(R.id.layout_set_screen);
        FrameLayout layout_set_frequency   = findViewById(R.id.layout_set_frequency);
        textScreenImage = findViewById(R.id.screen_image);
        textFrequency = findViewById(R.id.text_frequency);

        btnCancel.setOnClickListener(click);
        btnEdit.setOnClickListener(click);
        btnNext.setOnClickListener(click);
        layout_enable.setOnClickListener(click);
        layout_mobile_only.setOnClickListener(click);
        layout_wifi_only.setOnClickListener(click);
        currentWallpaper.setOnClickListener(click);
        layout_set_screen.setOnClickListener(click);
        layout_set_frequency.setOnClickListener(click);

        changeWallpaper = new ChangeWallpaper(settings, Image.Edited);

        // установка первоначальных данных. Если этого не сделать, то при смене пользователем стд настройки с "Раз в неделю" на любую другую произойдёт обновление обоев
        // setting the initial data. If this is not done, then when the user changes the std settings from "Once a week" to any other, the wallpaper will be updated
        Calendar calendar = Calendar.getInstance();
        if (!settings.contains(Pref.REFRESH_FREQUENCY_CURR_DAY)) {
            settings.put(Pref.REFRESH_FREQUENCY_CURR_DAY, calendar.get(Calendar.DATE));
        }
        if (!settings.contains(Pref.REFRESH_FREQUENCY_CURR_WEEK)) {
            settings.put(Pref.REFRESH_FREQUENCY_CURR_WEEK, calendar.get(Calendar.WEEK_OF_YEAR));
        }
        if (!settings.contains(Pref.REFRESH_FREQUENCY_CURR_MONTH)) {
            settings.put(Pref.REFRESH_FREQUENCY_CURR_MONTH, calendar.get(Calendar.MONTH));
        }

        // включено ли // is enabled
        if (settings.contains(Pref.ENABLED)) {
            checkBoxEnabled.setChecked(settings.getBoolean(Pref.ENABLED, false));
        }

        // разрешение обоев // wallpaper resolution
        if (settings.contains(Pref.MOBILE_ONLY)) {
            checkBoxMobileOnly.setChecked(settings.getBoolean(Pref.MOBILE_ONLY, true));
        }
        else {
            settings.put(Pref.MOBILE_ONLY, true);
        }

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

        // refresh frequency
        if (settings.contains(Pref.REFRESH_FREQUENCY)) {
            setFrequencyText(settings.getInt(Pref.REFRESH_FREQUENCY, 2)-1);
        }
        else {
            settings.put(Pref.REFRESH_FREQUENCY, 2); // once a week
            textFrequency.setText(getResources().getString(R.string.frequency_once_a_week));
        }

        // запуск сервиса, если надо // starting the service, if necessary
        if (checkBoxEnabled.isChecked()) {
            if (settings.contains(Pref.FLAG_MAIN_ACTIVITY_RESTART) && settings.getBoolean(Pref.FLAG_MAIN_ACTIVITY_RESTART, false)) { // if Main activity restarted after applying new theme
                settings.remove(Pref.FLAG_MAIN_ACTIVITY_RESTART);
            }
            else {
                Helper.startService(Main.this);
            }
        }
        else {
            stopService(new Intent(this, ServiceRefresh.class));
        }

        // hint
        if (!settings.contains(Pref.HINT_FIRST_LAUNCH)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
            builder.setTitle(R.string.hint_first_launch_alert_title);
            builder.setMessage(R.string.hint_first_launch_alert_msg);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    settings.put(Pref.HINT_FIRST_LAUNCH, false);
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }

        // установка ссылки // set link
        if (settings.contains(Pref.DOWNLOAD_URL)) {
            String link = settings.getString(Pref.DOWNLOAD_URL, "");
            if (link != null && !link.isEmpty()) {
                setLink(link);
                setWallpaperPreview();
            }
        }

        setButtonsState();

        progressDialog = new ProgressDialog(Main.this);
    } // onCreate
    //----------------------------------------------------------------------------------------------



    // Изменение ориентации экрана // Changing the screen orientation
    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setButtonsState();

        // Checks the orientation of the screen
        /*
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { // album
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) { // normal
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
         */
    }
    //-----------------------------------------------------------------------------------------------



    // при изменении темы, после нажатия на кнопку Принять это активити останавливается для перезапуска
    // when changing the theme, after clicking on the Apply button, this activity stops for restarting
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent == null) {
            return;
        }

        boolean keep = intent.getExtras().getBoolean(Themes.THEME_INTENT_FLAG);
        if (!keep) {
            Main.this.finish();
        }
    }
    //-----------------------------------------------------------------------------------------------



    // creating a 3-dot menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    //----------------------------------------------------------------------------------------------



    // 3-dot menu items
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_action_themes: // themes
                menuShowActivity(Themes.class);
                return true;

            case R.id.menu_item_action_help: // help
                menuShowActivity(Help.class);
                return true;

            case R.id.menu_item_action_about: // about
                menuShowActivity(About.class);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //----------------------------------------------------------------------------------------------



    private void menuShowActivity(Class param){
        Intent intent = new Intent(this, param);
        startActivity(intent);
    }
    //----------------------------------------------------------------------------------------------



    /*
    // вызов диалогового окна
    protected void callDialog(int title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }
     */



    View.OnClickListener click = new View.OnClickListener() {
        final Calendar calendar = Calendar.getInstance();
        int dialogScreenImage; //
        int dialogFrequency;
        int dflt = 0; // default value for alert dialogs with radio buttons

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                // buttons --->
                case R.id.btn_cancel: // Cancel button
                    File bg_edited = new File(
                        new ContextWrapper(getApplicationContext()).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
                        Pref.FILE_NAME_EDITED
                    );
                    if (bg_edited.exists()) {
                        if (bg_edited.delete()) {
                            currentWallpaper.setImageBitmap(changeWallpaper.loadWallpaper(getApplicationContext()));
                            Helper.toggleViewState(Main.this, btnCancel, false);
                        }
                        else {
                            Toast.makeText(Main.this, getResources().getString(R.string.settings_image_cancel2), Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(Main.this, getResources().getString(R.string.settings_image_cancel1), Toast.LENGTH_LONG).show();
                    }
                    break;

                case R.id.btn_edit: // Edit button
                    File input = new File(
                        new ContextWrapper(getApplicationContext()).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
                        Pref.FILE_NAME
                    );

                    if (!input.exists()) {
                        Toast.makeText(Main.this, R.string.hint_edit_first_click, Toast.LENGTH_LONG).show();
                        return;
                    }

                    File output = new File(
                        new ContextWrapper(getApplicationContext()).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
                        Pref.FILE_NAME_EDITED
                    );

                    // res - https://github.com/Yalantis/uCrop
                    UCrop.Options options = new UCrop.Options();
                    options.setCompressionFormat(Bitmap.CompressFormat.PNG);

                    options.setStatusBarColor(Themes.getThemeColorById(Main.this, R.attr.colorPrimaryDark));
                    options.setToolbarColor(Themes.getThemeColorById(Main.this, R.attr.colorPrimary));
                    options.setActiveControlsWidgetColor(Themes.getThemeColorById(Main.this, R.attr.colorAccent)); // текст снизу
                    options.setToolbarWidgetColor(getResources().getColor(R.color.white)); // title color

                    options.setShowCropGrid(false); // вид матрицы
                    options.setToolbarTitle(getResources().getString(R.string.settings_image_edit));
                    UCrop.of(Uri.fromFile(input), Uri.fromFile(output)).withOptions(options).start(Main.this);
                    break;

                case R.id.btn_next: // Next button
                    Helper.d("Main - Next");

                    if (Helper.checkInternetConnection(Main.this, settings.getBoolean(Pref.WIFI_ONLY, true))) {
                        progressDialog = new ProgressDialog(Main.this);
                        progressDialog.setTitle(getResources().getString(R.string.settings_progress_title));
                        progressDialog.setMessage(getResources().getString(R.string.settings_progress_msg));
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                Main.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); // screen orientation lock while ProgressDialog is showing, else will be "WindowLeaked" error

                                LoadNewWallpaper loadNewWallpaper = new LoadNewWallpaper(
                                    getApplicationContext(),
                                    settings,
                                    false // no need to change the background
                                );
                                LoadNewWallpaper.Codes code = loadNewWallpaper.load();
                                nextButtonProcessing(code); // processing the result and update UI

                                Main.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); // unlock screen orientation
                            }
                        };
                        thread.start();
                    }
                    else {
                        Toast.makeText(Main.this, R.string.settings_load_error, Toast.LENGTH_LONG).show();
                    }
                    break;
                // <--- buttons

                // layers --->
                case R.id.layout_enable:
                    if (checkBoxEnabled.isChecked()) {
                        checkBoxEnabled.setChecked(false);
                        stopService(new Intent(Main.this, ServiceRefresh.class));
                    }
                    else { // чекбокс был ВЫключен при нажатии // checkbox was unchecked when you clicked
                        checkBoxEnabled.setChecked(true);
                        if (!Helper.checkInternetConnection(Main.this, false)) {
                            Toast.makeText(Main.this, R.string.settings_internet_warn, Toast.LENGTH_LONG).show();
                        }
                        Calendar calendar = Calendar.getInstance();
                        settings.put(Pref.REFRESH_FREQUENCY_CURR_DAY, calendar.get(Calendar.DATE));
                        settings.put(Pref.REFRESH_FREQUENCY_CURR_WEEK, calendar.get(Calendar.WEEK_OF_YEAR));
                        settings.put(Pref.REFRESH_FREQUENCY_CURR_MONTH, calendar.get(Calendar.MONTH));

                        Helper.startService(Main.this);
                    }
                    settings.put(Pref.ENABLED, checkBoxEnabled.isChecked());
                    break;

                case R.id.layout_mobile_only:
                    if (checkBoxMobileOnly.isChecked()) {
                        checkBoxMobileOnly.setChecked(false);
                    }
                    else { // чекбокс был ВЫключен при нажатии // checkbox was unchecked when you clicked
                        checkBoxMobileOnly.setChecked(true);
                    }
                    settings.put(Pref.MOBILE_ONLY, checkBoxMobileOnly.isChecked());

                    if (checkBoxEnabled.isChecked()) {
                        restartService();
                    }

                    break;

                case R.id.layout_wifi_only:
                    if (checkBoxWifiOnly.isChecked()) {
                        checkBoxWifiOnly.setChecked(false);
                    }
                    else { // чекбокс был ВЫключен при нажатии // checkbox was unchecked when you clicked
                        checkBoxWifiOnly.setChecked(true);
                    }
                    settings.put(Pref.WIFI_ONLY, checkBoxWifiOnly.isChecked());

                    break;
                // <--- layers

                case R.id.preview_wallpaper: // press on the image
                    progressDialog = new ProgressDialog(Main.this);
                    progressDialog.setTitle(getResources().getString(R.string.changing_wallpaper_progress_title));
                    progressDialog.setMessage(getResources().getString(R.string.settings_progress_msg));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false); // back btn
                    progressDialog.show();

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            Main.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); // screen orientation lock while ProgressDialog is showing, else will be "WindowLeaked" error

                            changeWallpaper.setWallpaper(getApplicationContext());
                            progressDialog.dismiss();

                            Main.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); // unlock screen orientation
                        }
                    };
                    thread.start();

                    break;

                // частота обновления обоев // wallpaper refresh frequency
                case R.id.layout_set_frequency:
                    String[] frq = {
                        getResources().getString(R.string.frequency_once_a_day),
                        getResources().getString(R.string.frequency_once_a_week),
                        getResources().getString(R.string.frequency_once_a_month),
                    };

                    dflt = 1; // "once a week" radio button
                    if (settings.contains(Pref.REFRESH_FREQUENCY)) {
                        dflt = settings.getInt(Pref.REFRESH_FREQUENCY, 2);
                        --dflt;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                    builder.setTitle(getResources().getString(R.string.frequency_changing));
                    builder.setSingleChoiceItems(frq, dflt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogFrequency = which;
                        }
                    });
                    builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (settings.contains(Pref.REFRESH_FREQUENCY) && settings.getInt(Pref.REFRESH_FREQUENCY, 2) == dialogFrequency+1) {
                                return;
                            }

                            switch (dialogFrequency) {
                                case 0:
                                    settings.put(Pref.REFRESH_FREQUENCY_CURR_DAY, calendar.get(Calendar.DATE));
                                    settings.put(Pref.REFRESH_FREQUENCY, 1);
                                    break;

                                case 1:
                                    settings.put(Pref.REFRESH_FREQUENCY_CURR_WEEK, calendar.get(Calendar.WEEK_OF_YEAR));
                                    settings.put(Pref.REFRESH_FREQUENCY, 2);
                                    break;

                                case 2:
                                    settings.put(Pref.REFRESH_FREQUENCY_CURR_MONTH, calendar.get(Calendar.MONTH));
                                    settings.put(Pref.REFRESH_FREQUENCY, 3);
                                    break;
                            }

                            // update ui
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setFrequencyText(dialogFrequency);
                                }
                            });

                            // if enabled, restart service
                            if (settings.contains(Pref.ENABLED) && settings.getBoolean(Pref.ENABLED, false)) {
                                restartService();
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

                    AlertDialog.Builder builder2 = new AlertDialog.Builder(Main.this);
                    builder2.setTitle(getResources().getString(R.string.screen_alert_title));
                    builder2.setSingleChoiceItems(vars, dflt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogScreenImage = which;
                        }
                    });
                    builder2.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                    builder2.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder2.create().show();

                    break;
            }
        }
    };
    //----------------------------------------------------------------------------------------------



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



    // set text for frequency
    private void setFrequencyText(int dialogFrequency){
        switch (dialogFrequency) {
            case 0:
                textFrequency.setText(getResources().getString(R.string.frequency_once_a_day));
                break;
            case 1:
                textFrequency.setText(getResources().getString(R.string.frequency_once_a_week));
                break;
            case 2:
                textFrequency.setText(getResources().getString(R.string.frequency_once_a_month));
                break;
        }
    }
    //----------------------------------------------------------------------------------------------



    private void restartService(){
        stopService(new Intent(Main.this, ServiceRefresh.class));
        Helper.startService(Main.this);
    }
    //-----------------------------------------------------------------------------------------------



    // set wallpaper preview
    private void setWallpaperPreview () {
        if (currentWallpaper == null) {
            currentWallpaper = findViewById(R.id.preview_wallpaper);
        }

        if (currentWallpaper != null) {
            currentWallpaper.setImageBitmap(changeWallpaper.loadWallpaper(getApplicationContext())); // load wallpaper preview
            currentWallpaper.setVisibility(View.VISIBLE);
            textviewDownloadUrl.setVisibility(View.VISIBLE);
        }
        else {
            Toast.makeText(
                Main.this,
                String.format(
                    getResources().getString(R.string.error_unknown),
                    getResources().getString(R.string.wallpaper_preview)
                ),
                Toast.LENGTH_LONG)
            .show();
            textviewDownloadUrl.setVisibility(View.GONE);
        }
    }
    //-----------------------------------------------------------------------------------------------



    private void nextButtonProcessing(LoadNewWallpaper.Codes code){
        // update UI in Thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (code) {
                    case SUCCESS:
                        setWallpaperPreview();
                        Helper.toggleViewState(Main.this, btnCancel, false);
                        Helper.toggleViewState(Main.this, btnEdit,   true);

                        // установка ссылки для загрузки // set link
                        if (settings.contains(Pref.DOWNLOAD_URL)) {
                            setLink(settings.getString(Pref.DOWNLOAD_URL, ""));
                        }

                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }

                        // подсказка после первой загрузки изображения на кнопку "Дальше" // hint after the click on the"Next" button
                        if (!settings.contains(Pref.HINT_FIRST_NEXT)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                            builder.setMessage(R.string.hint_first_next_alert_msg);
                            builder.setCancelable(false);
                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    settings.put(Pref.HINT_FIRST_NEXT, false);
                                }
                            });
                            alertDialog = builder.create();
                            alertDialog.show();
                        }
                        break;

                    case NOT_CONNECTED:
                        Toast.makeText(Main.this, getResources().getString(R.string.settings_update_error), Toast.LENGTH_LONG).show();
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        break;

                    case NOT_JSON:
                        Toast.makeText(Main.this, getResources().getString(R.string.settings_download_error), Toast.LENGTH_LONG).show();
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        break;
                }
            }
        });
    }
    //-----------------------------------------------------------------------------------------------



    // установка ссылки на текст под изображением // set link under the image
    private void setLink (String link){
        String text = String.format(
            "<a href='%s'>%s</a>",
            link,
            getResources().getString(R.string.open_image_on_site)
        );

        textviewDownloadUrl.setText(Html.fromHtml(text));
        textviewDownloadUrl.setMovementMethod(LinkMovementMethod.getInstance());
    }
    //-----------------------------------------------------------------------------------------------



    // результат активити редактирования изо // result of the activity for image editing
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            currentWallpaper.setImageBitmap(changeWallpaper.loadWallpaper(getApplicationContext()));

            Helper.toggleViewState(Main.this, btnCancel, true);

            if (!settings.contains(Pref.HINT_FIRST_EDIT)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                builder.setMessage(R.string.hint_first_edit_text);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settings.put(Pref.HINT_FIRST_EDIT, true);
                    }
                });
                alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }
    //----------------------------------------------------------------------------------------------



    // enable or disable Cancel and Edit buttons; depends on existing respectful images
    public void setButtonsState () {
        Helper.toggleViewState(Main.this, btnCancel, editedImageExists());
        Helper.toggleViewState(Main.this, btnEdit,   originalImageExists());
    }
    //-----------------------------------------------------------------------------------------------



    // check if the original image exists
    public boolean originalImageExists(){
        File input = new File(
            new ContextWrapper(getApplicationContext()).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
            Pref.FILE_NAME
        );

        return input.exists();
    }
    //-----------------------------------------------------------------------------------------------



    // check if the edited image exists
    public boolean editedImageExists(){
        File bg_edited = new File(
            new ContextWrapper(getApplicationContext()).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
            Pref.FILE_NAME_EDITED
        );
        return bg_edited.exists();
    }
    //-----------------------------------------------------------------------------------------------



    @Override
    protected void onDestroy() {
        super.onDestroy();

        checkBoxEnabled = null;
        checkBoxMobileOnly = null;
        checkBoxWifiOnly = null;
        currentWallpaper = null;
        textviewDownloadUrl = null;
        settings = null;

        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = null;

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = null;

        System.gc();
    }
    //----------------------------------------------------------------------------------------------
}
