package com.example.tylerheers.notetoself;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

public class SettingsActivity extends AppCompatActivity {

    public static final int FAST_ANIM_SPEED = 0;
    public static final int MEDIUM_ANIM_SPEED = 1;
    public static final int SLOW_ANIM_SPEED = 2;
    public static final String SOUND_FX_SETTING = "sound_fx";
    public static final String ANIM_SPEED_SETTING = "anim_speed";

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    CheckBox animationSpeedCheckBox;
    RadioGroup settingsGroup;

    //default settings
    boolean isSoundFX = true;
    int animSpeedPref = FAST_ANIM_SPEED;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("Note To Self", MODE_PRIVATE);
        editor = preferences.edit();

        animationSpeedCheckBox = (CheckBox) findViewById(R.id.soundFXCheckBox);
        settingsGroup = (RadioGroup) findViewById(R.id.animSpeedRadioGroup);

        animationSpeedCheckBox.setChecked(isSoundFX);
        settingsGroup.check(R.id.fastSpeedOption);

        animationSpeedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSoundFXPref(isChecked);
            }
        });

        settingsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setAnimSpeedPref(group);
            }
        });


    }

    @Override
    public void onPause() {
        super.onPause();
        editor.commit();
    }

    public void setSoundFXPref(boolean isChecked){
        isSoundFX = isChecked;
        editor.putBoolean(SOUND_FX_SETTING, isSoundFX);
    }

    public void setAnimSpeedPref(RadioGroup g)
    {
        int idOfChecked = R.id.fastSpeedOption;
        if(g != null){
            idOfChecked = g.getId();
        }

        switch (idOfChecked)
        {
            case R.id.fastSpeedOption:
                animSpeedPref = FAST_ANIM_SPEED;
                break;
            case R.id.mediumSpeedOption:
                animSpeedPref = MEDIUM_ANIM_SPEED;
                break;
            case R.id.slowSpeedOption:
                animSpeedPref = SLOW_ANIM_SPEED;
                break;
            default:
                animSpeedPref = MEDIUM_ANIM_SPEED;
        }

        editor.putInt(ANIM_SPEED_SETTING, animSpeedPref);
    }

}
