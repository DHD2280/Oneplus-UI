package it.dhd.oneplusui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import it.dhd.oneplusui.sample.R;

public class PrefFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setStorageDeviceProtected();
        setPreferencesFromResource(R.xml.test_pref, rootKey);
    }

}
