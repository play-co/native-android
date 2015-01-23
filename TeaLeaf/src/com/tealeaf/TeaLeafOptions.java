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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

// there's probably some simpler way to do this by reading the android manifest metadata and
// overriding the defaults by reading a JSON file with the right appid, but this works for now
public class TeaLeafOptions {

	private Bundle meta;

	private String appID = null;
	private String simulateID = "tealeaf";
	private String tcpHost = null;
	private String codeHost = null;
	private Integer tcpPort = null;
	private Integer codePort = null;
	private Boolean develop = null;
	private Boolean remote_loading = null;
	private String displayName = null;
	private String sourceDir = null;
	private String buildID = null;
	private String pushUrl = null;
	private String sdkHash = null;
	private String androidHash = null;
	private String gameHash = null;
	private String splash = "loading.png";

	public TeaLeafOptions() {
		// we want a blank options, so set meta to an empty bundle
		meta = new Bundle();
		displayName = "EMPTY!";
		buildID = "NONE! FIXME!";
	}
	public TeaLeafOptions(Context context) {
		try {
			displayName = getDisplayName(context);

			PackageManager manager = context.getPackageManager();
			sourceDir = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).sourceDir;
			meta = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;

		} catch(NameNotFoundException e) {
			logger.log(e);
			throw new RuntimeException(e);
		}

	}

	private String getDisplayName(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			ApplicationInfo info = manager.getApplicationInfo(context.getPackageName(), 0);
			return manager.getApplicationLabel(info).toString();
		} catch (NameNotFoundException e) {
			return "TeaLeaf";
		}
	}

	public String get(String key, String defValue) {
		// If bundle does not have the key, return default
		if (!meta.containsKey(key)) {
			return defValue;
		}

		// If the bundle does not contain any data under the key, return default
		Object data = meta.get(key);
		if (data == null) {
			return defValue;
		}

		// Stringize and return the data
		return data.toString();
	}
	public int get(String key, int defValue) {
		// If bundle does not have the key, return default
		if (!meta.containsKey(key)) {
			return defValue;
		}

		// If the bundle does not contain any data under the key, return default
		Object data = meta.get(key);
		if (data == null) {
			return defValue;
		}

		// Stringize and return the data as an integer
		return Integer.parseInt(data.toString());
	}
	public boolean get(String key, boolean defValue) {
		// If bundle does not have the key, return default
		if (!meta.containsKey(key)) {
			return defValue;
		}

		// If the bundle does not contain any data under the key, return default
		Object data = meta.get(key);
		if (data == null) {
			return defValue;
		}

		// Equate data with true
		return data.equals(true);
	}
	public String get(String key) {
		return get(key, null);
	}

	public boolean isRemoteLoading() {
		return remote_loading != null ? remote_loading : get("remote_loading", false);
	}
	public boolean isDevelop() {
		return develop != null ? develop : get("develop", false);
	}
	public String getDisplayName() {
		return displayName;
	}
	public String getAppID() {
		return appID != null ? appID : get("appID", "tealeaf");
	}
	public String getCodeHost() {
		return codeHost != null ? codeHost : get("codeHost", "s.wee.cat");
	}
	public String getTcpHost() {
		return tcpHost != null ? tcpHost : get("tcpHost", "s.wee.cat");
	}
	public int getCodePort() {
		return codePort != null ? codePort : get("codePort", isDevelop() ? 9200 : 80);
	}
	public int getTcpPort() {
		return tcpPort != null ? tcpPort : get("tcpPort", 4747);
	}
	public String getBuildIdentifier() {
		return buildID != null ? buildID : get("buildIdentifier", "1");
	}
	public String getPushUrl() {
		return pushUrl != null ? pushUrl : get("pushUrl", "http://staging.api.gameclosure.com/push/%s/?device=%s&version=%s");
	}
	public String getSourceDir() {
		return sourceDir;
	}
	public String getSplash() {
		return splash;
	}
	public String getEntryPoint() {
		return get("entryPoint", "gc.native.launchClient");
	}
	public String getProtocol() {
		return getAppID();
	}
	public String getContactsUrl() {
		return get("contactsUrl", "http://staging.api.gameclosure.com/users/me/contacts/?device=%s");
	}
	public String getUserDataUrl() {
		return get("userDataUrl", "https://staging.api.gameclosure.com/users/me/?device=%s");
	}
	public String getServicesURL() {
		return get("servicesUrl", "http://api.gameclosure.com");
	}
	public String getGAKey() {
		return get("gaKey", "");
	}
	public int getPushDelay() {
		return get("pushDelay", 60);
	}
	public String getSimulateID() {
		return simulateID;
	}


	public String getSDKHash() {
		return sdkHash != null ? sdkHash : get("sdkHash", "Unknown");
	}

	public String getAndroidHash() {
		return androidHash != null ? androidHash : get("androidHash", "Unknown");
	}

	public String getGameHash() {
		return gameHash != null ? gameHash : get("gameHash", "Unknown");
	}

	public String getStudioName() {
		return get("studioName", "");
	}

	public void setAppID(String app) { appID = app; }
	public void setCodeHost(String host) { codeHost = host; }
	public void setTcpHost(String host) { tcpHost = host; }
	public void setCodePort(Integer port) { codePort = port; }
	public void setTcpPort(Integer port) { tcpPort = port; }
	public void setRemoteLoading(Boolean value) { remote_loading = value; }
	public void setDevelop(Boolean value) { develop = value; }
	public void setDisplayName(String value) { displayName = value; }
	public void setBuildIdentifier(String value) { buildID = value; }
	public void setPushUrl(String value) { pushUrl = value; }
	public void setSimulateID(String value) { simulateID = value; }
	public void setSplash(String value) { splash = value; }

	protected static String read(File f) {
		StringBuffer contents = new StringBuffer(1000);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			char[] buf = new char[1024];
			int read = 0;
			while ((read=reader.read(buf)) != -1) {
				contents.append(buf, 0, read);
			}
			reader.close();
		} catch(IOException e) {
			logger.log(e);
			contents = new StringBuffer();
		}
		return contents.toString();
	}

	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}
}
