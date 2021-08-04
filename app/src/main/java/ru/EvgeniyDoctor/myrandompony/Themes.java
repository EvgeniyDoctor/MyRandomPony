package ru.EvgeniyDoctor.myrandompony;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import net.grandcentrix.tray.AppPreferences;



enum eThemes {
    Chrysalis(R.style.Chrysalis, "Chrysalis"),
    Spike(R.style.Spike, "Spike")
    ;

    private final int themeId;
    private final String themeName;

    // constructor
    eThemes(int id, String name) {
        this.themeId = id;
        this.themeName = name;
    }

    public int getId(){
        return themeId;
    }
    //---

    public String getName(){
        return themeName;
    }
    //---
}



public class Themes extends AppCompatActivity {
    static AppPreferences settings;
    RadioButton
        radio_theme_chrysalis,
        radio_theme_spike;
    RadioGroup radioGroup;
    Button button;

    static final String THEME_INTENT_FLAG = "keep"; //
    static final String THEME_NAME_APP_SETTINGS = "theme"; // tag for app settings



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(loadTheme());

        setContentView(R.layout.themes);

        FrameLayout layout_theme_radio_chrysalis      = findViewById(R.id.layout_radio_theme_chrysalis);
        FrameLayout layout_theme_radio_spike      = findViewById(R.id.layout_radio_theme_spike);
        button = findViewById(R.id.btn_theme_save);

        radio_theme_chrysalis = findViewById(R.id.radio_theme_chrysalis);
        radio_theme_spike = findViewById(R.id.radio_theme_spike);
        radioGroup = findViewById(R.id.radio_group_themes);

        layout_theme_radio_chrysalis.setOnClickListener(click);
        layout_theme_radio_spike.setOnClickListener(click);
        button.setOnClickListener(click);
    }
    //-----------------------------------------------------------------------------------------------



    View.OnClickListener click = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_theme_save:

                    if (radio_theme_spike.isChecked()) {
                        changeTheme(eThemes.Spike.getId());
                    }
                    else if (radio_theme_chrysalis.isChecked()) {
                        changeTheme(eThemes.Chrysalis.getId());
                    }
                    break;

                case R.id.layout_radio_theme_chrysalis:
                    radio_theme_spike.setChecked(false);
                    radio_theme_chrysalis.setChecked(true);
                    break;

                case R.id.layout_radio_theme_spike:
                    radio_theme_spike.setChecked(true);
                    radio_theme_chrysalis.setChecked(false);
                    break;
            }
        }
    };
    //-----------------------------------------------------------------------------------------------



    @SuppressLint("NonConstantResourceId")
    public void changeTheme(int themeId) {
        //setTheme(themeId); // std Android method
        saveTheme(themeId);

        Intent intent = new Intent(this, Main.class);

        // disable animation
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        //overridePendingTransition(0, 0);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(THEME_INTENT_FLAG, false);

        finish();
        startActivity(intent);
    }
    //-----------------------------------------------------------------------------------------------



    public void saveTheme (int themeId) {
        for (eThemes theme : eThemes.values()) {
            if (themeId == theme.getId()) {
                settings.put(THEME_NAME_APP_SETTINGS, theme.getName());
                break;
            }
        }
    }
    //-----------------------------------------------------------------------------------------------



    public static int loadTheme (AppPreferences settings){
        if (settings.contains(THEME_NAME_APP_SETTINGS)) {
            String currentTheme = settings.getString(THEME_NAME_APP_SETTINGS, eThemes.Spike.getName());

            for (eThemes theme : eThemes.values()) {
                if (currentTheme.equals(theme.getName())) {
                    return theme.getId();
                }
            }
        }
        return eThemes.Spike.getId();
    }
    //-----------------------------------------------------------------------------------------------



    public static int loadTheme (){
        if (settings.contains(THEME_NAME_APP_SETTINGS)) {
            String currentTheme = settings.getString(THEME_NAME_APP_SETTINGS, eThemes.Spike.getName());

            for (eThemes theme : eThemes.values()) {
                if (currentTheme.equals(theme.getName())) {
                    return theme.getId();
                }
            }
        }
        return eThemes.Spike.getId();
    }
    //-----------------------------------------------------------------------------------------------
}
