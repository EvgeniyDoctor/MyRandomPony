package ru.EvgeniyDoctor.myrandompony;

// class with keys names in app prefs

public abstract class Pref {
    public static final String
        APP_LINK            = "https://play.google.com/store/apps/details?id=ru.EvgeniyDoctor.myrandompony",

        ENABLED             = "enabled_pony_wallpapers",
        WIFI_ONLY           = "wifi_only",
        SCREEN_IMAGE        = "screen_image",
        SCREEN_RESOLUTION   = "screen_resolution",
        REFRESH_FREQUENCY   = "refresh_frequency",
        SCREEN_SIZE_SMALL_OR_NORMAL = "screen_size_small_or_normal",
        IMAGE_SOURCES       = "image_sources",
        DERPIBOORU_TAGS     = "derpibooru_tags",

        REFRESH_FREQUENCY_CURR_DAY      = "refresh_frequency_curr_day",
        REFRESH_FREQUENCY_CURR_WEEK     = "refresh_frequency_curr_week",
        REFRESH_FREQUENCY_CURR_MONTH    = "refresh_frequency_curr_month",

        SAVE_PATH           = "My_Random_Pony", // path to save images
        IMAGE_ORIGINAL      = "bg.png",
        IMAGE_EDITED        = "bg_edited.png",
        IMAGE_URL           = "downloadurl", // link to the site under the image
        IMAGE_TITLE         = "image_title",

        HINT_FIRST_NEXT     = "settings_hint1_flag", // hint after the click on the "Next" button
        HINT_FIRST_LAUNCH   = "settings_hint2_flag", // hint at the first launch
        HINT_FIRST_EDIT     = "settings_first_edit_hint",

        FLAG_MAIN_ACTIVITY_RESTART      = "flag_main_activity_restart"
    ;

    public static final boolean
        WIFI_ONLY_DEFAULT = false
    ;

    public static final int
        SCREEN_IMAGE_DEFAULT        = 0, // both screens
        SCREEN_RESOLUTION_DEFAULT   = 0, // normal
        DERPIBOORU_TAGS_DEFAULT     = 0b11 // Wallpaper and Safe
    ;
}
