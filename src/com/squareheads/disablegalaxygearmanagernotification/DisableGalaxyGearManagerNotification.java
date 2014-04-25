package com.squareheads.disablegalaxygearmanagernotification;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedHelpers;


public class DisableGalaxyGearManagerNotification implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		
		//The gear one notifications originate from the gear1plugin app
		if (lpparam.packageName.equals("com.samsung.android.gear1plugin")){
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
		
		//This package has a setNotification(int) method which is called from code that looks gear2 related.
		if (lpparam.packageName.equals("com.samsung.android.hostmanager")){
			try {
				XposedHelpers.findAndHookMethod(
						"com.samsung.android.hostmanager.service.HMSAPProviderService",
						lpparam.classLoader,
						"setNotification",
						XC_MethodReplacement.DO_NOTHING
						);
			} catch (Exception e) {
				//Do nothing
			}
		}	}
}

