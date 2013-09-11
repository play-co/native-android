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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.content.res.AssetManager;
import android.os.AsyncTask;

public class Unpacker extends AsyncTask<Void, Void, Void> {
	
	private TeaLeaf tealeaf;
	private AssetManager assets;
	private String destination;

	public Unpacker(TeaLeaf tealeaf, ResourceManager resourceManager) {
		this.tealeaf = tealeaf;
		destination = resourceManager.getStorageDirectory();
	}
	
	private void unpack() {
		//get the directory to unpack to
		
		//open the assets and unpack the appropriate directory
		assets = tealeaf.getAssets();
		unpackDirectory("resources");
		
	}
	
	private void unpackDirectory(String directory) {
		String[] files = null;
		try {
			files = assets.list(directory);
		} catch (IOException e) {
			logger.log(e);
		}
		if (files == null) {
			logger.log("{unpacker} WARNING: No files in the resource directory");
			return;
		}
		File dir = new File(destination + File.separator + directory);
		dir.mkdirs();
		for (String file : files) {
			if(file.contains(".png") || file.contains(".jpg") || file.contains("native.js")) {
				continue;
			}
			String fullPath = directory + File.separator + file;
			InputStream in;
			try {
				in = assets.open(fullPath);
				unpackFile(directory, file, in);
			} catch (FileNotFoundException e) {
				//it's a directory?
				unpackDirectory(fullPath);
			} catch (IOException e) {
				logger.log(e);
			}
		} 
	}
	
	private void unpackFile(String path, String fileName, InputStream in) {
		String destPath = destination + File.separator + path;
		File outDir = new File(destPath);
		outDir.mkdirs();
		File outFile = new File(destPath + File.separator + fileName);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
			logger.log(e);
			return;
		}
		byte[] buffer = new byte[1024];
	    int read;
	    try {
			while((read = in.read(buffer)) != -1){
			  out.write(buffer, 0, read);
			}
		} catch (IOException e) {
			logger.log(e);
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		unpack();
		tealeaf.getSettings().markUnpacked(tealeaf.getOptions().getBuildIdentifier());
		return null;
	}
}
