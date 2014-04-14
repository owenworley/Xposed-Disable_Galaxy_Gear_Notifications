package com.squareheads.disablegalaxygearmanagernotification;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedHelpers;


public class DisableGalaxyGearManagerNotification implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (lpparam.packageName.equals("com.samsung.android.app.watchmanager")){
			
			try {
				XposedHelpers.findAndHookMethod(
						"com.samsung.android.app.watchmanager.service.BManagerConnectionService",
						lpparam.classLoader,
						"setNotification",
						XC_MethodReplacement.DO_NOTHING
						);
			} catch (Exception e) {
				//Do nothing.
			}
		}
		else if (lpparam.packageName.equals("com.samsung.android.gear1plugin")){
			try {
				XposedHelpers.findAndHookMethod(
						"com.samsung.android.gear1plugin.service.BManagerConnectionService",
						lpparam.classLoader,
						"setNotification",
						XC_MethodReplacement.DO_NOTHING
						);
			} catch (Exception e) {
				//Do nothing
			}
		}
		return;
		
		
		
		
		
	}
}

