package com.example.projectbreakout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //load(inflate) the XML definition of our preferences
        addPreferencesFromResource(R.xml.preferences);

        //register as a change listener
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        onSharedPreferenceChanged(prefs, "initialBrickCount");
        onSharedPreferenceChanged(prefs, "hitsToRemoveBrick");
        onSharedPreferenceChanged(prefs, "ballsPerLevel");
        prefs.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        switch(key){
            //EditTextPrefs will be strings, even if restricted numbers
            case "initialBrickCount":
                int interval = Integer.parseInt(sharedPreferences.getString(key,""));
                if(interval < 1) interval = 1;
                if(interval > 100) interval = 100;
                //set the title
                pref.setTitle("Initial Brick Count (1-100) = "+interval);
                //make sure the constrained value shows up in the edit box if reselected
                ((EditTextPreference)pref).setText(Integer.toString(interval));;
                break;
            case "hitsToRemoveBrick":
                int hits = sharedPreferences.getInt(key,1);
                //set the title
                pref.setTitle("Hits to Remove Brick (1-4) = "+hits);
                break;
            case "ballsPerLevel":
                int balls = sharedPreferences.getInt(key,1);
                //set the title
                pref.setTitle("Balls Per Level (1-4) = "+balls);
                break;


        }

    }

}
