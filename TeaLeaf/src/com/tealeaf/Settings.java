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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Settings {
	private SharedPreferences prefs;
	private Editor editor;

	private static Settings instance = null;
	public static void build(Context context) {
		synchronized(Settings.class){
			if (instance == null) {
				instance = new Settings(context);
			}
		}
	}

	public static Settings getInstance() {
		return instance;
	}
	public Settings(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		editor = prefs.edit();
	}

	// convenience methods for common operations
	public boolean isFirstRun() {
		return !is("first");
	}
	public void markFirstRun() {
		mark("first");
	}
	public boolean haveUnpacked(String version) {
		return is("unpacked_" + version);
	}
	public void markUnpacked(String version) {
		mark("unpacked_" + version);
	}
	public String getPushNotifications() {
		return getString("@__push_notifications__", "[]");
	}
	public void setPushNotifications(String notifications) {
		setString("@__push_notifications__", notifications);
	}
	public void clearPushNotifications() {
		setPushNotifications("[]");
	}
	public String getLastHost() {
		return getString("prev_host", "192.168.0.");
	}
	public int getLastPort() {
		return getInt("prev_port", 9200);
	}
	public void setLastServer(String host, int port) {
		setString("prev_host", host);
		setInt("prev_port", port);
	}
	public boolean isUpdateReady(String version) {
		return is("update_ready_" + version);
	}
	public void markUpdate(String version, String build, boolean market, String from) {
		mark("update_ready_" + version);
		setUpdateBuild(version, build);
		setUpdateUrl(version, from);
		if(market) {
			markMarketUpdate(version);
		}
	}
	public String getUpdateBuild(String version) {
		return getString("update_build_id_" + version, null);
	}
	private void setUpdateBuild(String version, String build) {
		setString("update_build_id_" + version, build);
	}
	public String getUpdateUrl(String version) {
		return getString("update_build_url_" + version, null);
	}
	private void setUpdateUrl(String version, String from) {
		setString("update_build_url_" + version, from);
	}
	private void markMarketUpdate(String version) {
		mark("update_requires_market_" + version);
	}
	public boolean isMarketUpdate(String version) {
		return is("update_requires_market_" + version);
	}
	public void clearUpdate(String version) {
		clear("update_ready_" + version);
	}
	public boolean is(String key) {
		return getBoolean("@__" + key + "__", false);
	}
	public void mark(String key) {
		setBoolean("@__" + key + "__", true);
	}
	public void clear(String key) {
		setBoolean("@__" + key + "__", false);
	}
	public boolean hasShortcut(String appid) {
		return is("shortcut_installed_" + appid);
	}
	public void markShortcut(String appid) {
		mark("shortcut_installed_" + appid);
	}
	public void increment(String key) {
		setInt(key, getInt(key, 0) + 1);
	}
	public void increment(String key, int maxValue) {
		setInt(key, (getInt(key, 0) + 1) % maxValue);
	}
	public void remove(String key) {
		Editor editor = prefs.edit();
		editor.remove(key);
		editor.commit();
	}
	public boolean contains(String key) {
		return prefs.contains(key);
	}
	public int getInt(String key, int defValue) {
		return prefs.getInt(key, defValue);
	}
	public String getString(String key, String defValue) {
		return prefs.getString(key, defValue);
	}
	public boolean getBoolean(String key, boolean defValue) {
		return prefs.getBoolean(key, defValue);
	}
	public float getFloat(String key, float defValue) {
		return prefs.getFloat(key, defValue);
	}
	public long getLong(String key, long defValue) {
		return prefs.getLong(key, defValue);
	}

	public void setInt(String key, int value) {
		editor.putInt(key, value);
		editor.commit();
	}
	public void setString(String key, String value) {
		editor.putString(key, value);
		editor.commit();
	}
	public void setBoolean(String key, boolean value) {
		editor.putBoolean(key, value);
		editor.commit();
	}
	public void setFloat(String key, float value) {
		editor.putFloat(key, value);
		editor.commit();
	}
	public void setLong(String key, long value) {
		editor.putLong(key, value);
		editor.commit();
	}
}
