package ru.EvgeniyDoctor.myrandompony;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



// class for load new image. Working for BackgroundService, ForegroundService and "Next" button



public class LoadNewWallpaper {
    enum Codes {
        SUCCESS,                    // successful load, the background will NOT be changed;
        SUCCESS_CHANGE_WALLPAPER,   // successful load, the background will be changed;
        NOT_CONNECTED,              // url does not exist or connect timeout
        NOT_JSON,                   // the response is not json
    }

    private final Context context;
    private final AppPreferences settings;
    private final boolean needChangeBg;

    private final String URL_GET_MOBILE_ONLY = "https://www.mylittlewallpaper.com/c/my-little-pony/api/v1/random.json?search=platform%3AMobile&limit=1";
    private final String URL_GET_ALL = "https://www.mylittlewallpaper.com/c/my-little-pony/api/v1/random.json?limit=1&search=";
    private final String URL_NEW_WALLPAPER = "https://www.mylittlewallpaper.com/images/o_%s.png"; // url full size download



    // constructor
    LoadNewWallpaper(Context context, AppPreferences settings, boolean needChangeBg) {
        this.context = context;
        this.settings = settings;
        this.needChangeBg = needChangeBg;
    }
    //-----------------------------------------------------------------------------------------------



    // load new image
    public Codes load() {
        JSONObject current_result = null;
        boolean error = false;

        // загрузка данные с внешнего ресурса // loading data from an external resource
        try {
            Helper.d("ParseTask execute");

            URL url;

            // нужное разрешение // required screen resolution
            if (settings.getBoolean(Pref.MOBILE_ONLY, true)) { // только с разрешением для мобильных // mobile screen resolution only
                url = new URL(URL_GET_MOBILE_ONLY);
            }
            else {
                url = new URL(URL_GET_ALL);
            }

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(10000); // 10 sec

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
                return Codes.NOT_CONNECTED;
            }

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
                    return Codes.NOT_JSON;
                }
                else { // если в ответе сервера json // json - ok
                    JSONObject dataJsonObj = new JSONObject(server_answer);
                    JSONArray jsonArray = dataJsonObj.getJSONArray("result");
                    current_result = jsonArray.getJSONObject(0);

                    settings.put(Pref.IMAGE_TITLE, current_result.getString("title")); // title of the image

                    // сохранение ссылки для загрузки (откроется страница с картинкой) // saving the download link (a page with an image opens)
                    settings.put(Pref.IMAGE_URL, current_result.getString("downloadurl"));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                error = true;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            error = true;
        }
        // ---

        if (!error) {
            try {
                Helper.d("IntentService_LoadNewWallpaper execute");

                URL url = new URL(String.format(URL_NEW_WALLPAPER, current_result.getString("imageid")));
                //URL url = new URL("https://www.mylittlewallpaper.com/images/o_501fb9f9f196a3.82550842.png");
                //URL url = new URL("https://www.mylittlewallpaper.com/images/o_5bbb8e74e84398.86540959.png");

                // загрузка новой обоины // load new wallpaper --->
                InputStream in = url.openStream();

                // масштабирование размера изображения из потока при загрузке // scaling the image size from the stream when loading --->
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, options);

                int maxSize = getMaxSize(); // 1000 by default
                options.inSampleSize = calculateSize(options, maxSize, maxSize);
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;

                in = url.openStream();
                Bitmap img = BitmapFactory.decodeStream(in, null, options); // открытие масштабированного изо // opening a scaled image
                // <--- scaling the image size from the stream when loading

                if (img != null) {
                    // удаление отредактированного изо, если оно было // deleting the edited image
                    File bg_edited = new File(
                        new ContextWrapper(context).getDir(Pref.SAVE_PATH, Context.MODE_PRIVATE),
                        Pref.IMAGE_EDITED
                    );
                    if (bg_edited.exists()) {
                        Helper.d("IntentService delete_bg_edited OK");
                        bg_edited.delete();
                    }

                    // сохранение нового изо // save new image
                    FileOutputStream fos = new FileOutputStream(
                        new File(
                            new ContextWrapper(context).getDir(Pref.SAVE_PATH, Context.MODE_PRIVATE),
                            Pref.IMAGE_ORIGINAL
                        )
                    ); // bg.png
                    img.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    img.recycle();

                    // send result
                    if (!needChangeBg) { // не нужно менять фон
                        return Codes.SUCCESS;
                    }
                    else { // нужно изменить фон // change wallpaper
                        return Codes.SUCCESS_CHANGE_WALLPAPER;
                    }
                }
                // <--- загрузка новой обоины // load new wallpaper
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Codes.NOT_CONNECTED;
    }
    //----------------------------------------------------------------------------------------------



    private int getMaxSize(){
        int resolution = 0;
        int max = 1000;

        if (settings.contains(Pref.SCREEN_RESOLUTION)) {
            resolution = settings.getInt(Pref.SCREEN_RESOLUTION, 0);
        }

        switch (resolution) {
            case 0: // normal
                max = 1000;
                break;
            case 1: // large
                max = 1500;
                break;
        }

        return max;
    }
    //----------------------------------------------------------------------------------------------



    // вычисление размеров картинки // calculating the size of the image
    private static int calculateSize (BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
}
