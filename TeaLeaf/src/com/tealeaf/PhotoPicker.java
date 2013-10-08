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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import android.net.Uri;

public class PhotoPicker {
	private Activity activity;
	private ResourceManager resourceManager;
	private Settings settings;
	public PhotoPicker(Activity context, Settings settings, ResourceManager manager) {
		this.activity = context;
		this.settings = settings;
		resourceManager = manager;
	}

	public static final int CAPTURE_IMAGE = 1000;
	public static final int PICK_IMAGE = 1001;

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
        File largeFile = getCaptureImageTmpFile();
		if (largeFile != null) {
			camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(largeFile));
		}
		int requestCode = CAPTURE_IMAGE;
		moveNextCameraId();
		activity.startActivityForResult(camera, requestCode);
	}

	public void choose(int id) {
		Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		int requestCode = PICK_IMAGE;
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

    //also deletes the file!!!
	public Bitmap getResult(String type, int id) {
		Bitmap bitmap = null;
        String filename = resourceManager.resolveFile(type + id + ".jpg");
		try {
			bitmap = BitmapFactory.decodeFile(filename);
		} catch(Exception e) {
			logger.log("{photos} ERROR: Unable to load picture", e);
		}
        if (bitmap != null) {
            File file = new File(filename);
            if (file != null && file.exists()) {
                file.delete();
            }
        }
		return bitmap;
	}

    private static File captureImageTmpFile = null;
    public static File getCaptureImageTmpFile() {
        if (captureImageTmpFile == null) {
            try {
                captureImageTmpFile = File.createTempFile(".gc_tmpfile", ".jpg", Environment.getExternalStorageDirectory());
            } catch(Exception e) {
				logger.log(e);
            }
        }
        return captureImageTmpFile;
    }

}
