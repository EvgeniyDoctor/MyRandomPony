package ru.EvgeniyDoctor.myrandompony;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class Derpibooru extends ImageProviders {
    Derpibooru(Context context, AppPreferences settings) {
        super(context, settings); // abstract constructor
    }
    //----------------------------------------------------------------------------------------------



    @Override
    public DownloadResult load() {
        return super.load("https://derpibooru.org/api/v1/json/search/images?q=wallpaper,safe&sf=random&per_page=1"); // api request
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
