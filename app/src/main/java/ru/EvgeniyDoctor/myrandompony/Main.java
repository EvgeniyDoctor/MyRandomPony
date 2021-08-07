package ru.EvgeniyDoctor.myrandompony;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import net.grandcentrix.tray.AppPreferences;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;



public class Main extends AppCompatActivity {
    private CheckBox
            checkBox_enabled,
            checkBox_mobile_only,
            checkBox_wifi_only;
    private ImageView
            currentWallpaper;
    private TextView
            textview_download_url;
    private ProgressDialog
            progressDialog = null;
    private RadioButton
            radio_button1,
            radio_button2,
            radio_button3;
    private Button
            btn_cancel,
            btn_edit,
            btn_next;
    static AppPreferences settings; // res. - https://github.com/grandcentrix/tray
    private AlertDialog
        alertDialog = null;
    /*
        alertDialog - переменная для показа диалоговых окон.
    Нужна, так как если делать напрямую - builder.show(); то если, например, во время показа диалога вёрстка экрана изменится
    ориентация экрана, это вызовет Activity ... has leaked.
    Обнуляется в onDestroy.
    res. - http://stackoverflow.com/questions/11051172/progressdialog-and-alertdialog-cause-leaked-window
     */



    // todo 05.08.2021: notf: show WIFI and Mobile state info?
    // todo 05.08.2021: if press "Enabled" or radio buttons quickly too much times; then will be this error: Context.startForegroundService() did not then call Service.startForeground()



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(Themes.loadTheme(settings));

        setContentView(R.layout.main);

        // логи при ошибке
        //RoboErrorReporter.bindReporter(this.getApplicationContext());

        btn_cancel                      = findViewById(R.id.btn_cancel);
        btn_edit                        = findViewById(R.id.btn_edit);
        btn_next                        = findViewById(R.id.btn_next);
        FrameLayout layout_enable       = findViewById(R.id.layout_enable);
        FrameLayout layout_mobile_only  = findViewById(R.id.layout_mobile_only);
        FrameLayout layout_wifi_only    = findViewById(R.id.layout_wifi_only);
        checkBox_enabled                = findViewById(R.id.enable_checkbox);
        checkBox_mobile_only            = findViewById(R.id.only_mobile);
        checkBox_wifi_only              = findViewById(R.id.only_wifi);
        currentWallpaper = findViewById(R.id.theme_preview);
        textview_download_url           = findViewById(R.id.download_url);
        radio_button1                   = findViewById(R.id.radio_1);
        radio_button2                   = findViewById(R.id.radio_2);
        radio_button3                   = findViewById(R.id.radio_3);
        FrameLayout layout_radio_1      = findViewById(R.id.layout_radio_1);
        FrameLayout layout_radio_2      = findViewById(R.id.layout_radio_2);
        FrameLayout layout_radio_3      = findViewById(R.id.layout_radio_3);

        btn_cancel.setOnClickListener(click);
        btn_edit.setOnClickListener(click);
        btn_next.setOnClickListener(click);
        layout_enable.setOnClickListener(click);
        layout_mobile_only.setOnClickListener(click);
        layout_wifi_only.setOnClickListener(click);
        currentWallpaper.setOnClickListener(click);
        layout_radio_1.setOnClickListener(click);
        layout_radio_2.setOnClickListener(click);
        layout_radio_3.setOnClickListener(click);

        // частота обновления
        if (settings.contains(getResources().getString(R.string.refresh_frequency))) {
            switch (settings.getInt(getResources().getString(R.string.refresh_frequency), 2)) {
                case 1:
                    radio_button1.setChecked(true);
                    radio_button2.setChecked(false);
                    radio_button3.setChecked(false);
                    break;
                case 2:
                    radio_button1.setChecked(false);
                    radio_button2.setChecked(true);
                    radio_button3.setChecked(false);
                    break;
                case 3:
                    radio_button1.setChecked(false);
                    radio_button2.setChecked(false);
                    radio_button3.setChecked(true);
                    break;
            }
        }
        else { // если это первый запуск программы, то раз в неделю
            settings.put(getResources().getString(R.string.refresh_frequency), 2);
        }

        // установка первоначальных данных. Если этого не сделать, то при смене пользователем стд настройки с "Раз в неделю" на любую другую произойдёт обновление обоев
        Calendar calendar = Calendar.getInstance();
        if (!settings.contains(getResources().getString(R.string.refresh_frequency_curr_day))) {
            settings.put(getResources().getString(R.string.refresh_frequency_curr_day), calendar.get(Calendar.DATE));
        }
        if (!settings.contains(getResources().getString(R.string.refresh_frequency_curr_week))) {
            settings.put(getResources().getString(R.string.refresh_frequency_curr_week), calendar.get(Calendar.WEEK_OF_YEAR));
        }
        if (!settings.contains(getResources().getString(R.string.refresh_frequency_curr_month))) {
            settings.put(getResources().getString(R.string.refresh_frequency_curr_month), calendar.get(Calendar.MONTH));
        }

        // включено ли
        if (settings.contains(getResources().getString(R.string.enabled_pony_wallpapers))) {
            checkBox_enabled.setChecked(settings.getBoolean(getResources().getString(R.string.enabled_pony_wallpapers), false));
        }

        // разрешение обоев
        if (settings.contains(getResources().getString(R.string.mobile_pony_wallpapers))) {
            checkBox_mobile_only.setChecked(settings.getBoolean(getResources().getString(R.string.mobile_pony_wallpapers), true));
        }
        else {
            settings.put(getResources().getString(R.string.mobile_pony_wallpapers), true);
        }

        // WiFi only
        if (settings.contains(getResources().getString(R.string.wifi_only))) {
            checkBox_wifi_only.setChecked(settings.getBoolean(getResources().getString(R.string.wifi_only), true));
        }
        else {
            settings.put(getResources().getString(R.string.wifi_only), true);
        }

        // запуск сервиса, если надо
        if (checkBox_enabled.isChecked()) {
            Helper.startService(Main.this);
        }
        else {
            stopService(new Intent(this, ServiceRefresh.class));
        }

        // hint
        if (!settings.contains(getResources().getString(R.string.settings_hint2_flag))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
            builder.setTitle(R.string.settings_hint2_alert_title);
            builder.setMessage(R.string.settings_hint2_alert_msg);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    settings.put(getResources().getString(R.string.settings_hint2_flag), false);
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }

        // установка ссылки
        if (settings.contains(getResources().getString(R.string.downloadurl))) {
            String link = settings.getString(getResources().getString(R.string.downloadurl), "");
            if (link != null && !link.isEmpty()) {
                setLink(link);
                setWallpaperPreview();
            }
        }

        setButtonsState();

        progressDialog = new ProgressDialog(Main.this);
    } // onCreate
    //----------------------------------------------------------------------------------------------



    // Изменение ориентации экрана
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
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean keep = intent.getExtras().getBoolean(Themes.THEME_INTENT_FLAG);
        if (!keep) {
            Main.this.finish();
        }
    }
    //-----------------------------------------------------------------------------------------------



    // создание меню из 3 точек
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    //----------------------------------------------------------------------------------------------



    // пункты меню из 3 точек
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_action_themes:
                menuShowActivity(Themes.class);
                return true;

            case R.id.menu_item_action_help: // помощь
                menuShowActivity(Help.class);
                return true;

            case R.id.menu_item_action_about: // о приложении
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



    // вызов диалогового окна
    /*
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

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                // buttons --->
                case R.id.btn_cancel: // кнопка отмены изменений
                    File bg_edited = new File(
                            new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path),
                                    MODE_PRIVATE), getResources().getString(R.string.file_name_edited));
                    if (bg_edited.exists()) {
                        if (bg_edited.delete()) {
                            currentWallpaper.setImageBitmap(openBackground());
                            Helper.toggleViewState(Main.this, btn_cancel, false);
                        }
                        else {
                            Toast.makeText(Main.this, getResources().getString(R.string.settings_image_cancel2), Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(Main.this, getResources().getString(R.string.settings_image_cancel1), Toast.LENGTH_LONG).show();
                    }
                    break;

                case R.id.btn_edit: // кнопка редактирования
                    File input = new File(
                            new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path),
                                    MODE_PRIVATE), getResources().getString(R.string.file_name));

                    if (!input.exists()) {
                        Toast.makeText(Main.this, R.string.hint_edit_first_click, Toast.LENGTH_LONG).show();
                        return;
                    }

                    File output = new File(
                            new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path),
                                    MODE_PRIVATE), getResources().getString(R.string.file_name_edited));

                    // res - https://github.com/Yalantis/uCrop
                    UCrop.Options options = new UCrop.Options();
                    options.setCompressionFormat(Bitmap.CompressFormat.PNG);

                    options.setStatusBarColor(Themes.getThemeColorById(Main.this, R.attr.colorPrimaryDark));
                    options.setToolbarColor(Themes.getThemeColorById(Main.this, R.attr.colorPrimary));
                    options.setActiveWidgetColor(Themes.getThemeColorById(Main.this, R.attr.colorAccent)); // текст снизу

                    options.setShowCropGrid(false); // вид матрицы
                    options.setToolbarTitle(getResources().getString(R.string.settings_image_edit));
                    UCrop.of(Uri.fromFile(input), Uri.fromFile(output)).withOptions(options).start(Main.this);
                    break;

                case R.id.btn_next: // кнопка "дальше"
                    if (Helper.checkInternetConnection(Main.this, settings.getBoolean(getResources().getString(R.string.wifi_only), true))) {
                        progressDialog.setTitle(getResources().getString(R.string.settings_progress_title));
                        progressDialog.setMessage(getResources().getString(R.string.settings_progress_msg));
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        registerReceiver(receiver, new IntentFilter(IntentServiceLoadNewWallpaper.NOTIFICATION_LOAD_NEW_WALLPAPER));

                        Intent intent = new Intent(Main.this, IntentServiceLoadNewWallpaper.class);
                        intent.putExtra(IntentServiceLoadNewWallpaper.FILENAME, getResources().getString(R.string.file_name));
                        intent.putExtra(IntentServiceLoadNewWallpaper.URL_STRING, "");
                        intent.putExtra(IntentServiceLoadNewWallpaper.need_change_bg, ""); // "" - не нужно менять фон
                        intent.setAction(Helper.ACTION_NEXT_BUTTON); // была нажата кнопка "Дальше", запустится обычный сервис, не ForegroundService
                        Helper.startService(Main.this, intent);
                    }
                    else {
                        Toast.makeText(Main.this, R.string.settings_load_error, Toast.LENGTH_LONG).show();
                    }
                    break;
                // <--- buttons

                // layers --->
                case R.id.layout_enable:
                    if (checkBox_enabled.isChecked()) {
                        checkBox_enabled.setChecked(false);
                        stopService(new Intent(Main.this, ServiceRefresh.class));
                    }
                    else { // чекбокс был ВЫключен при нажатии
                        checkBox_enabled.setChecked(true);
                        if (!Helper.checkInternetConnection(Main.this, false)) {
                            Toast.makeText(Main.this, R.string.settings_internet_warn, Toast.LENGTH_LONG).show();
                        }
                        Calendar calendar = Calendar.getInstance();
                        settings.put(getResources().getString(R.string.refresh_frequency_curr_day), calendar.get(Calendar.DATE));
                        settings.put(getResources().getString(R.string.refresh_frequency_curr_week), calendar.get(Calendar.WEEK_OF_YEAR));
                        settings.put(getResources().getString(R.string.refresh_frequency_curr_month), calendar.get(Calendar.MONTH));

                        Helper.startService(Main.this);
                    }
                    settings.put(getResources().getString(R.string.enabled_pony_wallpapers), checkBox_enabled.isChecked());
                    break;

                case R.id.layout_mobile_only:
                    if (checkBox_mobile_only.isChecked()) {
                        checkBox_mobile_only.setChecked(false);

                        settings.put(getResources().getString(R.string.mobile_pony_wallpapers), checkBox_mobile_only.isChecked());

                        // restart
                        if (checkBox_enabled.isChecked()) {
                            restartService();
                        }
                    }
                    else { // чекбокс был ВЫключен при нажатии
                        checkBox_mobile_only.setChecked(true);

                        settings.put(getResources().getString(R.string.mobile_pony_wallpapers), checkBox_mobile_only.isChecked());

                        if (checkBox_enabled.isChecked()) {
                            restartService();
                        }
                    }
                    break;

                case R.id.layout_wifi_only:
                    if (checkBox_wifi_only.isChecked()) {
                        checkBox_wifi_only.setChecked(false);
                        settings.put(getResources().getString(R.string.wifi_only), checkBox_wifi_only.isChecked());
                    }
                    else { // чекбокс был ВЫключен при нажатии
                        checkBox_wifi_only.setChecked(true);
                        settings.put(getResources().getString(R.string.wifi_only), checkBox_wifi_only.isChecked());
                    }

                    break;
                // <--- layers

                case R.id.theme_preview: // press on the image
                    ProgressDialog pd = new ProgressDialog(Main.this);
                    pd.setTitle(getResources().getString(R.string.changing_wallpaper_progress_title));
                    pd.setMessage(getResources().getString(R.string.settings_progress_msg));
                    pd.setCanceledOnTouchOutside(false);
                    pd.setCancelable(false); // back btn
                    pd.show();

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            setBackground();
                            pd.dismiss();
                        }
                    };
                    thread.start();

                    break;

                // частота обновления обоев
                case R.id.layout_radio_1:
                    radio_button1.setChecked(true);
                    radio_button2.setChecked(false);
                    radio_button3.setChecked(false);
                    settings.put(getResources().getString(R.string.refresh_frequency_curr_day), calendar.get(Calendar.DATE));
                    settings.put(getResources().getString(R.string.refresh_frequency), 1);
                    if (checkBox_enabled.isChecked()) {
                        restartService();
                    }
                    break;

                case R.id.layout_radio_2:
                    radio_button1.setChecked(false);
                    radio_button2.setChecked(true);
                    radio_button3.setChecked(false);
                    settings.put(getResources().getString(R.string.refresh_frequency_curr_week), calendar.get(Calendar.WEEK_OF_YEAR));
                    settings.put(getResources().getString(R.string.refresh_frequency), 2);
                    if (checkBox_enabled.isChecked()) {
                        restartService();
                    }
                    break;

                case R.id.layout_radio_3:
                    radio_button1.setChecked(false);
                    radio_button2.setChecked(false);
                    radio_button3.setChecked(true);
                    settings.put(getResources().getString(R.string.refresh_frequency_curr_month), calendar.get(Calendar.MONTH)); // current month
                    settings.put(getResources().getString(R.string.refresh_frequency), 3);
                    if (checkBox_enabled.isChecked()) {
                        restartService();
                    }
                    break;
            }
        }
    };
    //----------------------------------------------------------------------------------------------



    private void restartService(){
        stopService(new Intent(Main.this, ServiceRefresh.class));
        Helper.startService(Main.this);
    }
    //-----------------------------------------------------------------------------------------------



    // установка фона по нажатию на preview
    public void setBackground() {
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        try {
            myWallpaperManager.setBitmap(openBackground()); // установка фона
            //Toast.makeText(Main.this, R.string.settings_preview_setbackground, Toast.LENGTH_LONG).show(); // don't work in thread
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------------------------



    // открытие фона
    private Bitmap openBackground() {
        // res. - http://www.vogella.com/tutorials/AndroidApplicationOptimization/article.html#handling-bitmaps
        // https://habrahabr.ru/post/161027/

        File background = new File(
            new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path), MODE_PRIVATE),
            getResources().getString(R.string.file_name_edited)
        );

        // если существует bg_edited.jpeg, то он и будет открыт, иначе - откроется исходное изо
        if (!background.exists()) {
            background = new File( // bg.jpeg
                new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path), MODE_PRIVATE),
                getResources().getString(R.string.file_name)
            );
        }
        FileInputStream fileInputStream = null;

        if(background.exists()) {
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



    //
    private void setWallpaperPreview () {
        currentWallpaper.setImageBitmap(openBackground()); // load wallpaper preview
        currentWallpaper.setVisibility(View.VISIBLE);
    }
    //-----------------------------------------------------------------------------------------------



    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            IntentServiceLoadNewWallpaper.Codes res = (IntentServiceLoadNewWallpaper.Codes)
                    intent.getSerializableExtra(IntentServiceLoadNewWallpaper.RESULT);

            switch (res) {
                case SUCCESS:
                    // res. - http://stackoverflow.com/questions/20053919/programmatically-set-android-phones-background
                    setWallpaperPreview();

                    // установка ссылки для загрузки
                    if (settings.contains(getResources().getString(R.string.downloadurl))) {
                        setLink(settings.getString(getResources().getString(R.string.downloadurl), ""));
                    }

                    if (progressDialog != null)
                        progressDialog.cancel();
                    unregisterReceiver(receiver);

                    Helper.toggleViewState(Main.this, btn_cancel, false);
                    Helper.toggleViewState(Main.this, btn_edit,   true);

                    // подсказка после первой загрузки изображения на кнопку "Дальше"
                    if (!settings.contains(getResources().getString(R.string.settings_hint1_flag))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                        builder.setMessage(R.string.settings_hint1_alert_msg);
                        builder.setCancelable(false);
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                settings.put(getResources().getString(R.string.settings_hint1_flag), false);
                            }
                        });
                        alertDialog = builder.create();
                        alertDialog.show();
                    }

                    break;

                case NOT_CONNECTED:
                    Toast.makeText(Main.this, getResources().getString(R.string.settings_update_error), Toast.LENGTH_LONG).show();

                    if (progressDialog != null)
                        progressDialog.cancel();
                    unregisterReceiver(receiver);
                    break;

                case NOT_JSON:
                    Toast.makeText(Main.this, getResources().getString(R.string.settings_download_error), Toast.LENGTH_LONG).show();

                    if (progressDialog != null)
                        progressDialog.cancel();
                    unregisterReceiver(receiver);
                    break;
            }
        }
    };
    //----------------------------------------------------------------------------------------------



    // установка ссылки на текст под изображением
    private void setLink (String link){
        String text = String.format(
            "<a href='%s'>%s</a>",
            link,
            getResources().getString(R.string.open_image_on_site)
        );

        textview_download_url.setText(Html.fromHtml(text));
        textview_download_url.setMovementMethod(LinkMovementMethod.getInstance());
    }
    //-----------------------------------------------------------------------------------------------



    // результат активити редактирования изо
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            currentWallpaper.setImageBitmap(openBackground());

            Helper.toggleViewState(Main.this, btn_cancel, true);

            if (!settings.contains(getResources().getString(R.string.settings_first_edit_hint))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                builder.setMessage(R.string.hint_first_edit_text);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settings.put(getResources().getString(R.string.settings_first_edit_hint), true);
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
        Helper.toggleViewState(Main.this, btn_cancel, editedImageExists());
        Helper.toggleViewState(Main.this, btn_edit,   originalImageExists());
    }
    //-----------------------------------------------------------------------------------------------



    public boolean originalImageExists(){
        File input = new File(
                new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path),
                        MODE_PRIVATE), getResources().getString(R.string.file_name));

        return input.exists();
    }
    //-----------------------------------------------------------------------------------------------



    public boolean editedImageExists(){
        File bg_edited = new File(
                new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path),
                        MODE_PRIVATE), getResources().getString(R.string.file_name_edited));
        return bg_edited.exists();
    }
    //-----------------------------------------------------------------------------------------------



    @Override
    protected void onDestroy() {
        super.onDestroy();

        checkBox_enabled = null;
        checkBox_mobile_only = null;
        checkBox_wifi_only = null;
        currentWallpaper = null;
        textview_download_url = null;
        settings = null;
        radio_button1 = null;
        radio_button2 = null;
        radio_button3 = null;

        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = null;

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = null;

        if (receiver != null) {
            try {
                unregisterReceiver(receiver); // отмена ресивера. Может быть вызвано, например, при изменении ориентации экрана.
            }
            catch (IllegalArgumentException ignored) {}
        }
        receiver = null;

        System.gc();
    }
    //----------------------------------------------------------------------------------------------
}
