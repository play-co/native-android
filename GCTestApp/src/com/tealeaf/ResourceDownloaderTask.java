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
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.tealeaf.test_app.TestAppActivity;
import com.tealeaf.util.HTTP;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.widget.Toast;


public class ResourceDownloaderTask extends AsyncTask<Activity, Float, Boolean>
{
	private String host;
	private int port;
	private AppInfo appInfo;
	private ProgressDialog progressDialog;
	private Activity context;
	private String lastFilename;
	private boolean errorDownloading;
	public ResourceDownloaderTask(Activity context, String host, int port, AppInfo appInfo) {
		this.host = host;
		this.port = port;
		this.appInfo = appInfo;
		this.progressDialog = new ProgressDialog(context);
		this.progressDialog.setProgressStyle(1); //horizontal
		this.progressDialog.setMax(100);
		this.context = context;
		this.lastFilename = null;
		this.errorDownloading = false;

	}

	@Override
	protected Boolean doInBackground(Activity... params )
	{
		lastFilename = null;
		//get options and resource manager
		TeaLeafOptions opts = new TeaLeafOptions();
		opts.setAppID(appInfo.appid);
		final ResourceManager resourceManager = new ResourceManager(params[0], opts);
		//get the storage directory
		final String storageDir = resourceManager.getStorageDirectory();
		File storage = new File(storageDir);
		//assure storage directory folders are there
		if (!storage.exists()) {
			storage.mkdirs();
		}

		HTTP http = new HTTP();

		// get the file containing the hashes of the last download if it exists
		File hashFile = new File(storageDir + "/resource_list.json");
		JSONObject prevHashes = new JSONObject();
		if (hashFile.exists()) {
			String hashFileString = fileToString(hashFile);
			try {
				prevHashes = new JSONObject(hashFileString);
			} catch (JSONException e) {
				logger.log(e);
			}
		}

		//form the simulate url
		String simulateUrl = "http://" + host + ":" + port + "/simulate/debug/" + appInfo.id + "/native-android/";

		//first get native.js
		String url = simulateUrl + "native.js";
		http = new HTTP();
		String nativejsPath = storageDir + "/" + "native.js";
		http.getFile(URI.create(url), nativejsPath);

		//get loading.png
		url = simulateUrl + "splash/portrait960";
		String loadingPngPath = storageDir + "/" + "loading.png";
		File splash = http.getFile(URI.create(url), loadingPngPath);
		//try again if we couldn't find the higher res splash
		if(splash == null || !splash.exists()) {
			url = simulateUrl + "splash/portrait480";
			http.getFile(URI.create(url), loadingPngPath);
		}

		HashMap<String, String> requestHeaders = new HashMap<String, String>();
		//get new resource list
		String body  = null;
		int retry = 3;
		while (retry-- > 0) {
			url = simulateUrl + "resource_list.json";
			body = http.get(URI.create(url));
			if (body != null && !body.equals("")) {
				break;
			}
		}

		JSONObject serverHashes = null;
		try {
			// loop through all resources and download any
			// that we don't have cached locally
			serverHashes = new JSONObject(body);
			int numFiles = serverHashes.length();

			@SuppressWarnings("unchecked") // using legacy API, remove warning
			Iterator<String> files = serverHashes.keys();

			publishProgress(1.f, 1.f, (float) numFiles);

			http = new HTTP();
			int currentFile = 0;
			while (files.hasNext()) {
				String filePath = files.next();
				lastFilename = filePath;
				if (filePath.contains("native.js")) {
					continue;
				}

				String fullPath = storageDir + "/" + filePath;

				// need to get the directory this file is in and create it
				File fullPathFile = new File(fullPath);
				new File(fullPathFile.getParent() + "/").mkdirs();

				// check if there is a previous hash
				String prevHash = null;
				try {
					prevHash = (String) prevHashes.get(filePath);
				} catch (JSONException e) {
					// expected to happen first load
					logger.log(e);
				}

				// check if the server hash is a match
				boolean cached = false;
				try {
					String hash = (String) serverHashes.get(filePath);
					if (hash != null && hash.equals(prevHash)) {
						cached = true;
					}
				} catch (JSONException e) {
					// oh well, just download it!
				}

				if (!cached || !(new File(fullPath).exists())) {
					// get the file and cache it to disk
					url = "http://" + host + ":" + port + "/simulate/debug/" + appInfo.id + "/native-android/" + filePath;
					url = url.replace(" ", "%20");
					http.getFile(URI.create(url), fullPath, requestHeaders);
				}

				// update progress
				publishProgress(1.f, (float) ++currentFile, (float) numFiles);;
			}
		} catch (Exception e) {
			logger.log("broken!");
			logger.log("error is", e);
			errorDownloading = true;
			cancel(true);
			logger.log(e);
		}

		if (serverHashes != null) {
			stringToFile(hashFile, serverHashes.toString());
		}

		return true;
	}

	private String fileToString(final File file) {
		StringBuilder result = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			char[] buf = new char[1024];
			int r = 0;
			while ((r = reader.read(buf)) != -1) {
				result.append(buf, 0, r);
			}
		} catch (Exception e) {
			logger.log(e);
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.log(e);
				}
			}
		}

		return result.toString();
	}

	private void stringToFile(File file, String content) {

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
			fileWriter.write(content);
			fileWriter.close();
		} catch (IOException e) {
			logger.log(e);
		} finally {
			try {
				fileWriter.close();
			} catch (IOException e) {
				logger.log(e);
			}
		}
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		this.progressDialog.setMessage("running basil build...");
		this.progressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				ResourceDownloaderTask.this.cancel(true);
			}
		});
		this.progressDialog.show();

	}

	@Override
	protected void onProgressUpdate(Float... values)
	{
		super.onProgressUpdate(values);
		if (values[0].intValue() == 1) {
			this.progressDialog.setMessage("downloading resources...");
		}
		this.progressDialog.setMax(values[2].intValue());
		this.progressDialog.setProgress(values[1].intValue());
	}

	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		this.progressDialog.dismiss();
		String message = "downloading cancelled. try again!";
		Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
		if (errorDownloading && lastFilename != null) {
			errorDownloading = false;
			message = "\nError downloading " + lastFilename + ".";
			Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPostExecute(Boolean result)
	{
		super.onPostExecute(result);
		this.progressDialog.dismiss();
		Toast.makeText(this.context, "launching game...", Toast.LENGTH_LONG).show();
		((TestAppActivity)context).launchDebugApp();
	}
}
