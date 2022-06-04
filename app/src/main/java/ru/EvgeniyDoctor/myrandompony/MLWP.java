package ru.EvgeniyDoctor.myrandompony;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MLWP extends ImageProvider {
    private final String URL_GET_ALL = "https://www.mylittlewallpaper.com/c/my-little-pony/api/v1/random.json?limit=1&search=";
    private final String URL_NEW_WALLPAPER = "https://www.mylittlewallpaper.com/images/o_%s.png"; // url full size download



    MLWP(Context context, AppPreferences settings) {
        super(context, settings); // abstract constructor
    }
    //----------------------------------------------------------------------------------------------



    public DownloadResult load(){
        return super.load(URL_GET_ALL);
    }
    //----------------------------------------------------------------------------------------------



    public JSONObject parseJson(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            JSONObject json = jsonArray.getJSONObject(0);

            settings.put(Pref.IMAGE_TITLE, json.getString("title")); // title of the image
            settings.put(Pref.IMAGE_URL, json.getString("downloadurl")); // download link (a page with an image opens)

            return json;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
    //----------------------------------------------------------------------------------------------



    public String getDownloadLink(JSONObject jsonObject) {
        try {
            return String.format(URL_NEW_WALLPAPER, jsonObject.getString("imageid"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    //----------------------------------------------------------------------------------------------
}
