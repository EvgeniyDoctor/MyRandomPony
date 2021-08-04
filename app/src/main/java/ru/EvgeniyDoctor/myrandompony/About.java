package ru.EvgeniyDoctor.myrandompony;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import net.grandcentrix.tray.AppPreferences;

import static ru.EvgeniyDoctor.myrandompony.Themes.loadTheme;



public class About extends AppCompatActivity {
    static AppPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(Themes.loadTheme(settings));

        setContentView(R.layout.activity_about);
    }
}
