package ru.EvgeniyDoctor.myrandompony;

import android.app.IntentService;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class IntentServiceLoadNewWallpaper extends IntentService {
    // res. - http://www.vogella.com/tutorials/AndroidServices/article.html

    public static final String
            URL_STRING = "urlpath",
            FILENAME = "filename",
            RESULT = "result",
            NOTIFICATION_LOAD_NEW_WALLPAPER = "ru.EvgeniyDoctor.service.IntentServiceLoadNewWallpaper",
            NEED_CHANGE_BG = "";
    private boolean
            error = false;
    enum Codes {
        SUCCESS,
        CHANGE_WALLPAPER,
        NOT_CONNECTED, // url does not exist or connect timeout
        NOT_JSON, // в ответе не json // the response is not json
    }



    public IntentServiceLoadNewWallpaper() {
        super("IntentService");
    }
    //----------------------------------------------------------------------------------------------



    @Override
    protected void onHandleIntent(Intent intent) {
        JSONObject current_result = null;
        final boolean URL_STRING_is_empty = intent.getStringExtra(URL_STRING).equals("");

        Helper.d("URL_STRING 1 = " + intent.getStringExtra(URL_STRING));

        // загрузка данные с внешнего ресурса // loading data from an external resource
        if (URL_STRING_is_empty) { // если надо загрузить новую картинку // if you need to upload a new image
            try {
                Helper.d("ParseTask execute");

                URL url;
                final AppPreferences settings = new AppPreferences(getApplicationContext());

                // нужное разрешение // required screen resolution
                if (settings.getBoolean(getResources().getString(R.string.mobile_pony_wallpapers), true)) { // только с разрешением для мобильных // mobile screen resolution only
                    url = new URL(getResources().getString(R.string.url_request_mobile));
                }
                else {
                    url = new URL(getResources().getString(R.string.url_request_all));
                }

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(60000);

                // подключение к серверу // connect to the server
                try {
                    if (urlConnection.getResponseCode() == 200) { // restful code 200 (OK)
                        Helper.d("Connect OK");
                        urlConnection.connect();
                    }
                }
                catch (IOException e) {
                    Helper.d("HTTP answer != OK");
                    e.printStackTrace();
                    send(Codes.NOT_CONNECTED);
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

                    // разбор json-массива на нужные части // parsing a json array
                    try {
                        // json structure - https://www.mylittlewallpaper.com/c/my-little-pony/api-v1
                        String server_answer = buffer.toString();
                        if (server_answer.charAt(0) != '{') { // если первый символ buffer !={, значит, в ответ пришёл не json // if the first character in buffer != {, it means that the response did not come from json
                            send(Codes.NOT_JSON);
                            error = true;
                        }
                        else { // если в ответе сервера json // json - ok
                            JSONObject dataJsonObj = new JSONObject(server_answer);
                            JSONArray jsonArray = dataJsonObj.getJSONArray("result");
                            current_result = jsonArray.getJSONObject(0);

                            // сохранение ссылки для загрузки (откроется страница с картинкой) // saving the download link (a page with an image opens)
                            settings.put(getResources().getString(R.string.downloadurl), current_result.getString(getResources().getString(R.string.downloadurl)));
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
            try {
                Helper.d("IntentService_LoadNewWallpaper execute");

                if (current_result != null || !URL_STRING_is_empty) {
                    Helper.d("IntentService_LoadNewWallpaper TRUE current_result != null || !intent.getStringExtra(URL_STRING).equals(\"\")");

                    InputStream in;

                    if (URL_STRING_is_empty) { // загрузка новой обоины // load new wallpaper
                        in = new URL(getResources().getString(R.string.url_full_size_download) + current_result.getString("imageid") + ".png").openStream();
                    }
                    else { // восставновление ранее стоявшей обоины // restoring a previous wallpaper
                        in = new URL(intent.getStringExtra(URL_STRING)).openStream();
                    }

                    if (in != null) {
                        // масштабирование размера изображения из потока при загрузке // scaling the image size from the stream when loading --->
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(in, null, options);

                        options.inSampleSize = calculateSize(options, 1000, 1000);
                        options.inJustDecodeBounds = false;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;

                        if (URL_STRING_is_empty) { // загрузка новой обоины // load a new wallpaper
                            in = new URL(getResources().getString(R.string.url_full_size_download) + current_result.getString("imageid") + ".png").openStream();
                        }
                        else { // восставновление ранее стоявшей обоины // restoring a previous wallpaper
                            in = new URL(intent.getStringExtra(URL_STRING)).openStream();
                        }
                        Bitmap img = BitmapFactory.decodeStream(in, null, options); // открытие масштабированного изо // opening a scaled image
                        // <---



                        if (img != null) {
                            // удаление отредактированного изо, если оно было // deleting the edited image
                            File bg_edited = new File(
                                    new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path),
                                            MODE_PRIVATE), getResources().getString(R.string.file_name_edited));
                            if (bg_edited.exists()) {
                                Helper.d("IntentService delete_bg_edited OK");
                                bg_edited.delete();
                            }

                            // сохранение нового изо // save new image
                            FileOutputStream fos = new FileOutputStream(new File(
                                    new ContextWrapper(getApplicationContext()).getDir(getResources().getString(R.string.save_path),
                                            MODE_PRIVATE), intent.getStringExtra(FILENAME))); // bg.png
                            img.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            img.recycle();

                            // send result
                            if (intent.getStringExtra(NEED_CHANGE_BG).equals("")) { // не нужно менять фон
                                send(Codes.SUCCESS);
                            }
                            else { // нужно изменить фон // change wallpaper
                                send(Codes.CHANGE_WALLPAPER);
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



    // вычисление размеров картинки // calculating the size of the image
    public static int calculateSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
    // -1 - успех, фон НЕ будет изменён;    // success, the background will NOT be changed;
    // 0 - успех, фон будет изменён;        // success, the background will be changed;
    // 1 - ошибка подключения к серверу;    // server connection error;
    // 2 - в ответе сервера не json         // the server response is not json
    private void send (Codes value) {
        Intent intent = new Intent(NOTIFICATION_LOAD_NEW_WALLPAPER);
        intent.putExtra(RESULT, value);
        sendBroadcast(intent);

        Helper.d("IntentService sendBroadcast = " + value);
    }
    //----------------------------------------------------------------------------------------------
}