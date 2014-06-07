package com.squareheads.disablegalaxygearmanagernotification;

import android.app.Notification;
import android.os.Build;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class DisableGalaxyGearManagerNotification implements IXposedHookZygoteInit {
	public static final String MY_PACKAGE_NAME = DisableGalaxyGearManagerNotification.class.getPackage().getName();
	public static final String HOSTMANAGER_PACKAGE_NAME = "com.samsung.android.hostmanager";
	public static final String GEAR1_PLUGIN_PACKAGE_NAME = "com.samsung.android.gear1plugin";
	private static XSharedPreferences pref;
	private boolean logEnabled = false;

	public void Log(String s) {
		if(logEnabled) {
			XposedBridge.log(s);
		}
	}
		
	
	public void Log(Throwable t) {
		if(logEnabled) {
			XposedBridge.log(t);
		
		}
	}
	private boolean optionEnabled(String option, boolean defVal) {
		//return defVal;
		//TODO: once settings page done
		return pref.getBoolean(option, defVal);
	}
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		//Initialise the prefs object
		pref = new XSharedPreferences(MY_PACKAGE_NAME);
		//pref.registerOnSharedPreferenceChangeListener(this);
		
		logEnabled = optionEnabled(SettingsActivity.Pref_Log_Enabled, false);
		//If we are hiding all hostmanager notifications
		if(optionEnabled(SettingsActivity.Pref_Disable_Ongoing_Notification, true)) {
			Log("Hiding gear notis");
			try {
				hookNotifications();
				Log("Gear notis hidden");

			}
			catch (Throwable t) {
				Log("Failed to hide gear notis (could not hook method)");
				Log(t);
			}
		}
		else {
			Log("Not hiding gear notis");
		}
		
	}
	
	final int sdk = Build.VERSION.SDK_INT;
	public void hookNotifications() {
		XC_MethodHook notifyHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				
				String packageName = (String) param.args[0];
				//if the package is not hostmanager or gear1plugin
				if((packageName.compareTo(HOSTMANAGER_PACKAGE_NAME) == 0 || packageName.compareTo(GEAR1_PLUGIN_PACKAGE_NAME) == 0) == false) {
					Log("Ignore noti from " + packageName);
					return;
				}
				
				//Cast the Notification to read flags and check for onGoing state
				Notification n;
				if (sdk <= 15 || sdk >= 18)
					n = (Notification) param.args[6];
				else
					n = (Notification) param.args[5];
				Boolean isOngoing = (n.flags & Notification.FLAG_ONGOING_EVENT) != 0;
				
				if(isOngoing) {
					//setResult flags this method hook to return early. As this is a void method we set the result to null as it is unused.
					Log("Block ongoing noti from " + packageName);
					param.setResult(null);
				}
	
			}
		};
		
		//Hook for various SDK versions
		if (sdk <= 15) {
			XposedHelpers.findAndHookMethod("com.android.server.NotificationManagerService", null, "enqueueNotificationInternal", String.class, int.class, int.class,
					String.class, int.class, int.class, Notification.class, int[].class,
					notifyHook);
		} else if (sdk == 16) {
			XposedHelpers.findAndHookMethod("com.android.server.NotificationManagerService", null, "enqueueNotificationInternal", String.class, int.class, int.class,
					String.class, int.class, Notification.class, int[].class,
					notifyHook);
		} else if (sdk == 17) {
			XposedHelpers.findAndHookMethod("com.android.server.NotificationManagerService", null, "enqueueNotificationInternal", String.class, int.class, int.class,
					String.class, int.class, Notification.class, int[].class, int.class,
					notifyHook);
		} else if (sdk >= 18) {
			XposedHelpers.findAndHookMethod("com.android.server.NotificationManagerService", null, "enqueueNotificationInternal", String.class, String.class,
					int.class, int.class, String.class, int.class, Notification.class, int[].class, int.class,
					notifyHook);
		}
	}
}
		
		


