package com.squareheads.disablegalaxygearmanagernotification;

import de.robv.android.xposed.XSharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_screen);
    }

	/* (non-Javadoc)
	 * @see android.preference.PreferenceFragment#onPreferenceTreeClick(android.preference.PreferenceScreen, android.preference.Preference)
	 */
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		
		Log.d("gearssilence", "Pref clicked " + preference.getKey());
		//XSharedPreferences prefs = 
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}