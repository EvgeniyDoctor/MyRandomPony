package ru.EvgeniyDoctor.myrandompony;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class MLWP extends ImageProviders {
    MLWP(Context context, AppPreferences settings) {
        super(context, settings); // abstract constructor
    }
    //----------------------------------------------------------------------------------------------



    public DownloadResult load(){
        return super.load("https://www.mylittlewallpaper.com/c/my-little-pony/api/v1/random.json?limit=1&search="); // api request
    }
    //----------------------------------------------------------------------------------------------



    public JSONObject parseJson(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            JSONObject json = jsonArray.getJSONObject(0);

            settings.put(Pref.IMAGE_TITLE, json.getString("title")); // title of the image
            settings.put(Pref.IMAGE_URL, json.getString("downloadurl")); // image page

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
            return String.format("https://www.mylittlewallpaper.com/images/o_%s.png", jsonObject.getString("imageid"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    //----------------------------------------------------------------------------------------------
}
