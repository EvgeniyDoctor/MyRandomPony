package ru.EvgeniyDoctor.myrandompony;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;

import com.yalantis.ucrop.UCrop;

import net.grandcentrix.tray.AppPreferences;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.OutputStream;
import java.util.Calendar;


public class Main extends AppCompatActivity {
    private CheckBox
            checkBoxEnabled;
    private ImageView previewWallpaper = null;
    private TextView
            textFrequency;
    private ProgressDialog progressDialog = null;
    private Button
            btnCancel,
            btnEdit,
            btnNext;
    private static AppPreferences settings; // res. - https://github.com/grandcentrix/tray
    private Wallpaper wallpaper;
    private AlertDialog alertDialog = null;
    SaveToGallery saveToGallery;



    // todo 05.08.2021: if press "Enabled" or radio buttons quickly too much times; then will be this error: Context.startForegroundService() did not then call Service.startForeground()
    // todo 04.06.2022: ? add min size for download image



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(Themes.loadTheme(settings));

        setContentView(R.layout.main);

        btnCancel                       = findViewById(R.id.btn_cancel);
        btnEdit                         = findViewById(R.id.btn_edit);
        btnNext                         = findViewById(R.id.btn_next);
        RelativeLayout layout_enable            = findViewById(R.id.layout_enable);
        RelativeLayout layout_settings          = findViewById(R.id.layout_settings);
        RelativeLayout layout_set_frequency     = findViewById(R.id.layout_set_frequency);
        checkBoxEnabled                 = findViewById(R.id.enable_checkbox);
        previewWallpaper                = findViewById(R.id.preview_wallpaper);
        textFrequency                   = findViewById(R.id.text_frequency);

        btnCancel.setOnClickListener(click);
        btnEdit.setOnClickListener(click);
        btnNext.setOnClickListener(click);
        layout_enable.setOnClickListener(click);
        previewWallpaper.setOnClickListener(click);
        layout_set_frequency.setOnClickListener(click);
        layout_settings.setOnClickListener(click);

        wallpaper = new Wallpaper(getApplicationContext(), settings, Image.Edited);

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

        // load wallpaper preview
        if (wallpaper.exists(Image.Original)) {
            setWallpaperPreview();
        }

        setButtonsState();

        // set max height for imageview
        if (settings.contains(Pref.SCREEN_SIZE_SMALL_OR_NORMAL) && settings.getBoolean(Pref.SCREEN_SIZE_SMALL_OR_NORMAL, false)) {
            previewWallpaper.setMaxHeight((int) getResources().getDimension(R.dimen.preview_max_height));
        }
        else {
            ScreenSize screenSize = getScreenSize();
            if (screenSize == ScreenSize.SMALL || screenSize == ScreenSize.NORMAL) {
                previewWallpaper.setMaxHeight((int) getResources().getDimension(R.dimen.preview_max_height));
                settings.put(Pref.SCREEN_SIZE_SMALL_OR_NORMAL, true);
            }
        }

        progressDialog = new ProgressDialog(Main.this);
    } // onCreate
    //----------------------------------------------------------------------------------------------



    private ScreenSize getScreenSize(){
        // https://stackoverflow.com/questions/10689259/how-to-programatically-determine-which-xml-layout-my-android-apps-is-using
        int screen = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        switch (screen){
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return ScreenSize.SMALL;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return ScreenSize.NORMAL;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return ScreenSize.LARGE;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return ScreenSize.XLARGE;
            default:
                return ScreenSize.UNKNOWN;
        }
    }
    //----------------------------------------------------------------------------------------------



    // Изменение ориентации экрана // Changing the screen orientation
    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setButtonsState();

        // Checks the orientation of the screen
        /*
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { // album
            Toast.makeText(Main.this, "landscape", Toast.LENGTH_SHORT).show();
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) { // normal
            Toast.makeText(Main.this, "portrait", Toast.LENGTH_SHORT).show();
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
        MenuCompat.setGroupDividerEnabled(menu, true); // for dividers
        return true;
    }
    //----------------------------------------------------------------------------------------------



    void askPermission(){
        // for saving images to gallery
        ActivityCompat.requestPermissions(Main.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }
    //----------------------------------------------------------------------------------------------



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveToGallery = new SaveToGallery();
                saveToGallery.execute();
            }
            else {
                Toast.makeText(Main.this, getResources().getString(R.string.menu_item_save_perm_error), Toast.LENGTH_LONG).show();
            }
        }
    }
    //----------------------------------------------------------------------------------------------



    // 3-dot menu items
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

        // app
        else if (id == R.id.menu_item_app_rate) { // rate app
            imageOpen(Pref.APP_LINK);
            return true;
        }
        else if (id == R.id.menu_item_app_share) { // share app
            imageShare(Pref.APP_LINK);
            return true;
        }

        // image
        else if (id == R.id.menu_item_image_copy) { // copy
            imageCopy();
            return true;
        }
        else if (id == R.id.menu_item_image_open) { // open
            if (!wallpaper.exists(Image.Original)) {
                Toast.makeText(Main.this, getResources().getString(R.string.menu_item_save_error), Toast.LENGTH_LONG).show();
                return true;
            }

            imageOpen(settings.getString(Pref.IMAGE_URL, ""));
            return true;
        }
        else if (id == R.id.menu_item_image_save) { // save
            if (!wallpaper.exists(Image.Original)) {
                Toast.makeText(this, getResources().getString(R.string.menu_item_save_error), Toast.LENGTH_LONG).show();
                return true;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                new Main.SaveToGallery().execute();
            }
            else {
                askPermission();
            }
            return true;
        }
        else if (id == R.id.menu_item_image_share) { // share
            if (!wallpaper.exists(Image.Original)) {
                Toast.makeText(Main.this, getResources().getString(R.string.menu_item_save_error), Toast.LENGTH_LONG).show();
                return true;
            }

            imageShare(settings.getString(Pref.IMAGE_URL, ""));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //----------------------------------------------------------------------------------------------



    private void imageCopy(){
        if (!settings.contains(Pref.IMAGE_URL)) {
            Toast.makeText(Main.this, getResources().getString(R.string.menu_item_link_error), Toast.LENGTH_LONG).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("MRP_link", settings.getString(Pref.IMAGE_URL, ""));
        clipboard.setPrimaryClip(clip);
        Toast.makeText(Main.this, getResources().getString(R.string.menu_item_copy_success), Toast.LENGTH_LONG).show();
    }
    //----------------------------------------------------------------------------------------------



    private void imageShare(String text){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }
    //----------------------------------------------------------------------------------------------



    private void imageOpen(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
    //----------------------------------------------------------------------------------------------



    private class SaveToGallery extends AsyncTask<Void, Void, Void> {
        String name = settings.getString(Pref.IMAGE_TITLE, "MRP_" + System.currentTimeMillis() + ".png");
        boolean saved = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(Main.this);
            progressDialog.setTitle(getResources().getString(R.string.settings_progress_title_saving));
            progressDialog.setMessage(getResources().getString(R.string.settings_progress_msg));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        //---

        @Override
        protected Void doInBackground(Void... voids) {
            Bitmap bitmap = new Wallpaper(getApplicationContext(), settings, Image.Original).load();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // >= 10
                final String IMAGES_FOLDER_NAME = getResources().getString(R.string.app_name);

                ContentResolver resolver = getApplicationContext().getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + IMAGES_FOLDER_NAME);
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                try {
                    OutputStream fos = resolver.openOutputStream(imageUri);
                    saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else { // normal Android version
                String res = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, name, null);
                if (!res.isEmpty()) {
                    saved = true;
                }
            }
            return null;
        }
        //---

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            progressDialog.dismiss();
            if (saved) {
                Toast.makeText(Main.this, getResources().getString(R.string.menu_item_save_success), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(Main.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            }
        }
        //---

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }
        //---
    }
    //----------------------------------------------------------------------------------------------



    private void menuShowActivity(Class param){
        Intent intent = new Intent(this, param);
        startActivity(intent);
    }
    //----------------------------------------------------------------------------------------------



    View.OnClickListener click = new View.OnClickListener() {
        final Calendar calendar = Calendar.getInstance();
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
                        Pref.IMAGE_EDITED
                    );
                    if (bg_edited.exists()) {
                        if (bg_edited.delete()) {
                            previewWallpaper.setImageBitmap(wallpaper.load());
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
                        Pref.IMAGE_ORIGINAL
                    );

                    if (!input.exists()) {
                        Toast.makeText(Main.this, R.string.hint_edit_first_click, Toast.LENGTH_LONG).show();
                        return;
                    }

                    File output = new File(
                        new ContextWrapper(getApplicationContext()).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
                        Pref.IMAGE_EDITED
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
                    if (Helper.checkInternetConnection(Main.this, settings.getBoolean(Pref.WIFI_ONLY, true))) {
                        progressDialog = new ProgressDialog(Main.this);
                        progressDialog.setTitle(getResources().getString(R.string.settings_progress_title_downloading));
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
                                DownloadResult code = loadNewWallpaper.load();
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

                case R.id.layout_settings:
                    Intent intent = new Intent(Main.this, Settings.class);
                    startActivity(intent);
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

                            wallpaper.set();
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
            }
        }
    };
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
        if (previewWallpaper == null) {
            previewWallpaper = findViewById(R.id.preview_wallpaper);
        }

        if (previewWallpaper != null) {
            previewWallpaper.setImageBitmap(wallpaper.load()); // load wallpaper preview
            previewWallpaper.setVisibility(View.VISIBLE);
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
        }
    }
    //-----------------------------------------------------------------------------------------------



    private void nextButtonProcessing(DownloadResult code){
        // update UI in Thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (code) {
                    case SUCCESS:
                        setWallpaperPreview();
                        Helper.toggleViewState(Main.this, btnCancel, false);
                        Helper.toggleViewState(Main.this, btnEdit,   true);

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



    // результат активити редактирования изо // result of the activity for image editing
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            previewWallpaper.setImageBitmap(wallpaper.load());

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
            Pref.IMAGE_ORIGINAL
        );

        return input.exists();
    }
    //-----------------------------------------------------------------------------------------------



    // check if the edited image exists
    public boolean editedImageExists(){
        File bg_edited = new File(
            new ContextWrapper(getApplicationContext()).getDir(Pref.SAVE_PATH, MODE_PRIVATE),
            Pref.IMAGE_EDITED
        );
        return bg_edited.exists();
    }
    //-----------------------------------------------------------------------------------------------



    @Override
    protected void onDestroy() {
        super.onDestroy();

        checkBoxEnabled = null;
        previewWallpaper = null;
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
