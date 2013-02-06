/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with the Game Closure SDK.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tealeaf;

import java.util.Iterator;
import java.util.Set;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import com.tealeaf.plugin.PluginManager;

import android.os.Bundle;

public class ReferrerReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String referrer = "none";
		Bundle extras = intent.getExtras();
		if (extras != null) {
			referrer = extras.getString("referrer");
			Settings settings = new Settings(context);
			settings.setString("referrer", referrer);
			PluginManager.callAll("setInstallReferrer", referrer);
		}

		//get receiver info
		ActivityInfo activityInfo = null;
		try {
			activityInfo = context.getPackageManager().getReceiverInfo(new ComponentName(context, "com.tealeaf.ReferrerReceiver"), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		//extract meta-data
		Bundle bundle = activityInfo.metaData;
		if (bundle==null) {
			return;
		}
		Set<String> keys = bundle.keySet();
		//iterate through all metadata tags
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String k = it.next();
			String v = bundle.getString(k);
			try {
				//send intent by dynamically creating instance of receiver
				((BroadcastReceiver)Class.forName(v).newInstance()).onReceive(context, intent);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		}


	}

}
