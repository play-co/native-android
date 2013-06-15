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

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class LocalStorage {
	private SharedPreferences prefs;
	private LocalStorageWriter writer = null;
	private HashMap<String, String> keyValuePairs = new HashMap<String, String>();
	private ArrayList<String> toRemove = new ArrayList<String>();
	private boolean shouldClear = false;

	class LocalStorageWriter implements Runnable {

		private Object monitor = new Object();
		@Override
		public void run() {
			while (true) {
				synchronized(monitor) {
					try {
						monitor.wait();
					} catch (InterruptedException e) {
					}
				}
				writePairs();
				handleRemove();
				handleClear();
			}
		}
		
		public void doClear() {
			synchronized(monitor) {
				shouldClear = true;
				monitor.notifyAll();
			}
		}
		
		public void doRemove(String key) {
			synchronized(monitor) {
				toRemove.add(key);
				monitor.notifyAll();
			}
		}
		
		public void doSet(String key, String value) {
			synchronized(monitor) {
				keyValuePairs.put(key, value);
				monitor.notifyAll();
			}
		}
		
		private void writePairs() {
			HashMap<String, String> kvp;
			synchronized(monitor) {
				kvp = new HashMap<String, String>(keyValuePairs);
				//keyValuePairs.clear();
			}
			for (String key : kvp.keySet()) {
				String value = kvp.get(key);
				setData(key, value);
			}
			synchronized(monitor) {
				//if the value hasn't changed since we made our copy of the cache,
				//we can safely remove it.  Otherwise we need to keep it around
				for (String key : kvp.keySet()) {
					if (kvp.get(key).equals(keyValuePairs.get(key))) {
						keyValuePairs.remove(key);
					}
				}
			}
		}

		private void handleRemove() {
			ArrayList<String> readyToRemove;
			synchronized (monitor) {
				readyToRemove = new ArrayList<String>(toRemove);
				toRemove.clear();
			}
			for (String key : readyToRemove) {
				removeData(key);
			}
		}
		
		private void handleClear() {
			if (shouldClear) {
				clear();
				shouldClear = false;
			}
		}
		
		private void setData(String key, String data) {
			Editor editor = prefs.edit();
			editor.putString(key, data);
			editor.commit();	
		}
		
		private void removeData(String key) {
			Editor editor = prefs.edit();
			editor.remove(key);
			editor.commit();
		}
		
		private void clear() {
			Editor editor = prefs.edit();
			editor.clear();
			editor.commit();
		}
	}
	public LocalStorage(TeaLeaf tealeaf, TeaLeafOptions options) {
		prefs = tealeaf.getSharedPreferences(options.getAppID(), Context.MODE_PRIVATE);
		writer = new LocalStorageWriter();
		new Thread(writer).start();
	}
	public void setData(String key, String data) {
		writer.doSet(key, data);
	}
	
	public String getData(String key) {
		if (toRemove.contains(key) || shouldClear) {
			return null;
		}
		String data = keyValuePairs.get(key);
		if (data == null) {
			data = prefs.getString(key, null);
		}
		return data;
	}
	
	public void removeData(String key) {
		writer.doRemove(key);
	}
	
	public void clear() {
		writer.doClear();
	}
}
