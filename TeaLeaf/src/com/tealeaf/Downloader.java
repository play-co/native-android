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

import org.json.JSONArray;
import org.json.JSONObject;

import com.tealeaf.util.HTTP;

public abstract class Downloader {
	protected HTTP http;

	public Downloader() {
		http = new HTTP();
	}

	public abstract boolean prepare();
	public abstract boolean apply();
	public abstract String getRemoteManifest();
	public abstract String getLocalManifest();

	protected HashMap<String, String> parseCache(String contents) {
		try {
			JSONObject manifest = new JSONObject(contents).getJSONObject("fileHashes");
			HashMap<String, String> files = new HashMap<String, String>();
			JSONArray names = manifest.names();
			for(int i = 0, len = names.length(); i < len; i++) {
				String name = names.getString(i);
				String value = manifest.getString(name);
				files.put(name, value);
			}
			return files;
		} catch(Exception e) {
			return null;
		}
	}

	public HashMap<String, File> download(HashMap<String, String> uris) {
		HashMap<String, File> files = new HashMap<String, File>();
		if (uris == null) {
			return null;
		}

		String[] urls = new String[uris.size()];
		uris.keySet().toArray(urls);
		for(String url : urls) {
			if(cached(uris.get(url))) {
				logger.log("{downloader}", uris.get(url), "is cached");
				continue;
			}
			File f = http.getFile(URI.create(url), uris.get(url));
			if(f != null) {
				logger.log("{downloader} Downloading updated file", url, "to", f.getAbsolutePath());
				files.put(url, f);
			} else {
				logger.log("{downloader} ERROR: Unable to download file", url);
				return null;
			}
		}
		return files;
	}

	public boolean cached(String uri) {
		return new File(uri).exists();
	}

	protected String read(File f) {
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

	protected void write(File f, String contents) {
		try {
			if(!f.exists()) {
				File dir = new File(f.getParent());
				dir.mkdirs();
				f.createNewFile();
			}
			FileWriter fw = new FileWriter(f);
			fw.write(contents);
			fw.close();
		} catch (IOException e) {
			logger.log(e);
		}
	}

	public boolean moveAll(HashMap<String, File> files) {
		String[] uris = new String[files.size()];
		files.keySet().toArray(uris);
		for(int i = 0; i < uris.length; i++) {
			cache(uris[i], files.get(uris[i]));
		}
		return true;
	}

	protected boolean cache(String name, File contents) {
		logger.log("{downloader} Caching", contents.getAbsolutePath(), "to", name);
		int i = name.lastIndexOf("/");
		if (i >= 0) {
			File directory = new File(name.substring(0, i));
			if (!directory.exists() && !directory.mkdirs()) {
				// if we can't make the subdirectories, don't even bother trying to cache
				logger.log("{downloader} ERROR: Unable to make directory", directory.getName());
				return false;
			}
		}
		File file = new File(name);
		file.delete();
		try {
			file.createNewFile();
			if (!contents.renameTo(file)) {
				logger.log("{downloader} ERROR: Unable to rename file", name);
			}
		} catch (Exception e) {
			logger.log(e);
		}
		return true;
	}
}
