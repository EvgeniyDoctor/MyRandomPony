package ru.EvgeniyDoctor.myrandompony;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class Derpibooru extends ImageProviders {
    public static final int TAG_WALLPAPER = 1;
    public static final int TAG_SAFE      = 2;



    Derpibooru(Context context, AppPreferences settings) {
        super(context, settings); // abstract constructor
    }
    //----------------------------------------------------------------------------------------------



    @Override
    public DownloadResult load() {
        String url = "https://derpibooru.org/api/v1/json/search/images?q=%sanimated:false&sf=random&per_page=1";

        int tags = 0b11;
        if (settings.contains(Pref.DERPIBOORU_TAGS)){
            tags = settings.getInt(Pref.DERPIBOORU_TAGS, 0b11);
        }

        switch (tags) {
            case TAG_WALLPAPER | TAG_SAFE:
                url = String.format(url, "wallpaper,safe,");
                break;
            case TAG_WALLPAPER:
                url = String.format(url, "wallpaper,");
                break;
            case TAG_SAFE:
                url = String.format(url, "safe,");
                break;
            default:
                url = String.format(url, ""); // almost all images
                break;
        }

        return super.load(url); // api request
    }
    //----------------------------------------------------------------------------------------------



    @Override
    public JSONObject parseJson(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("images");
            JSONObject json = jsonArray.getJSONObject(0);

            settings.put(Pref.IMAGE_TITLE, json.getString("name")); // title of the image
            settings.put(Pref.IMAGE_URL, "https://derpibooru.org/images/" + json.getString("id")); // image page

            return json;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
    //----------------------------------------------------------------------------------------------



    @Override
    public String getDownloadLink(JSONObject jsonObject) {
        try {
            return jsonObject.getJSONObject("representations").getString("full");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    //----------------------------------------------------------------------------------------------
}
