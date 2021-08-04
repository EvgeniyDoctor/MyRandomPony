package ru.EvgeniyDoctor.myrandompony;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import net.grandcentrix.tray.AppPreferences;



public class Themes extends AppCompatActivity {
    static AppPreferences settings;
    RadioButton
        radio_theme_chrysalis,
        radio_theme_spike;
    RadioGroup radioGroup;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (settings.contains("theme_changed") && settings.getBoolean("theme_changed", false)) {
//            setTheme(R.style.Chrysalis);
//        }

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



        //LinearLayout layout_theme_preview = findViewById(R.id.layout_theme_preview);
    }
    //-----------------------------------------------------------------------------------------------



    View.OnClickListener click = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_theme_save:

                    if (radio_theme_spike.isChecked()) {
                        changeTheme(R.style.Spike);
                    }
                    else if (radio_theme_chrysalis.isChecked()) {
                        changeTheme(R.style.Chrysalis);
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
        settings = new AppPreferences(getApplicationContext());

        //settings.remove("theme_changed");
//        if (settings.contains("theme") && settings.getBoolean("theme", false)) {
//            setTheme(R.style.Chrysalis);
//        }

        setTheme(themeId);

        switch (themeId) {
            case R.style.Chrysalis:
                settings.put("theme", "Chrysalis");
                break;
            case R.style.Spike:
                settings.put("theme", "Spike");
                break;
        }

        Intent intent = new Intent(this, Main.class);

        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("keep", false); // todo
        //overridePendingTransition(0, 0);

        finish();
        startActivity(intent);
    }
    //-----------------------------------------------------------------------------------------------
}
