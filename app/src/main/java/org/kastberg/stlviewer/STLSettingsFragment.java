package org.kastberg.stlviewer;

import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link STLSettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link STLSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class STLSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }
}
