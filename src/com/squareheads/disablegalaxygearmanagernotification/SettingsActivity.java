package com.squareheads.disablegalaxygearmanagernotification;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
	public static final String Pref_Log_Enabled = "log_enabled";
	public static final String Pref_Disable_Sound_Autofocus = "autofocus_sound_disabled";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}

