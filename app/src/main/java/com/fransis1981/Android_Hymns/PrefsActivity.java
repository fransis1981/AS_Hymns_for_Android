package com.fransis1981.Android_Hymns;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Fransis on 22/09/2014.
 */
public class PrefsActivity extends PreferenceActivity {
    public static final String PREF_LANGUAGE_SELECTED = "pref_language_selected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}
