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
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class Device {
	private static String fakeId = null;
	private static final String storageFile = "__deviceID.dat";

	public static String getDeviceInfo() {
		return "{\"board\": \"" + Build.BOARD + "\"," +
				"\"bootloader\": \"" + Build.BOOTLOADER + "\"," +
				"\"brand\": \"" + Build.BRAND + "\"," +
				"\"cpu_abi\": \"" + Build.CPU_ABI + "\"," +
				"\"cpu_abi2\": \"" + Build.CPU_ABI2 + "\"," +
				"\"device\": \"" + Build.DEVICE + "\"," +
				"\"display\": \"" + Build.DISPLAY + "\"," +
				"\"version\": \"" + Build.VERSION.SDK_INT + "\"," +
				"\"fingerprint\": \"" + Build.FINGERPRINT + "\"," +
				"\"hardware\": \"" + Build.HARDWARE + "\"," +
				"\"id\": \"" + Build.ID + "\"," +
				"\"manufacturer\": \"" + Build.MANUFACTURER + "\"," +
				"\"model\": \"" + Build.MODEL + "\"," +
				"\"product\": \"" + Build.PRODUCT + "\"," +
				"\"tags\": \"" + Build.TAGS + "\"," +
				"\"time\": " + Build.TIME + "," +
				"\"type\": \"" + Build.TYPE + "\"}";
	}
	
	public static int getTotalMemory() {
		int tm=-1; 
		try { 
			RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r"); 
			String load = reader.readLine(); 
			reader.close();
			String[] totrm = load.split(" kB"); 
			String[] trm = totrm[0].split(" "); 
			tm=Integer.parseInt(trm[trm.length-1]); 
			
		} catch (IOException ex) { 
				
		} 
		logger.log("{device} Total memory = ", tm, "kB");
		return tm; 
	}

	public static String getDeviceID(Context context, Settings settings) {
		ResourceManager resourceManager = new ResourceManager(context, new TeaLeafOptions(context));
		String id = readDeviceID(context, settings, resourceManager);
		if (id == null) {
			id = generateDeviceID(context, settings, resourceManager);
			logger.log("{device} ID:", id, " (freshly generated)");
		} else {
			logger.log("{device} ID:", id);
		}
		return id;
	}
	
	private static String generateDeviceID(Context context, Settings settings, ResourceManager resourceManager) {
		String deviceID = getRawDeviceID(context);
		
		TelephonyManager telephonyManager =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = telephonyManager.getLine1Number();
		String imei = telephonyManager.getDeviceId();
		
		String seed = (deviceID == null ? "" : deviceID) + (phoneNumber == null ? "" : phoneNumber) + (imei == null ? "" : imei);
		if (seed.equals("")) {
			seed = "" + System.nanoTime();
		}
		UUID uuid = UUID.nameUUIDFromBytes(seed.getBytes());
		String uuidString = uuid.toString();
		saveDeviceID(uuidString, context, settings, resourceManager);
		return uuidString;
	}
	
	private static String readDeviceID(Context context, Settings settings, ResourceManager resourceManager) {
		String id = settings.getString("@__deviceID__", null);
		if (id == null) {
			id = resourceManager.readFromExternalStorage(storageFile);
			if (id != null) {
				logger.log("{device} ID:", id, " (read from SD card)");
				saveDeviceID(id, context, settings, resourceManager);
			} else {
				logger.log("{device} ID was not found");
			}
		} else {
			logger.log("{device} ID:", id, " (shared preferences)");
		}
		return id;
	}
	
	private static void saveDeviceID(String id, Context context, Settings settings, ResourceManager resourceManager) {
		settings.setString("@__deviceID__", id);
		//also save it to a file on external storage
		resourceManager.writeToExternalStorage(storageFile, id);
	}
	
	private static String getRawDeviceID(Context context) {
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}
	
	private static void generateFakeId(Context context, Settings settings) {
		String savedId = settings.getString("@__fakeId__", null);
		if (savedId == null) {
			fakeId = "" + System.nanoTime();
			settings.setString("@__fakeId__", fakeId);
		}
	}
	
	public static String getRealDeviceID(Context context, Settings settings) {
		String id = getRawDeviceID(context);
		if (id == null) {
			if (fakeId == null) {
				generateFakeId(context, settings);
			}
			id = fakeId;
		}
		return id;
	}

	public static String getNormalizedNumber(Context context) {
		TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String number = telephony.getLine1Number();
		if(number == null) { return "NONUMBER"; }
		return number.replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
	}

	public static String getNumberHash(Context context) {
		return sha1(getNormalizedNumber(context));
	}
	
	private static String sha1(String s) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			StringBuilder result = new StringBuilder(100);
			for(byte b : messageDigest) {
				result.append(String.format("%1$02x", b));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException e) {
			logger.log(e);
		}
		return "";
	}
}
