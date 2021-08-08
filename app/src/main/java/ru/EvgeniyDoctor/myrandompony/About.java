package ru.EvgeniyDoctor.myrandompony;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import net.grandcentrix.tray.AppPreferences;



public class About extends AppCompatActivity {
    static AppPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(Themes.loadTheme(settings));

        setContentView(R.layout.about);

        TextView textView = findViewById(R.id.textViewAbout);
        String text = getResources().getString(R.string.about_text);
        textView.setText(Helper.removeSpacesFromStringStart(text));
    }
}
