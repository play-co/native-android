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

public class UpdateNotificationEvent extends Event {
	protected String title, message, url, confirmText, denyText;
	public UpdateNotificationEvent(String type) {
		super(type);
		title = message = confirmText = denyText = url = null;
	}
	public UpdateNotificationEvent(String type, String title, String message, String confirmText, String denyText, String url) {
		super(type);
		this.title = title;
		this.message = message;
		this.confirmText = confirmText;
		this.denyText = denyText;
		this.url = url;
	}
}
