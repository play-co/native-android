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

import android.graphics.Bitmap;

public class TextureData {
	public TextureData(String url, boolean loaded) {
		this.url = url;
		this.loaded = loaded;
	}

	public TextureData(String url, int name, int width, int height, int originalWidth, int originalHeight, Bitmap bitmap, boolean loaded) {
		this.url = url;
		this.width = width;
		this.height = height;
		this.name = name;
		this.originalWidth = originalWidth;
		this.originalHeight = originalHeight;
		this.bitmap = bitmap;
		this.loaded = loaded;
	}

	public TextureData(String url, int name, int width, int height, int originalWidth, int originalHeight, boolean loaded) {
		this.url = url;
		this.width = width;
		this.height = height;
		this.name = name;
		this.originalWidth = originalWidth;
		this.originalHeight = originalHeight;
		this.bitmap = null;
		this.loaded = loaded;
	}

	public void clear() {
		if (this.bitmap != null) {
			this.bitmap.recycle();
			this.bitmap = null;
		}
	}

	public boolean loaded;
	public int name;
	public int width;
	public int height;
	public int originalWidth;
	public int originalHeight;
	public String url;
	public boolean isText = false;
	public Bitmap bitmap;
}
