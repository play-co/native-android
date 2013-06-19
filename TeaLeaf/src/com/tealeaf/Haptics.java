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
import android.content.Context;
import android.os.Vibrator;

/**
 * Wrapper for haptic feedback.
 * @author jsermeno
 */
public class Haptics {
	private Vibrator vibrator;

	public Haptics(Activity activity) {
		this.vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
	}

	public void cancel() {
		this.vibrator.cancel();
	}

	public void vibrate(long milliseconds) {
		this.vibrator.vibrate(milliseconds);
	}

	public void vibrate(long[] pattern, int repeat) {
		this.vibrator.vibrate(pattern, repeat);
	}

	// TODO: Implement for Android 3
	public boolean hasVibrator() {
		return true;
	}
}
