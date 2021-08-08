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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import net.grandcentrix.tray.AppPreferences;

import java.util.ArrayList;



/*
Для создания новой темы нужно:
1) добавить её параметры в eThemes ниже;
2) добавить новый radiobutton и в нём изменить:
    - RadioButton:
        - android:id
        - android:tag
    - FrameLayout (ниже RadioButton):
        - android:id
        - android:tag
    - TextView:
        android:text
3) в drawable создать новый файл selector_<theme_name>. Этот файл отвечает за цвета выбранного/невыбранного radiobutton и чекбокса
4) описать тему в файле values/styles.xml. Новые item в тему добавляются в файле values/attrs.xml.
*/



    // todo 08.08.2021:
//  граница иногда выпирает, Main too
//  если выбрать новую тему, а затем повернуть экран - кнопка Принять станет неактивной, превью сбросится ддо текущей темы, но селект останется правильный



// all themes and its attrs
enum eThemes {
    // name (theme name in styles.xml, name in app preferences and tags, id of the preview image)
    Chrysalis   (R.style.Chrysalis, "Chrysalis",    R.drawable.theme_preview_chrysalis),
    Spike       (R.style.Spike,     "Spike",        R.drawable.theme_preview_spike),
    Luna        (R.style.Luna,      "Luna",         R.drawable.theme_preview_luna),
    Celestia    (R.style.Celestia,  "Celestia",     R.drawable.theme_preview_celestia),
    Tirek       (R.style.Tirek,     "Tirek",        R.drawable.theme_preview_tirek),
    ;

    private final int themeId;
    private final String themeName;
    private final int themePreview;

    // constructor
    eThemes(int id, String name, int preview) {
        this.themeId = id;
        this.themeName = name;
        this.themePreview = preview;
    }

    public int getId(){
        return themeId;
    }
    //---

    public String getName(){
        return themeName;
    }
    //---

    public int getPreview(){
        return themePreview;
    }
    //---
}
//-----------------------------------------------------------------------------------------------



public class Themes extends AppCompatActivity {
    static AppPreferences settings;
    RadioGroup radioGroup;
    Button btn_theme_apply;
    ArrayList<RadioButton> listOfRadioButtons = new ArrayList<>();
    String currentTheme; // name of the current theme, "Chrysalis", "Spike", etc.
    ImageView imageView;

    static final String THEME_INTENT_FLAG = "keep"; // intent extra name to restart Main activity
    static final String THEME_NAME_APP_SETTINGS = "theme"; // tag for app settings



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(loadTheme());
        setContentView(R.layout.themes);

        btn_theme_apply = findViewById(R.id.btn_theme_apply);
        radioGroup      = findViewById(R.id.radio_group_themes);
        imageView       = findViewById(R.id.theme_preview);

        // get all radio buttons with themes into listOfRadioButtons
        getRadioButtons();

        // load
        uncheckAllRadioButtons();

        if (settings.contains(THEME_NAME_APP_SETTINGS)) { // load earlier selected theme
            currentTheme = settings.getString(THEME_NAME_APP_SETTINGS, eThemes.Spike.getName());
            setRadioButtonCheckedByTag(currentTheme);
            imageView.setImageResource(getThemePreviewByName(currentTheme)); // load preview image
        }
        else { // if this is the first launch
            currentTheme = settings.getString(THEME_NAME_APP_SETTINGS, eThemes.Spike.getName());
            setRadioButtonCheckedByTag(currentTheme);
            imageView.setImageResource(getThemePreviewByName(currentTheme)); // load preview image
        }
    }
    //-----------------------------------------------------------------------------------------------



    // Save button press
    public void themeApply(View view) {
        btn_theme_apply.setEnabled(false);
        btn_theme_apply.setBackgroundColor(getThemeColorById(Themes.this, R.attr.colorButtonSemitransparent));

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



    // get some color of the current theme
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

        Helper.toggleViewState(Themes.this, btn_theme_apply, !currentTheme.equals(tag)); // disable Save btn if current theme selected

        uncheckAllRadioButtons();
        setRadioButtonCheckedByTag(tag);

        imageView.setImageResource(getThemePreviewByName(tag)); // load preview image
    }
    //-----------------------------------------------------------------------------------------------



    // uncheck all RadioButtons
    private void uncheckAllRadioButtons() {
        for (RadioButton btn : listOfRadioButtons) {
            btn.setChecked(false);
        }
    }
    //-----------------------------------------------------------------------------------------------



    // check RadioButton by tag
    private void setRadioButtonCheckedByTag (String name){
        for (RadioButton btn : listOfRadioButtons) {
            if (btn.getTag().equals(name)) {
                btn.setChecked(true);
                break;
            }
        }
    }
    //-----------------------------------------------------------------------------------------------



    // get theme ID by its name
    private int getThemeIdByName(String name){
        for (eThemes theme : eThemes.values()) {
            if (name.equals(theme.getName())) {
                return theme.getId();
            }
        }
        return eThemes.Spike.getId();
    }
    //-----------------------------------------------------------------------------------------------



    // get theme preview by its name
    private int getThemePreviewByName(String name){
        for (eThemes theme : eThemes.values()) {
            if (name.equals(theme.getName())) {
                return theme.getPreview();
            }
        }
        return eThemes.Spike.getPreview();
    }
    //-----------------------------------------------------------------------------------------------



    // get all RadioButtons in RadioGroup in array
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



    // save selected theme and restart Main activity
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



    // load current theme from the app preferences
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



    // load current theme from the app preferences
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
