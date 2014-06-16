package com.squareheads.disablegalaxygearmanagerenotification;


import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
	public static final String Pref_Log_Enabled = "log_enabled";
	public static final String Pref_Disable_Ongoing_Notification = "pref_disable_gear_notif";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}

