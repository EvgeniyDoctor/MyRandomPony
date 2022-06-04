package ru.EvgeniyDoctor.myrandompony;



enum ScreenSize {
    SMALL,
    NORMAL,
    LARGE,
    XLARGE,
    UNKNOWN,
}



public class Screen {
    //Determine density
    //Helper.d(getApplicationContext().getResources().getDisplayMetrics().density); // float

        /*
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;
        if (density == DisplayMetrics.DENSITY_HIGH) {
            Helper.d("HIGH");
        }
        else if (density == DisplayMetrics.DENSITY_MEDIUM) {
            Helper.d("MEDIUM");
        }
        else if (density == DisplayMetrics.DENSITY_LOW) {
            Helper.d("LOW");
        }
        else if (density == DisplayMetrics.DENSITY_XHIGH) {
            Helper.d("XHIGH");
        }
        else if (density == DisplayMetrics.DENSITY_XXHIGH) {
            Helper.d("XXHIGH");
        }
        else {
            Helper.d("UNKNOWN_CATEGORY");
        }
         */
}
