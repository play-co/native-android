package com.tealeaf;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import android.content.pm.PackageManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import android.content.pm.PackageManager.NameNotFoundException;

public class TeaLeafReceiver extends BroadcastReceiver {

    public static String PACKAGE_DELIMITER = ";;;";
    public static String INSTALL_TIME_DELIMITER = ":";
    public static String INSTALLED_APPS_KEY = "installed_apps";
    private Settings settings;

    private Settings getSettings(Context context) {
        if (settings == null) {
            Settings.build(context);
            settings = Settings.getInstance();
        }

        return settings;
    }

    private String removePackage(String data, String packageName) {
        String result = data;

        if (!TextUtils.isEmpty(data) && TextUtils.indexOf(data, packageName) > -1) {
            result = "";
            Set<String> packages = new HashSet<String>(Arrays.asList(data.trim().split(PACKAGE_DELIMITER)));
            for (String currPackage: packages) {
                if (TextUtils.indexOf(currPackage, packageName) < 0) {
                    result = result + (TextUtils.isEmpty(result) ? currPackage : PACKAGE_DELIMITER + currPackage);
                }
            }
        }

        return result;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        
        if (action.equals("com.android.vending.INSTALL_REFERRER")) {
            
            // log referrer
            try {
                Bundle bundle = intent.getExtras();
                String referrer = URLDecoder.decode(bundle.getString("referrer"), "UTF-8");
                getSettings(context).setString("installReferrer.referrer", referrer);
            } catch (UnsupportedEncodingException e) {
                logger.log(e);
            }
        } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {

            try {
                String packageName = intent.getData().getEncodedSchemeSpecificPart();
                Bundle metaData = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
                String otherApps = metaData.getString("otherApps");
                String[] otherPackages = otherApps.trim().split("\\|");
                String installedPackages = getSettings(context).getString(INSTALLED_APPS_KEY, "");
                if (Arrays.asList(otherPackages).contains(packageName)) {
                    installedPackages = removePackage(installedPackages, packageName).trim();
                    installedPackages = TextUtils.isEmpty(installedPackages) ?  installedPackages : installedPackages + PACKAGE_DELIMITER;
                    getSettings(context).setString(INSTALLED_APPS_KEY, TextUtils.concat(installedPackages, packageName, INSTALL_TIME_DELIMITER, "" + System.currentTimeMillis()).toString());
                }
            } catch(NameNotFoundException e) {
                logger.log(e);
            } catch (NullPointerException e) {
		logger.log(e);
            }
        }
    }

}
