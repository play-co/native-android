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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Base64;

public class ContactList {
	private Activity activity;
	private ResourceManager resourceManager;

	public ContactList(Activity activity, ResourceManager resourceManager) {
		this.activity = activity;
		this.resourceManager = resourceManager;
	}

	public String getProfileLookup() {
		return null;
	}

	public Bitmap getPicture(String lookupKey, int size) {
		String encoded = "unknown";
		try {
			encoded = URLEncoder.encode(lookupKey, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.log(e);
		}
		File f = new File(resourceManager.getCacheDirectory() + "/contacts/" + encoded + "x" + size + ".png");
		if (f.exists()) {
			return BitmapFactory.decodeFile(f.getAbsolutePath());
		}
		Bitmap img = getUnscaledPicture(lookupKey);
		if (img != null) {
			Bitmap result = null;

			try {
				result = Bitmap.createBitmap(size, size,
						Bitmap.Config.ARGB_8888);
			} catch (OutOfMemoryError e) {
				logger.log(e);
				return null;
			}
			Canvas c = new Canvas(result);
			int originalWidth = img.getWidth();
			int originalHeight = img.getHeight();
			Rect src = new Rect(0, 0, originalWidth, originalHeight);
			float scale = Math.min((float)size / originalWidth, (float)size / originalHeight);
			int scaledWidth = (int)(scale * originalWidth);
			int scaledHeight = (int)(scale * originalHeight);
			Rect dst = new Rect(0, 0, scaledWidth, scaledHeight);
			c.drawBitmap(img, src, dst, null);

			try {
				File parent = f.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				f.createNewFile();
				result.compress(CompressFormat.PNG, 0, new FileOutputStream(f));
			} catch (Exception e) {
				logger.log(e);
				f.delete();
			}

			return result;
		}
		return null;
	}

	public Bitmap getUnscaledPicture(String lookupKey) {
		return null;
	}

	public String getProfilePicture(boolean base64) {
		return getPicture(getProfileLookup(), true);
	}

	public String getPicture(String lookupKey, boolean base64) {
		Bitmap img = getPicture(lookupKey, 128);
		if (img != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			img.compress(CompressFormat.PNG, 0, stream);
			return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
		}
		return "";
	}
}
