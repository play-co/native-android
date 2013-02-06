/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with the Game Closure SDK.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tealeaf;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;

public class PhotoPicker {
	private Activity activity;
	private ResourceManager resourceManager;
	private Settings settings;
	public PhotoPicker(Activity context, Settings settings, ResourceManager manager) {
		this.activity = context;
		this.settings = settings;
		resourceManager = manager;
	}

	public static final byte CAPTURE_IMAGE = (byte) 45;
	public static final byte PICK_IMAGE = (byte) 46;

	public int getNextCameraId() {
		return settings.getInt("@__camera_id__", 1);
	}
	public void moveNextCameraId() {
		settings.increment("@__camera_id__", 0xFFFFFF);
	}
	public int getNextGalleryId() {
		return settings.getInt("@__gallery_id__", 1);
	}
	public void moveNextGalleryId() {
		settings.increment("@__gallery_id__", 0xFFFFFF);
	}

	public void take(int id) {
		Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		int requestCode = (CAPTURE_IMAGE << 24) | (id & 0xFFFFFF);
		moveNextCameraId();
		activity.startActivityForResult(camera, requestCode);
	}
	public void choose(int id) {
		Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		int requestCode = (PICK_IMAGE << 24) | (id & 0xFFFFFF);
		moveNextGalleryId();
		activity.startActivityForResult(gallery, requestCode);
	}

	public void save(String type, int id, Bitmap result) {
		try {
			File f = new File(resourceManager.resolveFile(type + id + ".jpg"));
			if(!f.exists()) {
				File parent = f.getParentFile();
				if(!parent.exists() && !parent.mkdirs()) {
					logger.log("{photos} ERROR: Failed to make picture save path");
					return;
				}
				f.createNewFile();
			}
			result.compress(CompressFormat.JPEG, 100, new FileOutputStream(f));
		} catch(Exception e) {
			logger.log(e);
		}
	}

	public Bitmap getResult(String type, int id) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(resourceManager.resolveFile(type + id + ".jpg"));
		} catch(Exception e) {
			logger.log("{photos} ERROR: Unable to load picture", e);
		}
		return bitmap;
	}
}
