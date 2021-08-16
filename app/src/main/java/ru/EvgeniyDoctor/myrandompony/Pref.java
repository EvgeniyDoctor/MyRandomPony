package ru.EvgeniyDoctor.myrandompony;

// class with keys names in app prefs

abstract class Pref {
    public static final String
        ENABLED             = "enabled_pony_wallpapers",
        MOBILE_ONLY         = "mobile_pony_wallpapers",
        WIFI_ONLY           = "wifi_only",
        REFRESH_FREQUENCY   = "refresh_frequency",

        REFRESH_FREQUENCY_CURR_DAY      = "refresh_frequency_curr_day",
        REFRESH_FREQUENCY_CURR_WEEK     = "refresh_frequency_curr_week",
        REFRESH_FREQUENCY_CURR_MONTH    = "refresh_frequency_curr_month",

        SAVE_PATH           = "My_Random_Pony", // path to save images
        FILE_NAME           = "bg.png",
        FILE_NAME_EDITED    = "bg_edited.png",
        DOWNLOAD_URL        = "downloadurl", // link to the site under the image

        HINT_FIRST_NEXT     = "settings_hint1_flag", // hint after the click on the "Next" button
        HINT_FIRST_LAUNCH   = "settings_hint2_flag", // hint at the first launch
        HINT_FIRST_EDIT     = "settings_first_edit_hint",

        FLAG_MAIN_ACTIVITY_RESTART      = "flag_main_activity_restart"
    ;
}
