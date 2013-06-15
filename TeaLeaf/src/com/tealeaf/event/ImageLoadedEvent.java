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
package com.tealeaf.event;

public class ImageLoadedEvent extends Event {
	protected String url;
	protected int width;
	protected int height;
	protected int originalWidth;
	protected int originalHeight;
	protected int glName;

	public ImageLoadedEvent(String url, int width, int height, int originalWidth, int originalHeight, int name) {
		super("imageLoaded");
		this.url = url;
		this.width = width;
		this.height = height;
		this.originalWidth = originalWidth;
		this.originalHeight = originalHeight;
		this.glName = name;
	}
}
