package ru.EvgeniyDoctor.myrandompony;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import net.grandcentrix.tray.AppPreferences;

import java.util.ArrayList;



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



// todo 04.08.2021: посмотреть на планшете
// todo 04.08.2021: добавить тем
// todo 04.08.2021: картинки тем
// todo 04.08.2021: change color vars in xml



public class Themes extends AppCompatActivity {
    static AppPreferences settings;
    RadioGroup radioGroup;
    Button btn_theme_save;
    ArrayList<RadioButton> listOfRadioButtons = new ArrayList<>();
    String currentTheme; // name of the current theme, "Chrysalis", "Spike", etc.

    static final String THEME_INTENT_FLAG = "keep"; // intent extra name to restart Main activity
    static final String THEME_NAME_APP_SETTINGS = "theme"; // tag for app settings



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(loadTheme());
        setContentView(R.layout.themes);

        btn_theme_save  = findViewById(R.id.btn_theme_save);
        radioGroup      = findViewById(R.id.radio_group_themes);

        // get all radio buttons with themes into listOfRadioButtons
        getRadioButtons();

        // load
        uncheckAllRadioButtons();
        if (settings.contains(THEME_NAME_APP_SETTINGS)) {
            currentTheme = settings.getString(THEME_NAME_APP_SETTINGS, eThemes.Spike.getName());
            setRadioButtonCheckedByTag(currentTheme);
        }
    }
    //-----------------------------------------------------------------------------------------------



    // Save button press
    public void themeApply(View view) {
        btn_theme_save.setEnabled(false);
        btn_theme_save.setBackgroundColor(getThemeColorById(Themes.this, R.attr.colorButtonSemitransparent));

        // RadioButton and FrameLayout tags must be equal to the name in eThemes
        for (RadioButton btn : listOfRadioButtons) {
            if (btn.isChecked()) {
                changeTheme(
                    getThemeIdByName(btn.getTag().toString())
                );
            }
        }
    }
    //-----------------------------------------------------------------------------------------------



    public static int getThemeColorById (Context context, int id){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(id, typedValue, true);
        return typedValue.data;
    }
    //-----------------------------------------------------------------------------------------------



    // click on FrameLayout near RadioButton
    public void setCheckedFromLayout(View view) {
        String tag = view.getTag().toString();

        Helper.toggleViewState(Themes.this, btn_theme_save, !currentTheme.equals(tag)); // disable Save btn if current theme selected

        uncheckAllRadioButtons();
        setRadioButtonCheckedByTag(tag);
    }
    //-----------------------------------------------------------------------------------------------



    // отменить выбор со всех RadioButtons
    private void uncheckAllRadioButtons() {
        for (RadioButton btn : listOfRadioButtons) {
            btn.setChecked(false);
        }
    }
    //-----------------------------------------------------------------------------------------------



    // выбор RadioButton по заданному тегу
    private void setRadioButtonCheckedByTag (String name){
        for (RadioButton btn : listOfRadioButtons) {
            if (btn.getTag().equals(name)) {
                btn.setChecked(true);
                break;
            }
        }
    }
    //-----------------------------------------------------------------------------------------------



    // получение ид темы по её названию
    private int getThemeIdByName(String name){
        for (eThemes theme : eThemes.values()) {
            if (name.equals(theme.getName())) {
                return theme.getId();
            }
        }
        return eThemes.Spike.getId();
    }
    //-----------------------------------------------------------------------------------------------



    // формирование массива со всеми RadioButtons в группе
    private void getRadioButtons(){
        int count = radioGroup.getChildCount();

        for (int i=0; i<count; ++i) {
            View view1 = radioGroup.getChildAt(i);

            if (view1 instanceof FrameLayout) {
                for (int index = 0; index < ((FrameLayout) view1).getChildCount(); index++) {
                    View nextChild = ((FrameLayout) view1).getChildAt(index);

                    try {
                        if (nextChild instanceof RadioButton) {
                            listOfRadioButtons.add((RadioButton) nextChild);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    //-----------------------------------------------------------------------------------------------



    // сохранение выбранной темы и перезапуск Main activity
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



    // save theme to the app preferences
    public void saveTheme (int themeId) {
        for (eThemes theme : eThemes.values()) {
            if (themeId == theme.getId()) {
                settings.put(THEME_NAME_APP_SETTINGS, theme.getName());
                break;
            }
        }
    }
    //-----------------------------------------------------------------------------------------------



    // загрузка текущей темы из настроек
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



    // загрузка текущей темы из настроек
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
