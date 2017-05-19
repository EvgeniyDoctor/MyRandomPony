package ru.EvgeniyDoctor.myrandompony;

import android.app.IntentService;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IntentService_LoadNewWallpaper extends IntentService {
    // res. - http://www.vogella.com/tutorials/AndroidServices/article.html

    public static final String
            tag = "pony",
            URL_STRING = "urlpath",
            FILENAME = "filename",
            RESULT = "result",
            NOTIFICATION = "ru.EvgeniyDoctor.service.IntentService_LoadNewWallpaper",
            need_change_bg = "";
    private boolean
            error = false;



    public IntentService_LoadNewWallpaper() {
        super("IntentService");
    }
    //----------------------------------------------------------------------------------------------



    @Override
    protected void onHandleIntent(Intent intent) {
        JSONObject current_result = null;
        final boolean URL_STRING_is_empty = intent.getStringExtra(URL_STRING).equals("");

        Log.e (tag, "URL_STRING 1 = " + intent.getStringExtra(URL_STRING));

        // получаем данные с внешнего ресурса
        if (URL_STRING_is_empty) { // если надо загрузить новую картинку
            try { // функции нынче удалённого ParseTask
                Log.e (tag, "ParseTask execute");

                URL url;
                final AppPreferences settings = new AppPreferences(getApplicationContext());

                // нужное разрешение
                if (settings.getBoolean(getResources().getString(R.string.mobile_pony_wallpapers), true)) { // только с разрешением для мобильных
                    url = new URL(getResources().getString(R.string.url_request_mobile));
                }
                else { // все подряд обои
                    url = new URL(getResources().getString(R.string.url_request_all));
                }

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(60000);

                // подключение к серверу
                try {
                    if (urlConnection.getResponseCode() == 200) { // любой код restful, кроме 200 (OK)
                        Log.e(tag, "Connect OK");
                        urlConnection.connect();
                    }
                }
                catch (IOException e) {
                    Log.e(tag, "HTTP answer != OK");
                    e.printStackTrace();
                    send(1);
                    error = true;
                }

                if (!error) {
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    // ---

                    // разбор json-массива на нужные части
                    try {
                        // json structure - https://www.mylittlewallpaper.com/c/my-little-pony/api-v1
                        String server_answer = buffer.toString();
                        if (!server_answer.substring(0, 1).equals("{")) { // если первый символ buffer !={, значит, в ответ пришёл не json
                            send(2);
                            error = true;
                        }
                        else { // если в ответе сервера json
                            JSONObject dataJsonObj = new JSONObject(server_answer);
                            JSONArray jsonArray = dataJsonObj.getJSONArray("result");
                            current_result = jsonArray.getJSONObject(0);

                            // сохранение ссылки для загрузки
                            settings.put(getResources().getString(R.string.downloadurl), current_result.getString(getResources().getString(R.string.downloadurl)));

                            // сохранение ссылки для загрузки и ссылки на изображение в полном размере
                            File directory = new File(Environment.getExternalStorageDirectory() + File.separator +
                                    "/" + getResources().getString(R.string.app_name) + "/");
                            directory.mkdirs(); // создание директории, если её нет
                            File configuration = new File(directory, "data.txt"); // создание файла с указанным названием
                            FileWriter writer;
                            try {
                                writer = new FileWriter(configuration, false);
                                writer.write(current_result.getString(getResources().getString(R.string.downloadurl)) +
                                        ";" + getResources().getString(R.string.url_full_size_download) + current_result.getString("imageid") + ".png;");
                                writer.close();

                                Log.e(tag, "FileWriter");
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                                error = true;
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        error = true;
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                error = true;
            }
        }
        // ---

        if (!error) {
            try { // функции IntentService_LoadNewWallpaper
                Log.e(tag, "IntentService_LoadNewWallpaper execute");

                if (current_result != null || !URL_STRING_is_empty) {
                    Log.e(tag, "IntentService_LoadNewWallpaper TRUE current_result != null || !intent.getStringExtra(URL_STRING).equals(\"\")");

                    InputStream in;

                    if (URL_STRING_is_empty) { // загрузка новой обоины
                        in = new URL(getResources().getString(R.string.url_full_size_download) + current_result.getString("imageid") + ".png").openStream();
                    }
                    else { // восставновление ранее стоявшей обоины
                        in = new URL(intent.getStringExtra(URL_STRING)).openStream();
                    }

                    if (in != null) {
                        // масштабирование размера изображения из потока при загрузке --->
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(in, null, options);

                        options.inSampleSize = calculate_size(options, 1000, 1000);
                        options.inJustDecodeBounds = false;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;

                        if (URL_STRING_is_empty) { // загрузка новой обоины
                            in = new URL(getResources().getString(R.string.url_full_size_download) + current_result.getString("imageid") + ".png").openStream();
                        }
                        else { // восставновление ранее стоявшей обоины
                            in = new URL(intent.getStringExtra(URL_STRING)).openStream();
                        }
                        Bitmap img = BitmapFactory.decodeStream(in, null, options); // открытие масштабированного изо
                        // <---

                        if (img != null) {
                            // удаление отредактированного изо, если оно было
                            File bg_edited = new File(
                                    new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path),
                                            MODE_PRIVATE), getResources().getString(R.string.file_name_edited));
                            if (bg_edited.exists()) {
                                Log.e (tag, "IntentService delete_bg_edited OK");
                                bg_edited.delete();
                            }

                            // сохранение ноовго изо
                            FileOutputStream fos = new FileOutputStream(new File(
                                    new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path),
                                            MODE_PRIVATE), intent.getStringExtra(FILENAME))); // bg.png
                            img.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            img.recycle();

                            // send result
                            if (intent.getStringExtra(need_change_bg).equals("")) { // не нужно менять фон
                                send(-1);
                            }
                            else { // нужно изменить фон
                                send(0);
                            }

                            System.gc();
                        }
                    }
                }
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
    //----------------------------------------------------------------------------------------------



    // вычисление размеров картинки
    public static int calculate_size(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height >> 1;
            final int halfWidth = width >> 1;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize <<= 1;
            }
        }

        return inSampleSize;
    }
    //----------------------------------------------------------------------------------------------



    // value:
    // -1 - успех, фон НЕ будет изменён;
    // 0 - успех, фон будет изменён;
    // 1 - ошибка подключения к серверу;
    // 2 - в ответе сервера не json
    private void send (int value) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT, value);
        sendBroadcast(intent);

        Log.e(tag, "IntentService sendBroadcast = " + value);
    }
    //----------------------------------------------------------------------------------------------
}