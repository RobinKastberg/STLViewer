package org.kastberg.stlviewer;

import android.os.Bundle;
import android.preference.PreferenceFragment;


public class STLSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }
}
