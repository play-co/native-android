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


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class TwentyFourHourAlarm {
	public static void schedule(Context context, Settings settings, TeaLeafOptions options) {
		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent("com.tealeaf.ALARM_TIMER");
		intent.putExtra("appID", options.getAppID());
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
		if(pi != null) {
			alarm.cancel(pi);
			pi.cancel();
			// cancel the old alarm, reschedule it with the last fire time plus one day
		}

		long lastTime = settings.getLong("@__last_fired__", System.currentTimeMillis());
		PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, lastTime + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, broadcast);
	}


}
