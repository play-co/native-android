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
