package com.squareheads.disablegalaxygearmanagernotification;



import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedHelpers;

//For currently disabled debug methods
/*
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.widget.RemoteViews;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
*/

public class DisableGalaxyGearManagerNotification implements IXposedHookLoadPackage, IXposedHookZygoteInit {
	public static final String MY_PACKAGE_NAME = DisableGalaxyGearManagerNotification.class.getPackage().getName();
	public static final String HOSTMANAGER_PACKAGE_NAME = "com.samsung.android.hostmanager";
	public static final String GEAR1_PLUGIN_PACKAGE_NAME = "com.samsung.android.gear1plugin";
	private static XSharedPreferences pref;
	private boolean logEnabled = false;

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		//The gear one notifications originate from the gear1plugin app
		/*
		if (lpparam.packageName.equals("com.samsung.android.gear1plugin")){
			
			if(optionEnabled("gear1_hide_noti", true)) {
			
				Log("Attempting to hook Gear1 notifications...");
	
				try {
					XposedHelpers.findAndHookMethod(
							"com.samsung.android.gear1plugin.service.BManagerConnectionService",
							lpparam.classLoader,
							"setNotification",
							XC_MethodReplacement.DO_NOTHING
							);
					Log("Gear 1 Hook succeeded");
	
				} catch (Throwable t) {
					//Do nothing
					Log("Gear 1 Hook failed");
				}
			}
		}
		*/
		
	}
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
		return defVal;
		//TODO: once settings page done
		//return pref.getBoolean(option, defVal);
	}
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		//Initialise the prefs object
		pref = new XSharedPreferences(MY_PACKAGE_NAME);
		
		
		logEnabled = optionEnabled("log_enabled", false);
		//If we are hiding all hostmanager notifications
		if(optionEnabled("hostmanager_hide_noti", true)) {
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
	/*
	public void hookNotifications() {
		final int sdk = Build.VERSION.SDK_INT;
		
		XC_MethodHook notifyHookDebugLog = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				String packageName = (String) param.args[0];

				Notification n;
				if (sdk <= 15 || sdk >= 18)
					n = (Notification) param.args[6];
				else
					n = (Notification) param.args[5];
				Boolean isOngoing = (n.flags & Notification.FLAG_ONGOING_EVENT) != 0;
				
				if(isOngoing) {
					String dataString = "Contents ";
					List<String> data = getText(n);
					if(data != null) {
						for (String s : data)
						{
							dataString += s + ", ";
						}
						if(dataString.length() > 2 ) {
							dataString = dataString.substring(0, dataString.length()-2) + "\n";
						}
					}
					else {
						dataString = "Unable to read Notification data\n";
					}
					dataString +="Originated from trace:\n" +  stackTraceToString( new Exception());
					
					Log("Ongoing Notification created by package \"" + packageName +"\". " + dataString);
				}

			}
		};
		
		if (sdk <= 15) {
			XposedHelpers.findAndHookMethod("com.android.server.NotificationManagerService", null, "enqueueNotificationInternal", String.class, int.class, int.class,
					String.class, int.class, int.class, Notification.class, int[].class,
					notifyHookDebugLog);
		} else if (sdk == 16) {
			XposedHelpers.findAndHookMethod("com.android.server.NotificationManagerService", null, "enqueueNotificationInternal", String.class, int.class, int.class,
					String.class, int.class, Notification.class, int[].class,
					notifyHookDebugLog);
		} else if (sdk == 17) {
			XposedHelpers.findAndHookMethod("com.android.server.NotificationManagerService", null, "enqueueNotificationInternal", String.class, int.class, int.class,
					String.class, int.class, Notification.class, int[].class, int.class,
					notifyHookDebugLog);
		} else if (sdk >= 18) {
			XposedHelpers.findAndHookMethod("com.android.server.NotificationManagerService", null, "enqueueNotificationInternal", String.class, String.class,
					int.class, int.class, String.class, int.class, Notification.class, int[].class, int.class,
					notifyHookDebugLog);
		}
	}
	
	public static String stackTraceToString(Throwable e) {
	    StringBuilder sb = new StringBuilder();
	    for (StackTraceElement element : e.getStackTrace()) {
	        sb.append(element.toString());
	        sb.append("\n");
	    }
	    return sb.toString();
	}
	
	public static List<String> getText(Notification notification)
	{
	    // We have to extract the information from the view
	    RemoteViews        views = notification.bigContentView;
	    if (views == null) views = notification.contentView;
	    if (views == null) return null;

	    // Use reflection to examine the m_actions member of the given RemoteViews object.
	    // It's not pretty, but it works.
	    List<String> text = new ArrayList<String>();
	    try
	    {
	        Field field = views.getClass().getDeclaredField("mActions");
	        field.setAccessible(true);

	        @SuppressWarnings("unchecked")
	        ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);

	        // Find the setText() and setTime() reflection actions
	        for (Parcelable p : actions)
	        {
	            Parcel parcel = Parcel.obtain();
	            p.writeToParcel(parcel, 0);
	            parcel.setDataPosition(0);

	            // The tag tells which type of action it is (2 is ReflectionAction, from the source)
	            int tag = parcel.readInt();
	            if (tag != 2) continue;

	            // View ID
	            parcel.readInt();

	            String methodName = parcel.readString();
	            if (methodName == null) continue;

	            // Save strings
	            else if (methodName.equals("setText"))
	            {
	                // Parameter type (10 = Character Sequence)
	                parcel.readInt();

	                // Store the actual string
	                String t = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
	                text.add(t);
	            }

	            // Save times. Comment this section out if the notification time isn't important
	            else if (methodName.equals("setTime"))
	            {
	                // Parameter type (5 = Long)
	                parcel.readInt();

	                String t = new SimpleDateFormat("h:mm a").format(new Date(parcel.readLong()));
	                text.add(t);
	            }

	            parcel.recycle();
	        }
	    }

	    // It's not usually good style to do this, but then again, neither is the use of reflection...
	    catch (Exception e)
	    {
	    	return null;
	    }

	    return text;
	}
	*/
}
		
		


