package com.fransis1981.Android_Hymns;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * Created by Fransis on 22/09/2014.
 */
public class PrefsActivity extends PreferenceActivity {
    public static final String PREF_LANGUAGE_SELECTED = "pref_language_selected";

    SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, key + " preference changed.");
            }
        };

        addPreferencesFromResource(R.xml.prefs);
    }
}
