package ru.EvgeniyDoctor.myrandompony;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



enum DownloadResult {
    SUCCESS,
    CONNECTED,                    // successful load, the background will NOT be changed;
    SUCCESS_CHANGE_WALLPAPER,   // successful load, the background will be changed;
    NOT_CONNECTED,              // url does not exist or connect timeout
    NOT_JSON,                   // the response is not json
    ERROR_IMAGE,
    ERROR_JSON,
    ERROR_SAVE,
    UNKNOWN,
}



public abstract class ImageProvider {
    public final Context context;
    public final AppPreferences settings;
    public final int connectTimeout = 5000; // 5 sec
    public final int setReadTimeout = 10000; // 10 sec



    ImageProvider(Context context, AppPreferences settings) {
        this.context = context;
        this.settings = settings;
    }
    //----------------------------------------------------------------------------------------------



    public DownloadResult load(String apiRequest){
        // check connection
        HttpURLConnection httpURLConnection = checkConnection(apiRequest);
        if (httpURLConnection == null) {
            return DownloadResult.NOT_CONNECTED;
        }

        //
        JSONObject jsonObject;
        jsonObject = getJson(httpURLConnection);
        if (jsonObject == null) {
            return DownloadResult.NOT_JSON;
        }

        //
        jsonObject = parseJson(jsonObject);
        if (jsonObject == null) {
            return DownloadResult.ERROR_JSON;
        }

        //
        Bitmap bitmap = downloadImage(getDownloadLink(jsonObject));
        if (bitmap == null) {
            return DownloadResult.ERROR_IMAGE;
        }

        //
        boolean res = saveImage(bitmap);
        if (!res) {
            return DownloadResult.ERROR_SAVE;
        }

        return DownloadResult.SUCCESS;
    }
    //----------------------------------------------------------------------------------------------



    public HttpURLConnection checkConnection(String urlString) {
        try {
            URL url = new URL(urlString);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(connectTimeout);
            urlConnection.setReadTimeout(setReadTimeout);

            // connect
            if (urlConnection.getResponseCode() == 200) { // restful code 200 (OK)
                urlConnection.connect();
                return urlConnection;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    //----------------------------------------------------------------------------------------------




    public JSONObject getJson(HttpURLConnection httpURLConnection){
        try {
            InputStream inputStream = httpURLConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            // parsing json
            // json structure - https://www.mylittlewallpaper.com/c/my-little-pony/api-v1
            String answer = buffer.toString();
            if (answer.charAt(0) != '{') {
                return null;
            }
            else { // если в ответе сервера json // json - ok
                return new JSONObject(answer);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //----------------------------------------------------------------------------------------------



    public abstract JSONObject parseJson(JSONObject jsonObject);
    //----------------------------------------------------------------------------------------------



    public abstract String getDownloadLink(JSONObject jsonObject);
    //----------------------------------------------------------------------------------------------



    public Bitmap downloadImage(String urlString){
        try {
            URL url = new URL(urlString);

            // load new wallpaper
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

            return BitmapFactory.decodeStream(in, null, options); // открытие масштабированного изо // opening a scaled image
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    //----------------------------------------------------------------------------------------------



    public boolean saveImage(Bitmap bitmap){
        try {
            // deleting the edited image
            File bg_edited = new File(
                new ContextWrapper(context).getDir(Pref.SAVE_PATH, Context.MODE_PRIVATE),
                Pref.IMAGE_EDITED
            );
            if (bg_edited.exists()) {
                boolean res = bg_edited.delete();
            }

            // save new image
            FileOutputStream fos = new FileOutputStream(
                new File(
                    new ContextWrapper(context).getDir(Pref.SAVE_PATH, Context.MODE_PRIVATE),
                    Pref.IMAGE_ORIGINAL
                )
            );
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            bitmap.recycle();

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return false;

        // send result
//        if (!needChangeBg) { // не нужно менять фон
//            return Codes.CONNECTED;
//        }
//        else { // нужно изменить фон // change wallpaper
//            return Codes.SUCCESS_CHANGE_WALLPAPER;
//        }
    }
    //----------------------------------------------------------------------------------------------



    public int getMaxSize(){
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
    public static int calculateSize (BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
