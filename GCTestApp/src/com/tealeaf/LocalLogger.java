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

import android.content.Context;
import android.util.Log;

import com.tealeaf.util.ILogger;

public class LocalLogger implements ILogger {
	public LocalLogger(Context context) {
	}

	@Override
	public void log(Exception e) {
		Log.e("JS", "Exception", e);
	}

	@Override
	public void sendFirstLaunchEvent(Context context) {
		Log.d("JS", "Sending first launch log event");
	}

	@Override
	public void sendLaunchEvent(Context context) {
		Log.d("JS", "Sending launch log event");
	}

	@Override
	public void sendErrorEvent(Context context, String payload) {
		Log.e("JS", "Error: " + payload);
	}

	@Override
	public void sendDeviceInfoEvent(Context context) {
		Log.d("JS", "Sending device info event");
	}

	@Override
	public void sendGLErrorEvent(Context context, String payload) {
		Log.e("JS", "GL Error: " + payload);
	}
}
