package ru.EvgeniyDoctor.myrandompony;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import net.grandcentrix.tray.AppPreferences;



public class Help extends AppCompatActivity {
    static AppPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(Themes.loadTheme(settings));

        setContentView(R.layout.help);

        TextView textView = findViewById(R.id.textViewHelp);
        String text = getResources().getString(R.string.help_text);
        textView.setText(Helper.removeSpacesFromStringStart(text));
    }
}
