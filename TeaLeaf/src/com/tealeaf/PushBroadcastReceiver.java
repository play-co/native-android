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

import java.net.URI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tealeaf.util.HTTP;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;

public class PushBroadcastReceiver extends BroadcastReceiver {

	private static PendingIntent scheduledIntent = null;

	public static void scheduleNext(Context context, int timeout) {
		TeaLeafOptions options = new TeaLeafOptions(context);
		String appID = options.getAppID();
		logger.log("{push} Scheduling the next push for", timeout);
		Intent intent = new Intent("com.tealeaf.CHECK_PUSH_SERVER");
		intent.putExtra("appID", appID);
		AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		scheduledIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarms.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (timeout * 1000), scheduledIntent);
	}

	// ensure that the AlarmManager doesn't pull even if scheduled to, breaking the chain of scheduleNext()s
	public static void disableNotifications(Context context) {
		if(scheduledIntent != null) {
			AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			alarms.cancel(scheduledIntent);
			scheduledIntent = null;
		}
	}

	// enables pulling by restarting the cycle.
	public static void enableNotifications(final Context context, int timeout) {
		if(scheduledIntent == null) {
			scheduleNext(context, timeout);
		}
	}

	@Override
	public void onReceive(final Context context, Intent in) {
		final TeaLeafOptions options = new TeaLeafOptions(context);
		final String appID = options.getAppID();

		if(!appID.equals(in.getStringExtra("appID"))) {
			return;
		}
		scheduledIntent = null;

		new Thread(new Runnable(){
			public void run() {
				HTTP http = new HTTP();
				Settings settings = new Settings(context);
				String format = options.getPushUrl();
				String url = String.format(format, appID, Device.getDeviceID(context, settings), options.getBuildIdentifier());

				logger.log("{push} Polling for notifications on", url);
				Pair<String, Integer> result = http.getPush(URI.create(url));
				int timeout = options.getPushDelay();
				if(result != null) {
					timeout = result.second;
					if (timeout == -1) {
						timeout = options.getPushDelay();
					}
					String json = result.first;
					logger.log("{push} Got push notification", json, "and will delay for", timeout, "seconds before checking again");

					try {
						JSONArray array = new JSONArray(json);
						int len = array.length();
						JSONArray notifications = new JSONArray(settings.getPushNotifications());

						for (int i = 0; i < len; i++) {
							JSONObject msg = array.getJSONObject(i);
							if (msg.optBoolean("crossPromo", false)) {
								// a cross-promo
								Intent intent = new Intent("com.tealeaf.CROSS_PROMO");
								intent.putExtra("appid", msg.getString("appID"));
								intent.putExtra("url", msg.getString("url"));
								intent.putExtra("version", msg.getString("version"));
								intent.putExtra("displayName", msg.getString("displayName"));
								intent.putExtra("buildID", msg.getString("buildIdentifier"));
								intent.putExtra("image", msg.optString("image"));
								context.startService(intent);
							} else if (msg.optBoolean("hasUpdate", false) && !settings.is("updating_now")) {
								logger.log("{push} Got an update request (old build id:",
									options.getBuildIdentifier(), ", new build identifier:", msg.getString("buildIdentifier"),
									", old android version:", options.getGameHash(),
									", new android version:", msg.getString("gameHash"), ")");
								// an update notification
								Intent intent = new Intent("com.tealeaf.PERFORM_UPDATE");
								// required parameters
								// is the update supposed to be silent?
								intent.putExtra("silent", msg.getBoolean("silent"));
								// does the update require hitting the marketplace instead? (native runtime update)
								intent.putExtra("market", !msg.getString("gameHash").equals(options.getGameHash()));
								// what's the new build identifier of the update?
								intent.putExtra("buildIdentifier", msg.getString("buildIdentifier"));
								// what url can I download the updated build from?
								intent.putExtra("url", msg.getString("url"));
								context.sendOrderedBroadcast(intent, null);
							}
						}
						settings.setPushNotifications(notifications.toString());
					} catch (JSONException e) {
						logger.log(e);
					}
				}
				scheduleNext(context, timeout);
			}
		}).start();
	}
}
