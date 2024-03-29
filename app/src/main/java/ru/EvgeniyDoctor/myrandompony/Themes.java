package ru.EvgeniyDoctor.myrandompony;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;

import net.grandcentrix.tray.AppPreferences;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;



/*
Для создания новой темы нужно:
1) добавить её параметры в eThemes ниже;
2) добавить новый radiobutton и в нём изменить:
    - RadioButton:
        - android:id
        - android:tag
    - RelativeLayout (ниже RadioButton):
        - android:id
        - android:tag
    - TextView:
        android:text
3) в drawable создать новый файл selector_<theme_name>. Этот файл отвечает за цвета выбранного/невыбранного radiobutton и чекбокса
4) описать тему в файле values/styles.xml. Новые item в тему добавляются в файле values/attrs.xml.
*/



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
    private static AppPreferences settings;
    private RadioGroup radioGroup;
    private MenuItem themeMenuItemCheck;
    private ArrayList<RadioButton> listOfRadioButtons = new ArrayList<>();
    private String currentTheme; // name of the current theme, "Chrysalis", "Spike", etc.
    private ImageView imageView;
    private final String SAVE_INSTANCE_SELECTED_THEME = "SAVE_INSTANCE_SELECTED_THEME"; // key for save selected theme ID when screen orientation changed

    public static final String THEME_INTENT_FLAG = "keep"; // intent extra name to restart Main activity
    public static final String THEME_NAME_APP_SETTINGS = "theme"; // key for app settings



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppPreferences(getApplicationContext());
        setTheme(loadTheme());
        setContentView(R.layout.themes);

        radioGroup      = findViewById(R.id.radio_group_themes);
        imageView       = findViewById(R.id.preview_theme);

        // get all radio buttons with themes into listOfRadioButtons
        getRadioButtons();

        // load
        uncheckAllRadioButtons();

        if (settings.contains(THEME_NAME_APP_SETTINGS)) { // load earlier selected theme
            currentTheme = settings.getString(THEME_NAME_APP_SETTINGS, eThemes.Spike.getName());
        }
        else { // if this is the first launch
            currentTheme = eThemes.Spike.getName();
        }
        setRadioButtonCheckedByTag(currentTheme);
        imageView.setImageResource(getThemePreviewByName(currentTheme)); // load preview image
    }
    //-----------------------------------------------------------------------------------------------



    // creating a 3-dot menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.themes_menu, menu);
        MenuCompat.setGroupDividerEnabled(menu, true); // for dividers

        themeMenuItemCheck = menu.findItem(R.id.theme_menu_item_check);

        String name = getThemeNameById(getSelectedRadioButton()); // selected theme name
        themeMenuItemCheck.setEnabled(!currentTheme.equals(name)); // disable/enable Check btn

        return true;
    }
    //----------------------------------------------------------------------------------------------




    // 3-dot menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        themeApply();
        return true;
    }
    //----------------------------------------------------------------------------------------------



    // save data when screen orientation changed
    @Override
    protected void onSaveInstanceState(@NonNull @NotNull Bundle outState) { // protected, not public
        super.onSaveInstanceState(outState);

        int selected = getSelectedRadioButton();
        outState.putInt(SAVE_INSTANCE_SELECTED_THEME, selected);
    }
    //-----------------------------------------------------------------------------------------------



    // restore saved data when screen orientation changed
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int selected = savedInstanceState.getInt(SAVE_INSTANCE_SELECTED_THEME); // selected theme id
        String name = getThemeNameById(selected); // selected theme name

        imageView.setImageResource(getThemePreviewByName(name)); // load preview image
    }
    //-----------------------------------------------------------------------------------------------



    // Save button press
    public void themeApply() {
        themeMenuItemCheck.setEnabled(false);

        // RadioButton and RelativeLayout tags must be equal to the name in eThemes
        for (RadioButton btn : listOfRadioButtons) {
            if (btn.isChecked()) {
                changeTheme(
                    getThemeIdByName(btn.getTag().toString())
                );
            }
        }
    }
    //-----------------------------------------------------------------------------------------------



    // get requested color of the current theme
    public static int getThemeColorById (Context context, int id){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(id, typedValue, true);
        return typedValue.data;
    }
    //-----------------------------------------------------------------------------------------------



    // click on RelativeLayout near RadioButton
    public void setCheckedFromLayout(View view) {
        String tag = view.getTag().toString();

        int selectedBefore = getSelectedRadioButton();
        int selectedNow = getThemeIdByName(tag);
        if (selectedBefore == selectedNow) { // if clicked on already selected radio btn
            return;
        }

        themeMenuItemCheck.setEnabled(!currentTheme.equals(tag));

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



    // get selected radio button
    private int getSelectedRadioButton(){
        for (RadioButton btn : listOfRadioButtons) {
            if (btn.isChecked()) {
                return getThemeIdByName(btn.getTag().toString());
            }
        }
        return eThemes.Spike.getId();
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
    private int getThemeIdByName (String name){
        for (eThemes theme : eThemes.values()) {
            if (name.equals(theme.getName())) {
                return theme.getId();
            }
        }
        return eThemes.Spike.getId();
    }
    //-----------------------------------------------------------------------------------------------



    // get theme name by its id
    private String getThemeNameById (int id){
        for (eThemes theme : eThemes.values()) {
            if (theme.getId() == id) {
                return theme.getName();
            }
        }
        return eThemes.Spike.getName();
    }
    //-----------------------------------------------------------------------------------------------



    // get theme preview by its name
    private int getThemePreviewByName (String name){
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

            if (view1 instanceof RelativeLayout) {
                for (int index = 0; index < ((RelativeLayout) view1).getChildCount(); ++index) {
                    View nextChild = ((RelativeLayout) view1).getChildAt(index);

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

        settings.put(Pref.FLAG_MAIN_ACTIVITY_RESTART, true); // not restart service, if it is running

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
