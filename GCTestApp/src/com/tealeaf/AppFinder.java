/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License v. 2.0 as published by Mozilla.

 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v. 2.0 for more details.

 * You should have received a copy of the Mozilla Public License v. 2.0
 * along with the Game Closure SDK.  If not, see <http://mozilla.org/MPL/2.0/>.
 */
package com.tealeaf;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.tealeaf.util.HTTP;
import com.tealeaf.util.HTTP.Response;
import com.tealeaf.AppInfo;

import android.os.Handler;
import android.net.wifi.*;
import android.widget.ArrayAdapter;
import android.content.Context;
import java.net.URI;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import com.tealeaf.test_app.R;
import com.tealeaf.test_app.TestAppActivity;

public class AppFinder {
	WifiManager.MulticastLock lock;
	private String type = "_tealeaf._tcp.local.";
	private ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
	private AppAdapter adapter;
	private final Handler handler;

	public AppAdapter getAdapter() {
		return adapter;
	}

	public AppFinder(final TestAppActivity activity, Context context, final String host, final int port) {
		WifiManager wifi = (WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
		lock = wifi.createMulticastLock("tealeaflock");
		lock.setReferenceCounted(true);
		lock.acquire();

		handler = new Handler();
		// don't block the main thread
		// search /projects of the given host:port for manifests
		new Thread(new Runnable() {
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						activity.showAppLoadingDialog();
					}
				});

				ArrayList<AppInfo> appInfos = new ArrayList<AppInfo>();
				String url = "http://" + host + ":" + port + "/projects";
				HTTP http = new HTTP();
				logger.log("GETTING BODY", url);
				String body = http.get(URI.create(url));
				logger.log("BODY", body);

				try {
					JSONObject json = new JSONObject(body);
					JSONArray jsonNames = json.names();
					//loop through and add to list
					for (int i = 0; i < jsonNames.length(); i++) {

						try {
							String projectName  = jsonNames.get(i).toString();
							JSONObject curObj = json.getJSONObject(projectName);
							boolean isExample = false;
							//id is used for serving
							if (!curObj.has("id")) continue;
							String id = (String) curObj.get("id");
							if (!curObj.has("manifest")) continue;
							JSONObject manifest = curObj.getJSONObject("manifest");
							if (!manifest.has("title")) continue;
							String title = (String) manifest.get("title");
							if (!manifest.has("appID")) continue;
							String appid = (String) manifest.get("appID");
							if (manifest.has("group")){
								String group = (String) manifest.get("group");
								isExample = group.equals("examples");
							}
							//try and fail gracefully if no icons exist, use default
							JSONObject androidObj = manifest.getJSONObject("android");
							JSONObject icons = androidObj.has("icons") ? androidObj.getJSONObject("icons") : new JSONObject();
							JSONArray iconNames = null;
							int iconIndex = 0;
							if (icons.names() != null) {
								iconNames = icons.names();
								//hopefully get the url for the largest icon
								int lastNum = 0;
								for (int j = 0; j < iconNames.length(); j++) {
									try {
										int num = Integer.parseInt(iconNames.get(j).toString());
										if (num > lastNum) {
											lastNum = num;
											iconIndex = j;
										}
									} catch (Exception e) {
										//some number can't parse exception
										logger.log(e);
									}
								}
							}
							String iconURL = "default";
							if (icons.names() != null) {
								iconURL =  (String) icons.get(iconNames.get(iconIndex).toString());
							}
							if (!manifest.has("supportedOrientations")) continue;
							String orientation = (String) ((JSONArray) manifest.getJSONArray("supportedOrientations")).get(0);
							boolean isPortrait = orientation.equalsIgnoreCase("portrait");
							AppInfo app = new AppInfo(title, appid, isPortrait, id, iconURL);
							app.isExample = isExample;
							appInfos.add(app);
						} catch (Exception e) {
							logger.log(e);
							continue;
						}

					}
				} catch (Exception e) {
					logger.log(e);
				}
				handler.post(new Runnable() {
					public void run() {
						activity.dismissAppLoadingDialog();
					}
				});
				Collections.sort(appInfos,new Comparator<AppInfo>(){
					@Override
					public int compare(AppInfo lhs, AppInfo rhs) {
						//If the apps are in the same category compare by name
						if(lhs.isExample == rhs.isExample) {
							return lhs.name.compareTo(rhs.name);
						}
						//otherwise an example app is considered "greater" than
						//a non example app and is moved to the bottom of the list
						return lhs.isExample ? 1 : -1;
					}

				});
				createAdapter(activity, appInfos, host, port);
			}
		}).start();
	}


	private void createAdapter(final TestAppActivity activity, final ArrayList<AppInfo> appInfos, final String host, final int port) {
		handler.post(new Runnable() {
			public void run() {
				AppInfo [] appArr = appInfos.toArray(new AppInfo[appInfos.size()]);
				adapter = new AppAdapter(activity, R.layout.appview, appArr, host, port);
				activity.appListView.setAdapter(adapter);
				apps = appInfos;
			}
		});

	}



	public AppInfo getApp(int index) {
		return apps.get(index);
	}

}
