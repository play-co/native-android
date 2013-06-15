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

import java.io.File;
import java.net.URI;
import java.util.HashMap;

import android.content.Context;

public class Updater extends Downloader {
	private String appID;
	protected ResourceManager resourceManager;
	protected Settings settings;
	private String base;

	public Updater(Context context, TeaLeafOptions options, String from) {
		super();
		this.appID = options.getAppID();
		this.base = from;
		resourceManager = new ResourceManager(context, options);
		settings = new Settings(context);
	}

	public String getRemoteManifest() {
		String manifest = settings.getString("update_manifest", null);
		if(manifest == null) {
			String url = resourceManager.resolveUrl("metadata.json", base);
			manifest = http.get(URI.create(url));
			settings.setString("update_manifest", manifest);
		}
		return manifest;
	}

	public String getLocalManifest() {
		return settings.getString("local_manifest", null);
	}

	public HashMap<String, String> parseCache(String contents) {
		HashMap<String, String> remote = super.parseCache(contents);
		String localManifest = getLocalManifest();
		logger.log("{updater} Local manifest =", localManifest);
		HashMap<String, String> local = super.parseCache(localManifest);
		// do we have a local manifest too?
		if(remote != null && local != null) {
			logger.log("{updater} Filtering files for only the changed ones");
			HashMap<String, String> files = new HashMap<String, String>();
			// we do, pull out only those files that actually changed
			for(String file : remote.keySet()) {
				String old = local.get(file);
				String current = remote.get(file);
				logger.log("{updater} Comparing", old, "to", current);
				if(!old.equals(current)) {
					files.put(file, null);
				}
			}
			return files;
		// otherwise, we should have a local manifest
		} else if(remote != null) {
			logger.log("{updater} No local manifest, returning the whole file list");
			return remote;
		}
		// I guess we failed to parse the manifest, then?
		return null;
	}

	public boolean prepare() {
		HashMap<String, String> remote = parseCache(getRemoteManifest());
		if (remote != null && remote.size() > 0) {
			if (download(remote) != null) {
				return true;
			}
		}
		return false;
	}

	public boolean apply() {
		String manifest = getRemoteManifest();
		HashMap<String, String> cache = parseCache(manifest);
		if (cache != null) {
			HashMap<String, File> files = getTempFiles(cache);
			if (files != null) {
				settings.setString("local_manifest", manifest);
				moveAll(files);
				resourceManager.clearCacheDirectory();
				return true;
			}
		}
		return false;
	}

	public HashMap<String, File> download(HashMap<String, String> uris) {
		HashMap<String, String> urls = new HashMap<String, String>();
		for(String uri : uris.keySet()) {
			urls.put(resourceManager.resolveUrl(uri, base), makeCacheFilename(uri));
		}
		return super.download(urls);
	}

	public HashMap<String, File> getTempFiles(HashMap<String, String> uris) {
		HashMap<String, File> files = new HashMap<String, File>();
		for(String uri : uris.keySet()) {
			files.put(uri, new File(makeCacheFilename(uri)));
		}
		return files;
	}

	private String makeCacheFilename(String file) {
		return resourceManager.getCacheDirectory() + "/" + appID + "/" + resourceManager.encode(file);
	}

	protected boolean cache(String name, File contents) {
		name = resourceManager.resolveFile(name);
		return super.cache(name, contents);
	}
}
