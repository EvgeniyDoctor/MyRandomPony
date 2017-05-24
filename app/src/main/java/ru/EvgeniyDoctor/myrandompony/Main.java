package ru.EvgeniyDoctor.myrandompony;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.util.Log;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

public class Main extends AppCompatActivity {

    // TODO: 5/20/17 Несрочно - если нажать "Дальше", затем перевернуть экран
    // (и я не помню, то ли ещё раз надо перевернуть), то изображение пропадёт, а позже, видимо, при окончании загрузки,
    // при новом повороте экрана появится новое, только что загруженное изображение. Подумать, нужно ли с этим что-то делать.

    private boolean
            flag_qstn_about_load = false;
    private static final String
            tag = "pony";
    private CheckBox
            checkBox_enabled,
            checkBox_mobile_only,
            checkBox_wifi_only;
    private ImageView
            current_wallpaper;
    private TextView
            textview_download_url;
    private ProgressDialog
            progressDialog = null;
    private RadioButton
            radio_button1,
            radio_button2,
            radio_button3;
    private AlertDialog
        alertDialog = null;
    /*
        alertDialog - переменная для показа диалоговых окон.
    Нужна, так как если делать напрямую - builder.show(); то если, например, во время показа диалога вёрстка экрана изменится
    ориентация экрана, это вызовет Activity ... has leaked.
    Обнуляется в onDestroy.
    res. - http://stackoverflow.com/questions/11051172/progressdialog-and-alertdialog-cause-leaked-window
     */

    static AppPreferences
            settings; // res. - https://github.com/grandcentrix/tray

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // логи при ошибке
        RoboErrorReporter.bindReporter(this.getApplicationContext());

        settings = new AppPreferences(getApplicationContext());

        Button btn_cancel = (Button) findViewById(R.id.btn_cancel);
        Button btn_edit = (Button) findViewById(R.id.btn_edit);
        Button btn_next = (Button) findViewById(R.id.btn_next);
        FrameLayout enable_layout = (FrameLayout) findViewById(R.id.enable_layout);
        FrameLayout mobile_only_layout = (FrameLayout) findViewById(R.id.mobile_only_layout);
        FrameLayout wifi_only_layout = (FrameLayout) findViewById(R.id.wifi_only_layout);
        checkBox_enabled = (CheckBox) findViewById(R.id.enable_checkbox);
        checkBox_mobile_only = (CheckBox) findViewById(R.id.only_mobile);
        checkBox_wifi_only = (CheckBox) findViewById(R.id.only_wifi);
        current_wallpaper = (ImageView) findViewById(R.id.current_wallpaper);
        textview_download_url = (TextView) findViewById(R.id.download_url);
        radio_button1 = (RadioButton) findViewById(R.id.radio_1);
        radio_button2 = (RadioButton) findViewById(R.id.radio_2);
        radio_button3 = (RadioButton) findViewById(R.id.radio_3);

        btn_cancel.setOnClickListener(click);
        btn_edit.setOnClickListener(click);
        btn_next.setOnClickListener(click);
        enable_layout.setOnClickListener(click);
        mobile_only_layout.setOnClickListener(click);
        wifi_only_layout.setOnClickListener(click);

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
            startService(new Intent(this, Service_Refresh.class));
        }
        else {
            stopService(new Intent(this, Service_Refresh.class));
        }

        // если программа была установлена до этого, но удалена --->
        if (savedInstanceState != null) {
            flag_qstn_about_load = savedInstanceState.getBoolean(getResources().getString(R.string.flag_qstn_about_load));
        }

        if (!flag_qstn_about_load && !new File(
                new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path), MODE_PRIVATE),
                getResources().getString(R.string.file_name)).exists()
                &&
                new File(new File(Environment.getExternalStorageDirectory() + File.separator + "/" + getResources().getString(R.string.app_name) + "/"),
                        "data.txt").exists()
                ) { // если не сущесвует bg.jpeg и существует .My Random Pony/data
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.settings_alert_msg);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    flag_qstn_about_load = true;
                    load_previous_wallpaper();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    flag_qstn_about_load = true;
                    dialog.cancel();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
        //<---

        // установка ссылки
        if (settings.contains(getResources().getString(R.string.downloadurl))) {
            textview_download_url.setText(settings.getString(getResources().getString(R.string.downloadurl), ""));
            Linkify.addLinks(textview_download_url, Linkify.ALL);
        }

        // загрузка предпросмотра
        current_wallpaper.setImageBitmap(open_background());

        progressDialog = new ProgressDialog(Main.this);
    }
    //----------------------------------------------------------------------------------------------



    View.OnClickListener click = new View.OnClickListener() {
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
                            current_wallpaper.setImageBitmap(open_background());
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
                        return;
                    }

                    File output = new File(
                            new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path),
                                    MODE_PRIVATE), getResources().getString(R.string.file_name_edited));

                    // res - https://github.com/Yalantis/uCrop
                    UCrop.Options options = new UCrop.Options();
                    options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                    options.setStatusBarColor(Color.parseColor("#9a5da2"));
                    options.setToolbarColor(Color.parseColor("#c590c9"));
                    options.setActiveWidgetColor(Color.parseColor("#51c456")); // текст снизу
                    options.setShowCropGrid(false); // вид матрицы
                    options.setToolbarTitle(getResources().getString(R.string.settings_image_edit));
                    UCrop.of(Uri.fromFile(input), Uri.fromFile(output)).withOptions(options).start(Main.this);
                    break;

                case R.id.btn_next: // кнопка "дальше"
                    if (check_internet_connection(settings.getBoolean(getResources().getString(R.string.wifi_only), true))) {
                        // hint
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

                        progressDialog.setTitle(getResources().getString(R.string.settings_progress_title));
                        progressDialog.setMessage(getResources().getString(R.string.settings_progress_msg));
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        registerReceiver(receiver, new IntentFilter(IntentService_LoadNewWallpaper.NOTIFICATION));

                        Intent intent = new Intent(Main.this, IntentService_LoadNewWallpaper.class);
                        intent.putExtra(IntentService_LoadNewWallpaper.FILENAME, getResources().getString(R.string.file_name));
                        intent.putExtra(IntentService_LoadNewWallpaper.URL_STRING, "");
                        intent.putExtra(IntentService_LoadNewWallpaper.need_change_bg, ""); // "" - не нужно менять фон
                        startService(intent);
                    }
                    else {
                        Toast.makeText(Main.this, R.string.settings_load_error, Toast.LENGTH_LONG).show();
                    }
                    break;
                // <--- buttons

                // layers --->
                case R.id.enable_layout:
                    if (checkBox_enabled.isChecked()) {
                        checkBox_enabled.setChecked(false);
                        stopService(new Intent(Main.this, Service_Refresh.class));
                    }
                    else { // чекбокс был ВЫключен при нажатии
                        checkBox_enabled.setChecked(true);
                        if (!check_internet_connection(false)) {
                            Toast.makeText(Main.this, R.string.settings_internet_warn, Toast.LENGTH_LONG).show();
                        }
                        Calendar calendar = Calendar.getInstance();
                        settings.put(getResources().getString(R.string.refresh_frequency_curr_day), calendar.get(Calendar.DATE));
                        settings.put(getResources().getString(R.string.refresh_frequency_curr_week), calendar.get(Calendar.WEEK_OF_YEAR));
                        settings.put(getResources().getString(R.string.refresh_frequency_curr_month), calendar.get(Calendar.MONTH));

                        startService(new Intent(Main.this, Service_Refresh.class));
                    }
                    settings.put(getResources().getString(R.string.enabled_pony_wallpapers), checkBox_enabled.isChecked());
                    break;

                case R.id.mobile_only_layout:
                    if (checkBox_mobile_only.isChecked()) {
                        checkBox_mobile_only.setChecked(false);

                        settings.put(getResources().getString(R.string.mobile_pony_wallpapers), checkBox_mobile_only.isChecked());

                        // restart
                        if (checkBox_enabled.isChecked()) {
                            stopService(new Intent(Main.this, Service_Refresh.class));
                            startService(new Intent(Main.this, Service_Refresh.class));
                        }
                    }
                    else { // чекбокс был ВЫключен при нажатии
                        checkBox_mobile_only.setChecked(true);

                        settings.put(getResources().getString(R.string.mobile_pony_wallpapers), checkBox_mobile_only.isChecked());

                        if (checkBox_enabled.isChecked()) {
                            stopService(new Intent(Main.this, Service_Refresh.class));
                            startService(new Intent(Main.this, Service_Refresh.class));
                        }
                    }
                    break;

                case R.id.wifi_only_layout:
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
            }
        }
    };
    //----------------------------------------------------------------------------------------------



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(getResources().getString(R.string.flag_qstn_about_load), flag_qstn_about_load);
    }
    //----------------------------------------------------------------------------------------------



    @Override
    protected void onDestroy() {
        super.onDestroy();

        checkBox_enabled = null;
        checkBox_mobile_only = null;
        checkBox_wifi_only = null;
        current_wallpaper = null;
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



    // частота обновления обоев
    public void refresh_frequency(View view) {
        Calendar calendar = Calendar.getInstance();

        switch (view.getId()) {
            case R.id.layout_radio_1:
                radio_button1.setChecked(true);
                radio_button2.setChecked(false);
                radio_button3.setChecked(false);
                settings.put(getResources().getString(R.string.refresh_frequency_curr_day), calendar.get(Calendar.DATE));
                settings.put(getResources().getString(R.string.refresh_frequency), 1);
                if (checkBox_enabled.isChecked()) {
                    stopService(new Intent(this, Service_Refresh.class));
                    startService(new Intent(this, Service_Refresh.class));
                }
                break;
            case R.id.layout_radio_2:
                radio_button1.setChecked(false);
                radio_button2.setChecked(true);
                radio_button3.setChecked(false);
                settings.put(getResources().getString(R.string.refresh_frequency_curr_week), calendar.get(Calendar.WEEK_OF_YEAR));
                settings.put(getResources().getString(R.string.refresh_frequency), 2);
                if (checkBox_enabled.isChecked()) {
                    stopService(new Intent(this, Service_Refresh.class));
                    startService(new Intent(this, Service_Refresh.class));
                }
                break;
            case R.id.layout_radio_3:
                radio_button1.setChecked(false);
                radio_button2.setChecked(false);
                radio_button3.setChecked(true);
                settings.put(getResources().getString(R.string.refresh_frequency_curr_month), calendar.get(Calendar.MONTH)); // current month
                settings.put(getResources().getString(R.string.refresh_frequency), 3);
                if (checkBox_enabled.isChecked()) {
                    stopService(new Intent(this, Service_Refresh.class));
                    startService(new Intent(this, Service_Refresh.class));
                }
                break;
        }
    }
    //----------------------------------------------------------------------------------------------



    // вызывается, если прога была установлена ранее, но удалена, и нужна обоина, стоявшая ранее
    public void next_wallpaper() {
        progressDialog.setTitle(getResources().getString(R.string.settings_progress_title));
        progressDialog.setMessage(getResources().getString(R.string.settings_progress_msg));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        registerReceiver(receiver, new IntentFilter(IntentService_LoadNewWallpaper.NOTIFICATION));

        // res. - http://www.vogella.com/tutorials/AndroidServices/article.html
        Intent intent = new Intent(Main.this, IntentService_LoadNewWallpaper.class);
        intent.putExtra(IntentService_LoadNewWallpaper.FILENAME, getResources().getString(R.string.file_name));
        intent.putExtra(IntentService_LoadNewWallpaper.URL_STRING, settings.getString(getResources().getString(R.string.url_full_size_download_pref), "")); // путь уже содержит id и .jpeg
        intent.putExtra(IntentService_LoadNewWallpaper.need_change_bg, ""); // "" - не нужно менять фон
        startService(intent);
    }
    //----------------------------------------------------------------------------------------------



    // установка фона по нажатию на preview
    public void set_background(View view) {
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        try {
            myWallpaperManager.setBitmap(open_background()); // установка фона
            Toast.makeText(this, R.string.settings_preview_setbackground, Toast.LENGTH_LONG).show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------------------------



    // чтение файла со ссылками и их сохранение
    private void load_previous_wallpaper () {
        if (check_internet_connection(settings.getBoolean(getResources().getString(R.string.wifi_only), true))) { // проверка здесь, потому что иначе если нажать "Да" при отсутствии связи, появится ссылка без изображения
            // если не существует, чтение ссылок из файла страницы загрузки и загрузки в полном размере
            //File file_path = new File(Environment.getExternalStorageDirectory() + File.separator +
            //        "/." + getResources().getString(R.string.app_name) + "/");
            File configuration = new File(new File(Environment.getExternalStorageDirectory() + File.separator +
                    "/" + getResources().getString(R.string.app_name) + "/"),
                    "data.txt");
            StringBuilder stringBuilder = new StringBuilder();
            String str;

            // чтение файла data
            try {
                BufferedReader br = new BufferedReader(new FileReader(configuration));

                while ((str = br.readLine()) != null) {
                    stringBuilder.append(str);
                    stringBuilder.append('\n');
                }
                br.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            // разделение ссылок
            char[] _symbol = stringBuilder.toString().toCharArray();
            stringBuilder.setLength(0);
            boolean flag = false;
            for (char i : _symbol) { // data содержит все ссылки в одну строку
                switch (i) {
                    case ';':
                        if (!flag) {
                            settings.put(getResources().getString(R.string.downloadurl), stringBuilder.toString());

                            stringBuilder.setLength(0);
                            flag = true;
                        }
                        else {
                            settings.put(getResources().getString(R.string.url_full_size_download_pref), stringBuilder.toString());

                            stringBuilder.setLength(0);
                        }
                        break;

                    default:
                        stringBuilder.append(i);
                }
            }

            next_wallpaper(); // загрузка нужной обои
        }
        else {
            Toast.makeText(Main.this, R.string.settings_load_error, Toast.LENGTH_LONG).show();
        }
    }
    //----------------------------------------------------------------------------------------------



    // проверка, доступна ли сеть
    private boolean check_internet_connection(boolean need_type) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        }

        if (need_type) { // если важен тип подключения
            if (netInfo.isConnectedOrConnecting()) {
                if (settings.getBoolean(getResources().getString(R.string.wifi_only), true)) { // если нужен только wifi
                    return netInfo.getTypeName().equals("WIFI");
                }
            }
        }

        return netInfo.isConnectedOrConnecting();
    }
    //----------------------------------------------------------------------------------------------



    // о приложении
    public void about(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about);
        builder.setMessage(R.string.copyright);
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
    //----------------------------------------------------------------------------------------------



    // открытие фона
    private Bitmap open_background() {
        // res. - http://www.vogella.com/tutorials/AndroidApplicationOptimization/article.html#handling-bitmaps
        // https://habrahabr.ru/post/161027/

        File background = new File(
                new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path), MODE_PRIVATE),
                getResources().getString(R.string.file_name_edited));

        // если существует bg_edited.jpeg, то он и будет открыт, иначе - откроется исходное изо
        if (! background.exists()) {
            background = new File( // bg.jpeg
                    new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path), MODE_PRIVATE),
                    getResources().getString(R.string.file_name));
        }
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
    //----------------------------------------------------------------------------------------------



    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                //String string = bundle.getString(IntentService.FILEPATH);
                //int resultCode = bundle.getInt(IntentService_LoadNewWallpaper.RESULT);

                switch (bundle.getInt(IntentService_LoadNewWallpaper.RESULT)) {
                    case -1: // ok
                        // res. - http://stackoverflow.com/questions/20053919/programmatically-set-android-phones-background
                        // установка фона
                        current_wallpaper.setImageBitmap(open_background());

                        if (settings.contains(getResources().getString(R.string.downloadurl))) {
                            textview_download_url.setText(settings.getString(getResources().getString(R.string.downloadurl), ""));
                            Linkify.addLinks(textview_download_url, Linkify.WEB_URLS);
                        }

                        if (progressDialog != null)
                            progressDialog.cancel();
                        unregisterReceiver(receiver);
                        break;
                    case 1: // url does not exist or connect timeout
                        Toast.makeText(Main.this, getResources().getString(R.string.settings_update_error), Toast.LENGTH_LONG).show();

                        if (progressDialog != null)
                            progressDialog.cancel();
                        unregisterReceiver(receiver);
                        break;
                    case 2: // в ответ пришёл не json
                        Toast.makeText(Main.this, getResources().getString(R.string.settings_download_error), Toast.LENGTH_LONG).show();

                        if (progressDialog != null)
                            progressDialog.cancel();
                        unregisterReceiver(receiver);
                        break;
                }
            }
        }
    };
    //----------------------------------------------------------------------------------------------



    // результат активити редактирования изо
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            current_wallpaper.setImageBitmap(open_background());
            Log.e(tag, "crop OK");
        }
        else if (resultCode == UCrop.RESULT_ERROR) {
            Log.e(tag, "crop not OK");
        }
    }
    //----------------------------------------------------------------------------------------------
}
