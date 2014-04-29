package com.squareheads.disablegalaxygearmanagernotification;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Notification;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.widget.RemoteViews;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedHelpers;

public class DisableGalaxyGearManagerNotification implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		
		//The gear one notifications originate from the gear1plugin app
		if (lpparam.packageName.equals("com.samsung.android.gear1plugin")){
			XposedBridge.log("Attempting to hook Gear1 notifications...");

			try {
				XposedHelpers.findAndHookMethod(
						"com.samsung.android.gear1plugin.service.BManagerConnectionService",
						lpparam.classLoader,
						"setNotification",
						XC_MethodReplacement.DO_NOTHING
						);
				XposedBridge.log("Hook succeeded");

			} catch (Throwable t) {
				//Do nothing
				XposedBridge.log("Hook failed");

			}
		}
	}

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		// TODO Auto-generated method stub
		hookNotifications();
		
	}
	
	public void hookNotifications() {
		final int sdk = Build.VERSION.SDK_INT;
		XC_MethodHook notifyHook = new XC_MethodHook() {
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
				//if(true) {
					
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
					
					XposedBridge.log("Ongoing Notification created by package \"" + packageName +"\". " + dataString);
				}

			}
		};
		
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
}
		
		


