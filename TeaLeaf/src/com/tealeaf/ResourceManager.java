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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import com.tealeaf.util.HTTP;

import android.content.Context;
import android.os.Environment;

public class ResourceManager {

	private TeaLeafOptions options;
	private Context context;
	private HTTP http;
	private static boolean ONLY_USE_INTERNAL_STORAGE = true;
	public ResourceManager(Context ctx, TeaLeafOptions opts) {
		options = opts;
		context = ctx;
		http = new HTTP();
	}

	public File getFile(String url) {
		url = resolve(url);
		File f = null;
		if (url.matches("^http(s?)://.*")) {
			// we're dealing with a url, download it
			URI uri = URI.create(url);
			f = http.getFile(uri, getCacheDirectory() + encode(uri.getPath()));
		} else {
			f = new File(url);
		}
		return f;
	}

	public String getFileContents(String url) {
		String contents = null;
		File f = getFile(url);
		if(f == null) {
			// couldn't find the file, url unavailable, whatever.
			return null;
		}
		try {
			// FIXME This will break if the size of a file ever exceeds Integer.MAX_VALUE. Not sure how we should fix this, though :\
			byte[] buffer = new byte[(int)f.length()];
			FileInputStream r = new FileInputStream(f);
			r.read(buffer);
			contents = new String(buffer);
			r.close();
		} catch (Exception e) {
			logger.log(e);
		}
		return contents;
	}

	public String resolve(String relative) {
		if(relative.matches("^(http(s?)://|data).*")) {
			// actually not a relative url
			return relative;
		} else if(options.isDevelop() && options.get("forceURL", false)) {
			return resolveUrl(relative);
		} else {
			return resolveFile(relative);
		}
	}

	public String resolveUrl(String relative, String base) {
		return base + encode(relative);
	}

	public String resolveUrl(String relative) {
		// build the base url, rather than the relative url, if not specified
		String base = "";
		if(options.isDevelop() && !relative.startsWith("sdk")) {
			base = getStorageDirectory() + "/";
		} else {
			base = options.getAppID() + options.getBuildIdentifier() + "/";
		}
		return resolveUrl(relative, base);
	}

	public String resolveFileWithBase(String relative, String base) {
		return base + File.separator + encode(relative);
	}

	public String resolveFile(String relative) {
		String base = getStorageDirectory() + File.separator + "resources";
		return resolveFileWithBase(relative, base);
	}

	public String encode(String url) {
		try {
			// encode just the path components, not the slashes
			String[] parts = url.split("/");
			StringBuilder newUrl = new StringBuilder(255);
			int len = parts.length-1;
			for(int i = 0; i < len; i++) {
				if(parts[i].length() == 0) {
					continue;
				}
				newUrl.append("/" + URLEncoder.encode(parts[i], "UTF8"));
			}

			String last = parts[len];
			if(last.contains("?")) {
				int query = last.indexOf("?");
				newUrl.append("/" + URLEncoder.encode(last.substring(0, query), "UTF8") + "?");
				last = last.substring(query+1);
			} else {
				last = "/" + URLEncoder.encode(last, "UTF8");
			}
			newUrl.append(last);
			url = newUrl.substring(1).toString();
		} catch (UnsupportedEncodingException e) {
			logger.log(e);
		}
		return url;
	}

	public String getCacheDirectory() {
		if (!canUseExternalStorage()) {
			return context.getCacheDir().getAbsolutePath();
		} else {
			String path = getBaseStorageDirectory() + "/tmp/";
			File cache = new File(path);
			if (!cache.exists()) {
				cache.mkdirs();
			}
			return path;
		}
	}
	public String getExternalCacheDirectory() {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp/";
	}

	public void clearCacheDirectory() {
		File tmp = new File(getCacheDirectory());
		if (tmp.exists()) {
			deleteDirectory(tmp);
		}
	}

	public void cleanContentDirectory() {
		File tmp = new File(getStorageDirectory() + File.separator + "resources");
		if(tmp.exists()) {
			deleteDirectory(tmp);
		}
	}

	private void deleteDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				deleteDirectory(file);
			} else {
				file.delete();
			}
		}
		dir.delete();
	}
	public String getStorageDirectory() {
		File storageDir = getBaseStorageDirectory();

		String dataDirPath = storageDir.getAbsolutePath();
		return dataDirPath;
	}

	private File getBaseStorageDirectory() {
		File storageDir = null;
		if (!canUseExternalStorage() || ONLY_USE_INTERNAL_STORAGE) {
			storageDir = context.getFilesDir();
		} else {
			storageDir = Environment.getExternalStorageDirectory();
		}
		return storageDir;
	}

	public boolean writeToExternalStorage(String filename, String contents) {
		if (!canUseExternalStorage()) {
			return false;
		}
		File storageDir = Environment.getExternalStorageDirectory();
		File file = new File(storageDir.getAbsolutePath() + File.separator + filename);
		if (file.exists()) {
			logger.log("{resource} ERROR: External storage file exists");
			return false;
		}
		try {
			FileWriter writer = new FileWriter(file);
			writer.append(contents);
			writer.close();
		} catch (IOException e) {
			logger.log(e);
			return false;
		}
		return true;
	}

	// TODO holy crap, this needs fixing/refactoring
	public String readFromExternalStorage(String filename) {
		if (!canUseExternalStorage()) {
			return null;
		}
		File storageDir = Environment.getExternalStorageDirectory();
		File file = new File(storageDir.getAbsolutePath() + File.separator + filename);
		if (!file.exists()) {
			return null;
		}
		String contents = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream dis = new DataInputStream(bis);
			contents = "";
			while (dis.available() != 0) {
				contents += dis.readLine();
			}
			dis.close();
		} catch (Exception e) {
			logger.log(e);
			contents = null;
		}
		return contents;

	}

	private boolean canUseExternalStorage() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			//  to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return mExternalStorageAvailable && mExternalStorageWriteable;
	}
}
